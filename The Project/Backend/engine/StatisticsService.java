package engine;

import model.*;

/** Observer pattern: independently maintains statistics after document changes. */
public final class StatisticsService implements DocumentChangeListener {
    private DocumentStatistics current = new DocumentStatistics(0, 0, 0);

    /** Computes initial totals and registers for notifications. */
    public void initialize(Document doc) {
        if (doc == null) throw new IllegalArgumentException("Document required");
        onDocumentChanged(doc);
        doc.subscribe(this);
    }

    /** Recomputes totals from the current document tree. */
    @Override public void onDocumentChanged(Document doc) {
        int words = 0, characters = 0, paragraphs = 0;
        for (DocumentNode node : DocumentWalker.depthFirst(doc)) {
            String content;
            if (node instanceof Section section) content = section.getTitle().plainCharacters();
            else {
                paragraphs++;
                content = ((Paragraph) node).getBody().plainCharacters();
            }
            characters += content.length();
            String trimmed = content.trim();
            if (!trimmed.isEmpty()) words += trimmed.split("\s+").length;
        }
        current = new DocumentStatistics(words, characters, paragraphs);
    }

    /** Returns the latest observed totals. */
    public DocumentStatistics getStatistics() { return current; }
}
