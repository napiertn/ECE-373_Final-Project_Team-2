package command;

public class EditCommandFactoryTest {

    public static void main(String[] args) {

        CommandParser parser = new CommandParser();
        EditCommandFactory factory = new EditCommandFactory();

        String[] editCommands = {
                "insert p1 5 Hello",
                "delete p1 3 8",
                "replace p1 3 8 Goodbye",
                "move p1 3 8 12",
                "split p1 5 p2",
                "merge p1 p2",
                "format p1 0 5 bold=true"
        };

        for (String line : editCommands) {
            ParsedCommand request = parser.parse(line);

            System.out.println(
                    request.verb + " -> "
                            + factory.getEditCommandType(request)
            );
        }

        // Test a non-editing command.
        ParsedCommand undo = parser.parse("undo");

        System.out.println(
                "Is undo an edit command? "
                        + factory.isEditCommand(undo)
        );
    }
}