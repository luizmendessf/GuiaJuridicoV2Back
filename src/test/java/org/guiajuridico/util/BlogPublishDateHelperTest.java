package org.guiajuridico.util;

import org.guiajuridico.model.BlogArticle;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BlogPublishDateHelperTest {

    @Test
    void primeiraPublicacaoDefinePublishedAt() {
        BlogArticle article = new BlogArticle();
        Timestamp agora = Timestamp.valueOf("2026-03-15 10:00:00");

        BlogPublishDateHelper.applyPublishedAt(article, true, agora);

        assertEquals(agora, article.getPublishedAt());
    }

    @Test
    void rascunhoNaoDefinePublishedAt() {
        BlogArticle article = new BlogArticle();

        BlogPublishDateHelper.applyPublishedAt(article, false, Timestamp.valueOf("2026-03-15 10:00:00"));

        assertNull(article.getPublishedAt());
    }

    @Test
    void republicacaoPreservaPublishedAtOriginal() {
        BlogArticle article = new BlogArticle();
        Timestamp original = Timestamp.valueOf("2026-01-01 08:00:00");
        article.setPublishedAt(original);

        BlogPublishDateHelper.applyPublishedAt(article, true, Timestamp.valueOf("2026-06-01 12:00:00"));

        assertEquals(original, article.getPublishedAt());
    }

    @Test
    void despublicarNaoLimpaPublishedAt() {
        BlogArticle article = new BlogArticle();
        Timestamp original = Timestamp.valueOf("2026-01-01 08:00:00");
        article.setPublishedAt(original);

        BlogPublishDateHelper.applyPublishedAt(article, false, Timestamp.valueOf("2026-06-01 12:00:00"));

        assertEquals(original, article.getPublishedAt());
    }

    @Test
    void applyPublishedAtComNowNuloUsaTimestampAtual() {
        BlogArticle article = new BlogArticle();

        BlogPublishDateHelper.applyPublishedAt(article, true);

        assertNotNull(article.getPublishedAt());
    }
}
