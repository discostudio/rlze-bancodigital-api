package rlze.bancodigitalapi.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.model.Conta;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.entity.ContaEntity;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.mapper.ContaMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContaPersistenceAdapter implements ContaRepositoryPort {

    private final SpringDataContaRepository repository;

    /*@Override
    public void salvar(Conta conta) {
        ContaEntity entity = ContaMapper.toEntity(conta);
        repository.save(entity);
    }*/

    @Override
    public void salvar(Conta domain) {
        // 1. Tenta buscar a Entity que já está na sessão do Hibernate (Persistence Context)
        // Isso evita o erro de "A different object with the same identifier..."
        ContaEntity entity = repository.findById(domain.getId())
                .orElseGet(ContaEntity::new);

        // 2. Atualiza os dados da Entity com o estado atual do objeto de Domínio
        entity.setId(domain.getId());
        entity.setNomeTitular(domain.getNomeTitular());
        entity.setSaldo(domain.getSaldo());
        entity.setVersion(domain.getVersion());
        entity.setCriadoEm(domain.getCriadoEm());
        entity.setAtualizado_em(LocalDateTime.now());

        // 3. Salva a referência que o Hibernate já conhece
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
