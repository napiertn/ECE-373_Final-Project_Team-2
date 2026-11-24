# ECE 373 — Document Editor (Team 2)

A Java 17 command-line document editor for **structured sections, nested subsections, paragraphs, styled text, reversible edits, named checkpoints, search/replace, macros, and multiple rendering/line-breaking algorithms**. This is a text-command application, not a graphical word processor.

## Team responsibilities

| Contributor | Primary area | Production packages |
|---|---|---|
| **Ryan** | Editing operations and document-engine services | `edit`, `engine` |
| **Ali** | Text command parsing, dispatch, and application entry point | `command` |
| **Aidan** | Plain/marked-up presentation and line-breaking strategies | `presentation` |
| **Tommy** | Structured document hierarchy and styled-text representation | `model` |

The packages now work together as one integrated application; the above describes the team's division of responsibilities.

## Prerequisites

- **JDK 17 or later** (`java -version`, `javac -version`).
- **Apache Maven** (`mvn -version`).
- A terminal in the directory containing **`pom.xml`** (the repository root). IntelliJ's built-in terminal works.
- JUnit 5 is the project's test dependency.

## Quick start

From the repository root:

```bash
mvn clean test
mvn package
java -jar target/document-editor-1.0.0.jar
```

The last command launches **interactive mode**. At the `editor>` prompt, type `help` for the complete syntax reference and `exit` to quit. Each new application process starts with one empty document; there is no document import/export or automatic persistence.

### Run an existing demo script

```bash
java -jar target/document-editor-1.0.0.jar demo/01_core_walkthrough.txt
```

Or execute several scripts **in one process**, sequentially against **one shared document**:

```bash
java -jar target/document-editor-1.0.0.jar demo/01_core_walkthrough.txt demo/02_history_and_checkpoints.txt
```

**For the classroom:** run each numbered demo in a **separate invocation** unless you explicitly want their state combined. Node IDs such as `intro` or `p1` must be unique, and reusing them in the same document can cause a duplicate-ID error.

### Four supplied demos

| File | What it demonstrates | What to point out |
|---|---|---|
| `demo/01_core_walkthrough.txt` | Create a hierarchy, nested section, `outline`, `status`, `find`, whole-word `replaceall`, text/title formatting, views, widths, and two line breakers | Both presentation methods use the **same underlying document**; statistics change after edits |
| `demo/02_history_and_checkpoints.txt` | Replace All as one edit, `undo`, `redo`, `save`, `restore`, split and merge | A single undo reverses **every** replacement; restoring a checkpoint is itself undoable |
| `demo/03_macro_and_failures.txt` | Record/replay a macro, grouped undo/redo, switch presentation settings, attempt an invalid insertion | The final `ERROR: Invalid range ...` is **intentional** and does not corrupt the document |
| `demo/04_deep_nesting_and_move.txt` | Multiple subsection levels, formatting, styled text moved between paragraphs, undo, split/merge | Recursive traversal works across nested sections; moving text preserves the style information |

Script lines beginning with `#` and blank lines are ignored. A script uses the **same** parser/dispatcher as interactive input. A failed command prints `ERROR: ...` and subsequent script commands continue; a missing/unreadable script file instead causes the program to exit with an error.

## Try commands interactively

After running `java -jar target/document-editor-1.0.0.jar`, enter one line at a time:

```text
section intro Introduction
paragraph intro p1 Hello world
subsection intro methods Methods
paragraph methods p2 This is another paragraph
outline
insert p1 5 X
render
format p1 5 6 bold=true color=blue
view markup
render
save before-replacement
replaceall whole world | system
render
undo
render
redo
status
exit
```

Notes: Text offsets start at **0**. Selections are **half-open** (`[start,end)`), so `format p1 5 6` selects only the new `X` in `HelloX world`. The CLI does not interpret quotation marks as delimiters; quote characters would be included literally in input text.

### Command reference

Identifiers are document-wide unique; all commands are plain ASCII. Use `help` for the in-program reference.

| Function | Syntax | Example |
|---|---|---|
| Create root section | `section <id> <title>` | `section intro Introduction` |
| Create subsection | `subsection <parentId> <id> <title>` | `subsection intro methods Methods` |
| Add paragraph | `paragraph <sectionId> <id> [text]` | `paragraph intro p1 Hello world` |
| Insert at position | `sectionat <index> <id> <title>` | `sectionat 0 preface Preface` |
| Insert subsection at index | `subsectionat <parentId> <index> <id> <title>` | `subsectionat intro 0 setup Setup` |
| Insert paragraph at index | `paragraphat <parentId> <index> <id> [text]` | `paragraphat intro 0 p0 First paragraph` |
| View tree | `outline` | `outline` |
| Insert text | `insert <paragraphId> <offset> <text>` | `insert p1 5 X` |
| Delete selection | `delete <paragraphId> <start> <end>` | `delete p1 0 2` |
| Replace selection | `replace <paragraphId> <start> <end> <text>` | `replace p1 0 5 Greetings` |
| Move within paragraph | `move <id> <start> <end> <destinationOffset>` | `move p1 0 5 11` |
| Move between paragraphs | `moveto <sourceId> <start> <end> <targetId> <offset>` | `moveto p1 0 5 p2 0` |
| Split paragraph | `split <id> <offset> <newId>` | `split p1 5 p3` |
| Merge adjacent paragraphs | `merge <firstId> <nextId>` | `merge p1 p3` |
| Format characters | `format <id> <start> <end> <style options>` | `format p1 0 5 bold=true color=blue` |
| Edit/format a section title | `titleinsert`, `titledelete`, `titlereplace`, `titleformat` | `titleformat intro 0 5 fontsize=18` |
| Find | `find <substring\|whole> <query>` | `find whole cat` |
| Replace first/all | `replacefirst` or `replaceall` + mode, query, `\|`, replacement | `replaceall whole cat \| dog` |
| History | `undo` / `redo` | `undo` |
| Checkpoints | `save <name>` / `restore <name>` | `save before-edit` |
| Display | `render` / `view plain\|markup` / `width <n>` / `breaker greedy\|balanced` | `breaker balanced` |
| Live statistics | `status` | `status` |
| Distinct styles report | `styles` | `styles` |
| Macros | `macro start <name>` / `macro stop` / `macro replay <name>` | `macro replay demo` |
| Help/quit | `help` / `exit` | `help` |

Formatting options: `bold=true|false`, `italic=true|false`, `fontname=Arial`, `fontsize=16`, `color=blue`. Supply one or more space-separated properties; properties not supplied are preserved. The words `true` and `false` are text in command input, not Java code.

**Search is case-sensitive**. Substring mode matches text inside other words; whole-word mode uses ASCII letters/digits/underscores as word characters. `replaceall` is one undoable edit; `|` separates the query from the replacement and also allows an empty replacement (`replaceall whole cat |`). The parser does not use quoted-string or escape-sequence syntax.

## Build and automated tests

```bash
mvn clean test      # compile production and test code; execute all JUnit tests
mvn package         # build target/document-editor-1.0.0.jar (tests run by default)
```

The submitted repository snapshot includes **four JUnit 5 classes with 26 test methods**:

| Test class | Methods | What the tests establish |
|---|---:|---|
| `command/CommandIntegrationTest.java` | 12 | End-to-end parsing/dispatch, recursive structure, editing, validation, formatting, Replace All history, macros, checkpoints, script/interactive equivalence, and rollback |
| `engine/DocumentEngineTest.java` | 5 | Headless editing, statistics observer, cross-paragraph moves, atomic composites, deep search, and multi-level history |
| `model/StyledTextTest.java` | 4 | Styled-run preservation, deep copying, range safety, and adjacent-run merging |
| `presentation/PresentationTest.java` | 5 | Plain/markup views, greedy-vs-balanced differences, strategy extension, width validation, and rendering/search of a document containing **at least 100,000 characters** |
| **Total** | **26** | Functional/regression coverage, not a formal performance benchmark or a proof of every conceivable input |

The uploaded project ZIP included saved Maven Surefire reports showing **26 tests, 0 failures, 0 errors** at the time those reports were generated. Run `mvn clean test` yourself before presenting to confirm that the **current working checkout** still passes.

## Architecture

```text
Console or script
       |
EditorApplication -> CommandProcessor -> CommandParser
                           |                 |
                           +-- ParsedCommand-+
                                     |
                              CommandDispatcher
                             /                \
                 EditCommandFactory       PresentationService
                       |                 /                  \
                   EditCommand       DocumentRenderer    LineBreaker
                       |          (plain / markup)    (greedy / balanced)
                   EditorEngine <------ read-only rendering of Document
                  /   |    |    \
        HistoryManager | SearchService  MacroManager
          /   |         CheckpointManager
     undo/redo snapshots   StatisticsService (observes Document)
                          |
                    Document model
              Document -> Section -> DocumentNode
                                      |-- Section (nested)
                                      +-- Paragraph -> StyledText -> TextRun + TextStyle
```

A typical modifying command follows this route:

1. `EditorApplication` gets one line from the console or a script.
2. `CommandProcessor` skips comments/blanks and asks `CommandParser` to produce a `ParsedCommand`.
3. `CommandDispatcher` passes editing requests to `EditCommandFactory`, which builds an `EditCommand`.
4. `EditorEngine.apply()` sends the edit through `HistoryManager.perform()`; the command validates, changes the `Document`, and stores **before/after document snapshots** for undo/redo.
5. `Document.publishChanged()` notifies `StatisticsService`, which recalculates the current counts. Rendering reads the document through `PresentationService` without modifying it.

### Object-oriented patterns and design choices

- **Composite:** `DocumentNode` is the common base for `Section` containers and `Paragraph` leaves. `CompositeEditCommand` also groups replacements into one operation.
- **Command:** `EditCommand` standardizes validation, execution, undo, and redo across editing operations.
- **Memento/snapshots:** `DocumentSnapshot` and `SnapshotEditCommand` capture full document states for rollback, checkpoints, and history. The trade-off is memory/copying overhead.
- **Strategy:** `DocumentRenderer` and `LineBreaker` have interchangeable implementations selected at runtime.
- **Observer:** `DocumentChangeListener` lets `StatisticsService` react to edits without requiring each editing command to count words.
- **Simple factory:** `EditCommandFactory` converts parsed requests into editing objects; this is a factory role, not necessarily the GoF Factory Method pattern.

### Design boundaries and known limits

- **No GUI** or external document import/export: document state exists only while that program process is running.
- **Plain ASCII input** and no concurrency/networking.
- Character counts and word counts include **section titles and paragraph bodies**; paragraph count includes paragraph nodes only.
- The `styles` command counts the formatting values currently in use; it does **not** establish global style interning (the optional shared-style-storage requirement).
- Display widths apply to the text emitted by renderers, including markup. Very narrow widths can break markup markers into pieces and long words can be split.
- History uses full-document snapshots and imposes no small **fixed** undo limit; in practice, memory and large-document editing costs matter.
- The Draw.io Class UML depicts the overall intended design, but a few signatures/ownership relationships predate integration; the implementation is authoritative. For example, the live `CommandDispatcher` holds an `EditorEngine`, not separate fields for all engine managers, and macro steps use `MacroAction`.

### AI Disclosure

- In this project, ChatGPT and Claude AI were both used during the implementation of this project
- The 4 students were tasked with creating an interpretation of their section of the project which was split up using the UML Class diagram. AI was primarily used to determine the steps going forward for implementing the project into a succinct design that was ready for presentation. This allowed us to identify files that were missing, ways to integrate packages together, and greatly streamlined our process of stitching the code together.
- This led us to our final implementation of a document engine which can complete commands and track inputs. After helping us to stitch the code together, we obviously had to verify that the program could run commands and followed the system requirements for the project. We tested different commands such as inserting text, sections, subsections, etc and completed other commands in the intelliJ terminal to determine the validity of the project's final implementation. We were able to verify that the commands functioned as expected and that the implementation was valid for this project.

