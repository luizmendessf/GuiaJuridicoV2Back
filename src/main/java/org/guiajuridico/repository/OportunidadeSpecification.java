package org.guiajuridico.repository;

import org.guiajuridico.model.Oportunidade;
import org.springframework.data.jpa.domain.Specification;

public class OportunidadeSpecification {

    // Filtro para o campo 'type' (Estágio, Congresso, etc.)
    public static Specification<Oportunidade> porTipo(String tipo) {
        return (root, query, builder) ->
                builder.equal(root.get("type"), tipo);
    }

    // Filtro para o campo 'status' (Abertas, Encerradas, etc.)
    public static Specification<Oportunidade> porStatus(String status) {
        return (root, query, builder) ->
                builder.equal(root.get("status"), status);
    }

    // Filtro para a barra de busca (termo genérico)
    public static Specification<Oportunidade> porTermoDeBusca(String termo) {
        String termoLowerCase = "%" + termo.toLowerCase() + "%";
        return (root, query, builder) ->
                builder.or(
                        builder.like(builder.lower(root.get("title")), termoLowerCase),
                        builder.like(builder.lower(root.get("company")), termoLowerCase),
                        builder.like(builder.lower(root.get("description")), termoLowerCase)
                );
    }
}