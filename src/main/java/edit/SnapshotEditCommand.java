package edit;

import model.Document;
import model.DocumentSnapshot;

/** Memento-backed command implementation supporting atomic undo and redo. */
public abstract class SnapshotEditCommand implements EditCommand {
    private DocumentSnapshot before;
    private DocumentSnapshot after;

    /** Checks the operation before any document mutation. */
    @Override public abstract void validate(Document doc);

    /** Applies the concrete edit after validation. */
    protected abstract void apply(Document doc);

    /** Executes the command while saving full before/after states. */
    @Override public final void execute(Document doc) {
        validate(doc);
        DocumentSnapshot baseline = doc.createSnapshot();
        try {
            apply(doc);
            before = baseline;
            after = doc.createSnapshot();
        } catch (RuntimeException | Error e) {
            doc.restore(baseline);
            throw e;
        }
    }

    /** Restores the state before this command. */
    @Override public final void undo(Document doc) {
        if (before == null) throw new IllegalStateException("Command has not executed");
        doc.restore(before);
    }

    /** Restores the exact state after this command. */
    @Override public final void redo(Document doc) {
        if (after == null) throw new IllegalStateException("Command has not executed");
        doc.restore(after);
    }
}
