package editor.model;

import java.util.ArrayList
  import java.util.List

  //Helper class that walks through a document or a single section in top to bottom order
  // Used by search/statistics/renderers
  public class DocumentWalker{


  //Creates a new DocumentWalker
  public DocumentWalker(){
  }

//Returns every node in the docuemnt in top to bottom depth first order
//@param document the document to walk through
//@return ordered iterable of all DcoumentNode objects in the document
public Iterable<DocumentNode> depthFirst(document document){
  if (document == null){
    throw new IllegalArgumentException("Document cannot be null");
  }
  List<DocumentNode> visited = new ArrayList<>();
  for (Section section : document.getSections()) {
    collectDepthFirst(section, visited);
  }
  return visited;
}


//Returns a section and all its nested children in depth first order
//@param section the starting section
//@return ordered iterable of DocumentNode Objects in that section
public Iterable<DocumentNode> depthFirst(Section section) {
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null.");
        }
        List<DocumentNode> visited = new ArrayList<>();
        collectDepthFirst(section, visited);
        return visited;
    }

    private void collectDepthFirst(DocumentNode node, List<DocumentNode> visited) {
        visited.add(node);
        if (node instanceof Section) {
            Section section = (Section) node;
            for (DocumentNode child : section.getChildren()) {
                collectDepthFirst(child, visited);
            }
        }
    }
}
