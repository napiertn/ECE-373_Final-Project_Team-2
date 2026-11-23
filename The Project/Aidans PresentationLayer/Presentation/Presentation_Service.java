// Holds Current Renderer, Line Breaker, and Width,

package presentation;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import model.Document;

public class PresentationService {

    //Initialization stuff
    // Width used until the user sets one.
    public static final int DEFAULT_WIDTH = 80;
    // Smallest accepted width.
    public static final int MIN_WIDTH = 1;


    private final Map<String, DocumentRenderer> renderers = new LinkedHashMap<>();
    private final Map<String, LineBreaker> breakers = new LinkedHashMap<>();

    private DocumentRenderer renderer;
    private LineBreaker breaker;

    private int width = DEFAULT_WIDTH;


    // Now into constructor This Registers the built in views and breakers,
    public PresentationService() {
        //default Plain,Greedy,and Width of 80 as above
        registerRenderer(new PlainTextRenderer());
        registerRenderer(new MarkedUpRenderer());
        registerBreaker(new GreedyLineBreaker());
        registerBreaker(new BalancedLineBreaker());
        renderer = renderers.get("plain");
        breaker = breakers.get("greedy");
    }



//r is where the renderer registers its name same with b below but that is with a Breaker instead of Renderer
    public void registerRenderer(DocumentRenderer r) {
        renderers.put(r.name().toLowerCase(Locale.ROOT), r);
    }
        //Locale.root is essentially Javas Standard Languages, ie if i had it in Turkish it woult turn a I into an i without the dot on top
    //because different languages, but this is standard

    public void registerBreaker(LineBreaker b) {
        breakers.put(b.name().toLowerCase(Locale.ROOT), b);
    }

        // this is where the view can be switched, the view is
    public void selectView(String name) {
        DocumentRenderer r = name == null ? null : renderers.get(name.trim().toLowerCase(Locale.ROOT));
        if (r == null) throw new IllegalArgumentException("Unknown view '" + name + "'. Available: " + availableViews());
        renderer = r;
    }

    // this is to set the Break in Lines ie when a word reaches the width of the page
    public void setLineBreaker(String name) {
        LineBreaker b = name == null ? null : breakers.get(name.trim().toLowerCase(Locale.ROOT));
        if (b == null) throw new IllegalArgumentException("Unknown line breaker '" + name + "'. Available: " + availableBreakers());
        breaker = b;
    }

    // The Width of the page and how to set it
    public void setWidth(int newWidth) {
        if (newWidth < MIN_WIDTH) throw new IllegalArgumentException("Width must be at least " + MIN_WIDTH + ", got " + newWidth);
        width = newWidth;
    }


// this is what calls the document to be rendered, with a check if the document is null ##################################
    public String render(Document doc) {
        if (doc == null) throw new IllegalArgumentException("Document is required");
        return renderer.render(doc, width, breaker);
    }


    //RETURNS

    public Set<String> availableViews() {
        return renderers.keySet();
    }

    public Set<String> availableBreakers() {
        return breakers.keySet();
    }

    public int getWidth() {
        return width;
    }

    public String currentView() {
        return renderer.name();
    }

    public String currentBreaker() {
        return breaker.name();
    }
}