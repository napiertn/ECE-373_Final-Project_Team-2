package edit;

import model.*;

/** Splits a paragraph at an offset and inserts a new adjacent paragraph. */
public final class SplitParagraphCommand extends SnapshotEditCommand {
    private final String paragraphId, newId;
    private final int offset;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a split operation with a stable ID for the resulting paragraph. */
    public SplitParagraphCommand(String paragraphId, int offset, String newId) {
        this.paragraphId = paragraphId;
        this.offset = offset;
        this.newId = newId;
    }
    /** Checks paragraph, position, parent, and unused new ID. */
    @Override public void validate(Document doc) {
        validator.checkLocation(new TextLocation(paragraphId, TextField.BODY, offset), doc);
        if (doc.findParentSection(paragraphId) == null) throw new IllegalArgumentException("Paragraph has no containing section");
        if (newId == null || newId.isBlank() || doc.findNode(newId) != null)
            throw new IllegalArgumentException("New paragraph ID must be unique");
    }
    /** Splits while preserving existing formatting in both halves. */
    @Override protected void apply(Document doc) {
        Paragraph paragraph = (Paragraph) doc.findNode(paragraphId);
        Section parent = doc.findParentSection(paragraphId);
        StyledText body = paragraph.getBody();
        StyledText tail = body.delete(offset, body.length());
        int index = parent.getChildren().indexOf(paragraph);
        doc.addChild(parent.getId(), index + 1, new Paragraph(newId, tail));
    }
}
