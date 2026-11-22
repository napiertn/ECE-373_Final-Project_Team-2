package editor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Document Model layer covering structure, styling,
 * traversal, validation, snapshots, and change notifications.
 */
public class DocumentModelTest {

    private Document doc;
    private DocumentWalker walker;
    private DocumentValidator validator;

    /**
     * Simple test observer used to check that Document notifies listeners.
     */
    private static class CountingListener implements DocumentChangeListener {
        private int callCount = 0;

        @Override
        public void onDocumentChanged(Document changedDoc) {
            callCount++;
        }

        int getCallCount() {
            return callCount;
        }
    }

    @BeforeEach
    void setUp() {
        doc = new Document();
        walker = new DocumentWalker();
        validator = new DocumentValidator();
    }



    // 1. Document Structure & Arbitrary Nesting (Reqs 6.2.1 - 6.2.4)
    @Test
    void testBuildNestedDocumentAndLookupNodes() {
        Section intro = new Section("s1", "Introduction");
        Paragraph p1 = new Paragraph("p1", "First paragraph.");
        Section methods = new Section("s2", "Methods");
        Section setup = new Section("s3", "Setup");
        Section deepSub = new Section("s4", "Deep Hardware Details");
        Paragraph p2 = new Paragraph("p2", "Nested paragraph.");

        doc.addSection(0, intro);
        doc.addSection(1, methods);
        intro.addChild(0, p1);
        methods.addChild(0, setup);
        setup.addChild(0, deepSub);
        deepSub.addChild(0, p2);

        assertEquals(2, doc.getSections().size());
        assertEquals(intro, doc.findNode("s1"));
        assertEquals(p2, doc.findNode("p2"));
        assertNull(doc.findNode("does-not-exist"));

        // Parent lookups
        assertNull(doc.findParentSection("s1")); // top-level section has no parent section
        assertEquals(intro, doc.findParentSection("p1"));
        assertEquals(deepSub, doc.findParentSection("p2"));
        assertEquals(0, deepSub.indexOfChild("p2"));
    }

    @Test
    void testRemoveSectionAndChildNodes() {
        Section s1 = new Section("s1", "Intro");
        Paragraph p1 = new Paragraph("p1", "Hello");
        s1.addChild(0, p1);
        doc.addSection(0, s1);

        DocumentNode removedChild = s1.removeChild("p1");
        assertEquals(p1, removedChild);
        assertTrue(s1.getChildren().isEmpty());
        assertNull(s1.removeChild("missing"));

        Section removedSection = doc.removeSection("s1");
        assertEquals(s1, removedSection);
        assertTrue(doc.getSections().isEmpty());
    }

    @Test
    void testInvalidSectionAndChildIndexesThrow() {
        Section s1 = new Section("s1", "Intro");
        assertThrows(IllegalArgumentException.class, () -> doc.addSection(-1, s1));
        assertThrows(IllegalArgumentException.class, () -> doc.addSection(1, s1));

        doc.addSection(0, s1);
        Paragraph p1 = new Paragraph("p1", "Text");
        assertThrows(IllegalArgumentException.class, () -> s1.addChild(-1, p1));
        assertThrows(IllegalArgumentException.class, () -> s1.addChild(1, p1));
    }



    // 2. StyledText Editing & Boundaries (Reqs 6.3.1 - 6.3.3)
    @Test
    void testInsertDeleteAndSubText() {
        StyledText text = new StyledText("Hello World");

        // Insert in the middle
        text.insert(5, new StyledText(", Java"));
        assertEquals("Hello, Java World", text.plainCharacters());

        // Insert at start (boundary 0) and end (boundary length)
        text.insert(0, new StyledText(">> "));
        text.insert(text.length(), new StyledText("!"));
        assertEquals(">> Hello, Java World!", text.plainCharacters());

        // Delete returns the removed StyledText so Undo can restore it
        StyledText removed = text.delete(0, 3);
        assertEquals(">> ", removed.plainCharacters());
        assertEquals("Hello, Java World!", text.plainCharacters());

        // SubText extracts range without mutating original
        StyledText slice = text.subText(7, 11);
        assertEquals("Java", slice.plainCharacters());
        assertEquals("Hello, Java World!", text.plainCharacters());
    }

    @Test
    void testStyledTextOutOfBoundsThrows() {
        StyledText text = new StyledText("ABC");

        assertThrows(IllegalArgumentException.class, () -> text.insert(-1, new StyledText("X")));
        assertThrows(IllegalArgumentException.class, () -> text.insert(4, new StyledText("X")));
        assertThrows(IllegalArgumentException.class, () -> text.delete(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> text.delete(2, 1));
        assertThrows(IllegalArgumentException.class, () -> text.delete(0, 4));
    }



    // 3. Formatting, Boundary Crossing & Section Titles (Reqs 6.5.1 - 6.5.5)
    @Test
    void testFormattingCrossesBoundariesAndPreservesText() {
        StyledText text = new StyledText("123456789");

        // Bold on [0, 6) -> "123456"
        StylePatch makeBold = new StylePatch(true, null, null, null, null);
        text.applyStyle(0, 6, makeBold);

        // Italic on [3, 9) -> crosses the boundary at index 6
        StylePatch makeItalic = new StylePatch(null, true, null, null, null);
        text.applyStyle(3, 9, makeItalic);

        // Requirement 6.5.4: formatting must never change the actual text
        assertEquals("123456789", text.plainCharacters());

        // Should produce 3 distinct runs:
        // [0,3)="123" (bold), [3,6)="456" (bold+italic), [6,9)="789" (italic)
        List<TextRun> runs = text.getRuns();
        assertEquals(3, runs.size());

        assertEquals("123", runs.get(0).characters());
        assertTrue(runs.get(0).style().bold());
        assertFalse(runs.get(0).style().italic());

        assertEquals("456", runs.get(1).characters());
        assertTrue(runs.get(1).style().bold());
        assertTrue(runs.get(1).style().italic());

        assertEquals("789", runs.get(2).characters());
        assertFalse(runs.get(2).style().bold());
        assertTrue(runs.get(2).style().italic());
    }

    @Test
    void testAdjacentSameStyleRunsMergeBackTogether() {
        StyledText text = new StyledText("ABCDEF");
        StylePatch makeBold = new StylePatch(true, null, null, null, null);
        StylePatch unBold = new StylePatch(false, null, null, null, null);

        text.applyStyle(0, 3, makeBold);
        assertEquals(2, text.getRuns().size());

        // Removing bold should merge both halves back into 1 clean run
        text.applyStyle(0, 3, unBold);
        assertEquals(1, text.getRuns().size());
        assertEquals("ABCDEF", text.getRuns().get(0).characters());
        assertEquals(TextStyle.DEFAULT, text.getRuns().get(0).style());
    }

    @Test
    void testRestoreRunsForFormatUndo() {
        StyledText text = new StyledText("Design");
        List<TextRun> beforeFormatting = text.getRuns();

        StylePatch fancy = new StylePatch(true, true, "Georgia", 18, "blue");
        text.applyStyle(0, 6, fancy);
        assertEquals("Georgia", text.getRuns().get(0).style().fontName());
        assertEquals(18, text.getRuns().get(0).style().fontSize());
        assertEquals("blue", text.getRuns().get(0).style().color());

        // Simulate FormatTextCommand.undo()
        text.restoreRuns(beforeFormatting);
        assertEquals(TextStyle.DEFAULT, text.getRuns().get(0).style());
    }

    @Test
    void testFormatSectionTitleViaResolveText() {
        Section intro = new Section("s1", "Introduction");
        doc.addSection(0, intro);

        StyledText title = doc.resolveText("s1", TextField.SECTION_TITLE);
        StylePatch biggerTitle = new StylePatch(true, null, null, 20, null);
        title.applyStyle(0, title.length(), biggerTitle);

        assertEquals("Introduction", intro.getTitle().plainCharacters());
        assertEquals(20, intro.getTitle().getRuns().get(0).style().fontSize());
        assertTrue(intro.getTitle().getRuns().get(0).style().bold());
    }

    @Test
    void testInvalidTextStyleAndStylePatchValuesThrow() {
        assertThrows(IllegalArgumentException.class, () -> new TextStyle(false, false, "", 12, "black"));
        assertThrows(IllegalArgumentException.class, () -> new TextStyle(false, false, "Arial", 0, "black"));
        assertThrows(IllegalArgumentException.class, () -> new TextStyle(false, false, "Arial", 12, "   "));

        assertThrows(IllegalArgumentException.class, () -> new StylePatch(null, null, "", null, null));
        assertThrows(IllegalArgumentException.class, () -> new StylePatch(null, null, null, -5, null));
        assertThrows(IllegalArgumentException.class, () -> new StylePatch(null, null, null, null, ""));
    }

    // 4. DocumentWalker Traversal Order (Req 6.2.3)
     @Test
    void testDepthFirstWalkWholeDocumentAndSubtree() {
        Section s1 = new Section("s1", "Intro");
        Paragraph p1 = new Paragraph("p1", "A");
        Section s2 = new Section("s2", "Body");
        Section s3 = new Section("s3", "Sub");
        Paragraph p2 = new Paragraph("p2", "B");
        Paragraph p3 = new Paragraph("p3", "C");

        doc.addSection(0, s1);
        s1.addChild(0, p1);
        doc.addSection(1, s2);
        s2.addChild(0, s3);
        s3.addChild(0, p2);
        s2.addChild(1, p3);

        List<String> fullOrder = new ArrayList<>();
        for (DocumentNode node : walker.depthFirst(doc)) {
            fullOrder.add(node.getId());
        }
        assertEquals(List.of("s1", "p1", "s2", "s3", "p2", "p3"), fullOrder);

        List<String> subOrder = new ArrayList<>();
        for (DocumentNode node : walker.depthFirst(s2)) {
            subOrder.add(node.getId());
        }
        assertEquals(List.of("s2", "s3", "p2", "p3"), subOrder);
    }



    // 5. DocumentValidator Pre-Mutation Checks (Reqs 6.1.4, 6.1.5, 6.3.6)
     @Test
    void testValidatorChecksLocationsAndSelections() {
        Section s1 = new Section("s1", "Title");
        Paragraph p1 = new Paragraph("p1", "Hello");
        s1.addChild(0, p1);
        doc.addSection(0, s1);

        // Valid boundary locations: 0 and length (5)
        validator.checkLocation(new TextLocation("p1", TextField.BODY, 0), doc);
        validator.checkLocation(new TextLocation("p1", TextField.BODY, 5), doc);
        validator.checkLocation(new TextLocation("s1", TextField.SECTION_TITLE, 5), doc);

        // Invalid locations
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkLocation(new TextLocation("p1", TextField.BODY, -1), doc)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkLocation(new TextLocation("p1", TextField.BODY, 6), doc)
        );
        // Wrong field for node type (BODY on a Section)
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkLocation(new TextLocation("s1", TextField.BODY, 0), doc)
        );

        // Valid selection vs invalid selections (empty, inverted, out of bounds)
        validator.checkSelection(new TextSelection("p1", TextField.BODY, 0, 5), doc);
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkSelection(new TextSelection("p1", TextField.BODY, 2, 2), doc)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkSelection(new TextSelection("p1", TextField.BODY, 4, 2), doc)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkSelection(new TextSelection("p1", TextField.BODY, 0, 6), doc)
        );
    }

    @Test
    void testValidatorChecksAdjacentParagraphs() {
        Section s1 = new Section("s1", "S1");
        Section s2 = new Section("s2", "S2");
        Paragraph p1 = new Paragraph("p1", "One");
        Paragraph p2 = new Paragraph("p2", "Two");
        Paragraph p3 = new Paragraph("p3", "Three");
        Paragraph p4 = new Paragraph("p4", "Other section");

        s1.addChild(0, p1);
        s1.addChild(1, p2);
        s1.addChild(2, p3);
        s2.addChild(0, p4);
        doc.addSection(0, s1);
        doc.addSection(1, s2);

        // Adjacent in same section -> valid
        validator.checkAdjacentParagraphs("p1", "p2", doc);

        // Not immediately following -> invalid
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkAdjacentParagraphs("p1", "p3", doc)
        );
        // Wrong order -> invalid
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkAdjacentParagraphs("p2", "p1", doc)
        );
        // Different sections -> invalid
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkAdjacentParagraphs("p3", "p4", doc)
        );
        // Section ID instead of paragraph ID -> invalid
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.checkAdjacentParagraphs("s1", "p1", doc)
        );
    }




    // 6. DocumentSnapshot Deep Copy & Restore (Reqs 6.4.5, 6.4.6)
    @Test
    void testSnapshotDeepCopyAndRestore() {
        Section s1 = new Section("s1", "Intro");
        Paragraph p1 = new Paragraph("p1", "Original text");
        s1.addChild(0, p1);
        doc.addSection(0, s1);

        DocumentSnapshot checkpoint = doc.createSnapshot();

        // Heavily modify the live document after saving the checkpoint
        p1.getBody().insert(0, new StyledText("EDITED: "));
        p1.getBody().applyStyle(0, 6, new StylePatch(true, null, null, null, "red"));
        doc.addSection(1, new Section("s2", "Extra Section"));

        // Restore the checkpoint
        doc.restore(checkpoint);

        assertEquals(1, doc.getSections().size());
        assertNull(doc.findNode("s2"));

        Paragraph restoredP1 = (Paragraph) doc.findNode("p1");
        assertNotNull(restoredP1);
        assertEquals("Original text", restoredP1.getBody().plainCharacters());
        assertEquals(TextStyle.DEFAULT, restoredP1.getBody().getRuns().get(0).style());

        // Editing after restore must not corrupt the saved snapshot if restored again
        restoredP1.getBody().insert(0, new StyledText("Second edit "));
        doc.restore(checkpoint);
        Paragraph restoredAgain = (Paragraph) doc.findNode("p1");
        assertEquals("Original text", restoredAgain.getBody().plainCharacters());
    }



    // 7. Observer Notifications (Reqs 6.7.2, 6.7.3)
    @Test
    void testSubscribePublishAndUnsubscribe() {
        CountingListener listener = new CountingListener();
        doc.subscribe(listener);
        doc.subscribe(listener); // duplicate subscribe should not double-notify

        doc.publishChanged();
        assertEquals(1, listener.getCallCount());

        doc.publishChanged();
        assertEquals(2, listener.getCallCount());

        doc.unsubscribe(listener);
        doc.publishChanged();
        assertEquals(2, listener.getCallCount());
    }
}
