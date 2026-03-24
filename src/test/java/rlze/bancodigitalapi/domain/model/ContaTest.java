package rlze.bancodigitalapi.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rlze.bancodigitalapi.domain.exception.BusinessException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContaTest {

    @Test
    @DisplayName("Deve debitar valor com sucesso quando houver saldo")
    void deveDebitarComSucesso() {
        //Conta conta = new Conta("1", "Joaquim", new BigDecimal("100.00"));
        Conta conta = new Conta("Joaquim", new BigDecimal("100.00"));

        conta.debitar(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), conta.getSaldo());
    }

    @Test
    @DisplayName("Deve lancar excecao ao debitar valor maior que o saldo")
    void deveLancarExcecaoSaldoInsuficiente() {
        //Conta conta = new Conta("1", "Joaquim", new BigDecimal("50.00"));
        Conta conta = new Conta("Joaquim", new BigDecimal("50.00"));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            conta.debitar(new BigDecimal("50.01"));
        });

        assertEquals("Saldo insuficiente na conta: " + conta.getId(), ex.getMessage());
    }

    @Test
    @DisplayName("Deve creditar valor com sucesso")
    void deveCreditarComSucesso() {
        //Conta conta = new Conta("1", "Joaquim", new BigDecimal("100.00"));
        Conta conta = new Conta("Joaquim", new BigDecimal("100.00"));

        conta.creditar(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), conta.getSaldo());
    }
}
