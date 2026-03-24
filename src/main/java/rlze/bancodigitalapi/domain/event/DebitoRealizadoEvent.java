package rlze.bancodigitalapi.domain.event;

import java.math.BigDecimal;

public record DebitoRealizadoEvent(String idConta, BigDecimal valor, BigDecimal saldoAtualizado) {
}
