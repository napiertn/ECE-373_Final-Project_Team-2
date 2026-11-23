package model;

import java.util.ArrayList;
import java.util.List;

/** Defensive copy of the full document tree for undo/checkpoint restoration. */
public final class DocumentSnapshot {
    private final List<Section> savedSections;

    /** Stores a copy of the given roots. */
    public DocumentSnapshot(List<Section> roots) {
        if (roots == null) throw new IllegalArgumentException("Sections required");
        savedSections = new ArrayList<>();
        for (Section section : roots) savedSections.add(section.copy());
    }

    /** Returns a fresh deep copy of stored roots. */
    public List<Section> copySections() {
        List<Section> copy = new ArrayList<>();
        for (Section section : savedSections) copy.add(section.copy());
        return copy;
    }
}
