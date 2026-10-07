package com.plagiarism.detector.service;

import com.plagiarism.detector.model.DocumentInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class DocumentService {
    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    private final TextExtractionService extractor;
    private final PreprocessingService preprocessing;

    public DocumentService(TextExtractionService extractor, PreprocessingService preprocessing) {
        this.extractor = extractor;
        this.preprocessing = preprocessing;
        try { Files.createDirectories(uploadDir); } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DocumentInfo save(MultipartFile file) throws IOException {
        validate(file);
        String safe = Paths.get(Objects.requireNonNull(file.getOriginalFilename()))
                .getFileName().toString();
        String id = UUID.randomUUID().toString();
        Path destination = uploadDir.resolve(id + "_" + safe);
        file.transferTo(destination);

        return info(destination, id);
    }

    public List<DocumentInfo> list() throws IOException {
        List<DocumentInfo> list = new ArrayList<>();
        try (var stream = Files.list(uploadDir)) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                try {
                    String n = p.getFileName().toString();
                    int underscore = n.indexOf('_');
                    String id = underscore > 0 ? n.substring(0, underscore) : n;
                    list.add(info(p, id));
                } catch (IOException ignored) {}
            });
        }
        list.sort(Comparator.comparing(DocumentInfo::uploadedAt).reversed());
        return list;
    }

    public Path find(String id) throws IOException {
        try (var stream = Files.list(uploadDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(id + "_"))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchFileException("Document not found: " + id));
        }
    }

    public String extract(Path path) throws IOException {
        return extractor.extract(path);
    }

    public List<String> tokens(Path path) throws IOException {
        return preprocessing.tokenize(extractor.extract(path), true);
    }

    public void delete(String id) throws IOException {
        Files.deleteIfExists(find(id));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Empty file.");
        String name = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        if (!(name.endsWith(".pdf") || name.endsWith(".docx"))) {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported.");
        }
    }

    private DocumentInfo info(Path p, String id) throws IOException {
        String n = p.getFileName().toString();
        int underscore = n.indexOf('_');
        String original = underscore >= 0 ? n.substring(underscore + 1) : n;
        String type = original.toLowerCase().endsWith(".pdf") ? "PDF" : "DOCX";
        return new DocumentInfo(id, original, type, Files.size(p),
                OffsetDateTime.now().toString());
    }
}
