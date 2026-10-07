package com.plagiarism.detector.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;

@Service
public class TextExtractionService {

    public String extract(Path file) throws IOException {
        String name = file.getFileName().toString().toLowerCase();

        if (name.endsWith(".pdf")) {
            try (var document = Loader.loadPDF(Files.readAllBytes(file))) {
                return new PDFTextStripper().getText(document);
            }
        }

        if (name.endsWith(".docx")) {
            try (InputStream in = Files.newInputStream(file);
                 XWPFDocument document = new XWPFDocument(in);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        }

        throw new IllegalArgumentException("Only PDF and DOCX files are supported.");
    }
}
