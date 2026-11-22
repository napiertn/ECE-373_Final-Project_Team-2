package presentation;

import editor.model.Document;

// Strategy for turning a Document into display text. Must not modify the document.
public interface DocumentRenderer {
    /*
      @param doc        the document to render (read-only)
      @param width      maximum characters per output line, must be > 0
      @param breaker   the line-breaking strategy to wrap text with

      @return the rendered document
     */
    String render(Document doc, int width, LineBreaker breaker);

    /* Name used by the "view" command, e.g. "plain" or "marked". */
    String name();
}