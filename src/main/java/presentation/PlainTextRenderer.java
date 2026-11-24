package presentation;

import java.util.List;
import model.*;

/** Traverses sections recursively, rendering only original document characters. */
public final class PlainTextRenderer implements DocumentRenderer {
    /** Renders all section titles and paragraph bodies, including deeply nested sections. */
    @Override public String render(Document doc, int width, LineBreaker breaker) {
        if (doc == null || breaker == null) throw new IllegalArgumentException("Document and breaker required");
        StringBuilder out = new StringBuilder();
        for (Section section : doc.getSections()) renderNode(section, out, width, breaker);
        return out.toString();
    }
    private void renderNode(DocumentNode node, StringBuilder out, int width, LineBreaker breaker) {
        if (node instanceof Section s) {
            appendBlock(out, breaker.wrap(s.getTitle().plainCharacters(), width));
            for (DocumentNode child : s.getChildren()) renderNode(child, out, width, breaker);
        } else if (node instanceof Paragraph p) {
            appendBlock(out, breaker.wrap(p.getBody().plainCharacters(), width));
        }
    }
    private static void appendBlock(StringBuilder out, List<String> lines) {
        if (out.length() > 0) out.append("\n\n");
        out.append(String.join("\n", lines));
    }
    /** Returns the renderer name. */
    @Override public String name() { return "plain"; }
}
