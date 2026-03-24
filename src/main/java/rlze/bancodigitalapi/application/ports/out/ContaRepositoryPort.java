package rlze.bancodigitalapi.application.ports.out;

import rlze.bancodigitalapi.domain.model.Conta;

import java.util.List;
import java.util.Optional;

public interface ContaRepositoryPort {

    void salvar(Conta conta);
    List<Conta> buscarPorNome(String nome);
    //Optional<Conta> buscarPorId(String id);
}
