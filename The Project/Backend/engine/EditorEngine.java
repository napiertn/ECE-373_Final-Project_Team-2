package engine;

import edit.EditCommand;
import edit.RestoreCheckpointCommand;
import model.Document;
import model.TextSelection;
import java.util.List;
import presentation.PresentationService;

/** Integration facade for a future command dispatcher; contains no CLI syntax. */
public final class EditorEngine {
    private final Document document = new Document();
    private final HistoryManager history = new HistoryManager(document);
    private final CheckpointManager checkpoints = new CheckpointManager();
    private final SearchService search = new SearchService(document);
    private final MacroManager macros = new MacroManager();
    private final StatisticsService statistics = new StatisticsService();

    private final PresentationService presentation = new PresentationService();

    /** Constructs an engine with a live statistics observer. */
    public EditorEngine() { statistics.initialize(document); }

    /** Returns the mutable model for queries and presentation. */
    public Document document() { return document; }
    /** Returns the undo/redo manager. */
    public HistoryManager history() { return history; }
    /** Returns the macro recorder. */
    public MacroManager macros() { return macros; }
    /** Returns the search service. */
    public SearchService search() { return search; }
    /** Returns current document statistics. */
    public DocumentStatistics statistics() { return statistics.getStatistics(); }

    //Aidans Presentation stuff///////////////////////////////////////////////////////
    /** Returns the presentation settings (view, line breaker, width). */
    public PresentationService presentation() { return presentation; }

    /** Switches the rendering view; an unknown name leaves the old view. */
    public CommandResult selectView(String name) {
        try {
            presentation.selectView(name);
            return CommandResult.ok("View: " + presentation.currentView());
        } catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
    /** Switches the line-breaking method; an unknown name leaves the old one. */
    public CommandResult selectLineBreaker(String name) {
        try {
            presentation.setLineBreaker(name);
            return CommandResult.ok("Line breaker: " + presentation.currentBreaker());
        } catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
    /** Sets the output width from user text; invalid input leaves the old width. */
    public CommandResult setWidth(String text) {
        try {
            presentation.setWidth(Integer.parseInt(text.trim()));
            return CommandResult.ok("Width: " + presentation.getWidth());
        } catch (NumberFormatException e) {
            return CommandResult.fail("Width must be a whole number, got '" + text + "'");
        } catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
    /** Renders the document with the current settings. */
    public CommandResult render() { return CommandResult.ok(presentation.render(document)); }
    /** Reports the current word, character, and paragraph counts. */
    public CommandResult status() {
        DocumentStatistics s = statistics.getStatistics();
        return CommandResult.ok("Words: " + s.words() + ", Characters: " + s.characters()
                + ", Paragraphs: " + s.paragraphs());
    }
    /*//////////////////////////////////////////////////////////////////////////////////////////////////*/

    /** Executes a reversible edit through history. */
    public CommandResult apply(EditCommand command) { return history.perform(command); }
    /** Undoes one edit (or one macro replay). */
    public CommandResult undo() { return history.undo(); }
    /** Redoes one previously undone edit. */
    public CommandResult redo() { return history.redo(); }
    /** Saves the full document under a user-provided checkpoint name. */
    public void saveCheckpoint(String name) { checkpoints.save(name, document.createSnapshot()); }
    /** Restores a checkpoint with the operation itself undoable. */
    public CommandResult restoreCheckpoint(String name) {
        try { return apply(new RestoreCheckpointCommand(checkpoints.get(name))); }
        catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
    /** Finds all matches in the current document. */
    public List<TextSelection> find(String query, SearchMode mode) { return search.find(query, mode); }
    /** Replaces the first match with one undoable operation. */
    public CommandResult replaceFirst(String query, String value, SearchMode mode) {
        try {
            EditCommand edit = search.replaceFirst(query, value, mode);
            return edit == null ? CommandResult.ok("No matches") : apply(edit);
        } catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
    /** Replaces all matches as a SINGLE undoable operation. */
    public CommandResult replaceAll(String query, String value, SearchMode mode) {
        try {
            var edit = search.replaceAll(query, value, mode);
            return edit.size() == 0 ? CommandResult.ok("No matches") : apply(edit);
        } catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
}
