package com.plagiarism.detector.model;

import java.util.List;

public record AnalysisResponse(
        String targetDocument,
        List<SimilarityResult> results,
        String reportFile
) {}
