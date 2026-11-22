package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One mutable document, composed of root sections and an observer registry. */
public final class Document {
    private final List<Section> sections = new ArrayList<>();
    private final List<DocumentChangeListener> listeners = new ArrayList<>();

    /** Returns root sections in document order. */
    public List<Section> getSections() { return Collections.unmodifiableList(sections); }

    /** Finds any node at any nesting level, or null when absent. */
    public DocumentNode findNode(String id) {
        if (id == null) return null;
        for (DocumentNode node : DocumentWalker.depthFirst(this))
            if (node.getId().equals(id)) return node;
        return null;
    }

    /** Adds a root section, validating all subtree identifiers. */
    public void addRootSection(int index, Section section) {
        if (section == null) throw new IllegalArgumentException("Root section required");
        if (index < 0 || index > sections.size()) throw new IllegalArgumentException("Invalid root index " + index);
        requireUniqueSubtree(section);
        sections.add(index, section.copy());
    }

    /** Adds a node beneath an existing section. */
    public void addChild(String parentId, int index, DocumentNode child) {
        DocumentNode parent = findNode(parentId);
        if (!(parent instanceof Section section)) throw new IllegalArgumentException("Parent section not found: " + parentId);
        requireUniqueSubtree(child);
        section.addChild(index, child);
    }

    /** Returns the immediate containing section for a node, or null for roots. */
    public Section findParentSection(String childId) {
        for (DocumentNode node : DocumentWalker.depthFirst(this)) {
            if (node instanceof Section section)
                for (DocumentNode child : section.getChildren())
                    if (child.getId().equals(childId)) return section;
        }
        return null;
    }

    /** Registers a document-change observer. */
    public void subscribe(DocumentChangeListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener required");
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /** Unregisters a document-change observer. */
    public void unsubscribe(DocumentChangeListener listener) { listeners.remove(listener); }

    /** Notifies observers after successful changes. */
    public void publishChanged() {
        for (DocumentChangeListener listener : List.copyOf(listeners)) listener.onDocumentChanged(this);
    }

    /** Captures the full document state. */
    public DocumentSnapshot createSnapshot() { return new DocumentSnapshot(sections); }

    /** Restores a previously captured state (caller publishes one change notification). */
    public void restore(DocumentSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("Snapshot required");
        sections.clear();
        sections.addAll(snapshot.copySections());
    }

    private void requireUniqueSubtree(DocumentNode candidate) {
        if (candidate == null) throw new IllegalArgumentException("Node required");
        List<String> ids = new ArrayList<>();
        collectIds(candidate, ids);
        for (String id : ids) {
            if (findNode(id) != null) throw new IllegalArgumentException("Duplicate node ID: " + id);
        }
        if (ids.size() != ids.stream().distinct().count()) throw new IllegalArgumentException("Duplicate ID within inserted subtree");
    }
    private static void collectIds(DocumentNode node, List<String> output) {
        output.add(node.getId());
        if (node instanceof Section section)
            for (DocumentNode child : section.getChildren()) collectIds(child, output);
    }
}
