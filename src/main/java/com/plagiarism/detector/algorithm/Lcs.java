package com.plagiarism.detector.algorithm;

import java.util.*;

public final class Lcs {
    public record Result(int length, List<String> sequence) {}

    private Lcs() {}

    public static Result calculate(List<String> a, List<String> b, int maxTokens) {
        List<String> x = a.size() > maxTokens ? a.subList(0, maxTokens) : a;
        List<String> y = b.size() > maxTokens ? b.subList(0, maxTokens) : b;

        int[][] dp = new int[x.size() + 1][y.size() + 1];

        for (int i = 1; i <= x.size(); i++) {
            for (int j = 1; j <= y.size(); j++) {
                dp[i][j] = x.get(i - 1).equals(y.get(j - 1))
                        ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }

        LinkedList<String> seq = new LinkedList<>();
        int i = x.size(), j = y.size();
        while (i > 0 && j > 0) {
            if (x.get(i - 1).equals(y.get(j - 1))) {
                seq.addFirst(x.get(i - 1));
                i--; j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return new Result(dp[x.size()][y.size()], new ArrayList<>(seq));
    }
}
