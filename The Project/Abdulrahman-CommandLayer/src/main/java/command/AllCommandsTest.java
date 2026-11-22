package command;

public class AllCommandsTest {

    public static void main(String[] args) {

        CommandParser parser = new CommandParser();

        String[] commands = {
                "insert p1 5 Hello world",
                "delete p1 3 8",
                "replace p1 3 8 Goodbye",
                "move p1 3 8 12",
                "split p1 5 p2",
                "merge p1 p2",
                "undo",
                "redo",
                "save checkpoint1",
                "restore checkpoint1",
                "find substring Hello",
                "replacefirst substring Hello | Goodbye",
                "replaceall whole Hello | Goodbye",
                "format p1 0 5 bold=true italic=true",
                "render",
                "status",
                "width 80",
                "view plain",
                "breaker greedy",
                "macro start test1",
                "macro stop",
                "macro replay test1"
        };

        int passed = 0;

        for (String line : commands) {
            try {
                ParsedCommand command = parser.parse(line);

                System.out.println(
                        "PASS: " + line + " -> " + command.verb
                );

                passed++;

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "FAIL: " + line + " -> " + e.getMessage()
                );
            }
        }

        System.out.println();
        System.out.println(
                "Passed: " + passed + "/" + commands.length
        );
    }
}