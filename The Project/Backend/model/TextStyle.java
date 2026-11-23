package model;

/** Immutable appearance of a single text run. */
public record TextStyle(boolean bold, boolean italic, String fontName, int fontSize, String color) {
    public TextStyle {
        if (fontName == null || fontName.isBlank()) throw new IllegalArgumentException("Font name is required");
        if (fontSize <= 0) throw new IllegalArgumentException("Font size must be positive");
        if (color == null || color.isBlank()) throw new IllegalArgumentException("Color is required");
    }

    /** Returns the default character style. */
    public static TextStyle normal() { return new TextStyle(false, false, "Monospaced", 12, "black"); }
}
