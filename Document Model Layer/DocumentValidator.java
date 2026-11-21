package editor.model;

import java.util.Objects;


//Central validation helper thats invoked before commands mutatue a {@link Document}
//it will ensure invalid locations, ranges, or merge targets fail cleanly without partial modifications

public class DocumentValidator{

  //Creates a new {@code DocumentValidator}
  public DocumentValidator(){
  }

  //verifies that {@code location} refers to an existing node/field and that
  //its offset lies within {@code [0, length()]}
  //@param location                    target text location
  //@param doc                         document to validate against
  //@throws IllegalArgumentException   if the location is invalid
public void checkLocation(TextLocation location, Document doc) {
        Objects.requireNonNull(location, "TextLocation must not be null.");
        Objects.requireNonNull(doc, "Document must not be null.");
        StyledText text = doc.resolveText(location.nodeId(), location.field());
        int offset = location.offset();
        if (offset < 0 || offset > text.length()) {
            throw new IllegalArgumentException(
                "Offset " + offset + " is out of bounds for length " + text.length() + "."
            );
        }
    }
  

//Verifies that {@code selection} refers to an existing node/field and defines a valid non empty 
//range {@code 0 <= start < end <= length()}
//@param selection         target text selection
//@param doc               document to validate against
//@throwsIllegalArgument   if the selection is invalid
public void checkSelection(TextSelection selection, Document doc) {
        Objects.requireNonNull(selection, "TextSelection must not be null.");
        Objects.requireNonNull(doc, "Document must not be null.");
        StyledText text = doc.resolveText(selection.nodeId(), selection.field());
        int start = selection.start();
        int end = selection.end();
        if (start < 0 || end > text.length() || start >= end) {
            throw new IllegalArgumentException(
                "Invalid selection [" + start + ", " + end + ") for length " + text.length() + "."
            );
        }
    } 



  //Varfies that {@code firstId} & {@code secondId} are both {@link Paragraph}
  //nodes in the same paraent {@link Section}, with {@code secondId} immediately
  //following {@code firstId}
  //@param firstId            ID of the first paragraph       
  //@param secondId           ID of the immediately following paragraph        
  //@param doc                document to validate against   
  //@throwsIllegalArgument    if the paragraphs is missingor not adjacent
  public void checkAdjacentParagraphs(String firstId, String secondId, Document doc) {
        Objects.requireNonNull(doc, "Document must not be null.");
        if (firstId == null || secondId == null) {
            throw new IllegalArgumentException("Paragraph IDs must not be null.");
        }
        DocumentNode first = doc.findNode(firstId);
        DocumentNode second = doc.findNode(secondId);
        if (!(first instanceof Paragraph)) {
            throw new IllegalArgumentException("Node '" + firstId + "' is not a paragraph.");
        }
        if (!(second instanceof Paragraph)) {
            throw new IllegalArgumentException("Node '" + secondId + "' is not a paragraph.");
        }
        Section firstParent = doc.findParentSection(firstId);
        Section secondParent = doc.findParentSection(secondId);
        if (firstParent == null || firstParent != secondParent) {
            throw new IllegalArgumentException(
                "Paragraphs '" + firstId + "' and '" + secondId + "' are not in the same section."
            );
        }
        int firstIdx = firstParent.indexOfChild(firstId);
        int secondIdx = firstParent.indexOfChild(secondId);
        if (secondIdx != firstIdx + 1) {
            throw new IllegalArgumentException(
                "Paragraph '" + secondId + "' does not immediately follow '" + firstId + "'."
            );
        }
    }
}





