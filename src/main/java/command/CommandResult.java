package command;

/** Stable result of a console instruction, including the error explanation on failure. */
public record CommandResult(boolean success, String message) {
    /** Creates an affirmative result. */
    public static CommandResult ok(String message) { return new CommandResult(true, message); }
    /** Creates a failed result. */
    public static CommandResult fail(String message) { return new CommandResult(false, message); }
}
