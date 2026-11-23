package model;

/** One immutable block of characters sharing a style. */
public record TextRun(String characters, TextStyle style) {
    public TextRun {
        if (characters == null || style == null) throw new IllegalArgumentException("Run contents and style are required");
    }
    /** Number of characters in this run. */
    public int length() { return characters.length(); }
}
