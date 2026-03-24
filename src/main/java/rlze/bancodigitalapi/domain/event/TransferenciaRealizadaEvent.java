package rlze.bancodigitalapi.domain.event;

import java.math.BigDecimal;

public record TransferenciaRealizadaEvent(
        String idOrigem,
        String idDestino,
        BigDecimal valor
) {}
