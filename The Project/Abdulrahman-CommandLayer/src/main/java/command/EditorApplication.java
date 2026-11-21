package command;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class EditorApplication {

    private final CommandProcessor processor;

    public EditorApplication() {
        this.processor = new CommandProcessor();
    }

    public static void main(String[] args) {
        EditorApplication app = new EditorApplication();
        if (args.length == 0){
            app.runInteractive();
        } else if (args.length == 1) {
            app.runScript(Path.of(args[0]));

        }else {
            System.out.println("Usage: EditorApplication  [script-file]");
        }
    }

    public void runInteractive() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Document Editor Started!");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine();

            if (line.trim().equalsIgnoreCase("exit")) {
                break;
            }

            CommandResult result = processor.processLine(line);
            System.out.println(result.message);
        }

        System.out.println("Document Editor Closed.");
    }

    public void runScript(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Script path cannot be null");
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            System.out.println("Running script: " + path);

            processor.processScript(reader);

            System.out.println("Script processing completed.");

        } catch (IOException e) {
            System.out.println("Error reading script: " + e.getMessage());
        }
    }
}
