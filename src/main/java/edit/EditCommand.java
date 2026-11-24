package edit;

import model.Document;

/** Command pattern: an independently testable, reversible document operation. */
public interface EditCommand {
    /** Validates the operation against the current document. */
    void validate(Document doc);
    /** Applies this command. */
    void execute(Document doc);
    /** Reverts this command. */
    void undo(Document doc);
    /** Applies the same command again after undo. */
    void redo(Document doc);
}
