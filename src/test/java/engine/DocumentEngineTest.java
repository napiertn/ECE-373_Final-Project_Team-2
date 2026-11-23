package engine;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import edit.*;
import model.*;

/** Headless editing, search and history tests without a text command interface. */
class DocumentEngineTest {
    private final EditorEngine engine = new EditorEngine();
    private void add(String id, String text) {
        assertTrue(engine.apply(new AddNodeCommand(null, engine.document().getSections().size(),
                new Section(id, new StyledText(text)))).success());
    }
    @Test void observerIsNotCoupledToEditingCommands() {
        add("s", "Intro");
        assertTrue(engine.apply(new AddNodeCommand("s", 0, new Paragraph("p", new StyledText("hello world")))).success());
        assertEquals(1, engine.statistics().paragraphs());
        assertEquals(3, engine.statistics().words()); // Intro + hello + world
    }
    @Test void rangeValidationAndCrossParagraphMove() {
        add("s", "S");
        assertTrue(engine.apply(new AddNodeCommand("s", 0, new Paragraph("a", new StyledText("12345")))).success());
        assertTrue(engine.apply(new AddNodeCommand("s", 1, new Paragraph("b", new StyledText("ABCDE")))).success());
        assertTrue(engine.apply(new MoveTextCommand(new TextSelection("a", TextField.BODY, 1, 4),
                new TextLocation("b", TextField.BODY, 2))).success());
        assertEquals("15", ((Paragraph)engine.document().findNode("a")).getBody().plainCharacters());
        assertEquals("AB234CDE", ((Paragraph)engine.document().findNode("b")).getBody().plainCharacters());
        engine.undo();
        assertEquals("12345", ((Paragraph)engine.document().findNode("a")).getBody().plainCharacters());
    }
    @Test void failedCompositeRollsBackAllChanges() {
        add("s", "T");
        assertTrue(engine.apply(new AddNodeCommand("s", 0, new Paragraph("p", new StyledText("abc")))).success());
        CompositeEditCommand c = new CompositeEditCommand();
        c.add(new InsertTextCommand(new TextLocation("p", TextField.BODY, 3), new StyledText("ok")));
        c.add(new InsertTextCommand(new TextLocation("p", TextField.BODY, 999), new StyledText("bad")));
        int depth = engine.history().undoDepth();
        assertFalse(engine.apply(c).success());
        assertEquals("abc", ((Paragraph)engine.document().findNode("p")).getBody().plainCharacters());
        assertEquals(depth, engine.history().undoDepth());
    }
    @Test void searchAcrossTitlesAndDeepNodes() {
        add("s", "cat introduction");
        assertTrue(engine.apply(new AddNodeCommand("s", 0, new Section("nested", new StyledText("cat")))).success());
        assertTrue(engine.apply(new AddNodeCommand("nested", 0, new Paragraph("p", new StyledText("cat caterpillar cat")))).success());
        assertEquals(4, engine.find("cat", SearchMode.WHOLE_WORD).size());
        assertEquals(5, engine.find("cat", SearchMode.SUBSTRING).size());
        assertTrue(engine.replaceAll("cat", "dog", SearchMode.WHOLE_WORD).success());
        assertEquals(0, engine.find("cat", SearchMode.WHOLE_WORD).size());
        engine.undo(); assertEquals(4, engine.find("cat", SearchMode.WHOLE_WORD).size());
    }
    @Test void multiLevelHistoryIsNotFixedAtTen() {
        add("s", "Title");
        assertTrue(engine.apply(new AddNodeCommand("s", 0, new Paragraph("p", new StyledText("")))).success());
        for (int i=0; i<55; i++) assertTrue(engine.apply(new InsertTextCommand(
                new TextLocation("p", TextField.BODY, i), new StyledText("x"))).success());
        assertTrue(engine.history().undoDepth() >= 57);
        for (int i=0; i<55; i++) assertTrue(engine.undo().success());
        assertEquals("", ((Paragraph)engine.document().findNode("p")).getBody().plainCharacters());
        for (int i=0; i<55; i++) assertTrue(engine.redo().success());
        assertEquals(55, ((Paragraph)engine.document().findNode("p")).getBody().length());
    }
}
