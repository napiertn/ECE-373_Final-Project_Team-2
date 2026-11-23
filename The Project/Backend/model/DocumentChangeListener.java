package model;

/** Observer of completed document changes. */
@FunctionalInterface
public interface DocumentChangeListener {
    /** Called after the document changes. */
    void onDocumentChanged(Document doc);
}
