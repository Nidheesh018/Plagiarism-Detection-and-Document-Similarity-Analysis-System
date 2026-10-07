# Plagiarism Detection & Document Similarity System

Local Java/Spring Boot application for comparing PDF/DOCX documents.

## Requirements
- JDK 21
- Maven 3.9+
- Internet access during the first Maven build so dependencies can be downloaded

## Run
From the project folder:

```bash
mvn spring-boot:run
```

Open:

http://localhost:8080

## Workflow
1. Upload two or more PDF/DOCX documents.
2. Select the document to investigate.
3. Click Analyze Selected Document.
4. The target is compared with every other document.
5. Results use TF-IDF/Cosine for document-level similarity, Rabin-Karp for exact phrase windows, and LCS for partial similarity.
6. Download the generated PDF report.

## Folders
- `uploads/` contains uploaded source documents.
- `reports/` contains generated PDF reports.

## Notes
The overall score is an implementation heuristic for this student project:
70% cosine + 20% exact-match percentage + 10% partial-match percentage.
It is not a forensic or institutional plagiarism verdict.

The LCS implementation is bounded to 1800 tokens per document to prevent large quadratic memory use.
