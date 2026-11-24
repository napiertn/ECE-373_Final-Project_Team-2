package edit;

import model.*;

/** Adds a root section, subsection, or paragraph to the hierarchy. */
public final class AddNodeCommand extends SnapshotEditCommand {
    private final String parentId;
    private final int index;
    private final DocumentNode node;

    /** A null parentId indicates a new root section. */
    public AddNodeCommand(String parentId, int index, DocumentNode node) {
        this.parentId = parentId;
        this.index = index;
        if (node == null) throw new IllegalArgumentException("Node required");
        this.node = node.copy();
    }
    /** Validates the parent, insertion index and node IDs. */
    @Override public void validate(Document doc) {
        Document snapshotOfNode = new Document(); // Validate without touching the actual document.
        snapshotOfNode.restore(doc.createSnapshot());
        insertInto(snapshotOfNode);
    }
    /** Adds an independent copy of the provided node. */
    @Override protected void apply(Document doc) { insertInto(doc); }

    private void insertInto(Document doc) {
        if (parentId == null) {
            if (!(node instanceof Section section)) throw new IllegalArgumentException("Root node must be a section");
            doc.addRootSection(index, section);
        } else doc.addChild(parentId, index, node);
    }
}
