package rlze.bancodigitalapi.application.dto;

import java.math.BigDecimal;

public record NovaContaRequest(
        String nomeTitular,
        BigDecimal saldo
) {
    // Adicione validações customizadas se não usar @Valid do Bean Validation
    public NovaContaRequest {
        if (nomeTitular == null || nomeTitular.isBlank()) {
            throw new IllegalArgumentException("Nome do titular não pode ser vazio.");
        }
        if (saldo != null && saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial não pode ser negativo.");
        }
    }
}