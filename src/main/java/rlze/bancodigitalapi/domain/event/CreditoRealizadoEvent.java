package rlze.bancodigitalapi.domain.event;

import java.math.BigDecimal;

public record CreditoRealizadoEvent(String idConta, BigDecimal valor, BigDecimal saldoAtualizado) {
}
