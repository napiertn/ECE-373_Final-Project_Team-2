package command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/** Connects the textual parser to an independently testable dispatcher. */
public final class CommandProcessor {
    private final CommandParser parser;
    private final CommandDispatcher dispatcher;
    /** Builds a complete processor with its own one-document engine. */
    public CommandProcessor() { this(new CommandParser(), new CommandDispatcher()); }
    /** Constructs a processor using injected collaborators. */
    public CommandProcessor(CommandParser parser, CommandDispatcher dispatcher) {
        if (parser == null || dispatcher == null) throw new IllegalArgumentException("Parser and dispatcher required");
        this.parser = parser; this.dispatcher = dispatcher;
    }
    /** Executes one line; returns success for comments and blanks. */
    public CommandResult processLine(String line) {
        if (line == null || line.isBlank() || line.stripLeading().startsWith("#")) return CommandResult.ok("");
        try { return dispatcher.dispatch(parser.parse(line)); }
        catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
    }
    /** Runs script lines in order; returns their results for deterministic tests and the CLI. */
    public List<CommandResult> processScript(Reader lines) {
        if (lines == null) throw new IllegalArgumentException("Script reader required");
        List<CommandResult> results = new ArrayList<>();
        try {
            BufferedReader buffered = new BufferedReader(lines);
            String line;
            while ((line = buffered.readLine()) != null) {
                if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
                results.add(processLine(line));
                if (line.strip().equalsIgnoreCase("exit")) break;
            }
        } catch (IOException e) { throw new UncheckedIOException("Unable to read script", e); }
        return results;
    }
    /** Returns the same dispatcher used for all subsequent commands. */
    public CommandDispatcher dispatcher() { return dispatcher; }
}
