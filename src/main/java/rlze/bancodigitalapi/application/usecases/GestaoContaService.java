package rlze.bancodigitalapi.application.usecases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rlze.bancodigitalapi.application.ports.in.GestaoContaUseCase;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.model.Conta;

import java.math.BigDecimal;
import java.util.List;

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
    }

    @Override
    public List<Conta> listarPorNome(String nome) {
        return contaRepositoryPort.buscarPorNome(nome);
    }
}
