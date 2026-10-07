package com.plagiarism.detector.algorithm;

import java.util.*;

public final class RabinKarp {
    private static final long MOD = 1_000_000_007L;
    private static final long BASE = 911382323L;

    private RabinKarp() {}

    public static List<int[]> findMatches(List<String> target, List<String> source, int window) {
        List<int[]> matches = new ArrayList<>();
        if (window <= 0 || target.size() < window || source.size() < window) return matches;

        Map<String, List<Integer>> sourceWindows = new HashMap<>();
        for (int i = 0; i <= source.size() - window; i++) {
            String key = key(source, i, window);
            sourceWindows.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        Set<String> seen = new HashSet<>();
        for (int i = 0; i <= target.size() - window; i++) {
            String key = key(target, i, window);
            if (sourceWindows.containsKey(key) && seen.add(key)) {
                for (int j : sourceWindows.get(key)) {
                    if (same(target, i, source, j, window)) {
                        matches.add(new int[]{i, i + window, j, j + window});
                    }
                }
            }
        }
        return matches;
    }

    private static String key(List<String> tokens, int start, int window) {
        long h = 0;
        for (int i = start; i < start + window; i++) {
            h = (h * BASE + tokens.get(i).hashCode()) % MOD;
        }
        return Long.toString(h) + ":" + String.join(" ", tokens.subList(start, start + window));
    }

    private static boolean same(List<String> a, int ai, List<String> b, int bi, int n) {
        for (int k = 0; k < n; k++) {
            if (!a.get(ai + k).equals(b.get(bi + k))) return false;
        }
        return true;
    }
}
