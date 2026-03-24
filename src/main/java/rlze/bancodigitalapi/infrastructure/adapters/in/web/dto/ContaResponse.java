package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;

@Schema(description = "DTO para retornar os dados de uma conta")
public record ContaResponse(
        @Schema(description = "ID da conta", example = "506dc261-2730-11f1-9746-8e822b232c7c")
        String id,
        @Schema(description = "Nome o titular da conta", example = "João da Silva")
        String nomeTitular,
        @Schema(description = "Saldo da conta", example = "500")
        BigDecimal saldo
) {

    /**
     * Factory method para converter o objeto de Domínio para DTO de Resposta.
     * Isso evita expor a lógica de mapeamento dentro do Controller.
     */
    public static ContaResponse fromDomain(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNomeTitular(),
                conta.getSaldo()
        );
    }
}
