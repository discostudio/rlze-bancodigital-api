package rlze.bancodigitalapi.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.entity.ContaEntity;

import java.util.List;

public interface SpringDataContaRepository extends JpaRepository<ContaEntity, String> {

    List<ContaEntity> findByNomeTitularContainingIgnoreCase(String nome);
}
