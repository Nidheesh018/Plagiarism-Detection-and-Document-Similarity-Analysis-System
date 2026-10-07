package com.plagiarism.detector.model;

public record ExactMatch(
        String sourceDocument,
        String text,
        int sourceTokenStart,
        int sourceTokenEnd,
        int targetTokenStart,
        int targetTokenEnd
) {}
