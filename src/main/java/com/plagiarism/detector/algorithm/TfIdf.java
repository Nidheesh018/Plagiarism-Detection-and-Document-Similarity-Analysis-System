package com.plagiarism.detector.algorithm;

import java.util.*;

public final class TfIdf {
    private TfIdf() {}

    public static List<Map<String, Double>> vectors(List<List<String>> documents) {
        int n = documents.size();
        Map<String, Integer> df = new HashMap<>();

        for (List<String> doc : documents) {
            Set<String> unique = new HashSet<>(doc);
            for (String term : unique) df.merge(term, 1, Integer::sum);
        }

        List<Map<String, Double>> result = new ArrayList<>();

        for (List<String> doc : documents) {
            Map<String, Integer> counts = new HashMap<>();
            for (String term : doc) counts.merge(term, 1, Integer::sum);

            Map<String, Double> vector = new HashMap<>();
            int length = Math.max(doc.size(), 1);

            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                double tf = (double) e.getValue() / length;
                double idf = Math.log((n + 1.0) / (df.get(e.getKey()) + 1.0)) + 1.0;
                vector.put(e.getKey(), tf * idf);
            }
            result.add(vector);
        }
        return result;
    }
}
