package command;

import edit.EditCommand;
import engine.EditorEngine;
import engine.MacroManager;
import engine.SearchMode;
import java.util.Set;
import model.*;
import presentation.PresentationService;

/** Integrates parsed command requests with the engine, presentation and macro services. */
public final class CommandDispatcher {
    private static final Set<String> RECORDABLE = Set.of(
            "section", "subsection", "paragraph", "sectionat", "subsectionat", "paragraphat",
            "insert", "titleinsert", "delete", "titledelete", "replace", "titlereplace", "move", "moveto",
            "split", "merge", "format", "titleformat", "find", "replacefirst", "replaceall",
            "render", "status", "width", "view", "breaker", "outline", "styles");
    private final EditorEngine engine;
    private final EditCommandFactory factory;
    private final PresentationService presentation;
    /** Creates a working application dispatcher. */
    public CommandDispatcher() { this(new EditorEngine(), new PresentationService()); }
    /** Supports independent injection of services for unit tests and alternative front ends. */
    public CommandDispatcher(EditorEngine engine, PresentationService presentation) {
        if (engine == null || presentation == null) throw new IllegalArgumentException("Engine and presentation required");
        this.engine = engine; this.presentation = presentation; this.factory = new EditCommandFactory();
    }
    /** Executes one request; no invalid command is allowed to partially mutate the model. */
    public CommandResult dispatch(ParsedCommand request) {
        if (request == null) return CommandResult.fail("Command is required");
        CommandResult result = dispatchInternal(request);
        if (result.success() && engine.macros().isRecording() && RECORDABLE.contains(request.verb)) {
            try { engine.macros().record(() -> adapt(dispatchInternal(request))); }
            catch (RuntimeException e) { return CommandResult.fail("Recording failed: " + e.getMessage()); }
        }
        return result;
    }
    private CommandResult dispatchInternal(ParsedCommand p) {
        try {
            if (factory.isEditCommand(p)) {
                EditCommand edit = factory.create(p, engine.document());
                return adapt(engine.apply(edit));
            }
            return switch (p.verb) {
                case "undo" -> adapt(engine.undo());
                case "redo" -> adapt(engine.redo());
                case "save" -> { engine.saveCheckpoint(p.argument("name")); yield CommandResult.ok("Saved checkpoint: " + p.argument("name")); }
                case "restore" -> adapt(engine.restoreCheckpoint(p.argument("name")));
                case "find" -> CommandResult.ok(formatMatches(engine.find(p.argument("query"), mode(p))));
                case "replacefirst" -> adapt(engine.replaceFirst(p.argument("query"), p.argument("replacement"), mode(p)));
                case "replaceall" -> adapt(engine.replaceAll(p.argument("query"), p.argument("replacement"), mode(p)));
                case "render" -> CommandResult.ok(engine.document().getSections().isEmpty() ? "(empty document)" : presentation.render(engine.document()));
                case "status" -> {
                    engine.DocumentStatistics s = engine.statistics();
                    yield CommandResult.ok("Words: " + s.words() + " | Characters: " + s.characters() + " | Paragraphs: " + s.paragraphs());
                }
                case "width" -> { presentation.setWidth(Integer.parseInt(p.argument("width"))); yield CommandResult.ok("Width: " + presentation.getWidth()); }
                case "view" -> { presentation.selectView(p.argument("name")); yield CommandResult.ok("View: " + presentation.currentView()); }
                case "breaker" -> { presentation.setLineBreaker(p.argument("name")); yield CommandResult.ok("Line breaker: " + presentation.currentBreaker()); }
                case "macro" -> runMacro(p);
                case "outline" -> CommandResult.ok(outline());
                case "styles" -> CommandResult.ok(styleCount());
                case "help" -> CommandResult.ok(Help.TEXT);
                case "exit" -> CommandResult.ok("Goodbye");
                default -> CommandResult.fail("Unsupported command: " + p.verb);
            };
        } catch (RuntimeException e) {
            return CommandResult.fail(e.getMessage() == null ? "Command failed" : e.getMessage());
        }
    }
    private CommandResult runMacro(ParsedCommand p) {
        MacroManager macros = engine.macros();
        String action = p.argument("action");
        return switch (action) {
            case "start" -> { macros.start(p.argument("name")); yield CommandResult.ok("Recording macro: " + p.argument("name")); }
            case "stop" -> { macros.stop(); yield CommandResult.ok("Macro saved"); }
            case "replay" -> {
                String view = presentation.currentView(), breaker = presentation.currentBreaker();
                int width = presentation.getWidth();
                engine.CommandResult result = macros.replay(p.argument("name"), engine.history());
                if (!result.success()) {
                    // Restore presentation state too when a macro fails halfway through.
                    presentation.selectView(view); presentation.setLineBreaker(breaker); presentation.setWidth(width);
                }
                yield adapt(result);
            }
            default -> CommandResult.fail("Unknown macro action: " + action);
        };
    }
    private static SearchMode mode(ParsedCommand p) {
        return "whole".equals(p.argument("mode")) ? SearchMode.WHOLE_WORD : SearchMode.SUBSTRING;
    }
    private static String formatMatches(java.util.List<TextSelection> matches) {
        if (matches.isEmpty()) return "No matches";
        StringBuilder sb = new StringBuilder("Found " + matches.size() + " match(es):");
        for (TextSelection match : matches) sb.append("\n  ").append(match.nodeId()).append("/")
                .append(match.field()).append(" [").append(match.start()).append(",").append(match.end()).append(")");
        return sb.toString();
    }
    private String outline() {
        StringBuilder output = new StringBuilder();
        for (Section section : engine.document().getSections()) outlineNode(section, 0, output);
        return output.length() == 0 ? "(empty document)" : output.toString().stripTrailing();
    }
    private static void outlineNode(DocumentNode node, int depth, StringBuilder out) {
        out.append("  ".repeat(depth)).append(node instanceof Section ? "[section] " : "[paragraph] ").append(node.getId());
        if (node instanceof Section section) {
            out.append(" : ").append(section.getTitle().plainCharacters());
            out.append('\n');
            for (DocumentNode child : section.getChildren()) outlineNode(child, depth + 1, out);
        } else out.append(" (" + ((Paragraph) node).getBody().length() + " chars)\n");
    }
    private String styleCount() {
        java.util.Set<TextStyle> styles = new java.util.HashSet<>();
        for (DocumentNode n : DocumentWalker.depthFirst(engine.document())) {
            StyledText content = n instanceof Section s ? s.getTitle() : ((Paragraph)n).getBody();
            for (TextRun run : content.getRuns()) styles.add(run.style());
        }
        return "Distinct formatting values: " + styles.size() + " | Storage: one style per run (not global interning)";
    }
    private static engine.CommandResult adapt(CommandResult r) {
        return new engine.CommandResult(r.success(), r.message());
    }
    private static CommandResult adapt(engine.CommandResult r) { return new CommandResult(r.success(), r.message()); }
    /** Exposes the headless engine for automated testing. */
    public EditorEngine engine() { return engine; }
    /** Exposes the presentation strategies for automated testing. */
    public PresentationService presentation() { return presentation; }
}
