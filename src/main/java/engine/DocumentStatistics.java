package engine;

/** Immutable current document totals (titles and paragraph bodies included). */
public record DocumentStatistics(int words, int characters, int paragraphs) { }
