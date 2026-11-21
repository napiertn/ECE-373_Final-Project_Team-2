package editor.model;

import java.util.ArrayList;
import java.util.List;

//Represents a section in the document, a section has a formattable title and
//can hold paragraphs as well as nested subsections
public class Section extends DocumentNode {
  private final StyledText title;
  private final List<DocumentNode> children;

//creates a section with a StyledText title.
//@param id    unique sectoin ID
//@param title formatted section title
public Section(String id, StyledText title) {
super(id);
  if(title ==null){
    throw new IllegalArgumentException("Section title cannot be null.");
  }
  this.title = title;
  this.children = new ArrayList<>();
}

  //Creates a section from a plain text title string
  //@param id        unique section Id
  //@param titleText plain title text
  public Section(String id, String titleText){
this(id, new StyledText(titleText));
  }

  //Inserts a child node (a Paragraph or another section) at the given position.
  //@param index position to insert at (from 0 to children size)
  //@param node the child node to add
  public void addChild(int index, DocumentNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Child node cannot be null.");
        }
        if (index < 0 || index > children.size()) {
            throw new IllegalArgumentException(
                "Child index " + index + " is out of bounds for size " + children.size()
            );
        }
        children.add(index, node);
    }


  //Removes and returns the direc child that matches the given ID.
  //@param id the ID of the child to remove
  //@return the removed child, or null if no direct child has that ID
  public DocumentNode removeChild(String id){
    if(id == null){ 
      return null;
    }
    for(int i = 0; i < children.size(); i++) {
if (children.get(i).getId().equals(id)){
  return children.remove(i);
}
    }
    return null;
  }

//returns the formattable title of this section
  //@retrun the sections StyledText title
  public StyledText getTitle(){
return title;
  }


  //returns a copy of this sections direct children list so callers cant modify the interanl list directly
  //@return list of direct child nodes
  public List<DocumentNode> getChildren(){
return new ArrayList<>(children);
  }


//finds the position of a direct child by its ID.
  //@param childId the ID to look for
  //@return the childs index, or -1 if not found
  public int indexOfChild(String childId){
if (childId == null) {
            return -1;
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId().equals(childId)) {
                return i;
            }
        }
        return -1;
  }

//creates a deep copu of this section (the title and all nested children)
  //@return an independent copy of this Section

  @Override
public Section copy() {
        Section clone = new Section(getId(), title.copy());
        for (DocumentNode child : children) {
            clone.children.add(child.copy());
        }
        return clone;
    }
  
}
