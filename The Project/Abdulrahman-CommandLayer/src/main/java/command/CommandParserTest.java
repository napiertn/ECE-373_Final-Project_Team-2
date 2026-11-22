package command;

public class CommandParserTest {
    public static void main(String[] args) {

        CommandParser parser = new CommandParser();

        // Test 1: Valid command
        ParsedCommand command = parser.parse("insert Hello world");

        System.out.println("Verb: " + command.verb);
        System.out.println("Text: " + command.argument("text"));

        // Test 2: Invalid command
        try {
            parser.parse("jump Hello world");
            System.out.println("ERROR: Invalid command accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}