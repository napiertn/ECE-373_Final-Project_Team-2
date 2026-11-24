package command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Parses an ASCII command line without knowing anything about document storage. */
public final class CommandParser {
    /** Constructs the stateless parser used by the CLI and scripts. */
    public CommandParser() { }
    private static final Set<String> VERBS = Set.of(
            "section", "subsection", "paragraph", "sectionat", "subsectionat", "paragraphat",
            "insert", "titleinsert", "delete", "titledelete", "replace", "titlereplace", "move", "moveto",
            "split", "merge", "format", "titleformat", "undo", "redo", "save", "restore",
            "find", "replacefirst", "replaceall", "render", "status", "width", "view", "breaker",
            "macro", "help", "outline", "styles", "exit");

    /** Parses a whole command into a normalized verb and immutable argument map. */
    public ParsedCommand parse(String line) {
        if (line == null || line.isBlank()) throw new IllegalArgumentException("Command cannot be empty");
        for (int i = 0; i < line.length(); i++)
            if (line.charAt(i) > 127) throw new IllegalArgumentException("Only plain ASCII input is supported");
        String[] head = line.strip().split("\\s+", 2);
        String verb = head[0].toLowerCase(Locale.ROOT);
        String input = head.length == 2 ? head[1].strip() : "";
        if (!VERBS.contains(verb)) throw new IllegalArgumentException("Unknown command: " + verb + " (try help)");
        Map<String, String> a = new HashMap<>();
        switch (verb) {
            case "section", "subsection", "paragraph", "sectionat", "subsectionat", "paragraphat" -> {
                int prefix = switch (verb) {
                    case "section" -> 1;
                    case "subsection", "sectionat", "paragraph" -> 2;
                    default -> 3;
                };
                String[] parts = input.split("\\s+", prefix + 1);
                if (parts.length < prefix || input.isBlank()) throw new IllegalArgumentException("Usage: " + verb + " <parent?> <index?> <id> [text]");
                int j = 0;
                if (verb.startsWith("subsection") || verb.startsWith("paragraph")) a.put("parent", parts[j++]);
                if (verb.endsWith("at")) a.put("index", number(parts[j++], "index"));
                a.put("id", nonblank(parts[j++], "Node ID"));
                String text = parts.length > j ? parts[j] : "";
                if (isSectionVerb(verb) && text.isBlank()) throw new IllegalArgumentException("Section title required");
                a.put("text", text);
            }
            case "insert", "titleinsert" -> {
                String[] parts = input.split("\\s+", 3);
                count(parts, 3, verb + " <nodeId> <offset> <text>");
                a.put("id", parts[0]); a.put("offset", number(parts[1], "offset"));
                a.put("text", nonblank(parts[2], "Inserted text"));
            }
            case "delete", "titledelete" -> {
                String[] parts = input.split("\\s+");
                count(parts, 3, verb + " <nodeId> <start> <end>");
                range(a, parts[0], parts[1], parts[2]);
            }
            case "replace", "titlereplace" -> {
                String[] parts = input.split("\\s+", 4);
                count(parts, 4, verb + " <nodeId> <start> <end> <text>");
                range(a, parts[0], parts[1], parts[2]); a.put("text", parts[3]);
            }
            case "move", "moveto" -> {
                String[] parts = input.split("\\s+");
                count(parts, verb.equals("move") ? 4 : 5,
                        "move <id> <start> <end> <offset> OR moveto <id> <start> <end> <targetId> <offset>");
                range(a, parts[0], parts[1], parts[2]);
                a.put("target", verb.equals("move") ? parts[0] : parts[3]);
                a.put("offset", number(parts[verb.equals("move") ? 3 : 4], "offset"));
            }
            case "split" -> {
                String[] parts = input.split("\\s+"); count(parts, 3, "split <id> <offset> <newId>");
                a.put("id", parts[0]); a.put("offset", number(parts[1], "offset")); a.put("newId", parts[2]);
            }
            case "merge" -> {
                String[] parts = input.split("\\s+"); count(parts, 2, "merge <firstId> <nextId>");
                a.put("first", parts[0]); a.put("second", parts[1]);
            }
            case "format", "titleformat" -> {
                String[] parts = input.split("\\s+", 4);
                count(parts, 4, verb + " <nodeId> <start> <end> bold=true italic=true fontname=... fontsize=... color=...");
                range(a, parts[0], parts[1], parts[2]);
                for (String opt : parts[3].split("\\s+")) {
                    String[] kv = opt.split("=", 2);
                    if (kv.length != 2 || kv[1].isBlank()) throw new IllegalArgumentException("Invalid style option: " + opt);
                    String key = kv[0].toLowerCase(Locale.ROOT);
                    if (!Set.of("bold", "italic", "fontname", "fontsize", "color").contains(key))
                        throw new IllegalArgumentException("Unknown style property: " + key);
                    if (a.containsKey(key)) throw new IllegalArgumentException("Duplicate property: " + key);
                    if ((key.equals("bold") || key.equals("italic")) && !Set.of("true", "false").contains(kv[1].toLowerCase(Locale.ROOT)))
                        throw new IllegalArgumentException(key + " must be true or false");
                    if (key.equals("fontsize") && Integer.parseInt(number(kv[1], key)) < 1)
                        throw new IllegalArgumentException("Font size must be positive");
                    a.put(key, kv[1]);
                }
            }
            case "undo", "redo", "render", "status", "help", "outline", "styles", "exit" -> noArgs(input, verb);
            case "save", "restore" -> {
                String[] parts = input.split("\\s+"); count(parts, 1, verb + " <checkpointName>");
                a.put("name", parts[0]);
            }
            case "find" -> {
                String[] parts = input.split("\\s+", 2); count(parts, 2, "find <substring|whole> <query>");
                a.put("mode", searchMode(parts[0])); a.put("query", nonblank(parts[1], "Query"));
            }
            case "replacefirst", "replaceall" -> {
                String[] parts = input.split("\\s+", 2);
                count(parts, 2, verb + " <substring|whole> <query> | <replacement>");
                a.put("mode", searchMode(parts[0]));
                int separator = parts[1].indexOf('|');
                if (separator < 0) throw new IllegalArgumentException("Separate search and replacement with | (empty replacement is allowed)");
                a.put("query", nonblank(parts[1].substring(0, separator).strip(), "Query"));
                a.put("replacement", parts[1].substring(separator + 1).strip());
            }
            case "width" -> {
                String[] parts = input.split("\\s+"); count(parts, 1, "width <positiveInteger>");
                int n = Integer.parseInt(number(parts[0], "width"));
                if (n < 1) throw new IllegalArgumentException("Width must be positive");
                a.put("width", String.valueOf(n));
            }
            case "view", "breaker" -> {
                String[] parts = input.split("\\s+"); count(parts, 1, verb + " <name>");
                String name = parts[0].toLowerCase(Locale.ROOT);
                if (! (verb.equals("view") ? Set.of("plain", "markup") : Set.of("greedy", "balanced")).contains(name))
                    throw new IllegalArgumentException("Unknown " + verb + ": " + name);
                a.put("name", name);
            }
            case "macro" -> {
                String[] parts = input.split("\\s+");
                if (parts.length < 1 || parts[0].isBlank()) throw new IllegalArgumentException("macro <start|stop|replay> [name]");
                String action = parts[0].toLowerCase(Locale.ROOT);
                if (action.equals("stop")) count(parts, 1, "macro stop");
                else if (action.equals("start") || action.equals("replay")) {
                    count(parts, 2, "macro " + action + " <name>"); a.put("name", parts[1]);
                } else throw new IllegalArgumentException("Unknown macro action: " + action);
                a.put("action", action);
            }
            default -> throw new IllegalStateException("Unhandled command: " + verb);
        }
        return new ParsedCommand(verb, a);
    }

    private static boolean isSectionVerb(String verb) {
        return verb.startsWith("section") || verb.startsWith("subsection");
    }
    private static void count(String[] values, int expected, String usage) {
        if (values.length != expected || expected > 0 && values[0].isBlank()) throw new IllegalArgumentException("Usage: " + usage);
    }
    private static void noArgs(String input, String verb) {
        if (!input.isBlank()) throw new IllegalArgumentException(verb + " does not accept arguments");
    }
    private static String nonblank(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " cannot be blank");
        return value;
    }
    private static String number(String value, String label) {
        try {
            int n = Integer.parseInt(value);
            if (n < 0) throw new IllegalArgumentException(label + " cannot be negative");
            return String.valueOf(n);
        } catch (NumberFormatException e) { throw new IllegalArgumentException(label + " must be an integer"); }
    }
    private static String searchMode(String mode) {
        if (mode.equalsIgnoreCase("substring") || mode.equalsIgnoreCase("whole")) return mode.toLowerCase(Locale.ROOT);
        throw new IllegalArgumentException("Search mode must be substring or whole");
    }
    private static void range(Map<String, String> a, String id, String start, String end) {
        a.put("id", nonblank(id, "Node ID"));
        int s = Integer.parseInt(number(start, "start")); int e = Integer.parseInt(number(end, "end"));
        if (s > e) throw new IllegalArgumentException("start must be <= end");
        a.put("start", String.valueOf(s)); a.put("end", String.valueOf(e));
    }
}
