package command;

import java.util.Set;

public class EditCommandFactory {

    private static final Set<String> EDIT_COMMANDS = Set.of(
            "insert",
            "delete",
            "replace",
            "move",
            "split",
            "merge",
            "format"
    );

    public boolean isEditCommand(ParsedCommand request) {
        if (request == null) {
            return false;
        }

        return EDIT_COMMANDS.contains(request.verb);
    }

    public String getEditCommandType(ParsedCommand request) {
        if (!isEditCommand(request)) {
            throw new IllegalArgumentException(
                    "Not an editing command"
            );
        }

        return switch (request.verb) {
            case "insert" -> "InsertTextCommand";
            case "delete" -> "DeleteTextCommand";
            case "replace" -> "ReplaceTextCommand";
            case "move" -> "MoveTextCommand";
            case "split" -> "SplitParagraphCommand";
            case "merge" -> "MergeParagraphsCommand";
            case "format" -> "FormatTextCommand";
            default -> throw new IllegalArgumentException(
                    "Unsupported editing command"
            );
        };
    }
}
