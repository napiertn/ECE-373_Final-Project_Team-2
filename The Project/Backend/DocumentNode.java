package editor.model;

/**
 * Abstract component in the composite document hierarchy.
 * Subclassed by {@link Section} (composite) and {@link Paragraph} (leaf).
 */
public abstract class DocumentNode{
    private final String id;

    /**
     * Initializes a document node with a unique nonblank ID
     * @param id unique node identifier
     */
    protected DocumentNode(String id){
        if (id == null || id.isBlank()){
            throw new IllegalArgumentException("Node ID must not be blank.");
        }
        this.id = id;
    }

    /**
     * Returns the nodes unique identifier
     * @return node ID
     */
    public String getId(){
        return id;
    }

    /**
     * Creates a deep copy of this node for snapshots w/ any nested children
     * @return deep copy of this node
     */
    public abstract DocumentNode copy();
}
