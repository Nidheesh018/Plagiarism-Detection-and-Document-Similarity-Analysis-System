package com.plagiarism.detector.service;

import com.plagiarism.detector.algorithm.CosineSimilarity;
import com.plagiarism.detector.algorithm.Lcs;
import com.plagiarism.detector.algorithm.RabinKarp;
import com.plagiarism.detector.algorithm.TfIdf;
import com.plagiarism.detector.model.ExactMatch;
import com.plagiarism.detector.model.PartialMatch;
import com.plagiarism.detector.model.SimilarityResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimilarityService {

    private static final int EXACT_WINDOW = 8;
    private static final int LCS_LIMIT = 1800;

    public List<SimilarityResult> compare(
            String targetName,
            List<String> sourceNames,
            List<List<String>> tokenDocuments) {

        List<Map<String, Double>> vectors = TfIdf.vectors(tokenDocuments);
        List<String> target = tokenDocuments.get(0);
        List<SimilarityResult> results = new ArrayList<>();

        for (int i = 1; i < tokenDocuments.size(); i++) {
            List<String> source = tokenDocuments.get(i);

            double cosine = CosineSimilarity.calculate(vectors.get(0), vectors.get(i)) * 100.0;

            List<int[]> rawExact = RabinKarp.findMatches(target, source, EXACT_WINDOW);
            List<ExactMatch> exact = new ArrayList<>();

            Set<String> uniqueExact = new LinkedHashSet<>();
            for (int[] m : rawExact) {
                String phrase = String.join(" ", target.subList(m[0], m[1]));
                if (uniqueExact.add(phrase)) {
                    exact.add(new ExactMatch(
                            sourceNames.get(i - 1), phrase,
                            m[2], m[3], m[0], m[1]));
                }
            }

            double exactPct = target.isEmpty()
                    ? 0
                    : Math.min(100.0, (exact.size() * EXACT_WINDOW * 100.0) / target.size());

            Lcs.Result lcs = Lcs.calculate(target, source, LCS_LIMIT);
            double partialPct = target.isEmpty()
                    ? 0
                    : Math.min(100.0, (lcs.length() * 100.0) / Math.min(target.size(), LCS_LIMIT));

            List<PartialMatch> partial = new ArrayList<>();
            if (lcs.length() >= 5 && exact.isEmpty()) {
                String text = String.join(" ", lcs.sequence());
                partial.add(new PartialMatch(sourceNames.get(i - 1), text, lcs.length()));
            }

            double overall = Math.min(100.0,
                    0.70 * cosine + 0.20 * exactPct + 0.10 * partialPct);

            double unique = Math.max(0, 100.0 - overall);

            results.add(new SimilarityResult(
                    sourceNames.get(i - 1),
                    round(cosine),
                    round(exactPct),
                    round(partialPct),
                    round(unique),
                    exact,
                    partial
            ));
        }

        results.sort(Comparator.comparingDouble(SimilarityResult::overallSimilarity).reversed());
        return results;
    }

    private double round(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
