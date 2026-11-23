package command;

public class CommandDispatcherTest {

    public static void main(String[] args) {

        CommandParser parser = new CommandParser();
        CommandDispatcher dispatcher = new CommandDispatcher();

        String[] commands = {
                "insert p1 5 Hello",
                "delete p1 3 8",
                "undo",
                "redo",
                "save checkpoint1",
                "restore checkpoint1",
                "find substring Hello",
                "render",
                "status",
                "macro replay test1"
        };

        for (String line : commands) {
            ParsedCommand request = parser.parse(line);
            CommandResult result = dispatcher.dispatch(request);

            System.out.println(
                    line + " -> " + result.message
            );
        }
    }
}
