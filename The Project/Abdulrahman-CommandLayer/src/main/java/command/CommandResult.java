package command;

public class CommandResult {

    public final boolean success;
    public final String message;

    public CommandResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
