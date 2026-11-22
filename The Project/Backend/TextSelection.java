package editor.model;

import java.util.Objects;

/**
 * Identifies a character range [start, end) within a node's text field.
 *
 * @param nodeId identifier of the target DocumentNode
 * @param field  which text field (BODY or SECTION_TITLE)
 * @param start  inclusive start index
 * @param end    exclusive end index
 */
public record TextSelection(String nodeId, TextField field, int start, int end){
    /**
     * Validates that nodeId and field are not null.
     */
    public TextSelection{
        Objects.requireNonNull(nodeId, "Node ID not be null.");
        Objects.requireNonNull(field, "TextField must not be null.");
    }

}
