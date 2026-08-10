-- Auditoria de criação/edição de oportunidades (somente no banco; não exposto na UI).
-- Rode no MySQL se ddl-auto=update não estiver ativo no ambiente.
-- Se alguma coluna/constraint já existir, ignore o erro correspondente.

ALTER TABLE oportunidades
    ADD COLUMN data_atualizacao TIMESTAMP NULL DEFAULT NULL;

ALTER TABLE oportunidades
    ADD COLUMN atualizado_por_id INT NULL DEFAULT NULL;

UPDATE oportunidades
SET data_atualizacao = data_criacao
WHERE data_atualizacao IS NULL AND data_criacao IS NOT NULL;

ALTER TABLE oportunidades
    ADD CONSTRAINT fk_oportunidades_atualizado_por
    FOREIGN KEY (atualizado_por_id) REFERENCES usuarios(id);
