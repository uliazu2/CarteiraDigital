package org.example;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID usuarioId;

    @Column(nullable = false)
    private Long saldoEmCentavos;

    public Conta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public Long getSaldoEmCentavos() { return saldoEmCentavos; }
    public void setSaldoEmCentavos(Long saldoEmCentavos) { this.saldoEmCentavos = saldoEmCentavos; }

    public void debitar(Long valor) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Valor de débito inválido.");
        }
        if (this.saldoEmCentavos < valor) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
        this.saldoEmCentavos -= valor;
    }

    public void creditar(Long valor) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Valor de crédito inválido.");
        }
        this.saldoEmCentavos += valor;
    }
}
