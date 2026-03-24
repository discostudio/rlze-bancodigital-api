package rlze.bancodigitalapi.application.ports.in;

import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;
import java.util.List;

public interface GestaoContaUseCase {

    void criarConta(String nome, BigDecimal saldoInicial);
    List<Conta> listarPorNome(String nome);
}
