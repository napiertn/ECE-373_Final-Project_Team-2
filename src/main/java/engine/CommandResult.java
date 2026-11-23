package engine;

/** Success indicator and human-readable explanation for an engine operation. */
public record CommandResult(boolean success, String message) {
    /** Creates a successful result. */
    public static CommandResult ok(String message) { return new CommandResult(true, message); }
    /** Creates a failed result. */
    public static CommandResult fail(String message) { return new CommandResult(false, message); }
}
