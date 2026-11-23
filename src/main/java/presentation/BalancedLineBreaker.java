package presentation;

import java.util.ArrayList;
import java.util.List;


/** Balanced line breaking that minimizes the sum of nonfinal squared line slack. */
public final class BalancedLineBreaker implements LineBreaker {
    /** Constructs the stateless dynamic-programming breaking strategy. */
    public BalancedLineBreaker() { }

    @Override
    public List<String> wrap(String text, int width) {

        //Checks for Illegal Arguments
        if (text == null) throw new IllegalArgumentException("Text is required");
        if (width < 1) throw new IllegalArgumentException("Width must be at least 1, got " + width);

        // Creation of splits on whitespace, like in greedy line breaker
        List<String> words = new ArrayList<>();
        String trimmed = text.trim();
        if (!trimmed.isEmpty()) {
            for (String word : trimmed.split("\\s+")) {
                for (int i = 0; i < word.length(); i += width)
                    words.add(word.substring(i, Math.min(word.length(), i + width)));
            }
        }


        int n = words.size();
        List<String> lines = new ArrayList<>();
        if (n == 0) return lines;

        long[] best = new long[n + 1];   // best[i] = minimum cost of laying out words[i..n)
        int[] next = new int[n + 1];     // next[i] = index where the line starting at i ends
        for (int i = n - 1; i >= 0; i--) {
            best[i] = Long.MAX_VALUE;
            int len = -1;                // length of words[i..j) joined by single spaces
            for (int j = i + 1; j <= n; j++) {
                len += 1 + words.get(j - 1).length();
                if (len > width) break;
                long cost = (j == n) ? 0 : (long) (width - len) * (width - len) + best[j];
                if (cost < best[i]) { best[i] = cost; next[i] = j; }
            }
        }
        for (int i = 0; i < n; i = next[i])
            lines.add(String.join(" ", words.subList(i, next[i])));
        return lines;
    }

    @Override
    public String name() { return "balanced"; }
}



