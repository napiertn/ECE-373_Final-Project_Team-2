package engine;

import edit.CompositeEditCommand;
import edit.EditCommand;
import java.util.ArrayDeque;
import java.util.Deque;
import model.Document;
import model.DocumentSnapshot;

/** Unlimited-by-policy undo/redo manager; groups macro edits as one history entry. */
public final class HistoryManager {
    private final Document document;
    private final Deque<EditCommand> undoStack = new ArrayDeque<>();
    private final Deque<EditCommand> redoStack = new ArrayDeque<>();
    private CompositeEditCommand activeGroup;
    private DocumentSnapshot groupStart;

    /** Creates a history manager for a single document. */
    public HistoryManager(Document document) {
        if (document == null) throw new IllegalArgumentException("Document required");
        this.document = document;
    }

    /** Returns the document managed by this history. */
    public Document document() { return document; }

    /** Performs an edit with validation, rollback on failure, and notification. */
    public CommandResult perform(EditCommand command) {
        if (command == null) return CommandResult.fail("Command required");
        DocumentSnapshot initial = document.createSnapshot();
        try {
            command.validate(document);
            command.execute(document);
            if (activeGroup != null) activeGroup.add(command);
            else {
                undoStack.push(command);
                redoStack.clear();
            }
            document.publishChanged();
            return CommandResult.ok("Edit completed");
        } catch (RuntimeException e) {
            document.restore(initial);
            return CommandResult.fail(e.getMessage() == null ? "Edit failed" : e.getMessage());
        }
    }

    /** Undo one command or committed group. */
    public CommandResult undo() {
        if (activeGroup != null) return CommandResult.fail("Cannot undo during a command group");
        if (undoStack.isEmpty()) return CommandResult.fail("Nothing to undo");
        EditCommand edit = undoStack.peek();
        DocumentSnapshot initial = document.createSnapshot();
        try {
            edit.undo(document);
            undoStack.pop(); redoStack.push(edit);
            document.publishChanged();
            return CommandResult.ok("Undo completed");
        } catch (RuntimeException e) {
            document.restore(initial);
            return CommandResult.fail("Undo failed: " + e.getMessage());
        }
    }

    /** Redo one previously undone command or group. */
    public CommandResult redo() {
        if (activeGroup != null) return CommandResult.fail("Cannot redo during a command group");
        if (redoStack.isEmpty()) return CommandResult.fail("Nothing to redo");
        EditCommand edit = redoStack.peek();
        DocumentSnapshot initial = document.createSnapshot();
        try {
            edit.redo(document);
            redoStack.pop(); undoStack.push(edit);
            document.publishChanged();
            return CommandResult.ok("Redo completed");
        } catch (RuntimeException e) {
            document.restore(initial);
            return CommandResult.fail("Redo failed: " + e.getMessage());
        }
    }

    /** Starts an atomic edit group (normally macro replay). */
    public void beginGroup() {
        if (activeGroup != null) throw new IllegalStateException("Group already active");
        groupStart = document.createSnapshot();
        activeGroup = new CompositeEditCommand();
    }

    /** Commits a successful edit group into one undo entry. */
    public void commitGroup() {
        if (activeGroup == null) throw new IllegalStateException("No group active");
        if (activeGroup.size() > 0) {
            // The edits are already applied. Rebuild a group whose snapshots cover the entire transaction.
            EditCommand committed = new TransactionCommand(groupStart, document.createSnapshot());
            undoStack.push(committed);
            redoStack.clear();
        }
        activeGroup = null;
        groupStart = null;
    }

    /** Rolls back a failed macro/group without touching existing history. */
    public void rollbackGroup() {
        if (activeGroup == null) throw new IllegalStateException("No group active");
        document.restore(groupStart);
        activeGroup = null;
        groupStart = null;
        document.publishChanged();
    }

    /** Returns current undo depth. */
    public int undoDepth() { return undoStack.size(); }

    /** Returns current redo depth. */
    public int redoDepth() { return redoStack.size(); }

    private record TransactionCommand(DocumentSnapshot before, DocumentSnapshot after) implements EditCommand {
        @Override public void validate(Document doc) { }
        @Override public void execute(Document doc) { doc.restore(after); }
        @Override public void undo(Document doc) { doc.restore(before); }
        @Override public void redo(Document doc) { doc.restore(after); }
    }
}
