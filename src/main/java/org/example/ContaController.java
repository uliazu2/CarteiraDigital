package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
public class ContaController {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @PostMapping
    public ResponseEntity<Conta> criarConta(@RequestBody ContaRequest request) {
        Conta novaConta = new Conta();
        novaConta.setUsuarioId(UUID.randomUUID());

        Long saldoInicial = request.getSaldoInicialEmCentavos() != null ? request.getSaldoInicialEmCentavos() : 0L;
        if (saldoInicial < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
        }
        novaConta.setSaldoEmCentavos(saldoInicial);

        return ResponseEntity.ok(contaRepository.save(novaConta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conta> buscarConta(@PathVariable Long id) {
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));
        return ResponseEntity.ok(conta);
    }

    @GetMapping("/{id}/extrato")
    public ResponseEntity<List<Transacao>> tirarExtrato(@PathVariable Long id) {
        if (!contaRepository.existsById(id)) {
            throw new IllegalArgumentException("Conta não encontrada.");
        }
        List<Transacao> extrato = transacaoRepository
                .findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(id, id);
        return ResponseEntity.ok(extrato);
    }

    @PostMapping("/{id}/deposito")
    @Transactional
    public ResponseEntity<Conta> depositar(@PathVariable Long id, @RequestBody ValorRequest request) {
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        Long valor = request.getValorEmCentavos();
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser maior que zero.");
        }

        conta.creditar(valor);
        Conta contaAtualizada = contaRepository.save(conta);

        transacaoRepository.save(new Transacao(null, id, valor));

        return ResponseEntity.ok(contaAtualizada);
    }

    @PostMapping("/{id}/saque")
    @Transactional
    public ResponseEntity<Conta> sacar(@PathVariable Long id, @RequestBody ValorRequest request) {
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        Long valor = request.getValorEmCentavos();
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser maior que zero.");
        }

        if (conta.getSaldoEmCentavos() < valor) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar este saque.");
        }

        conta.debitar(valor);
        Conta contaAtualizada = contaRepository.save(conta);

        transacaoRepository.save(new Transacao(id, null, valor));

        return ResponseEntity.ok(contaAtualizada);
    }
}
