package rlze.bancodigitalapi.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;

public record TransferenciaRequest(
        String idContaOrigem,
        String idContaDestino,
        BigDecimal valor
) {}
