package edit;

import model.*;

/** Modifies appearance of selected characters, including section titles. */
public final class FormatTextCommand extends SnapshotEditCommand {
    private final TextSelection selection;
    private final StylePatch patch;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a formatting operation. */
    public FormatTextCommand(TextSelection selection, StylePatch patch) {
        this.selection = selection;
        if (patch == null) throw new IllegalArgumentException("Style patch required");
        this.patch = patch;
    }
    /** Validates selection and resulting style values. */
    @Override public void validate(Document doc) {
        validator.checkSelection(selection, doc);
        StyledText selected = validator.resolve(selection.nodeId(), selection.field(), doc)
                .subText(selection.start(), selection.end());
        for (TextRun run : selected.getRuns()) patch.applyTo(run.style());
    }
    /** Applies the patch across all run boundaries. */
    @Override protected void apply(Document doc) {
        validator.resolve(selection.nodeId(), selection.field(), doc)
                .applyStyle(selection.start(), selection.end(), patch);
    }
}
