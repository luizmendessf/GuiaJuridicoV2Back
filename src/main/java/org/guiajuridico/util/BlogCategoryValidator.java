package org.guiajuridico.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlogCategoryValidator {

    public static final String CATEGORIA_ENTREVISTAS = "Entrevistas";
    public static final String CATEGORIA_ARTIGOS = "Artigos";
    public static final String CATEGORIA_CARREIRA = "Carreira";
    public static final String CATEGORIA_ANALISE_MERCADO = "Análise de Mercado";
    public static final String CATEGORIA_MATERIAIS_ENSINO = "Materiais de Ensino";
    public static final String CATEGORIA_DICAS_DEMAIS = "Dicas e Demais";
    public static final String CATEGORIA_MINISSIMULADO = "Minissimulado";

    private static final Set<String> CATEGORIAS = Set.of(
            CATEGORIA_ENTREVISTAS,
            CATEGORIA_ARTIGOS,
            CATEGORIA_CARREIRA,
            CATEGORIA_ANALISE_MERCADO,
            CATEGORIA_MATERIAIS_ENSINO,
            CATEGORIA_DICAS_DEMAIS,
            CATEGORIA_MINISSIMULADO
    );

    private static final Map<String, List<String>> SUBCATEGORIAS = Map.of(
            CATEGORIA_ARTIGOS, List.of("Informativos", "Científicos"),
            CATEGORIA_MATERIAIS_ENSINO, List.of(
                    "Civil",
                    "Processual Civil",
                    "Penal",
                    "Processual Penal",
                    "Constitucional",
                    "Administrativo",
                    "Tributário",
                    "Empresarial",
                    "Justiça Multiportas"
            )
    );

    private BlogCategoryValidator() {}

    public static boolean categoriaValida(String categoria) {
        return categoria != null && CATEGORIAS.contains(categoria);
    }

    public static boolean subcategoriaValida(String categoria, String subcategoria) {
        if (subcategoria == null || subcategoria.isBlank()) return true;
        if (!categoriaValida(categoria)) return false;
        List<String> permitidas = SUBCATEGORIAS.get(categoria);
        return permitidas != null && permitidas.contains(subcategoria);
    }

    public static void validar(String categoria, String subcategoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new RuntimeException("category é obrigatório");
        }
        if (!categoriaValida(categoria)) {
            throw new RuntimeException("category inválida");
        }
        if (subcategoria != null && !subcategoria.isBlank() && !subcategoriaValida(categoria, subcategoria)) {
            throw new RuntimeException("subcategory inválida para a category selecionada");
        }
        List<String> permitidas = SUBCATEGORIAS.get(categoria);
        if (permitidas != null && (subcategoria == null || subcategoria.isBlank())) {
            throw new RuntimeException("subcategory é obrigatória para a category selecionada");
        }
    }
}
