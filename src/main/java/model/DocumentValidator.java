package model;

import java.util.List;

/** Checks document positions and relationships before editing occurs. */
public final class DocumentValidator {
    /** Resolves BODY on paragraphs and SECTION_TITLE on sections. */
    public StyledText resolve(String id, TextField field, Document doc) {
        if (field == null || doc == null) throw new IllegalArgumentException("Field and document required");
        DocumentNode node = doc.findNode(id);
        if (field == TextField.BODY && node instanceof Paragraph paragraph) return paragraph.getBody();
        if (field == TextField.SECTION_TITLE && node instanceof Section section) return section.getTitle();
        throw new IllegalArgumentException("Invalid node/field combination: " + id + "/" + field);
    }

    /** Checks a cursor position in inclusive range [0,length]. */
    public void checkLocation(TextLocation location, Document doc) {
        if (location == null) throw new IllegalArgumentException("Location required");
        resolve(location.nodeId(), location.field(), doc).checkRange(location.offset(), location.offset());
    }

    /** Checks an existing half-open text selection. */
    public void checkSelection(TextSelection selection, Document doc) {
        if (selection == null) throw new IllegalArgumentException("Selection required");
        resolve(selection.nodeId(), selection.field(), doc).checkRange(selection.start(), selection.end());
    }

    /** Confirms two paragraphs are directly adjacent siblings in the given order. */
    public void checkAdjacentParagraphs(String firstId, String secondId, Document doc) {
        if (!(doc.findNode(firstId) instanceof Paragraph) || !(doc.findNode(secondId) instanceof Paragraph))
            throw new IllegalArgumentException("Both nodes must be paragraphs");
        Section parent = doc.findParentSection(firstId);
        if (parent == null || parent != doc.findParentSection(secondId))
            throw new IllegalArgumentException("Paragraphs must share a section");
        List<DocumentNode> children = parent.getChildren();
        for (int i = 0; i + 1 < children.size(); i++) {
            if (children.get(i).getId().equals(firstId) && children.get(i + 1).getId().equals(secondId)) return;
        }
        throw new IllegalArgumentException("Paragraphs are not adjacent");
    }
}
