package editor.model;

import java.util.ArrayList;
import java.util.List;

/**
Top level Document model that hold the documents sections/notifies lsteners when the document changes and creates/restores snapshots.
  */
public class Document{
    private final List<Section> sections;
    private final List<DocumentChangeListener> listeners;

    /**Creates a empty document with no sections or listeners*/
    public Document(){
        this.sections = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }


    /** sereches the entire doc tree for a node with given ID
        @param id the Id to search for
        @return the matching DocumentNode, or null if not found
    */
    public DocumentNode findNode(String id) {
        if (id == null) {
            return null;
        }
        for (Section section : sections) {
            DocumentNode found = findRecursive(section, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

/**Finds the immediate parent section that contains the given child ID.
//returns null if the ID is a top-level section or does not exist
//@param childId the ID of the child node
//@return the parent Section, or null if not found*/
    public Section findParentSection(String childId){
        if (childId == null) {
            return null;
        }
        for (Section section : sections) {
            Section parent = findParentRecursive(section, childId);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    /**returns the StyledText for a specific node and field (a pragraphs body or a sections title)
    //@param nodeId the target node ID
    //@param field whether to get the paragraph BODY or SECTION_TITLE
    //@return the matching StyledText object*/
    public StyledText resolveText(String nodeId, TextField field){
        if (field == null) {
            throw new IllegalArgumentException("TextField cannot be null.");
        }
        DocumentNode node = findNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }

        if (field == TextField.BODY) {
            if (node instanceof Paragraph paragraph) {
                return paragraph.getBody();
            }
            throw new IllegalArgumentException("Node '" + nodeId + "' is not a paragraph.");
        }

        if (node instanceof Section section) {
            return section.getTitle();
        }
        throw new IllegalArgumentException("Node '" + nodeId + "' is not a section.");
    }


    /**Inserts a top level section at given position
    //@param index    position to insert at (from 0 to sections size)
    //@param section  the top level section to add*/
    public void addSection(int index, Section section){
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null.");
        }
        if (index < 0 || index > sections.size()) {
            throw new IllegalArgumentException(
                    "Section index " + index + " is out of bounds for size " + sections.size()
            );
        }
        sections.add(index, section);
    }

    /**removes and returns the top level section matching the given ID
    //@param id the ID of the top level section to remove
    //@return the removed Section, or null if not found*/
    public Section removeSection(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId().equals(id)) {
                return sections.remove(i);
            }
        }
        return null;
    }


    /**Registers a listener so it gets notified when the document changes.
    //@param listener the observer to subscribe*/
    public void subscribe(DocumentChangeListener listener){
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**Removes a previously subscribed listener
    //@param listner the observer to unsubscribe*/
    public void unsubscribe(DocumentChangeListener listener){
        listeners.remove(listener);
    }

    /**Alerts all subscribed listeners that document has been modified.*/
    public void publishChanged(){
/** Loop over a copy in case a listener unsubscribes during notification*/
        List<DocumentChangeListener> currentListeners = new ArrayList<>(listeners);
        for (DocumentChangeListener listener : currentListeners) {
            listener.onDocumentChanged(this);
        }
    }

    /**Creates a deep copy snapshot of the current document state for checkpoints
    //@return a new DocumentSnapshot*/
    public DocumentSnapshot createSnapshot(){
        return new DocumentSnapshot(sections);
    }

    /**Resotres the documents sections from a previously saved snapshot
    //@param snapshot the snapshot to restore*/
    public void restore(DocumentSnapshot snapshot){
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot cannot be null.");
        }
        sections.clear();
        sections.addAll(snapshot.getSavedSectionsCopy());
    }


    /**Returns a copy of the list of top level sections in order
    //@return list of top level sections*/
    public List<Section> getSections(){
        return new ArrayList<>(sections);
    }

    private DocumentNode findRecursive(DocumentNode current, String id){
        if (current.getId().equals(id)) {
            return current;
        }
        if (current instanceof Section section) {
            for (DocumentNode child : section.getChildren()) {
                DocumentNode found = findRecursive(child, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }


    private Section findParentRecursive(Section current, String childId) {
        for (DocumentNode child : current.getChildren()) {
            if (child.getId().equals(childId)) {
                return current;
            }
            if (child instanceof Section subSection) {
                Section found = findParentRecursive(subSection, childId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
