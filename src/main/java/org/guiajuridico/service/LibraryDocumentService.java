package org.guiajuridico.service;

import org.guiajuridico.dto.LibraryDocumentDetailDto;
import org.guiajuridico.dto.LibraryDocumentSummaryDto;
import org.guiajuridico.dto.LibraryDocumentUpsertDto;
import org.guiajuridico.model.LibraryDocument;
import org.guiajuridico.model.Usuario;
import org.guiajuridico.repository.LibraryDocumentRepository;
import org.guiajuridico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LibraryDocumentService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern SAFE_PDF_NAME = Pattern.compile("^[a-zA-Z0-9._-]+\\.pdf$");

    @Autowired
    private LibraryDocumentRepository libraryDocumentRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PdfCoverService pdfCoverService;

    @Value("${pdf.upload.dir}")
    private String pdfUploadDirString;

    private Path pdfDir() {
        return Paths.get(pdfUploadDirString);
    }

    public List<LibraryDocumentSummaryDto> listarPublicados() {
        return libraryDocumentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(d -> Boolean.TRUE.equals(d.getPublished()))
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public LibraryDocumentDetailDto buscarPublicadoPorIdOuSlug(String idOuSlug) {
        Optional<LibraryDocument> found;

        Integer maybeId = tryParseInt(idOuSlug);
        if (maybeId != null) {
            found = libraryDocumentRepository.findById(maybeId).filter(d -> Boolean.TRUE.equals(d.getPublished()));
        } else {
            found = libraryDocumentRepository.findBySlug(idOuSlug)
                    .filter(d -> Boolean.TRUE.equals(d.getPublished()));
        }

        LibraryDocument doc = found.orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        return toDetailDto(doc);
    }

    public List<LibraryDocumentSummaryDto> listarTodosParaAdmin() {
        return libraryDocumentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public LibraryDocumentDetailDto buscarPorIdParaAdmin(Integer id) {
        LibraryDocument doc = libraryDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        return toDetailDto(doc);
    }

    public LibraryDocumentDetailDto criar(LibraryDocumentUpsertDto dto) {
        validar(dto);

        Usuario author = getUsuarioLogado();
        String pdfFilename = resolverPdfFilename(dto.getPdfFilename());

        LibraryDocument doc = new LibraryDocument();
        doc.setTitle(dto.getTitle().trim());
        doc.setDescription(dto.getDescription().trim());
        doc.setCoverImagePath(resolverCoverImagePath(dto.getCoverImagePath(), pdfFilename, null));
        doc.setPdfFilename(pdfFilename);
        doc.setPublished(dto.getPublished() != null ? dto.getPublished() : Boolean.FALSE);
        doc.setAuthorUser(author);

        String slug = escolherSlug(dto.getSlug(), dto.getTitle());
        if (!isBlank(slug)) {
            garantirSlugDisponivel(slug, null);
            doc.setSlug(slug);
        } else {
            doc.setSlug(null);
        }

        LibraryDocument saved = libraryDocumentRepository.save(doc);
        return toDetailDto(saved);
    }

    public LibraryDocumentDetailDto atualizar(Integer id, LibraryDocumentUpsertDto dto) {
        validar(dto);

        LibraryDocument doc = libraryDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        Usuario author = getUsuarioLogado();
        String newPdf = resolverPdfFilename(dto.getPdfFilename());
        String oldPdf = doc.getPdfFilename();

        boolean pdfChanged = oldPdf != null && !oldPdf.equals(newPdf);

        doc.setTitle(dto.getTitle().trim());
        doc.setDescription(dto.getDescription().trim());
        String cover = resolverCoverImagePath(dto.getCoverImagePath(), newPdf, pdfChanged ? null : doc.getCoverImagePath());
        doc.setCoverImagePath(cover);
        doc.setPdfFilename(newPdf);
        doc.setPublished(dto.getPublished() != null ? dto.getPublished() : Boolean.FALSE);
        doc.setAuthorUser(author);

        String slug = escolherSlug(dto.getSlug(), dto.getTitle());
        if (!isBlank(slug)) {
            garantirSlugDisponivel(slug, doc.getId());
            doc.setSlug(slug);
        } else {
            doc.setSlug(null);
        }

        LibraryDocument saved = libraryDocumentRepository.save(doc);
        if (oldPdf != null && !oldPdf.isBlank() && !oldPdf.equals(newPdf)) {
            tryDeletePdfFile(oldPdf);
        }
        return toDetailDto(saved);
    }

    public void deletar(Integer id) {
        LibraryDocument doc = libraryDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        if (Boolean.TRUE.equals(doc.getPublished()) && !usuarioLogadoEhAdmin()) {
            throw new AccessDeniedException("Apenas ADMIN pode excluir documentos publicados");
        }

        String pdf = doc.getPdfFilename();
        libraryDocumentRepository.delete(doc);
        tryDeletePdfFile(pdf);
    }

    /**
     * Usa capa personalizada se fornecida; caso contrário gera a partir da primeira página do PDF.
     */
    private String resolverCoverImagePath(String coverInput, String pdfFilename, String existingCover) {
        String normalized = normalizeImagePath(coverInput);
        if (!isBlank(normalized)) {
            return normalized;
        }
        if (!isBlank(existingCover) && pdfFilename != null) {
            return existingCover;
        }
        return pdfCoverService.ensureCoverForPdf(pdfFilename);
    }

    /**
     * Aceita nome de arquivo ou URL retornada por /api/images/upload.
     */
    private static String normalizeImagePath(String raw) {
        if (isBlank(raw)) return null;
        String s = raw.trim();
        int idx = s.indexOf("/images/");
        if (idx >= 0) {
            s = s.substring(idx + "/images/".length());
        }
        int slash = s.lastIndexOf('/');
        if (slash >= 0) {
            s = s.substring(slash + 1);
        }
        int q = s.indexOf('?');
        if (q >= 0) {
            s = s.substring(0, q);
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private void validar(LibraryDocumentUpsertDto dto) {
        if (dto == null) throw new RuntimeException("Payload inválido");
        if (isBlank(dto.getTitle())) throw new RuntimeException("O título é obrigatório");
        if (isBlank(dto.getDescription())) throw new RuntimeException("O subtítulo é obrigatório");
        if (isBlank(dto.getPdfFilename())) throw new RuntimeException("O PDF é obrigatório (envie o arquivo antes de salvar)");
    }

    private String resolverPdfFilename(String raw) {
        String normalized = normalizePdfFilename(raw);
        if (normalized == null || normalized.isBlank()) {
            throw new RuntimeException("Nome do PDF inválido. Faça upload do arquivo .pdf e use o nome retornado pela API.");
        }
        Path path = pdfDir().resolve(normalized).normalize();
        if (!path.startsWith(pdfDir().normalize()) || !Files.isRegularFile(path)) {
            throw new RuntimeException("O PDF indicado não existe no servidor. Faça upload novamente.");
        }
        return normalized;
    }

    /**
     * Aceita só o nome do arquivo ou uma URL que termine em /api/pdfs/{nome}.pdf
     */
    static String normalizePdfFilename(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        int idx = s.indexOf("/pdfs/");
        if (idx >= 0) {
            s = s.substring(idx + "/pdfs/".length());
        }
        int slash = s.lastIndexOf('/');
        if (slash >= 0) {
            s = s.substring(slash + 1);
        }
        int q = s.indexOf('?');
        if (q >= 0) {
            s = s.substring(0, q);
        }
        s = s.trim();
        if (!SAFE_PDF_NAME.matcher(s).matches()) {
            return null;
        }
        return s;
    }

    private void tryDeletePdfFile(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path p = pdfDir().resolve(filename).normalize();
            if (!p.startsWith(pdfDir().normalize())) {
                return;
            }
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // arquivo já ausente ou erro de FS — não bloqueia o fluxo
        }
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
        Optional<LibraryDocument> existing = libraryDocumentRepository.findBySlug(slug);
        if (existing.isEmpty()) return;
        if (idAtual == null) {
            throw new RuntimeException("Já existe um documento com este slug. Deixe o slug em branco para gerar outro ou informe um slug diferente.");
        }
        if (!idAtual.equals(existing.get().getId())) {
            throw new RuntimeException("Já existe um documento com este slug. Deixe o slug em branco para gerar outro ou informe um slug diferente.");
        }
    }

    private LibraryDocumentSummaryDto toSummaryDto(LibraryDocument d) {
        LibraryDocumentSummaryDto dto = new LibraryDocumentSummaryDto();
        dto.setId(d.getId());
        dto.setTitle(d.getTitle());
        dto.setDescription(d.getDescription());
        dto.setSlug(d.getSlug());
        dto.setCoverImagePath(d.getCoverImagePath());
        dto.setPdfFilename(d.getPdfFilename());
        dto.setPublished(d.getPublished());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private LibraryDocumentDetailDto toDetailDto(LibraryDocument d) {
        LibraryDocumentDetailDto dto = new LibraryDocumentDetailDto();
        dto.setId(d.getId());
        dto.setTitle(d.getTitle());
        dto.setDescription(d.getDescription());
        dto.setSlug(d.getSlug());
        dto.setCoverImagePath(d.getCoverImagePath());
        dto.setPdfFilename(d.getPdfFilename());
        dto.setPublished(d.getPublished());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
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
