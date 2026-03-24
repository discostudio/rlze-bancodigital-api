package rlze.bancodigitalapi.application.dto;

import java.math.BigDecimal;

public record TransferenciaRequest(
        String idContaOrigem,
        String idContaDestino,
        BigDecimal valor
) {}
