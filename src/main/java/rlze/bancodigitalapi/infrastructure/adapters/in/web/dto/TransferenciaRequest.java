package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO para a execução de uma transferência entre contas")
public record TransferenciaRequest(
        @Schema(description = "ID da conta de origem para a transferência", example = "506dc261-2730-11f1-9746-8e822b232c7c")
        String idContaOrigem,
        @Schema(description = "ID da conta de destino para a transferência", example = "506dc261-2730-11f1-9746-8e822b232c7c")
        String idContaDestino,
        @Schema(description = "Valor da transferência", example = "100")
        BigDecimal valor
) {}
