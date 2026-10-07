package com.plagiarism.detector.algorithm;

import java.util.Map;

public final class CosineSimilarity {
    private CosineSimilarity() {}

    public static double calculate(Map<String, Double> a, Map<String, Double> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        double dot = 0, na = 0, nb = 0;

        for (double x : a.values()) na += x * x;
        for (double y : b.values()) nb += y * y;

        for (Map.Entry<String, Double> e : a.entrySet()) {
            dot += e.getValue() * b.getOrDefault(e.getKey(), 0.0);
        }

        if (na == 0 || nb == 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, dot / (Math.sqrt(na) * Math.sqrt(nb))));
    }
}
