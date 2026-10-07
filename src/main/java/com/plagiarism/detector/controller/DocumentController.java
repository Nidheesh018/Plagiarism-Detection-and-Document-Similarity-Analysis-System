package com.plagiarism.detector.controller;

import com.plagiarism.detector.model.AnalysisResponse;
import com.plagiarism.detector.model.DocumentInfo;
import com.plagiarism.detector.service.DocumentService;
import com.plagiarism.detector.service.PdfReportService;
import com.plagiarism.detector.service.SimilarityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class DocumentController {
    private final DocumentService documents;
    private final SimilarityService similarity;
    private final PdfReportService reports;

    public DocumentController(DocumentService documents, SimilarityService similarity, PdfReportService reports) {
        this.documents = documents;
        this.similarity = similarity;
        this.reports = reports;
    }

    @GetMapping("/documents")
    public List<DocumentInfo> documents() throws IOException {
        return documents.list();
    }

    @PostMapping("/documents/upload")
    public DocumentInfo upload(@RequestParam("file") MultipartFile file) throws IOException {
        return documents.save(file);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) throws IOException {
        documents.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @PostMapping("/analyze/{targetId}")
    public AnalysisResponse analyze(@PathVariable String targetId) throws IOException {
        List<DocumentInfo> all = documents.list();
        DocumentInfo targetInfo = all.stream()
                .filter(d -> d.id().equals(targetId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target document not found."));

        List<String> names = new ArrayList<>();
        List<List<String>> tokenDocs = new ArrayList<>();

        tokenDocs.add(documents.tokens(documents.find(targetId)));

        for (DocumentInfo d : all) {
            if (!d.id().equals(targetId)) {
                names.add(d.fileName());
                tokenDocs.add(documents.tokens(documents.find(d.id())));
            }
        }

        if (tokenDocs.size() == 1) {
            return new AnalysisResponse(targetInfo.fileName(), List.of(), null);
        }

        var results = similarity.compare(targetInfo.fileName(), names, tokenDocs);
        var report = reports.generate(targetInfo.fileName(), results);

        return new AnalysisResponse(
                targetInfo.fileName(),
                results,
                report.getFileName().toString()
        );
    }
}
