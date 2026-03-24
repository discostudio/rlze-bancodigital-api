package rlze.bancodigitalapi.domain.model;

import lombok.Getter;
import rlze.bancodigitalapi.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Conta {

    private String id;
    private String nomeTitular;
    private BigDecimal saldo;
    private Integer version;
    private LocalDateTime criadoEm;

    // 1. Construtor para NOVA conta (Usado pelo UseCase)
    public Conta(String nomeTitular, BigDecimal saldoInicial) {
        this.id = UUID.randomUUID().toString();
        this.nomeTitular = nomeTitular;
        this.saldo = (saldoInicial != null) ? saldoInicial : BigDecimal.ZERO;
        this.version = 0;
        this.criadoEm = LocalDateTime.now();
    }

    // 2. NOVO: Construtor para conta EXISTENTE (Usado pelo Mapper)
    // O Mapper precisa deste aqui para compilar!
    public Conta(String id, String nomeTitular, BigDecimal saldo, Integer version, LocalDateTime criadoEm) {
        this.id = id;
        this.nomeTitular = nomeTitular;
        this.saldo = saldo;
        this.version = version;
        this.criadoEm = criadoEm;
    }

    /*private void validar() {
        if (nomeTitular == null || nomeTitular.isBlank()) {
            throw new BusinessException("Nome do titular é obrigatório.");
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Saldo inicial não pode ser negativo.");
        }
    }*/

    public void debitar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do débito deve ser positivo.");
        }
        if (this.saldo.compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente na conta: " + this.id);

        }
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do crédito deve ser positivo.");
        }
        this.saldo = this.saldo.add(valor);
    }
}
