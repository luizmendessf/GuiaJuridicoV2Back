-- Opcional: permitir senha NULL para contas só Google (Hibernate ddl-auto nem sempre altera NOT NULL).
-- Execute manualmente no MySQL de produção se quiser senha ausente em vez de placeholder.
ALTER TABLE usuarios MODIFY COLUMN senha VARCHAR(255) NULL;
