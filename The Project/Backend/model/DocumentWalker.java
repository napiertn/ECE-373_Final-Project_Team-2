package model;

import java.util.ArrayList;
import java.util.List;

/** Depth-first traversal that has no fixed nesting limit. */
public final class DocumentWalker {
    private DocumentWalker() { }

    /** Lists all sections and paragraphs in document order. */
    public static List<DocumentNode> depthFirst(Document document) {
        List<DocumentNode> results = new ArrayList<>();
        for (Section root : document.getSections()) visit(root, results);
        return results;
    }

    /** Lists a section and all of its descendants. */
    public static List<DocumentNode> depthFirst(Section section) {
        List<DocumentNode> results = new ArrayList<>();
        visit(section, results);
        return results;
    }

    private static void visit(DocumentNode node, List<DocumentNode> result) {
        result.add(node);
        if (node instanceof Section section)
            for (DocumentNode child : section.getChildren()) visit(child, result);
    }
}
