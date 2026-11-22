package editor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable memento capturing a complete deep copy of {@link Document} top level sections and all nested content
 * Internal state is package private so external caretakers (Like {@code CheckpointManager}) cannot mutate it
 */
public final class DocumentSnapshot{
    private final List<Section> savedSections;

    DocumentSnapshot(List<Section> sections) {
        this.savedSections = deepCopySections(sections);
    }

    List<Section> getSavedSectionsCopy() {
        return deepCopySections(savedSections);
    }

    private static List<Section> deepCopySections(List<Section> source) {
        List<Section> copy = new ArrayList<>(source.size());
        for (Section section : source) {
            copy.add(section.copy());
        }
        return copy;
    }
}
