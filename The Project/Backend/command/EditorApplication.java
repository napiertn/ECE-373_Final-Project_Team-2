package command;

import java.nio.file.Path;
import java.util.Scanner;

public class EditorApplication {

    private final CommandProcessor processor;

    public EditorApplication() {
        this.processor = new CommandProcessor();
    }

    public static void main(String[] args) {
        EditorApplication app = new EditorApplication();
        app.runInteractive();
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
    }
}
