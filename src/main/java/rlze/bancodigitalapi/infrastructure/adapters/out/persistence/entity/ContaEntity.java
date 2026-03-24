package rlze.bancodigitalapi.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContaEntity {

    @Id
    private String id;

    @Column(name = "nome_titular", nullable = false)
    private String nomeTitular;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Version
    private Integer version;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizado_em;
}
