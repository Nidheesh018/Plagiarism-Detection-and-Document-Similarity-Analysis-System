package com.plagiarism.detector.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.plagiarism.detector.model.SimilarityResult;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PdfReportService {
    private final Path reportDir = Paths.get("reports").toAbsolutePath().normalize();

    public PdfReportService() {
        try { Files.createDirectories(reportDir); } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path generate(String targetName, List<SimilarityResult> results) throws IOException {
        String id = "report_" + System.currentTimeMillis() + ".pdf";
        Path output = reportDir.resolve(id);

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        try (OutputStream out = Files.newOutputStream(output)) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font heading = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 10);

            document.add(new Paragraph("PLAGIARISM DETECTION REPORT", title));
            document.add(new Paragraph("Target document: " + targetName, normal));
            document.add(new Paragraph("Generated: " + OffsetDateTime.now(), normal));
            document.add(new Paragraph(" "));

            for (int i = 0; i < results.size(); i++) {
                SimilarityResult r = results.get(i);

                document.add(new Paragraph(
                        (i + 1) + ". " + r.sourceDocument(), heading));

                document.add(new Paragraph(
                        String.format("Overall similarity: %.2f%% | Cosine: %.2f%% | Exact: %.2f%% | Partial: %.2f%% | Unique: %.2f%%",
                                r.overallSimilarity(), r.cosineSimilarity(),
                                r.exactMatchPercentage(), r.partialMatchPercentage(),
                                r.uniqueContentPercentage()), normal));

                document.add(new Paragraph("Classification: " + r.classification(), normal));

                if (!r.exactMatches().isEmpty()) {
                    document.add(new Paragraph("Exact matches:", heading));
                    for (var m : r.exactMatches()) {
                        document.add(new Paragraph(
                                "\"" + m.text() + "\" | target tokens " +
                                m.targetTokenStart() + "-" + m.targetTokenEnd(), normal));
                    }
                }

                if (!r.partialMatches().isEmpty()) {
                    document.add(new Paragraph("Partial matches:", heading));
                    for (var m : r.partialMatches()) {
                        document.add(new Paragraph(
                                "\"" + m.text() + "\" | LCS length: " + m.length(), normal));
                    }
                }
                document.add(new Paragraph(" "));
            }

            document.close();
        }
        return output;
    }

    public Path find(String filename) {
        return reportDir.resolve(Paths.get(filename).getFileName().toString());
    }
}
