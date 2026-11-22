package edit;

import java.util.ArrayList;
import java.util.List;
import model.Document;

/** Composite pattern: execute many edits as one atomic and reversible unit. */
public final class CompositeEditCommand extends SnapshotEditCommand {
    private final List<EditCommand> children = new ArrayList<>();

    /** Appends an editing command. */
    public void add(EditCommand command) {
        if (command == null) throw new IllegalArgumentException("Command required");
        children.add(command);
    }

    /** Returns number of child commands. */
    public int size() { return children.size(); }

    /** Ensures the composite contains at least one operation. */
    @Override public void validate(Document doc) {
        if (children.isEmpty()) throw new IllegalArgumentException("Composite command must contain edits");
    }

    /** Validates and executes each child in sequence; base class rolls back on error. */
    @Override protected void apply(Document doc) {
        for (EditCommand child : children) {
            child.validate(doc);
            child.execute(doc);
        }
    }
}
