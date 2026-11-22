package editor.model;

import java.util.Objects;

/*
value object identifying a half open character range {Code [start, end)}
withing a nodes text field
*/
public record TextSelection(String nodeId, TextField field, int start, int end){
//validates non-null components
public TextSelection{
  Objects.requireNonNull(nodeId, "Node ID not be null.");
  Objects.requireNonNull(field, "TextField must not be null.");
}
  
}

