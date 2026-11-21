package editor.model;

import java.util.Objects;

/*
Value object identifying a single characters offset within a nodes text field
param nodeID identifier of the target {link DocumentNode}
param field which text field ({link TextField#BODY} or {link TextField#SECTION_TITLE})
param offset zero-based character offset
*/
public record TextLocation(String nodeId, TextField field, int offset){
  /*
  validates non null components
  */

  public TextLocation{
    Objects.requireNonNull(nodeId, "Node ID must not be null.");
    Objects.requireNonNull(field, "TextField must not be null.");
  }
}
