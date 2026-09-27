-- Tipo de documento da biblioteca (filtros públicos + formulário admin).
-- Rode no MySQL se ddl-auto=update não estiver ativo no ambiente.

ALTER TABLE library_documents
    ADD COLUMN document_type VARCHAR(64) NULL DEFAULT NULL;

UPDATE library_documents
SET document_type = 'Outros'
WHERE document_type IS NULL OR document_type = '';

ALTER TABLE library_documents
    MODIFY COLUMN document_type VARCHAR(64) NOT NULL;
