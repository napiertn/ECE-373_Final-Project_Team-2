package edit;

import model.*;

/** Moves text within or between paragraph bodies using PRE-edit coordinates. */
public final class MoveTextCommand extends SnapshotEditCommand {
    private final TextSelection source;
    private final TextLocation destination;
    private final DocumentValidator validator = new DocumentValidator();

    /** Creates a paragraph-text move. */
    public MoveTextCommand(TextSelection source, TextLocation destination) {
        this.source = source;
        this.destination = destination;
    }
    /** Checks both pre-move positions and disallows landing inside the source. */
    @Override public void validate(Document doc) {
        validator.checkSelection(source, doc);
        validator.checkLocation(destination, doc);
        if (source.field() != TextField.BODY || destination.field() != TextField.BODY)
            throw new IllegalArgumentException("Move supports paragraph bodies only");
        if (source.nodeId().equals(destination.nodeId())
                && destination.offset() > source.start() && destination.offset() < source.end())
            throw new IllegalArgumentException("Cannot move into the interior of the selected range");
    }
    /** Deletes from the source then inserts at the correctly adjusted position. */
    @Override protected void apply(Document doc) {
        StyledText origin = validator.resolve(source.nodeId(), source.field(), doc);
        StyledText portion = origin.subText(source.start(), source.end());
        origin.delete(source.start(), source.end());
        int insertion = destination.offset();
        if (source.nodeId().equals(destination.nodeId()) && insertion >= source.end())
            insertion -= (source.end() - source.start());
        validator.resolve(destination.nodeId(), destination.field(), doc).insert(insertion, portion);
    }
}
