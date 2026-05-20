package org.guiajuridico.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Gera capas (PNG) a partir da primeira página de PDFs da biblioteca.
 * Imagens são salvas em {@code image.upload.dir}, como os demais uploads.
 */
@Service
public class PdfCoverService {

    private static final Pattern SAFE_PDF_NAME = Pattern.compile("^[a-zA-Z0-9._-]+\\.pdf$");
    private static final int RENDER_DPI = 144;

    @Value("${pdf.upload.dir}")
    private String pdfUploadDirString;

    @Value("${image.upload.dir}")
    private String imageUploadDirString;

    private Path pdfDir() {
        return Paths.get(pdfUploadDirString);
    }

    private Path imageDir() {
        return Paths.get(imageUploadDirString);
    }

    /**
     * Gera (ou reutiliza) o nome do arquivo de capa para o PDF indicado.
     *
     * @return nome do arquivo PNG em image.upload.dir, ou null se falhar
     */
    public String generateCoverFromPdf(String pdfFilename) throws IOException {
        if (pdfFilename == null || !SAFE_PDF_NAME.matcher(pdfFilename).matches()) {
            throw new IOException("Nome de PDF inválido");
        }

        Path pdfPath = pdfDir().resolve(pdfFilename).normalize();
        if (!pdfPath.startsWith(pdfDir().normalize()) || !Files.isRegularFile(pdfPath)) {
            throw new IOException("PDF não encontrado no servidor");
        }

        String coverFilename = coverFilenameFor(pdfFilename);
        Path coverPath = imageDir().resolve(coverFilename).normalize();
        if (!coverPath.startsWith(imageDir().normalize())) {
            throw new IOException("Caminho de capa inválido");
        }

        if (Files.isRegularFile(coverPath)) {
            return coverFilename;
        }

        Files.createDirectories(imageDir());

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            if (document.getNumberOfPages() < 1) {
                throw new IOException("PDF sem páginas");
            }
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, RENDER_DPI, ImageType.RGB);
            ImageIO.write(image, "png", coverPath.toFile());
        }

        return coverFilename;
    }

    /**
     * Devolve o nome da capa associada ao PDF, gerando-a se ainda não existir.
     */
    public String ensureCoverForPdf(String pdfFilename) {
        try {
            return generateCoverFromPdf(pdfFilename);
        } catch (IOException e) {
            return null;
        }
    }

    public static String coverFilenameFor(String pdfFilename) {
        String base = pdfFilename;
        if (base.toLowerCase().endsWith(".pdf")) {
            base = base.substring(0, base.length() - 4);
        }
        String safe = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "pdfcover_" + safe + ".png";
    }

    public Path resolveCoverPath(String pdfFilename) throws IOException {
        String coverFilename = coverFilenameFor(pdfFilename);
        Path coverPath = imageDir().resolve(coverFilename).normalize();
        if (!coverPath.startsWith(imageDir().normalize())) {
            throw new IOException("Caminho inválido");
        }
        if (!Files.isRegularFile(coverPath)) {
            generateCoverFromPdf(pdfFilename);
        }
        return coverPath;
    }
}
