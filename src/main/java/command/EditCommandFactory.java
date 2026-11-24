package command;

import edit.*;
import model.*;
import java.util.Set;

/** Factory pattern: translates validated parsed requests into independent editing commands. */
public final class EditCommandFactory {
    private static final Set<String> CHANGING = Set.of(
            "section", "subsection", "paragraph", "sectionat", "subsectionat", "paragraphat",
            "insert", "titleinsert", "delete", "titledelete", "replace", "titlereplace", "move", "moveto",
            "split", "merge", "format", "titleformat");
    /** Tells the dispatcher whether a parsed request is implemented by an EditCommand. */
    public boolean isEditCommand(ParsedCommand p) { return p != null && CHANGING.contains(p.verb); }
    /** Builds an edit without modifying the document. */
    public EditCommand create(ParsedCommand p, Document document) {
        if (!isEditCommand(p)) throw new IllegalArgumentException("Not an edit command: " + p.verb);
        String id = p.argument("id");
        return switch (p.verb) {
            case "section", "subsection", "paragraph", "sectionat", "subsectionat", "paragraphat" -> {
                String parent = p.argument("parent");
                boolean isSection = p.verb.startsWith("section") || p.verb.startsWith("subsection");
                DocumentNode node = isSection ? new Section(id, new StyledText(p.argument("text")))
                        : new Paragraph(id, new StyledText(p.argument("text")));
                int index = p.argument("index") != null ? number(p, "index")
                        : parent == null ? document.getSections().size()
                        : ((Section) requireSection(document, parent)).getChildren().size();
                yield new AddNodeCommand(parent, index, node);
            }
            case "insert", "titleinsert" -> new InsertTextCommand(
                    new TextLocation(id, field(p), number(p, "offset")), new StyledText(p.argument("text")));
            case "delete", "titledelete" -> new DeleteTextCommand(selection(p));
            case "replace", "titlereplace" -> new ReplaceTextCommand(selection(p), new StyledText(p.argument("text")));
            case "move", "moveto" -> new MoveTextCommand(selection(p),
                    new TextLocation(p.argument("target"), TextField.BODY, number(p, "offset")));
            case "split" -> new SplitParagraphCommand(id, number(p, "offset"), p.argument("newId"));
            case "merge" -> new MergeParagraphsCommand(p.argument("first"), p.argument("second"));
            case "format", "titleformat" -> new FormatTextCommand(selection(p), new StylePatch(
                    bool(p, "bold"), bool(p, "italic"), p.argument("fontname"),
                    p.argument("fontsize") == null ? null : number(p, "fontsize"), p.argument("color")));
            default -> throw new IllegalArgumentException("Unsupported edit: " + p.verb);
        };
    }
    private static DocumentNode requireSection(Document doc, String id) {
        DocumentNode node = doc.findNode(id);
        if (!(node instanceof Section)) throw new IllegalArgumentException("Parent section not found: " + id);
        return node;
    }
    private static TextSelection selection(ParsedCommand p) {
        return new TextSelection(p.argument("id"), field(p), number(p, "start"), number(p, "end"));
    }
    private static TextField field(ParsedCommand p) {
        return p.verb.startsWith("title") ? TextField.SECTION_TITLE : TextField.BODY;
    }
    private static Boolean bool(ParsedCommand p, String key) {
        return p.argument(key) == null ? null : Boolean.valueOf(p.argument(key));
    }
    private static int number(ParsedCommand p, String key) { return Integer.parseInt(p.argument(key)); }
}
