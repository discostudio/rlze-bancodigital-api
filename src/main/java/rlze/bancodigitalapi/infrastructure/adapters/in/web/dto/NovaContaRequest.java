package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO para a criação de uma conta")
public record NovaContaRequest(
        @Schema(description = "Nome do titular da conta", example = "João da Silva")
        String nomeTitular,
        @Schema(description = "Saldo da conta", example = "500")
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