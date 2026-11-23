package model;

/** Leaf node containing a formatted paragraph body. */
public final class Paragraph extends DocumentNode {
    private final StyledText body;

    /** Creates a paragraph. */
    public Paragraph(String id, StyledText body) {
        super(id);
        if (body == null) throw new IllegalArgumentException("Paragraph body is required");
        this.body = body.copy();
    }

    /** Returns the mutable formatted paragraph text. */
    public StyledText getBody() { return body; }

    /** Returns a deep copy. */
    @Override public Paragraph copy() { return new Paragraph(getId(), body); }
}
