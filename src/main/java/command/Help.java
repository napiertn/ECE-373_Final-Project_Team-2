package command;

/** Text reference used by both the interactive application and scripted demos. */
public final class Help {
    private Help() { }
    /** Supported command syntax; text positions are zero-based, [start,end). */
    public static final String TEXT = """
        DOCUMENT EDITOR  (all IDs unique; positions zero-based; ranges [start,end))
        section <id> <title>                    root section (append)
        subsection <parentId> <id> <title>      nested section (append)
        paragraph <sectionId> <id> [text]       paragraph (append; may be empty)
        sectionat <index> <id> <title>          insert root at index
        subsectionat <parentId> <index> <id> <title>
        paragraphat <parentId> <index> <id> [text]
        outline                                 show the node IDs and nesting
        insert <paragraphId> <offset> <text>    insert at index
        delete <paragraphId> <start> <end>     delete [start,end)
        replace <paragraphId> <start> <end> <text>
        move <paragraphId> <start> <end> <destinationOffset>
        moveto <sourceId> <start> <end> <targetId> <destinationOffset>
        split <paragraphId> <offset> <newParagraphId>
        merge <firstParagraphId> <nextParagraphId>   adjacent siblings only
        format <paragraphId> <start> <end> bold=true italic=true fontname=Arial fontsize=14 color=red
        titleinsert / titledelete / titlereplace / titleformat: same args but edit section title
        find <substring|whole> <query>
        replacefirst <substring|whole> <query> | <replacement>
        replaceall <substring|whole> <query> | <replacement>   (one undo)
        undo | redo | save <name> | restore <name>
        width <positiveInteger> | view <plain|markup> | breaker <greedy|balanced>
        render | status | styles
        macro start <name> | macro stop | macro replay <name>
        help | exit
        A line starting with # is a comment in scripts. All text is plain ASCII.
        """;
}
