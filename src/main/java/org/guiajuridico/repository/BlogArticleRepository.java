package org.guiajuridico.repository;

import org.guiajuridico.model.BlogArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogArticleRepository extends JpaRepository<BlogArticle, Integer> {
    List<BlogArticle> findByPublishedTrueOrderByCreatedAtDesc();
    List<BlogArticle> findAllByOrderByCreatedAtDesc();
    Optional<BlogArticle> findBySlug(String slug);
    Optional<BlogArticle> findBySlugAndPublishedTrue(String slug);
}
