package engine;

import java.util.HashMap;
import java.util.Map;
import model.DocumentSnapshot;

/** Named, immutable full-document snapshots. */
public final class CheckpointManager {
    private final Map<String, DocumentSnapshot> saved = new HashMap<>();

    /** Creates or overwrites a named checkpoint. */
    public void save(String name, DocumentSnapshot snapshot) {
        requireName(name);
        if (snapshot == null) throw new IllegalArgumentException("Snapshot required");
        saved.put(name, new DocumentSnapshot(snapshot.copySections()));
    }

    /** Returns an independent snapshot saved under the given name. */
    public DocumentSnapshot get(String name) {
        requireName(name);
        DocumentSnapshot snapshot = saved.get(name);
        if (snapshot == null) throw new IllegalArgumentException("Unknown checkpoint: " + name);
        return new DocumentSnapshot(snapshot.copySections());
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Checkpoint name required");
    }
}
