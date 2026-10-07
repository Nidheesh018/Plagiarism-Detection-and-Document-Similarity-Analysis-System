package com.plagiarism.detector.model;

import java.util.List;

public record SimilarityResult(
        String sourceDocument,
        double cosineSimilarity,
        double exactMatchPercentage,
        double partialMatchPercentage,
        double uniqueContentPercentage,
        List<ExactMatch> exactMatches,
        List<PartialMatch> partialMatches
) {
    public double overallSimilarity() {
        return Math.min(100.0,
                0.70 * cosineSimilarity +
                0.20 * exactMatchPercentage +
                0.10 * partialMatchPercentage);
    }

    public String classification() {
        double score = overallSimilarity();
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}
