package edit;

import model.*;

/** Restores a named checkpoint as one reversible editing operation. */
public final class RestoreCheckpointCommand extends SnapshotEditCommand {
    private final DocumentSnapshot target;

    /** Captures the checkpoint that will be restored. */
    public RestoreCheckpointCommand(DocumentSnapshot target) {
        if (target == null) throw new IllegalArgumentException("Checkpoint required");
        this.target = target;
    }
    /** Checkpoint object is validated by the constructor. */
    @Override public void validate(Document doc) { if (doc == null) throw new IllegalArgumentException("Document required"); }
    /** Applies the checkpoint to the active document. */
    @Override protected void apply(Document doc) { doc.restore(target); }
}
