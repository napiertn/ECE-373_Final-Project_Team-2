package presentation;

import model.Document;

/** Strategy interface for independent text rendering implementations. */
public interface DocumentRenderer {
    /** Renders the document at the given width with the supplied line-breaking strategy. */
    String render(Document doc, int width, LineBreaker breaker);
    /** Provides a stable command-line name for this renderer. */
    String name();
}
