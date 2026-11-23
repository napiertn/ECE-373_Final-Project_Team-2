package engine;

import edit.CompositeEditCommand;
import edit.ReplaceTextCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.*;

/** Searches the full nested document and creates undoable replacement commands. */
public final class SearchService {
    private final Document document;

    /** Creates search operations bound to a document. */
    public SearchService(Document document) {
        if (document == null) throw new IllegalArgumentException("Document required");
        this.document = document;
    }

    /** Finds all nonoverlapping, case-sensitive matches, including section titles. */
    public List<TextSelection> find(String query, SearchMode mode) {
        validateQuery(query, mode);
        List<TextSelection> matches = new ArrayList<>();
        for (DocumentNode node : DocumentWalker.depthFirst(document)) {
            if (node instanceof Section section)
                appendMatches(matches, section.getId(), TextField.SECTION_TITLE, section.getTitle(), query, mode);
            else if (node instanceof Paragraph paragraph)
                appendMatches(matches, paragraph.getId(), TextField.BODY, paragraph.getBody(), query, mode);
        }
        return List.copyOf(matches);
    }

    /** Builds one replacement command for the first match (null if none). */
    public ReplaceTextCommand replaceFirst(String query, String value, SearchMode mode) {
        if (value == null) throw new IllegalArgumentException("Replacement required");
        List<TextSelection> matches = find(query, mode);
        return matches.isEmpty() ? null : new ReplaceTextCommand(matches.get(0), new StyledText(value));
    }

    /** Builds one atomic command replacing ALL matches, right-to-left per text field. */
    public CompositeEditCommand replaceAll(String query, String value, SearchMode mode) {
        if (value == null) throw new IllegalArgumentException("Replacement required");
        List<TextSelection> matches = new ArrayList<>(find(query, mode));
        Collections.reverse(matches);
        CompositeEditCommand edits = new CompositeEditCommand();
        for (TextSelection match : matches) edits.add(new ReplaceTextCommand(match, new StyledText(value)));
        return edits;
    }

    private static void validateQuery(String query, SearchMode mode) {
        if (query == null || query.isEmpty()) throw new IllegalArgumentException("Search string cannot be empty");
        if (mode == null) throw new IllegalArgumentException("Search mode required");
    }

    private static void appendMatches(List<TextSelection> out, String id, TextField field,
                                      StyledText text, String query, SearchMode mode) {
        String source = text.plainCharacters();
        int from = 0, found;
        while ((found = source.indexOf(query, from)) >= 0) {
            int end = found + query.length();
            boolean qualifies = mode == SearchMode.SUBSTRING
                    || ((found == 0 || !isWord(source.charAt(found - 1)))
                    && (end == source.length() || !isWord(source.charAt(end))));
            if (qualifies) out.add(new TextSelection(id, field, found, end));
            from = end;
        }
    }

    private static boolean isWord(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9') || c == '_';
    }
}
