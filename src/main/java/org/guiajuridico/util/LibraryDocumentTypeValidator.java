package org.guiajuridico.util;

import java.util.List;
import java.util.Set;

public final class LibraryDocumentTypeValidator {

    public static final String TIPO_ARTIGO_CIENTIFICO = "Artigo Científico";
    public static final String TIPO_OBRA_LITERARIA = "Obra Literária";
    public static final String TIPO_LEGISLACAO = "Legislação";
    public static final String TIPO_DOUTRINA = "Doutrina";
    public static final String TIPO_JURISPRUDENCIA = "Jurisprudência";
    public static final String TIPO_MANUAL_APOSTILA = "Manual / Apostila";
    public static final String TIPO_OUTROS = "Outros";

    public static final List<String> TIPOS = List.of(
            TIPO_ARTIGO_CIENTIFICO,
            TIPO_OBRA_LITERARIA,
            TIPO_LEGISLACAO,
            TIPO_DOUTRINA,
            TIPO_JURISPRUDENCIA,
            TIPO_MANUAL_APOSTILA,
            TIPO_OUTROS
    );

    private static final Set<String> TIPOS_SET = Set.copyOf(TIPOS);

    private LibraryDocumentTypeValidator() {}

    public static boolean tipoValido(String tipo) {
        return tipo != null && TIPOS_SET.contains(tipo);
    }

    public static void validar(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new RuntimeException("documentType é obrigatório");
        }
        String trimmed = tipo.trim();
        if (!TIPOS_SET.contains(trimmed)) {
            throw new RuntimeException("documentType inválido");
        }
    }
}
