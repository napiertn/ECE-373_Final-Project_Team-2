package command;

public class CommandParserTest {
    public static void main(String[] args) {

        CommandParser parser = new CommandParser();

        ParsedCommand command = parser.parse("insert Hello world");

        System.out.println("Verb: " + command.verb);
        System.out.println("Text: " + command.argument("text"));
    }
}