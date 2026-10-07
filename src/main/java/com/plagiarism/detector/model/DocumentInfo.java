package com.plagiarism.detector.model;

public record DocumentInfo(
        String id,
        String fileName,
        String fileType,
        long size,
        String uploadedAt
) {}
