package rlze.bancodigitalapi.infrastructure.adapters.out.persistence.mapper;

import rlze.bancodigitalapi.domain.model.Conta;
import rlze.bancodigitalapi.infrastructure.adapters.out.persistence.entity.ContaEntity;

public class ContaMapper {

    public static ContaEntity toEntity(Conta domain) {
        ContaEntity entity = new ContaEntity();
        entity.setId(domain.getId());
        entity.setNomeTitular(domain.getNomeTitular());
        entity.setSaldo(domain.getSaldo());
        entity.setCriadoEm(domain.getCriadoEm());

        // Para novos registros, deixe a versão como NULL ou 0
        // Se o Hibernate vir NULL, ele entende que é um INSERT
        entity.setVersion(domain.getVersion() == 0 ? null : domain.getVersion());

        return entity;
    }

    public static Conta toDomain(ContaEntity entity) {
        return new Conta(
                entity.getId(),
                entity.getNomeTitular(),
                entity.getSaldo(),
                entity.getVersion(),
                entity.getCriadoEm()
        );
    }
}
