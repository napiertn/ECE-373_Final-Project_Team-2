/** Immutable snapshot of document counts. */
public final class DocumentStatistics {
    private final int words;
    private final int characters;
    private final int paragraphs;

    public DocumentStatistics(int words, int characters, int paragraphs) {
        this.words = words;
        this.characters = characters;
        this.paragraphs = paragraphs;
    }

    public int getWords()      {
        return words;
    }

    public int getCharacters() {
        return characters;
    }

    public int getParagraphs() {
        return paragraphs;
    }



    public static DocumentStatistics empty() {
        return new DocumentStatistics(0, 0, 0);
    }

    @Override
    public String toString() {
        return "Words: " + words + ", Characters: " + characters
                + ", Paragraphs: " + paragraphs;
    }
}