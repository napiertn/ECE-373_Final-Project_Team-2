package command;

public class CommandParserTest {

    public static void main(String[] args) {

        CommandParser parser = new CommandParser();

        // Test 1: Valid insert
        ParsedCommand command =
                parser.parse("insert p1 5 Hello world");

        System.out.println("Verb: " + command.verb);
        System.out.println("Node ID: " + command.argument("nodeId"));
        System.out.println("Offset: " + command.argument("offset"));
        System.out.println("Text: " + command.argument("text"));

        // Test 2: Invalid command
        try {
            parser.parse("jump Hello world");
            System.out.println("ERROR: Invalid command accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Test 3: Invalid offset
        try {
            parser.parse("insert p1 abc Hello");
            System.out.println("ERROR: Invalid offset accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Test 4: Valid delete
        ParsedCommand deleteCommand =
                parser.parse("delete p1 3 8");

        System.out.println("Delete Verb: " + deleteCommand.verb);
        System.out.println("Delete Node ID: "
                + deleteCommand.argument("nodeId"));
        System.out.println("Delete Start: "
                + deleteCommand.argument("start"));
        System.out.println("Delete End: "
                + deleteCommand.argument("end"));

        // Test 5: Invalid delete range
        try {
            parser.parse("delete p1 8 3");
            System.out.println("ERROR: Invalid delete accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}