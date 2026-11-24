package presentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import model.Document;

/** Holds switchable view strategies, line breakers, and current output width. */
public final class PresentationService {
    /** Initial line width in columns. */
    public static final int DEFAULT_WIDTH = 80;
    private final Map<String, DocumentRenderer> renderers = new LinkedHashMap<>();
    private final Map<String, LineBreaker> breakers = new LinkedHashMap<>();
    private DocumentRenderer renderer;
    private LineBreaker breaker;
    private int width = DEFAULT_WIDTH;
    /** Registers the two standard views and two standard line-breaking algorithms. */
    public PresentationService() {
        registerRenderer(new PlainTextRenderer());
        registerRenderer(new MarkedUpRenderer());
        registerBreaker(new GreedyLineBreaker());
        registerBreaker(new BalancedLineBreaker());
        renderer = renderers.get("plain");
        breaker = breakers.get("greedy");
    }
    /** Adds another rendering style without altering any other component. */
    public void registerRenderer(DocumentRenderer r) {
        if (r == null || r.name() == null) throw new IllegalArgumentException("Renderer required");
        renderers.put(r.name().toLowerCase(Locale.ROOT), r);
    }
    /** Adds another line-breaking method without altering model or views. */
    public void registerBreaker(LineBreaker b) {
        if (b == null || b.name() == null) throw new IllegalArgumentException("Breaker required");
        breakers.put(b.name().toLowerCase(Locale.ROOT), b);
    }
    /** Chooses a registered view. */
    public void selectView(String name) {
        DocumentRenderer found = renderers.get(key(name));
        if (found == null) throw new IllegalArgumentException("Unknown view: " + name + " (" + availableViews() + ")");
        renderer = found;
    }
    /** Chooses a registered line-breaker. */
    public void setLineBreaker(String name) {
        LineBreaker found = breakers.get(key(name));
        if (found == null) throw new IllegalArgumentException("Unknown breaker: " + name + " (" + availableBreakers() + ")");
        breaker = found;
    }
    /** Changes the maximum line width. */
    public void setWidth(int width) {
        if (width < 1) throw new IllegalArgumentException("Width must be positive");
        this.width = width;
    }
    /** Produces output without changing the document. */
    public String render(Document doc) { return renderer.render(doc, width, breaker); }
    /** Names of available views. */
    public Set<String> availableViews() { return Set.copyOf(renderers.keySet()); }
    /** Names of available line-breaking methods. */
    public Set<String> availableBreakers() { return Set.copyOf(breakers.keySet()); }
    /** Returns the current width. */
    public int getWidth() { return width; }
    /** Returns the currently selected view. */
    public String currentView() { return renderer.name(); }
    /** Returns the currently selected line-breaker. */
    public String currentBreaker() { return breaker.name(); }
    private static String key(String text) { return text == null ? "" : text.trim().toLowerCase(Locale.ROOT); }
}
