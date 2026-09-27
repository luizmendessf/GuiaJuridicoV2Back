package org.guiajuridico.util;

import org.guiajuridico.model.BlogArticle;

import java.sql.Timestamp;

/**
 * Regra de data de publicação: seta publishedAt apenas na 1ª publicação;
 * não sobrescreve em republicações; não limpa ao despublicar.
 */
public final class BlogPublishDateHelper {

    private BlogPublishDateHelper() {}

    public static void applyPublishedAt(BlogArticle article, boolean published, Timestamp now) {
        if (article == null) return;
        if (!published) return;
        if (article.getPublishedAt() != null) return;
        article.setPublishedAt(now != null ? now : new Timestamp(System.currentTimeMillis()));
    }

    public static void applyPublishedAt(BlogArticle article, boolean published) {
        applyPublishedAt(article, published, null);
    }
}
