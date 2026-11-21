package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Recursive section that can contain paragraphs and nested sections. */
public final class Section extends DocumentNode {
    private final StyledText title;
    private final List<DocumentNode> children = new ArrayList<>();

    /** Creates a named section. */
    public Section(String id, StyledText title) {
        super(id);
        if (title == null) throw new IllegalArgumentException("Section title is required");
        this.title = title.copy();
    }

    /** Returns the section title. */
    public StyledText getTitle() { return title; }

    /** Returns children without giving callers list-mutation access. */
    public List<DocumentNode> getChildren() { return Collections.unmodifiableList(children); }

    /** Inserts a child at a specified index. */
    public void addChild(int index, DocumentNode node) {
        if (node == null) throw new IllegalArgumentException("Child required");
        if (index < 0 || index > children.size()) throw new IllegalArgumentException("Invalid child index " + index);
        children.add(index, node.copy());
    }

    /** Removes a direct child by identifier. */
    public DocumentNode removeChild(String id) {
        for (int i = 0; i < children.size(); i++)
            if (children.get(i).getId().equals(id)) return children.remove(i);
        throw new IllegalArgumentException("Not a direct child: " + id);
    }

    /** Returns a deep copy of this section and all its descendants. */
    @Override public Section copy() {
        Section result = new Section(getId(), title);
        for (DocumentNode child : children) result.children.add(child.copy());
        return result;
    }
}
