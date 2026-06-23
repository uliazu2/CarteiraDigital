package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferenciaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Transactional
    public Transacao transferir(Long origemId, Long destinoId, Long valorEmCentavos) {
        System.out.println("DEBUG SERVICE: Origem=" + origemId + " | Destino=" + destinoId + " | Valor=" + valorEmCentavos);
        if (origemId == null || destinoId == null) {
            throw new IllegalArgumentException("IDs de origem e destino são obrigatórios.");
        }
        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta.");
        }
        if (valorEmCentavos == null || valorEmCentavos <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }

        Long firstId = Math.min(origemId, destinoId);
        Long secondId = Math.max(origemId, destinoId);

        contaRepository.findByIdForUpdate(firstId);
        contaRepository.findByIdForUpdate(secondId);

        Conta contaOrigem = contaRepository.findById(origemId)
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada."));
        Conta contaDestino = contaRepository.findById(destinoId)
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada."));

        Long taxaEmCentavos = valorEmCentavos / 100;
        Long custoTotal = valorEmCentavos + taxaEmCentavos;

        if (contaOrigem.getSaldoEmCentavos() < custoTotal) {
            throw new IllegalArgumentException(
                    String.format("Saldo insuficiente. Necessário R$ %.2f (valor + taxa de 1%%).",
                            custoTotal / 100.0));
        }

        contaOrigem.debitar(custoTotal);
        contaDestino.creditar(valorEmCentavos);

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

        Transacao transacao = new Transacao();
        transacao.setContaOrigemId(origemId);
        transacao.setContaDestinoId(destinoId);
        transacao.setValorEmCentavos(valorEmCentavos);
        transacao.setDataHora(java.time.LocalDateTime.now());
        System.out.println("DEBUG: Salvando transacao - Origem: " + origemId + ", Destino: " + destinoId);
        return transacaoRepository.save(transacao);
    }
}
