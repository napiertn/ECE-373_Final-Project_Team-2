package edit;

import model.*;

/** Replaces one half-open range with formatted replacement text. */
public final class ReplaceTextCommand extends SnapshotEditCommand {
    private final TextSelection selection;
    private final StyledText replacement;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a single replacement operation. */
    public ReplaceTextCommand(TextSelection selection, StyledText replacement) {
        this.selection = selection;
        if (replacement == null) throw new IllegalArgumentException("Replacement required");
        this.replacement = replacement.copy();
    }
    /** Validates the selected range. */
    @Override public void validate(Document doc) { validator.checkSelection(selection, doc); }
    /** Replaces the selected text. */
    @Override protected void apply(Document doc) {
        StyledText target = validator.resolve(selection.nodeId(), selection.field(), doc);
        target.delete(selection.start(), selection.end());
        target.insert(selection.start(), replacement);
    }
}
