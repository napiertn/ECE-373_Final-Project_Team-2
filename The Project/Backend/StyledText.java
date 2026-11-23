package editor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores editable text as a list of formatted TextRun pieces.
 * Handles inserting, deleting, styling across boundaries, and slicing text.
 */
public class StyledText {
    private List<TextRun> runs;

    /**
     * Creates an empty StyledText.
     */
    public StyledText() {
        this.runs = new ArrayList<>();
    }

    /**
     * Creates a StyledText from plain text using the default style.
     *
     * @param plainText the starting text
     */
    public StyledText(String plainText) {
        this(plainText, TextStyle.DEFAULT);
    }

    /**
     * Creates a StyledText from plain text with a single starting style.
     *
     * @param plainText the starting text
     * @param style     the style to apply to the text
     */
    public StyledText(String plainText, TextStyle style) {
        if (plainText == null) {
            throw new IllegalArgumentException("Plain text cannot be null.");
        }
        if (style == null) {
            throw new IllegalArgumentException("Style cannot be null.");
        }
        this.runs = new ArrayList<>();
        if (!plainText.isEmpty()) {
            this.runs.add(new TextRun(plainText, style));
        }
    }

    /**
     * Creates a StyledText from an existing list of runs.
     *
     * @param initialRuns the runs to copy into this text
     */
    public StyledText(List<TextRun> initialRuns) {
        if (initialRuns == null) {
            throw new IllegalArgumentException("Initial runs cannot be null.");
        }
        this.runs = mergeAdjacentRuns(initialRuns);
    }

    /**
     * Inserts another StyledText at the given character position.
     *
     * @param offset the index where the text should be inserted
     * @param text   the styled text to insert
     */
    public void insert(int offset, StyledText text) {
        if (text == null) {
            throw new IllegalArgumentException("Inserted text cannot be null.");
        }
        int totalLength = length();
        if (offset < 0 || offset > totalLength) {
            throw new IllegalArgumentException("Offset out of bounds: " + offset);
        }
        if (text.length() == 0) {
            return;
        }

        // Split into [before offset] + [inserted text] + [after offset]
        List<TextRun> combined = new ArrayList<>();
        combined.addAll(sliceRuns(0, offset));
        combined.addAll(text.runs);
        combined.addAll(sliceRuns(offset, totalLength));
        this.runs = mergeAdjacentRuns(combined);
    }

    /**
     * Deletes the characters from start (inclusive) to end (exclusive)
     * and returns what was deleted so Undo can put it back.
     *
     * @param start inclusive start index
     * @param end   exclusive end index
     * @return the deleted portion as a new StyledText
     */
    public StyledText delete(int start, int end) {
        validateRange(start, end);
        if (start == end) {
            return new StyledText();
        }

        int totalLength = length();
        List<TextRun> removed = sliceRuns(start, end);

        List<TextRun> remaining = new ArrayList<>();
        remaining.addAll(sliceRuns(0, start));
        remaining.addAll(sliceRuns(end, totalLength));
        this.runs = mergeAdjacentRuns(remaining);

        return new StyledText(removed);
    }

    /**
     * Applies a StylePatch to the range [start, end) without changing the letters.
     *
     * @param start inclusive start index
     * @param end   exclusive end index
     * @param patch the formatting changes to apply
     */
    public void applyStyle(int start, int end, StylePatch patch) {
        if (patch == null) {
            throw new IllegalArgumentException("StylePatch cannot be null.");
        }
        validateRange(start, end);
        if (start == end) {
            return;
        }

        int totalLength = length();

        // Keep text before start as-is
        List<TextRun> updated = new ArrayList<>(sliceRuns(0, start));

        // Update styles only for the middle piece
        for (TextRun middleRun : sliceRuns(start, end)) {
            TextStyle newStyle = patch.applyTo(middleRun.style());
            updated.add(new TextRun(middleRun.characters(), newStyle));
        }

        // Keep text after end as-is
        updated.addAll(sliceRuns(end, totalLength));
        this.runs = mergeAdjacentRuns(updated);
    }

    /**
     * Returns all text combined as a plain String without formatting.
     *
     * @return the plain text characters
     */
    public String plainCharacters() {
        StringBuilder sb = new StringBuilder();
        for (TextRun run : runs) {
            sb.append(run.characters());
        }
        return sb.toString();
    }

    /**
     * Returns a copy of the styled text between start (inclusive) and end (exclusive).
     *
     * @param start inclusive start index
     * @param end   exclusive end index
     * @return a new StyledText for the requested range
     */
    public StyledText subText(int start, int end) {
        validateRange(start, end);
        return new StyledText(sliceRuns(start, end));
    }

    /**
     * Returns the total number of characters in this text.
     *
     * @return total character count
     */
    public int length() {
        int total = 0;
        for (TextRun run : runs) {
            total += run.length();
        }
        return total;
    }

    /**
     * Returns a copy of the list of runs so callers cannot modify the internal list directly.
     *
     * @return list of current TextRun segments
     */
    public List<TextRun> getRuns() {
        return new ArrayList<>(runs);
    }

    /**
     * Replaces all runs with a saved list (used when undoing formatting).
     *
     * @param priorRuns the previously saved runs to restore
     */
    public void restoreRuns(List<TextRun> priorRuns) {
        if (priorRuns == null) {
            throw new IllegalArgumentException("Prior runs cannot be null.");
        }
        this.runs = mergeAdjacentRuns(priorRuns);
    }

    /**
     * Creates an independent copy of this StyledText for snapshots.
     *
     * @return a copied StyledText
     */
    public StyledText copy() {
        return new StyledText(runs);
    }

    private void validateRange(int start, int end) {
        int totalLength = length();
        if (start < 0 || end > totalLength || start > end) {
            throw new IllegalArgumentException(
                    "Invalid range [" + start + ", " + end + ") for length " + totalLength
            );
        }
    }

    /**
     * Pulls out just the piece of the runs that falls inside [start, end).
     */
    private List<TextRun> sliceRuns(int start, int end) {
        List<TextRun> result = new ArrayList<>();
        if (start >= end) {
            return result;
        }

        int cursor = 0;
        for (TextRun run : runs) {
            int runStart = cursor;
            int runEnd = cursor + run.length();

            // Check if this run overlaps with [start, end)
            if (runEnd > start && runStart < end) {
                int cutStart = Math.max(start, runStart) - runStart;
                int cutEnd = Math.min(end, runEnd) - runStart;
                String part = run.characters().substring(cutStart, cutEnd);
                if (!part.isEmpty()) {
                    result.add(new TextRun(part, run.style()));
                }
            }

            cursor = runEnd;
            if (cursor >= end) {
                break;
            }
        }
        return result;
    }

    /**
     * Combines back-to-back runs that have the exact same TextStyle
     * EXAMPLE: ("Hel" bold + "lo" bold -> "Hello" bold).
     */
    private List<TextRun> mergeAdjacentRuns(List<TextRun> source) {
        List<TextRun> merged = new ArrayList<>();
        for (TextRun run : source) {
            if (run == null) {
                throw new IllegalArgumentException("Run cannot be null.");
            }
            if (run.characters().isEmpty()) {
                continue;
            }

            if (!merged.isEmpty()) {
                TextRun previous = merged.get(merged.size() - 1);
                if (previous.style().equals(run.style())) {
                    TextRun combined = new TextRun(
                            previous.characters() + run.characters(),
                            previous.style()
                    );
                    merged.set(merged.size() - 1, combined);
                    continue;
                }
            }
            merged.add(run);
        }
        return merged;
    }
}
