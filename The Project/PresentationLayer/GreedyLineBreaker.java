package presentation;

import java.util.ArrayList;
import java.util.List;


//Greedy Puts ass many words as possible on a line, words longer than width are split

public final class GreedyLineBreaker implements LineBreaker {
    @Override
    public List<String> wrap(String text, int width) {
        if (text == null) throw new IllegalArgumentException("Text is required");
        if (width < 1) throw new IllegalArgumentException("Width must be at least 1, you have " + width);

        List<String> words = new ArrayList<>();
        String trimmed = text.trim();
        if (!trimmed.isEmpty()) {
            for (String word : trimmed.split("\\s+")) {   // "\\s+" splits at string into an array of substrings at whitespaces
                for (int i = 0; i < word.length(); i += width)
                    words.add(word.substring(i, Math.min(word.length(), i + width)));
            }
        }

        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() == 0) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= width) {
                line.append(' ').append(word);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    @Override
    public String name() { return "greedy"; }
}