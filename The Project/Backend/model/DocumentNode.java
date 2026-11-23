package model;

/** Base node of the document's nested section/paragraph hierarchy. */
public abstract class DocumentNode {
    private final String id;

    /** Creates a node with a unique stable identifier. */
    protected DocumentNode(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Node ID is required");
        this.id = id;
    }

    /** Returns the stable identifier. */
    public String getId() { return id; }

    /** Returns an independently editable copy. */
    public abstract DocumentNode copy();
}
