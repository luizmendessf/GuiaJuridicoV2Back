package org.guiajuridico.repository;

import org.guiajuridico.model.LibraryDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryDocumentRepository extends JpaRepository<LibraryDocument, Integer> {

    List<LibraryDocument> findAllByOrderByCreatedAtDesc();

    Optional<LibraryDocument> findBySlug(String slug);
}
