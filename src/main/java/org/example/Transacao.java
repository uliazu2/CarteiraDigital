package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_origem_id", nullable = true)
    private Long contaOrigemId;

    @Column(name = "conta_destino_id", nullable = true)
    private Long contaDestinoId;

    @Column(nullable = false)
    private Long valorEmCentavos;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    public Transacao() {}

    public Transacao(Long contaOrigemId, Long contaDestinoId, Long valorEmCentavos) {
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.valorEmCentavos = valorEmCentavos;
        this.dataHora = LocalDateTime.now();
    }

    @Transient
    public String getTipo() {
        if (contaOrigemId == null) return "DEPOSITO";
        if (contaDestinoId == null) return "SAQUE";
        return "TRANSFERENCIA";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContaOrigemId() { return contaOrigemId; }
    public void setContaOrigemId(Long contaOrigemId) { this.contaOrigemId = contaOrigemId; }

    public Long getContaDestinoId() { return contaDestinoId; }
    public void setContaDestinoId(Long contaDestinoId) { this.contaDestinoId = contaDestinoId; }

    public Long getValorEmCentavos() { return valorEmCentavos; }
    public void setValorEmCentavos(Long valorEmCentavos) { this.valorEmCentavos = valorEmCentavos; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
