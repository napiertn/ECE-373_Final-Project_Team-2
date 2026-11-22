package command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CommandParser {

    private static final Set<String> SUPPORTED_COMMANDS = Set.of(
            "insert",
            "delete",
            "replace",
            "move",
            "split",
            "merge",
            "undo",
            "redo",
            "save",
            "restore",
            "find",
            "replacefirst",
            "replaceall",
            "format",
            "render",
            "status",
            "width",
            "view",
            "breaker",
            "macro"
    );

    public ParsedCommand parse(String line) {

        if (line == null || line.isBlank()){
            throw new IllegalArgumentException("Command cannot be empty");
        }

        String[] parts = line.trim().split("\\s+", 2);

        String verb = parts[0].toLowerCase(Locale.ROOT);
        if (!SUPPORTED_COMMANDS.contains(verb)) {
            throw new IllegalArgumentException(
                    "Unknown command: " + verb
            );
        }
        Map<String, String> arguments = new HashMap<>();

        if (parts.length > 1){
            arguments.put("text", parts[1]);
        }
        return new ParsedCommand(verb, arguments);
    }
}
