package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;

public record ContaResponse(
        String id,
        String nomeTitular,
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
