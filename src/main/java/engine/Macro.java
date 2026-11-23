package engine;

import java.util.List;

/** Immutable named sequence of replayable actions. */
public record Macro(String name, List<MacroAction> steps) {
    public Macro { steps = List.copyOf(steps); }
}
