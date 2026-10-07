package com.plagiarism.detector.model;

public record PartialMatch(
        String sourceDocument,
        String text,
        int length
) {}
