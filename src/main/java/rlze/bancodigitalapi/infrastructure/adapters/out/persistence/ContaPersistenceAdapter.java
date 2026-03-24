package rlze.bancodigitalapi.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.model.Conta;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.entity.ContaEntity;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.mapper.ContaMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContaPersistenceAdapter implements ContaRepositoryPort {

    private final SpringDataContaRepository repository;

    @Override
    public void salvar(Conta conta) {
        ContaEntity entity = ContaMapper.toEntity(conta);
        repository.save(entity);
    }

    @Override
    public List<Conta> buscarPorNome(String nome) {
        return repository.findByNomeTitularContainingIgnoreCase(nome)
                .stream()
                .map(ContaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Conta> buscarPorId(String id) {
        return repository.findById(id).map(ContaMapper::toDomain);
    }
}
