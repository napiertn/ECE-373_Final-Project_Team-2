// Holds Current Renderer, Line Breaker, and Width,

import editor.model.Document;

public class PresentationService {
    private DocumentRenderer renderer;   // default set in constructor
    private LineBreaker breaker;         // default set in constructor
    private int width = 80;

    /* @return true if the view exists; otherwise keeps the old view. */
    public boolean selectView(String name) { /* TODO */ return false; }

    public boolean setLineBreaker(String name) { /* TODO */ return false; }

    public boolean setWidth(int width) { /* TODO */ return false; }

    public String render(Document doc) { /* TODO */ return ""; }
}