package editor.model;

/**
 * leaf node in the composite hierarchy containing formatting body text
 */
public class Paragraph extends DocumentNode{
    private final StyledText body;


    /**
     * creates paragraph with the given {@link StyledText} body
     * @param id   unique paragraph ID
     * @param body formatted paragraph body
     */
    public Paragraph(String id, StyledText body) {
        super(id);
        if (body == null) {
            throw new IllegalArgumentException("Paragraph body cannot be null.");
        }
        this.body = body;
    }


    /**
     * Convenience constructor creating a paragraph from plain text.
     * @param id         unique paragraph ID
     * @param plainText  initial plain text
     */
    public Paragraph(String id, String plainText) {
        this(id, new StyledText(plainText));
    }

    /**
     * returns the editable {@link StyledText} body of this paragraph
     * @return paragraph body
     */
    public StyledText getBody() {
        return body;
    }

    @Override
    public Paragraph copy() {
        return new Paragraph(getId(), body.copy());
    }
}
