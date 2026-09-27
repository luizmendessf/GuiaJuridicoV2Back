-- Data de publicação real dos artigos do blog.
-- Rode no MySQL se ddl-auto=update não estiver ativo no ambiente.

ALTER TABLE blog_articles
    ADD COLUMN published_at TIMESTAMP NULL DEFAULT NULL;

UPDATE blog_articles
SET published_at = created_at
WHERE published = 1 AND published_at IS NULL AND created_at IS NOT NULL;
