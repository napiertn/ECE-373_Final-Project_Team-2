package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mutable text composed of styled runs; neighboring identical styles are merged. */
public final class StyledText {
    private final List<TextRun> runs = new ArrayList<>();

    /** Creates empty styled text. */
    public StyledText() { }

    /** Creates plain text with the default style. */
    public StyledText(String text) { this(text, TextStyle.normal()); }

    /** Creates text in a chosen style. */
    public StyledText(String text, TextStyle style) { append(text, style); }

    /** Returns a deep copy with independent mutable run storage. */
    public StyledText copy() {
        StyledText result = new StyledText();
        for (TextRun run : runs) result.append(run.characters(), run.style());
        return result;
    }

    /** Returns the total number of characters. */
    public int length() { return runs.stream().mapToInt(TextRun::length).sum(); }

    /** Returns all characters without markup. */
    public String plainCharacters() {
        StringBuilder out = new StringBuilder(length());
        for (TextRun run : runs) out.append(run.characters());
        return out.toString();
    }

    /** Exposes an immutable view of styled runs. */
    public List<TextRun> getRuns() { return Collections.unmodifiableList(runs); }

    /** Adds text to the end, merging compatible runs. */
    public void append(String text, TextStyle style) {
        if (text == null || style == null) throw new IllegalArgumentException("Text and style are required");
        if (text.isEmpty()) return;
        if (!runs.isEmpty() && runs.get(runs.size() - 1).style().equals(style)) {
            TextRun last = runs.remove(runs.size() - 1);
            runs.add(new TextRun(last.characters() + text, style));
        } else runs.add(new TextRun(text, style));
    }

    /** Returns a styled substring using half-open coordinates [start,end). */
    public StyledText subText(int start, int end) {
        checkRange(start, end);
        StyledText output = new StyledText();
        int cursor = 0;
        for (TextRun run : runs) {
            int from = Math.max(start, cursor), to = Math.min(end, cursor + run.length());
            if (from < to) output.append(run.characters().substring(from - cursor, to - cursor), run.style());
            cursor += run.length();
        }
        return output;
    }

    /** Inserts formatted text at an offset. */
    public void insert(int offset, StyledText newText) {
        checkRange(offset, offset);
        if (newText == null) throw new IllegalArgumentException("Inserted text is required");
        StyledText rebuilt = subText(0, offset);
        for (TextRun r : newText.copy().getRuns()) rebuilt.append(r.characters(), r.style());
        StyledText tail = subText(offset, length());
        for (TextRun r : tail.getRuns()) rebuilt.append(r.characters(), r.style());
        replaceAllRuns(rebuilt);
    }

    /** Deletes and returns a styled substring using half-open coordinates. */
    public StyledText delete(int start, int end) {
        checkRange(start, end);
        StyledText removed = subText(start, end);
        StyledText rebuilt = subText(0, start);
        for (TextRun r : subText(end, length()).getRuns()) rebuilt.append(r.characters(), r.style());
        replaceAllRuns(rebuilt);
        return removed;
    }

    /** Changes appearance but preserves the underlying characters. */
    public void applyStyle(int start, int end, StylePatch patch) {
        checkRange(start, end);
        if (patch == null) throw new IllegalArgumentException("Style patch is required");
        StyledText rebuilt = subText(0, start);
        for (TextRun r : subText(start, end).getRuns()) rebuilt.append(r.characters(), patch.applyTo(r.style()));
        for (TextRun r : subText(end, length()).getRuns()) rebuilt.append(r.characters(), r.style());
        replaceAllRuns(rebuilt);
    }

    /** Validates an offset or half-open character range. */
    public void checkRange(int start, int end) {
        if (start < 0 || end < start || end > length())
            throw new IllegalArgumentException("Invalid range [" + start + "," + end + ") for length " + length());
    }

    private void replaceAllRuns(StyledText other) {
        runs.clear();
        for (TextRun r : other.runs) append(r.characters(), r.style());
    }
}
