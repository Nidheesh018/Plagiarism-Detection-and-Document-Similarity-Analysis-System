package com.plagiarism.detector.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class PreprocessingService {
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{Nd}\\s]");
    private static final Set<String> STOP_WORDS = Set.of(
            "a","an","the","and","or","but","if","then","than","this","that","these","those",
            "is","are","was","were","be","been","being","to","of","in","on","for","with",
            "by","as","at","from","into","about","it","its","we","our","you","your","they",
            "their","he","she","his","her","i","me","my","do","does","did","has","have","had"
    );

    public String normalize(String text) {
        String s = text == null ? "" : text.toLowerCase(Locale.ROOT);
        s = NON_WORD.matcher(s).replaceAll(" ");
        return s.replaceAll("\\s+", " ").trim();
    }

    public List<String> tokenize(String text, boolean removeStopWords) {
        String normalized = normalize(text);
        if (normalized.isBlank()) return new ArrayList<>();

        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split(" ")) {
            if (!token.isBlank() && (!removeStopWords || !STOP_WORDS.contains(token))) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
