package presentation;

import java.util.ArrayList;
import java.util.List;
import model.*;

/** Displays heading hierarchy and run-specific visible formatting markers. */
public final class MarkedUpRenderer implements DocumentRenderer {
    /** Returns document text annotated with style changes. */
    @Override public String render(Document doc, int width, LineBreaker breaker) {
        if (doc == null || breaker == null) throw new IllegalArgumentException("Document and breaker required");
        StringBuilder out = new StringBuilder();
        for (Section section : doc.getSections()) renderNode(section, 1, out, width, breaker);
        return out.toString();
    }
    private static void renderNode(DocumentNode node, int depth, StringBuilder out, int width, LineBreaker breaker) {
        if (node instanceof Section section) {
            appendBlock(out, breaker.wrap("#".repeat(Math.min(depth, 6)) + " " + markup(section.getTitle()), width));
            for (DocumentNode child : section.getChildren()) renderNode(child, depth + 1, out, width, breaker);
        } else if (node instanceof Paragraph p) {
            appendBlock(out, breaker.wrap(markup(p.getBody()), width));
        }
    }
    private static void appendBlock(StringBuilder out, List<String> lines) {
        if (out.length() > 0) out.append("\n\n");
        out.append(String.join("\n", lines));
    }
    private static String markup(StyledText value) {
        StringBuilder out = new StringBuilder();
        TextStyle base = TextStyle.normal();
        for (TextRun run : value.getRuns()) {
            TextStyle style = run.style();
            List<String> extras = new ArrayList<>();
            if (!style.fontName().equals(base.fontName())) extras.add("font=" + style.fontName());
            if (style.fontSize() != base.fontSize()) extras.add("size=" + style.fontSize());
            if (!style.color().equals(base.color())) extras.add("color=" + style.color());
            if (!extras.isEmpty()) out.append('{').append(String.join(",", extras)).append('}');
            if (style.bold()) out.append("**");
            if (style.italic()) out.append('_');
            out.append(run.characters());
            if (style.italic()) out.append('_');
            if (style.bold()) out.append("**");
            if (!extras.isEmpty()) out.append("{/}");
        }
        return out.toString();
    }
    /** Returns the renderer name. */
    @Override public String name() { return "markup"; }
}
