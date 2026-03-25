package rlze.bancodigitalapi.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rlze.bancodigitalapi.application.ports.in.GestaoContaUseCase;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.exception.EntityNotFoundException;
import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestaoContaService implements GestaoContaUseCase {

    private final ContaRepositoryPort contaRepositoryPort;

    @Override
    @Transactional
    public void criarConta(String nome, BigDecimal saldoInicial) {
        // O domínio cria o objeto e já valida as regras (nome vazio, saldo negativo, etc)
        Conta novaConta = new Conta(nome, saldoInicial);
        contaRepositoryPort.salvar(novaConta);

        log.info("criarConta({})", nome);
    }

    @Override
    public List<Conta> listarPorNome(String nome) {
        List<Conta> contas = contaRepositoryPort.buscarPorNome(nome);;

        // Se a lista vier nula ou vazia, lançamos o 404
        if (contas == null || contas.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma conta encontrada para o nome: " + nome);
        }

        log.info("listarConta({})", nome);

        return contas;
    }
}
