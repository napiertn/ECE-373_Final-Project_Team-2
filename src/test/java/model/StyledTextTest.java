package model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Verifies half-open ranges and style-preserving text operations without the CLI. */
class StyledTextTest {
    @Test void editingPreservesStyledRunsAndSupportsBoundaries() {
        StyledText t = new StyledText("abcdef");
        t.applyStyle(1, 4, StylePatch.bold(true));
        t.applyStyle(3, 6, StylePatch.italic(true));
        assertEquals("abcdef", t.plainCharacters());
        assertEquals(4, t.getRuns().size());
        StyledText segment = t.subText(2, 5);
        t.delete(2, 5);
        assertEquals("abf", t.plainCharacters());
        t.insert(2, segment);
        assertEquals("abcdef", t.plainCharacters());
    }
    @Test void copyCannotAccidentallyShareMutableText() {
        StyledText original = new StyledText("alpha");
        StyledText copy = original.copy();
        copy.insert(0, new StyledText("X"));
        assertEquals("alpha", original.plainCharacters());
        assertEquals("Xalpha", copy.plainCharacters());
    }
    @Test void invalidRangesCannotDestroyText() {
        StyledText original = new StyledText("hello");
        assertThrows(IllegalArgumentException.class, () -> original.delete(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> original.insert(6, new StyledText("Y")));
        assertThrows(IllegalArgumentException.class, () -> original.subText(3, 2));
        assertEquals("hello", original.plainCharacters());
    }
    @Test void runMergingKeepsFormattingStorageSmall() {
        StyledText text = new StyledText("a");
        for (int i = 0; i < 100; i++) text.append("b", TextStyle.normal());
        assertEquals(1, text.getRuns().size());
    }
}
