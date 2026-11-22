package edit;

import model.*;

/** Combines adjacent paragraphs, retaining formatted text. */
public final class MergeParagraphsCommand extends SnapshotEditCommand {
    private final String firstId, secondId;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a merge operation for two adjacent sibling paragraphs. */
    public MergeParagraphsCommand(String firstId, String secondId) {
        this.firstId = firstId;
        this.secondId = secondId;
    }
    /** Validates that paragraphs are directly adjacent. */
    @Override public void validate(Document doc) { validator.checkAdjacentParagraphs(firstId, secondId, doc); }
    /** Appends second paragraph contents and removes its node. */
    @Override protected void apply(Document doc) {
        Paragraph first = (Paragraph) doc.findNode(firstId);
        Paragraph second = (Paragraph) doc.findNode(secondId);
        first.getBody().insert(first.getBody().length(), second.getBody());
        doc.findParentSection(firstId).removeChild(secondId);
    }
}
