package model;

/** Immutable partial style update; null means leave a property unchanged. */
public record StylePatch(Boolean bold, Boolean italic, String fontName, Integer fontSize, String color) {
    /** Applies only the properties explicitly supplied by this patch. */
    public TextStyle applyTo(TextStyle old) {
        if (old == null) throw new IllegalArgumentException("Style is required");
        return new TextStyle(bold == null ? old.bold() : bold,
                italic == null ? old.italic() : italic,
                fontName == null ? old.fontName() : fontName,
                fontSize == null ? old.fontSize() : fontSize,
                color == null ? old.color() : color);
    }
    /** Convenience constructor for bold changes. */
    public static StylePatch bold(boolean value) { return new StylePatch(value, null, null, null, null); }
    /** Convenience constructor for italic changes. */
    public static StylePatch italic(boolean value) { return new StylePatch(null, value, null, null, null); }
}
