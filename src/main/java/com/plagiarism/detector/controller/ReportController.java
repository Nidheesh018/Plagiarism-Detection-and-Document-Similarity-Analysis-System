package com.plagiarism.detector.controller;

import com.plagiarism.detector.service.PdfReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {
    private final PdfReportService reports;

    public ReportController(PdfReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String filename) throws IOException {
        var path = reports.find(filename);
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        var resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }
}
