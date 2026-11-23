package editor.model;
import java.util.Objects;

/**
 * Contiguous run of characters sharing a single {@link TextStyle}.
 *
 * @param characters non-null sequence of characters
 * @param style      non-null formatting style applied to those characters
 */

public record TextRun(String characters, TextStyle style){
    /**
     * Validates non-null components.
     */
    public TextRun{
        Objects.requireNonNull(characters, "Run characters must not be null.");
        Objects.requireNonNull(style, "Run style must not be null.");
    }

    /**
     * Returns the number of characters in this run.
     * @return character count
     */
    public int length(){
        return characters.length();
    }
}
