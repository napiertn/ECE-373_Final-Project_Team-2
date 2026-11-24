package command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Text-based entry point, supporting scripts and interactive commands using identical processing. */
public final class EditorApplication {
    private final CommandProcessor processor = new CommandProcessor();
    /** Starts interactive mode or runs each named script in order. */
    public static void main(String[] args) {
        EditorApplication app = new EditorApplication();
        if (args.length == 0) app.runInteractive();
        else {
            for (String arg : args) {
                try { app.runScript(Path.of(arg)); }
                catch (RuntimeException e) { System.err.println("ERROR: " + e.getMessage()); System.exit(1); }
            }
        }
    }
    /** Starts the command loop. */
    public void runInteractive() {
        System.out.println("ECE 373 Document Editor | type help or exit");
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            while (true) {
                System.out.print("editor> ");
                String line = input.readLine();
                if (line == null) break;
                CommandResult result = processor.processLine(line);
                show(result);
                if (line.strip().equalsIgnoreCase("exit")) break;
            }
        } catch (IOException e) { System.err.println("ERROR: " + e.getMessage()); }
    }
    /** Runs commands from a UTF-8 script file on this application's single document. */
    public void runScript(Path path) {
        if (path == null) throw new IllegalArgumentException("Script path required");
        try (BufferedReader input = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            for (CommandResult result : processor.processScript(input)) show(result);
        } catch (IOException e) { throw new IllegalArgumentException("Cannot read script: " + path + " (" + e.getMessage() + ")", e); }
    }
    private static void show(CommandResult result) {
        if (!result.message().isBlank()) System.out.println((result.success() ? "" : "ERROR: ") + result.message());
    }
}
