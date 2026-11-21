package edit;

import model.*;

/** Deletes a half-open character range. */
public final class DeleteTextCommand extends SnapshotEditCommand {
    private final TextSelection selection;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a text deletion. */
    public DeleteTextCommand(TextSelection selection) { this.selection = selection; }
    /** Validates the selected range. */
    @Override public void validate(Document doc) { validator.checkSelection(selection, doc); }
    /** Removes the selected range. */
    @Override protected void apply(Document doc) {
        validator.resolve(selection.nodeId(), selection.field(), doc).delete(selection.start(), selection.end());
    }
}
