package org.guiajuridico.service;

import org.guiajuridico.dto.BlogArticleDetailDto;
import org.guiajuridico.dto.BlogArticleSummaryDto;
import org.guiajuridico.dto.BlogArticleUpsertDto;
import org.guiajuridico.model.BlogArticle;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.BlogArticleRepository;
import org.guiajuridico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BlogArticleService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    @Autowired
    private BlogArticleRepository blogArticleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<BlogArticleSummaryDto> listarPublicados() {
        return blogArticleRepository.findByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public BlogArticleDetailDto buscarPublicadoPorIdOuSlug(String idOuSlug) {
        Optional<BlogArticle> found;

        Integer maybeId = tryParseInt(idOuSlug);
        if (maybeId != null) {
            found = blogArticleRepository.findById(maybeId).filter(a -> Boolean.TRUE.equals(a.getPublished()));
        } else {
            found = blogArticleRepository.findBySlugAndPublishedTrue(idOuSlug);
        }

        BlogArticle article = found.orElseThrow(() -> new RuntimeException("Artigo não encontrado"));
        return toDetailDto(article);
    }

    public List<BlogArticleSummaryDto> listarTodosParaAdmin() {
        return blogArticleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public BlogArticleDetailDto buscarPorIdParaAdmin(Integer id) {
        BlogArticle article = blogArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));
        return toDetailDto(article);
    }

    public BlogArticleDetailDto criar(BlogArticleUpsertDto dto) {
        validar(dto);

        Usuario author = getUsuarioLogado();

        BlogArticle article = new BlogArticle();
        article.setTitle(dto.getTitle().trim());
        article.setSubtitle(dto.getSubtitle().trim());
        article.setImagePath(isBlank(dto.getImagePath()) ? null : dto.getImagePath().trim());
        article.setContent(dto.getContent());
        article.setPublished(dto.getPublished() != null ? dto.getPublished() : Boolean.FALSE);
        article.setAuthorUser(author);

        String slug = escolherSlug(dto.getSlug(), dto.getTitle());
        if (!isBlank(slug)) {
            garantirSlugDisponivel(slug, null);
            article.setSlug(slug);
        } else {
            article.setSlug(null);
        }

        BlogArticle saved = blogArticleRepository.save(article);
        return toDetailDto(saved);
    }

    public BlogArticleDetailDto atualizar(Integer id, BlogArticleUpsertDto dto) {
        validar(dto);

        BlogArticle article = blogArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        Usuario author = getUsuarioLogado();

        article.setTitle(dto.getTitle().trim());
        article.setSubtitle(dto.getSubtitle().trim());
        article.setImagePath(isBlank(dto.getImagePath()) ? null : dto.getImagePath().trim());
        article.setContent(dto.getContent());
        article.setPublished(dto.getPublished() != null ? dto.getPublished() : Boolean.FALSE);
        article.setAuthorUser(author);

        String slug = escolherSlug(dto.getSlug(), dto.getTitle());
        if (!isBlank(slug)) {
            garantirSlugDisponivel(slug, article.getId());
            article.setSlug(slug);
        } else {
            article.setSlug(null);
        }

        BlogArticle saved = blogArticleRepository.save(article);
        return toDetailDto(saved);
    }

    public void deletar(Integer id) {
        BlogArticle article = blogArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        if (Boolean.TRUE.equals(article.getPublished()) && !usuarioLogadoEhAdmin()) {
            throw new AccessDeniedException("Apenas ADMIN pode excluir artigos publicados");
        }

        blogArticleRepository.delete(article);
    }

    private void validar(BlogArticleUpsertDto dto) {
        if (dto == null) throw new RuntimeException("Payload inválido");
        if (isBlank(dto.getTitle())) throw new RuntimeException("title é obrigatório");
        if (isBlank(dto.getSubtitle())) throw new RuntimeException("subtitle é obrigatório");
        if (isBlank(dto.getContent())) throw new RuntimeException("content é obrigatório");
    }

    private Usuario getUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private boolean usuarioLogadoEhAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) return false;
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void garantirSlugDisponivel(String slug, Integer idAtual) {
        Optional<BlogArticle> existing = blogArticleRepository.findBySlug(slug);
        if (existing.isEmpty()) return;
        if (idAtual == null) throw new RuntimeException("slug já está em uso");
        if (!idAtual.equals(existing.get().getId())) throw new RuntimeException("slug já está em uso");
    }

    private BlogArticleSummaryDto toSummaryDto(BlogArticle a) {
        BlogArticleSummaryDto dto = new BlogArticleSummaryDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setSubtitle(a.getSubtitle());
        dto.setSlug(a.getSlug());
        dto.setImagePath(a.getImagePath());
        dto.setPublished(a.getPublished());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }

    private BlogArticleDetailDto toDetailDto(BlogArticle a) {
        BlogArticleDetailDto dto = new BlogArticleDetailDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setSubtitle(a.getSubtitle());
        dto.setSlug(a.getSlug());
        dto.setImagePath(a.getImagePath());
        dto.setContent(a.getContent());
        dto.setPublished(a.getPublished());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }

    private static Integer tryParseInt(String s) {
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String escolherSlug(String slugInput, String title) {
        String raw = isBlank(slugInput) ? title : slugInput;
        return slugify(raw);
    }

    private static String slugify(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;

        String noWhitespace = WHITESPACE.matcher(trimmed).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String lower = withoutAccents.toLowerCase();
        String slug = NONLATIN.matcher(lower).replaceAll("");
        slug = slug.replaceAll("[-]{2,}", "-");
        slug = slug.replaceAll("(^-+|-+$)", "");
        return slug.isEmpty() ? null : slug;
    }
}
