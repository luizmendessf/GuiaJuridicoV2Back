package org.guiajuridico.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public final class BlogHtmlSanitizer {

    private static final Safelist BLOG_CONTENT_SAFELIST = Safelist.relaxed()
            .addTags("h2", "h3", "span")
            .addAttributes("a", "href", "target", "rel")
            .addAttributes("img", "src", "alt", "title", "width", "height")
            .addAttributes(":all", "style")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");

    private BlogHtmlSanitizer() {
    }

    public static String sanitize(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        if (!looksLikeHtml(content)) {
            return content;
        }

        String cleaned = Jsoup.clean(content, BLOG_CONTENT_SAFELIST);
        Document doc = Jsoup.parseBodyFragment(cleaned);
        doc.select("a[href]").forEach(BlogHtmlSanitizer::applyLinkAttributes);
        return doc.body().html();
    }

    private static boolean looksLikeHtml(String content) {
        return content.trim().matches("(?s).*<\\/?[a-z][^>]*>.*");
    }

    private static void applyLinkAttributes(Element anchor) {
        String href = anchor.attr("href");
        if (href.startsWith("http://") || href.startsWith("https://")) {
            anchor.attr("target", "_blank");
            anchor.attr("rel", "noopener noreferrer");
        }
    }
}
