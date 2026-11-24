package presentation;

import java.util.List;

/** Strategy for wrapping a block of text into lines no longer than width. */
public interface LineBreaker {
    /**
      @param text  text to wrap (may be empty)
      @param width maximum line length, must be > 0

      @return the wrapped lines; never null; empty text gives an empty list
     */
    List<String> wrap(String text, int width);

    /** Name used by the "break" command, e.g. "greedy" or "balanced". */
    String name();
}