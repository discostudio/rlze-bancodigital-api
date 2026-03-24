package rlze.bancodigitalapi.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rlze.bancodigitalapi.application.ports.out.ContaRepositoryPort;
import rlze.bancodigitalapi.domain.model.Conta;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.entity.ContaEntity;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.mapper.ContaMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContaPersistenceAdapter implements ContaRepositoryPort {

    private final SpringDataContaRepository repository;

    @Override
    public void salvar(Conta domain) {

        // 1. Tenta buscar a Entity existente
        Optional<ContaEntity> entityOpt = repository.findById(domain.getId());

        ContaEntity entity;
        if (entityOpt.isPresent()) {
            // ATUALIZAÇÃO: Usamos a que o Hibernate já conhece
            entity = entityOpt.get();
            entity.setAtualizado_em(LocalDateTime.now(Clock.systemUTC()));
            // Não setamos o 'version' manualmente aqui!
            // O Hibernate compara o que está no banco com o que está na memória.
        } else {
            // CRIAÇÃO: Nova instância
            entity = new ContaEntity();
            entity.setId(domain.getId());
            entity.setCriadoEm(domain.getCriadoEm());
            // IMPORTANTE: Para o primeiro INSERT, o version deve ser NULL
            // para o Hibernate entender que é um registro virgem.
            entity.setVersion(null);
        }

        // 2. Atualiza os campos de negócio
        entity.setNomeTitular(domain.getNomeTitular());
        entity.setSaldo(domain.getSaldo());

        // 3. Executa a gravação
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
