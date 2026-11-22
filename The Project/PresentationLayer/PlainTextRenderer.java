// This Piece should Take the Document and its sections / paragraphs

package presentation;

import java.util.List;
import model.Document;
import model.DocumentNode;
import model.DocumentWalker;
import model.Paragraph;
import model.Section;

public final class PlainTextRenderer implements DocumentRenderer {
    @Override

    public String render(Document doc, int width, LineBreaker breaker) {

        // Check for illegal arguement
        if (doc == null || breaker == null) throw new IllegalArgumentException("Document and line breaker are required");

        //
        StringBuilder out = new StringBuilder();
        // Loop Reads document
        for (DocumentNode node : DocumentWalker.depthFirst(doc)) {
            String text = (node instanceof Section s)
                    ? s.getTitle().plainCharacters()    // Checks section
                    : ((Paragraph) node).getBody().plainCharacters();  // Paragraph check
            appendBlock(out, breaker.wrap(text, width));  // Splits text into list of lines
        }
        return out.toString();
    }

    // Adds wrapped lines to the output, with one blank line between blocks.
    private static void appendBlock(StringBuilder out, List<String> lines) {
        if (lines.isEmpty()) return;
        if (out.length() > 0) out.append("\n\n");
        out.append(String.join("\n", lines));
    }

    @Override
    public String name() { return "plain_text"; }
}

    }