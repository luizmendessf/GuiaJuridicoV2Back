package org.guiajuridico.repository;

import org.guiajuridico.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OportunidadeRepository extends JpaRepository<Oportunidade, Integer>, JpaSpecificationExecutor<Oportunidade> {
}