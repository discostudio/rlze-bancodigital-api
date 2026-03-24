package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "DTO para a criação de uma conta")
public record NovaContaRequest(
        @Schema(description = "Nome do titular da conta", example = "João da Silva")
        @NotNull(message = "O nome do titular é obrigatório")
        @NotBlank(message = "O nome do titular é obrigatório")
        String nomeTitular,
        @Schema(description = "Saldo da conta", example = "500")
        @PositiveOrZero(message = "O saldo inicial da conta não pode ser negativo.")
        BigDecimal saldo
) {
    // Adicione validações customizadas se não usar @Valid do Bean Validation
    /*public NovaContaRequest {
        if (nomeTitular == null || nomeTitular.isBlank()) {
            throw new IllegalArgumentException("Nome do titular não pode ser vazio.");
        }
        if (saldo != null && saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial não pode ser negativo.");
        }
    }*/
}