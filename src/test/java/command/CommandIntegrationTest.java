package command;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.util.List;
import model.*;

/** End-to-end command-system scenarios executed through the same parser as the user interface. */
class CommandIntegrationTest {
    private final CommandProcessor app = new CommandProcessor();
    private String ok(String command) {
        CommandResult r = app.processLine(command);
        assertTrue(r.success(), command + " -> " + r.message());
        return r.message();
    }
    @Test void createsArbitrarilyNestedSectionsAndParagraphs() {
        ok("section a Alpha"); ok("subsection a b Beta"); ok("subsection b c Gamma");
        ok("paragraph c p1 Hello world");
        assertTrue(ok("outline").contains("[section] c"));
        assertTrue(ok("render").contains("Hello world"));
        assertTrue(ok("status").contains("Paragraphs: 1"));
        assertInstanceOf(Paragraph.class, app.dispatcher().engine().document().findNode("p1"));
    }
    @Test void everyEditWorksAndCanUndo() {
        ok("section a Alpha"); ok("paragraph a p1 123456");
        ok("insert p1 3 Z"); assertTrue(ok("render").contains("123Z456"));
        ok("delete p1 3 4"); assertTrue(ok("render").contains("123456"));
        ok("replace p1 0 2 XY"); assertTrue(ok("render").contains("XY3456"));
        ok("move p1 0 2 6"); assertTrue(ok("render").contains("3456XY"));
        ok("undo"); assertTrue(ok("render").contains("XY3456"));
        ok("redo"); assertTrue(ok("render").contains("3456XY"));
        ok("split p1 4 p2"); assertTrue(ok("status").contains("Paragraphs: 2"));
        ok("merge p1 p2"); assertTrue(ok("status").contains("Paragraphs: 1"));
    }
    @Test void invalidEditNeverMutatesTheDocumentOrHistory() {
        ok("section a Alpha"); ok("paragraph a p1 hello");
        String before = ok("render");
        int depth = app.dispatcher().engine().history().undoDepth();
        assertFalse(app.processLine("insert p1 100 invalid").success());
        assertFalse(app.processLine("split p1 1 p1").success());
        assertFalse(app.processLine("paragraph a p1 duplicate").success());
        assertFalse(app.processLine("merge p1 other").success());
        assertEquals(before, ok("render"));
        assertEquals(depth, app.dispatcher().engine().history().undoDepth());
    }
    @Test void formatsCrossRunBoundariesAndSectionTitles() {
        ok("section a Heading"); ok("paragraph a p1 alpha beta gamma");
        ok("format p1 0 5 bold=true"); ok("format p1 3 12 italic=true color=red");
        ok("titleformat a 0 7 bold=true fontsize=18");
        String before = ((Paragraph)app.dispatcher().engine().document().findNode("p1")).getBody().plainCharacters();
        String marked = ok("view markup");
        assertTrue(ok("render").contains("**"));
        assertTrue(ok("render").contains("size=18"));
        assertEquals(before, ((Paragraph)app.dispatcher().engine().document().findNode("p1")).getBody().plainCharacters());
        ok("undo"); assertFalse(ok("render").contains("size=18"));
    }
    @Test void replaceAllAndUndoAreAtomic() {
        ok("section a Alpha"); ok("paragraph a p1 cat cat cat");
        int depth = app.dispatcher().engine().history().undoDepth();
        ok("replaceall whole cat | dog");
        assertEquals(depth + 1, app.dispatcher().engine().history().undoDepth());
        assertTrue(ok("render").contains("dog dog dog"));
        ok("undo"); assertTrue(ok("render").contains("cat cat cat"));
        ok("redo"); assertTrue(ok("render").contains("dog dog dog"));
    }
    @Test void macroReplaysWithOneUndoAndRecordsPresentationCommands() {
        ok("section a Title"); ok("paragraph a p1 abc def ghi");
        ok("macro start m"); ok("replaceall whole def | DEF");
        ok("format p1 0 3 bold=true"); ok("view markup"); ok("width 16");
        ok("macro stop");
        int depth = app.dispatcher().engine().history().undoDepth();
        ok("macro replay m");
        assertEquals(depth + 1, app.dispatcher().engine().history().undoDepth());
        assertTrue(ok("render").contains("DEF"));
        ok("undo"); assertTrue(ok("render").contains("DEF")); // Undo replay only: earlier recorded edit remains
        ok("undo"); assertFalse(ok("render").contains("**"));
        ok("undo"); assertTrue(ok("render").contains("def"));
    }
    @Test void checkpointRestoreIsItselfUndoableAndCanBranch() {
        ok("section a Title"); ok("paragraph a p1 First"); ok("save baseline");
        ok("replace p1 0 5 Second"); ok("restore baseline");
        assertTrue(ok("render").contains("First"));
        ok("undo"); assertTrue(ok("render").contains("Second"));
        ok("insert p1 0 New-"); assertFalse(app.processLine("redo").success());
    }
    @Test void supportsEmptyReplacementAndWholeWords() {
        ok("section a Alpha"); ok("paragraph a p1 cats cat catdog cat");
        assertEquals(2, app.dispatcher().engine().find("cat", engine.SearchMode.WHOLE_WORD).size());
        ok("replaceall whole cat |");
        assertTrue(((Paragraph) app.dispatcher().engine().document().findNode("p1")).getBody().plainCharacters().equals("cats  catdog "));
    }
    @Test void interactiveAndScriptProcessorHaveEquivalentResults() {
        String data = "section a T\nparagraph a p hello world\nformat p 0 5 bold=true\nstatus\n";
        List<CommandResult> fromScript = new CommandProcessor().processScript(new StringReader(data));
        CommandProcessor direct = new CommandProcessor();
        List<CommandResult> fromInput = java.util.Arrays.stream(data.strip().split("\\n")).map(direct::processLine).toList();
        assertEquals(fromInput, fromScript);
        assertEquals(4, fromInput.size());
    }
    @Test void failedMacroReplayRestoresDocumentAndPresentation() {
        ok("section s Heading"); ok("paragraph s p abc");
        ok("macro start fragile");
        ok("insert p 2 X");           // Now 4 characters
        ok("view markup");
        ok("insert p 4 Y");           // Now 5 characters
        ok("macro stop");
        ok("replace p 0 5 QZ");       // Two characters before replay
        ok("view plain");
        int depth = app.dispatcher().engine().history().undoDepth();
        assertFalse(app.processLine("macro replay fragile").success()); // first insert succeeds, second fails
        assertEquals(depth, app.dispatcher().engine().history().undoDepth());
        assertEquals("QZ", ((Paragraph) app.dispatcher().engine().document().findNode("p")).getBody().plainCharacters());
        assertEquals("plain", app.dispatcher().presentation().currentView());
    }
    @Test void invalidMoveCannotLandInsideItsOwnSource() {
        ok("section s Heading"); ok("paragraph s p 123456789");
        int depth = app.dispatcher().engine().history().undoDepth();
        assertFalse(app.processLine("move p 2 6 4").success());
        assertEquals("123456789", ((Paragraph) app.dispatcher().engine().document().findNode("p")).getBody().plainCharacters());
        assertEquals(depth, app.dispatcher().engine().history().undoDepth());
    }
    @Test void rejectsInvalidArgsAndNonASCII() {
        assertFalse(app.processLine("width 0").success());
        assertFalse(app.processLine("paragraph x p alpha").success());
        assertFalse(app.processLine("format p 0 1 bold=maybe").success());
        assertFalse(app.processLine("section s Caf\u00e9").success());
    }
}
