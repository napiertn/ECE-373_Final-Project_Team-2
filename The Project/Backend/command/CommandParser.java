package command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CommandParser {

    public ParsedCommand parse(String line) {

        if (line == null || line.isBlank()){
            throw new IllegalArgumentException("Command cannot be empty");
        }

        String[] parts = line.trim().split("\\s+", 2);

        String verb = parts[0].toLowerCase(Locale.ROOT);
        Map<String, String> arguments = new HashMap<>();

        if (parts.length > 1){
            arguments.put("text", parts[1]);
        }
        return new ParsedCommand(verb, arguments);
    }
}
