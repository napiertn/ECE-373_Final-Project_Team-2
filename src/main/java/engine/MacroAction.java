package engine;

/** Replayable user operation; may call history, presentation, or status services. */
@FunctionalInterface
public interface MacroAction {
    /** Executes the recorded step and reports success/failure. */
    CommandResult execute();
}
