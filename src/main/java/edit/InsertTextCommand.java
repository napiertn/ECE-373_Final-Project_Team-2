package edit;

import model.*;

/** Inserts styled characters into a paragraph or section title. */
public final class InsertTextCommand extends SnapshotEditCommand {
    private final TextLocation at;
    private final StyledText inserted;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a text insertion. */
    public InsertTextCommand(TextLocation at, StyledText inserted) {
        this.at = at;
        if (inserted == null) throw new IllegalArgumentException("Inserted text required");
        this.inserted = inserted.copy();
    }
    /** Validates the target offset. */
    @Override public void validate(Document doc) { validator.checkLocation(at, doc); }
    /** Inserts a copy of styled text. */
    @Override protected void apply(Document doc) {
        validator.resolve(at.nodeId(), at.field(), doc).insert(at.offset(), inserted);
    }
}
