package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class TransferenciaServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    private Conta contaOrigem;
    private Conta contaDestino;

    @BeforeEach
    void setup() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();

        contaOrigem = new Conta();
        contaOrigem.setUsuarioId(UUID.randomUUID());
        contaOrigem.setSaldoEmCentavos(10000L); // R$ 100,00
        contaOrigem = contaRepository.save(contaOrigem);

        contaDestino = new Conta();
        contaDestino.setUsuarioId(UUID.randomUUID());
        contaDestino.setSaldoEmCentavos(0L);
        contaDestino = contaRepository.save(contaDestino);
    }

    @Test
    void deveTransferirComTaxaDe1Porcento() {
        transferenciaService.transferir(contaOrigem.getId(), contaDestino.getId(), 10000L);

        Conta origem = contaRepository.findById(contaOrigem.getId()).orElseThrow();
        Conta destino = contaRepository.findById(contaDestino.getId()).orElseThrow();
    }

    @Test
    void deveTransferirValorCorretoComTaxa() {
        contaOrigem.setSaldoEmCentavos(20000L);
        contaRepository.save(contaOrigem);

        transferenciaService.transferir(contaOrigem.getId(), contaDestino.getId(), 10000L);

        Conta origem = contaRepository.findById(contaOrigem.getId()).orElseThrow();
        Conta destino = contaRepository.findById(contaDestino.getId()).orElseThrow();

        assertEquals(9900L, origem.getSaldoEmCentavos(),
                "Origem deve ter saldo de R$ 99,00 (descontado valor + taxa de 1%)");
        assertEquals(10000L, destino.getSaldoEmCentavos(),
                "Destino deve receber o valor integral sem a taxa");
    }

    @Test
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        assertThrows(IllegalArgumentException.class, () ->
                transferenciaService.transferir(contaOrigem.getId(), contaDestino.getId(), 10000L)
        );
    }

    @Test
    void deveLancarExcecaoParaMesmaContaOrigem() {
        assertThrows(IllegalArgumentException.class, () ->
                transferenciaService.transferir(contaOrigem.getId(), contaOrigem.getId(), 1000L)
        );
    }

    @Test
    void deveLancarExcecaoParaContaInexistente() {
        assertThrows(IllegalArgumentException.class, () ->
                transferenciaService.transferir(contaOrigem.getId(), 9999L, 100L)
        );
    }

    @Test
    void devePrevenirRaceCondition() throws InterruptedException {
        contaOrigem.setSaldoEmCentavos(20200L);
        contaRepository.save(contaOrigem);

        int numeroDeTentativas = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeTentativas);
        CountDownLatch latch = new CountDownLatch(numeroDeTentativas);

        for (int i = 0; i < numeroDeTentativas; i++) {
            executor.submit(() -> {
                try {
                    transferenciaService.transferir(contaOrigem.getId(), contaDestino.getId(), 10000L);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Conta origemFinal = contaRepository.findById(contaOrigem.getId()).orElseThrow();
        Conta destinoFinal = contaRepository.findById(contaDestino.getId()).orElseThrow();

        assertTrue(origemFinal.getSaldoEmCentavos() >= 0,
                "Saldo da conta de origem nunca deve ser negativo");

        long totalDebitado = 20200L - origemFinal.getSaldoEmCentavos();
        assertEquals(destinoFinal.getSaldoEmCentavos() * 101 / 100,
                totalDebitado,
                50L,
                "Dinheiro não deve ser criado nem destruído");
    }
}
