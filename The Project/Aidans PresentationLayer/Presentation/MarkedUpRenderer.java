// this one is for text that is not plain, ie different fonts, Bold, italic, etc
// needs Document to paragraph and section like plain text,
//But also needs Styled Text
//TextRun
//TextStyle


//bold: {**text**}
//italic: {_text_}
// {font=Arial,size=14,color=red}
//section title headings: {@code #}

public final class MarkedUpRenderer implements DocumentRenderer {
    private static final int MAX_HEADING_LEVEL = 6;

    @Override
    // makes sure that it can read the docunment
    public String render(Document doc, int width, LineBreaker breaker) {
        if (doc == null || breaker == null)
            throw new IllegalArgumentException("Document and line breaker are required");
        StringBuilder out = new StringBuilder();
        for (Section root : doc.getSections()) renderNode(root, 1, out, width, breaker);
        return out.toString();

    }

        // Renders the different nodes,
    private void renderNode(DocumentNode node, int depth, StringBuilder out, int width, LineBreaker breaker) {
        if (node instanceof Section s) {
            String heading = "#".repeat(Math.min(depth, MAX_HEADING_LEVEL)) + " " + markup(s.getTitle());
            AppendBlock(out, breaker.wrap(heading, width));
            for (DocumentNode child : s.getChildren()) renderNode(child, depth + 1, out, width, breaker);
        } else {
            AppendBlock(out, breaker.wrap(markup(((Paragraph) node).getBody()), width));
        }
    }

    //adds a wrapped block to the output buffer, so blocks are seperated cleanly
    private static void AppendBlock(StringBuilder out, List<String> lines) {
        if (lines.isEmpty()) return;
        if (out.length() > 0) out.append("\n\n");
        out.append(String.join("\n", lines));
    }

    // Converts one StyledText into a single markup string (no line wrapping).
    private static String markup(StyledText text) {
        //  turns stylized text into single markup string, getting the default text and comparing against empty
        TextStyle normal = TextStyle.normal();
        StringBuilder sb = new StringBuilder();
        // goes through each textrun (characters with same formatting
        // and gives the list of attributes
        for (TextRun run : text.getRuns()) {
            TextStyle s = run.style();
            List<String> attrs = new ArrayList<>();
            if (!s.fontName().equals(normal.fontName())) attrs.add("font=" + s.fontName());
            if (s.fontSize() != normal.fontSize()) attrs.add("size=" + s.fontSize());
            if (!s.color().equals(normal.color())) attrs.add("color=" + s.color());
            if (!attrs.isEmpty()) sb.append('{').append(String.join(",", attrs)).append('}');
            if (s.bold()) sb.append("**");
            if (s.italic()) sb.append('_');
            sb.append(run.characters());
            if (s.italic()) sb.append('_');
            if (s.bold()) sb.append("**");
            if (!attrs.isEmpty()) sb.append("{/}");
        }
        return sb.toString();
    }

    @Override
    public String name() { return "marked"; }
}