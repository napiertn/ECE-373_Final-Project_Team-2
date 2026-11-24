package presentation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import model.*;

/** Independent strategy and view behavior, including a 100,000-character document. */
class PresentationTest {
    @Test void selectableLineBreakingStrategiesProduceDifferentLines() {
        String source = "a xxxx a xxxxxx";
        var greedy = new GreedyLineBreaker().wrap(source, 7);
        var balanced = new BalancedLineBreaker().wrap(source, 7);
        assertNotEquals(greedy, balanced);
        assertTrue(greedy.stream().allMatch(s -> s.length() <= 7));
        assertTrue(balanced.stream().allMatch(s -> s.length() <= 7));
        assertEquals("a xxxx", greedy.get(0));
        assertEquals("a", balanced.get(0));
    }
    @Test void viewsAreReadOnlyAndShowTitleFormatting() {
        Document doc = new Document();
        doc.addRootSection(0, new Section("s", new StyledText("Overview")));
        doc.addChild("s", 0, new Paragraph("p", new StyledText("hello world")));
        ((Section)doc.findNode("s")).getTitle().applyStyle(0, 8, StylePatch.bold(true));
        ((Paragraph)doc.findNode("p")).getBody().applyStyle(0, 5, StylePatch.italic(true));
        var present = new PresentationService();
        String before = ((Paragraph)doc.findNode("p")).getBody().plainCharacters();
        assertTrue(present.render(doc).contains(before));
        present.selectView("markup");
        assertTrue(present.render(doc).contains("# **Overview**"));
        assertTrue(present.render(doc).contains("_hello_"));
        assertEquals(before, ((Paragraph)doc.findNode("p")).getBody().plainCharacters());
    }
    @Test void largeDocumentCanRenderAndSearchWithoutFramework() {
        Document doc = new Document();
        doc.addRootSection(0, new Section("s", new StyledText("Load test")));
        String big = "alpha beta ".repeat(10_000);
        doc.addChild("s", 0, new Paragraph("p", new StyledText(big)));
        assertTrue(big.length() >= 100_000);
        PresentationService service = new PresentationService();
        assertTrue(service.render(doc).length() >= 100_000);
        service.setLineBreaker("balanced");
        assertTrue(service.render(doc).contains("alpha"));
        assertEquals(10_000, new engine.SearchService(doc).find("alpha", engine.SearchMode.SUBSTRING).size());
    }
    @Test void canRegisterNewStrategiesWithoutEditingDocumentModel() {
        PresentationService present = new PresentationService();
        present.registerBreaker(new LineBreaker() {
            public java.util.List<String> wrap(String text, int width) { return java.util.List.of("custom: " + text); }
            public String name() { return "custom"; }
        });
        present.setLineBreaker("custom");
        Document d = new Document();
        d.addRootSection(0, new Section("s", new StyledText("Hello")));
        assertTrue(present.render(d).contains("custom: Hello"));
    }
    @Test void protectsAgainstInvalidWidths() {
        PresentationService present = new PresentationService();
        assertThrows(IllegalArgumentException.class, () -> present.setWidth(0));
        assertThrows(IllegalArgumentException.class, () -> new GreedyLineBreaker().wrap("test", -1));
        assertThrows(IllegalArgumentException.class, () -> present.selectView("missing"));
    }
}
