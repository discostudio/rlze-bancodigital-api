CREATE TABLE contas (
    id VARCHAR(36) PRIMARY KEY,
    nome_titular VARCHAR(255) NOT NULL,
    saldo DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    version INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NULL
);

-- Dados iniciais (UUID() gera o ID no MySQL)
INSERT INTO contas (id, nome_titular, saldo, version, criado_em)
VALUES (UUID(), 'Joaquim Silveira', 1000.00, 0, UTC_TIMESTAMP());

INSERT INTO contas (id, nome_titular, saldo, version, criado_em)
VALUES (UUID(), 'Bob Silva', 500.00, 0, UTC_TIMESTAMP());

INSERT INTO contas (id, nome_titular, saldo, version, criado_em)
VALUES (UUID(), 'Joana Oliveira', 100.00, 0, UTC_TIMESTAMP());