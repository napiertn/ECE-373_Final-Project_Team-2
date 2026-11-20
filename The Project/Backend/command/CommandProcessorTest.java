package command;

public class CommandProcessorTest {
    public static void main(String[] args) {

        CommandProcessor processor = new CommandProcessor();

        CommandResult result = processor.processLine("insert Hello world");

        System.out.println("Success: " + result.success);
        System.out.println("Message: " + result.message);
    }
}
