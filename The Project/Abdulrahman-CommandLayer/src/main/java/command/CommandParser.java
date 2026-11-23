package command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CommandParser {

    private static final Set<String> SUPPORTED_COMMANDS = Set.of(
            "insert", "delete", "replace", "move",
            "split", "merge", "undo", "redo",
            "save", "restore", "find",
            "replacefirst", "replaceall", "format",
            "render", "status", "width", "view",
            "breaker", "macro"
    );

    public ParsedCommand parse(String line) {

        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException(
                    "Command cannot be empty");
        }

        // Project design specifies ASCII input.
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) > 127) {
                throw new IllegalArgumentException(
                        "Only ASCII input is supported");
            }
        }

        String[] parts = line.trim().split("\\s+", 2);
        String verb = parts[0].toLowerCase(Locale.ROOT);
        String input = parts.length == 2 ? parts[1] : "";

        if (!SUPPORTED_COMMANDS.contains(verb)) {
            throw new IllegalArgumentException(
                    "Unknown command: " + verb);
        }

        Map<String, String> arguments = new HashMap<>();

        switch (verb) {

            case "insert" -> {
                String[] values = input.split("\\s+", 3);
                requireCount(values, 3,
                        "insert <paragraphId> <offset> <text>");

                arguments.put("nodeId", values[0]);
                arguments.put("offset",
                        number(values[1], "offset"));
                arguments.put("text",
                        requiredText(values[2], "Insert text"));
            }

            case "delete" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 3,
                        "delete <paragraphId> <start> <end>");

                putRange(arguments, values[0],
                        values[1], values[2]);
            }

            case "replace" -> {
                String[] values = input.split("\\s+", 4);
                requireCount(values, 4,
                        "replace <paragraphId> <start> <end> <text>");

                putRange(arguments, values[0],
                        values[1], values[2]);

                arguments.put("text",
                        requiredText(values[3], "Replacement text"));
            }

            case "move" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 4,
                        "move <paragraphId> <start> <end> <destination>");

                putRange(arguments, values[0],
                        values[1], values[2]);

                arguments.put("destination",
                        number(values[3], "destination"));
            }

            case "split" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 3,
                        "split <paragraphId> <offset> <newId>");

                arguments.put("nodeId", values[0]);
                arguments.put("offset",
                        number(values[1], "offset"));
                arguments.put("newId", values[2]);
            }

            case "merge" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 2,
                        "merge <firstId> <secondId>");

                if (values[0].equals(values[1])) {
                    throw new IllegalArgumentException(
                            "Cannot merge a paragraph with itself");
                }

                arguments.put("firstId", values[0]);
                arguments.put("secondId", values[1]);
            }

            case "undo", "redo", "render", "status" -> {
                requireNoArguments(input, verb);
            }

            case "save", "restore" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 1,
                        verb + " <checkpointName>");

                arguments.put("name", values[0]);
            }

            case "find" -> {
                String[] values = input.split("\\s+", 2);
                requireCount(values, 2,
                        "find <substring|whole> <query>");

                arguments.put("mode", searchMode(values[0]));
                arguments.put("query",
                        requiredText(values[1], "Search query"));
            }

            case "replacefirst", "replaceall" -> {
                String[] values = input.split("\\s+", 2);
                requireCount(values, 2,
                        verb + " <substring|whole> <query> | <replacement>");

                arguments.put("mode", searchMode(values[0]));

                String[] textParts = values[1].split("\\|", 2);
                requireCount(textParts, 2,
                        verb + " <substring|whole> <query> | <replacement>");

                arguments.put("query",
                        requiredText(textParts[0].trim(), "Search query"));
                arguments.put("replacement",
                        requiredText(textParts[1].trim(),
                                "Replacement text"));
            }

            case "format" -> {
                String[] values = input.split("\\s+", 4);
                requireCount(values, 4,
                        "format <paragraphId> <start> <end> <style=value>");

                putRange(arguments, values[0],
                        values[1], values[2]);

                String options = values[3].trim();

                for (String option : options.split("\\s+")) {
                    String[] pair = option.split("=", 2);

                    if (pair.length != 2 ||
                            pair[0].isBlank() ||
                            pair[1].isBlank()) {
                        throw new IllegalArgumentException(
                                "Invalid format option: " + option);
                    }

                    String key = pair[0].toLowerCase(Locale.ROOT);

                    if (!Set.of("bold", "italic", "fontname",
                            "fontsize", "color").contains(key)) {
                        throw new IllegalArgumentException(
                                "Unknown format option: " + key);
                    }

                    if (arguments.containsKey(key)) {
                        throw new IllegalArgumentException(
                                "Duplicate format option: " + key);
                    }

                    if ((key.equals("bold") || key.equals("italic"))
                            && !pair[1].equalsIgnoreCase("true")
                            && !pair[1].equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException(
                                key + " must be true or false");
                    }

                    if (key.equals("fontsize") &&
                            Integer.parseInt(
                                    number(pair[1], "fontsize")) <= 0) {
                        throw new IllegalArgumentException(
                                "Font size must be positive");
                    }

                    arguments.put(key, pair[1]);
                }
            }

            case "width" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 1, "width <positiveNumber>");

                int width = Integer.parseInt(
                        number(values[0], "width"));

                if (width <= 0) {
                    throw new IllegalArgumentException(
                            "Width must be positive");
                }

                arguments.put("width", String.valueOf(width));
            }

            case "view" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 1, "view <plain|markup>");

                String view = values[0].toLowerCase(Locale.ROOT);

                if (!view.equals("plain") &&
                        !view.equals("markup")) {
                    throw new IllegalArgumentException(
                            "View must be plain or markup");
                }

                arguments.put("view", view);
            }

            case "breaker" -> {
                String[] values = input.split("\\s+");
                requireCount(values, 1,
                        "breaker <greedy|balanced>");

                String breaker =
                        values[0].toLowerCase(Locale.ROOT);

                if (!breaker.equals("greedy") &&
                        !breaker.equals("balanced")) {
                    throw new IllegalArgumentException(
                            "Breaker must be greedy or balanced");
                }

                arguments.put("breaker", breaker);
            }

            case "macro" -> {
                String[] values = input.split("\\s+");
                if (values.length < 1 || values[0].isBlank()) {
                    throw new IllegalArgumentException(
                            "Usage: macro <start|stop|replay> [name]");
                }

                String action =
                        values[0].toLowerCase(Locale.ROOT);

                switch (action) {
                    case "start", "replay" -> {
                        requireCount(values, 2,
                                "macro " + action + " <name>");

                        arguments.put("action", action);
                        arguments.put("name", values[1]);
                    }

                    case "stop" -> {
                        requireCount(values, 1, "macro stop");
                        arguments.put("action", "stop");
                    }

                    default -> throw new IllegalArgumentException(
                            "Unknown macro action: " + action);
                }
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported command: " + verb);
        }

        return new ParsedCommand(verb, arguments);
    }

    private static void requireCount(
            String[] values, int count, String usage) {

        if (values.length != count ||
                (count > 0 && values[0].isBlank())) {
            throw new IllegalArgumentException(
                    "Usage: " + usage);
        }
    }

    private static void requireNoArguments(
            String input, String verb) {

        if (!input.isBlank()) {
            throw new IllegalArgumentException(
                    verb + " does not accept arguments");
        }
    }

    private static String requiredText(
            String value, String description) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    description + " cannot be empty");
        }

        return value;
    }

    private static String number(
            String value, String name) {

        try {
            int parsed = Integer.parseInt(value);

            if (parsed < 0) {
                throw new IllegalArgumentException(
                        name + " cannot be negative");
            }

            return String.valueOf(parsed);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    name + " must be an integer");
        }
    }

    private static void putRange(
            Map<String, String> arguments,
            String nodeId, String startText, String endText) {

        int start = Integer.parseInt(
                number(startText, "start"));
        int end = Integer.parseInt(
                number(endText, "end"));

        if (end <= start) {
            throw new IllegalArgumentException(
                    "Invalid range: end must be greater than start");
        }

        arguments.put("nodeId", nodeId);
        arguments.put("start", String.valueOf(start));
        arguments.put("end", String.valueOf(end));
    }

    private static String searchMode(String value) {

        String mode = value.toLowerCase(Locale.ROOT);

        if (!mode.equals("substring") && !mode.equals("whole")) {
            throw new IllegalArgumentException(
                    "Search mode must be substring or whole");
        }

        return mode;
    }
}
