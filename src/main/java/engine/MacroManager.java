package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Records reusable user actions, replaying their edits as one undoable group. */
public final class MacroManager {
    private final Map<String, Macro> macros = new HashMap<>();
    private String recordingName;
    private List<MacroAction> recordingSteps;

    /** Indicates whether commands are currently being recorded. */
    public boolean isRecording() { return recordingName != null; }

    /** Begins recording a named macro. */
    public void start(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Macro name required");
        if (recordingName != null) throw new IllegalStateException("A macro is already recording");
        recordingName = name;
        recordingSteps = new ArrayList<>();
    }

    /** Records a replayable operation during an active recording. */
    public void record(MacroAction action) {
        if (recordingName == null) throw new IllegalStateException("No macro recording is active");
        if (action == null) throw new IllegalArgumentException("Macro action required");
        recordingSteps.add(action);
    }

    /** Stops recording and saves the macro. */
    public void stop() {
        if (recordingName == null) throw new IllegalStateException("No macro recording is active");
        macros.put(recordingName, new Macro(recordingName, recordingSteps));
        recordingName = null;
        recordingSteps = null;
    }

    /** Retrieves a previously saved macro. */
    public Macro get(String name) {
        Macro macro = macros.get(name);
        if (macro == null) throw new IllegalArgumentException("Unknown macro: " + name);
        return macro;
    }

    /** Replays all steps atomically with a single undo entry. */
    public CommandResult replay(String name, HistoryManager history) {
        if (recordingName != null) return CommandResult.fail("Cannot replay while recording");
        if (history == null) return CommandResult.fail("History manager required");
        final Macro macro;
        try { macro = get(name); }
        catch (IllegalArgumentException e) { return CommandResult.fail(e.getMessage()); }
        try {
            history.beginGroup();
        } catch (RuntimeException e) { return CommandResult.fail(e.getMessage()); }
        try {
            for (MacroAction action : macro.steps()) {
                CommandResult result = action.execute();
                if (result == null || !result.success()) {
                    history.rollbackGroup();
                    return CommandResult.fail("Macro failed: " + (result == null ? "no result" : result.message()));
                }
            }
            history.commitGroup();
            return CommandResult.ok("Macro replayed: " + name);
        } catch (RuntimeException e) {
            history.rollbackGroup();
            return CommandResult.fail("Macro failed: " + e.getMessage());
        }
    }
}
