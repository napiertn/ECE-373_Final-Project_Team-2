package model;

/** Half-open character selection in one paragraph or section title. */
public record TextSelection(String nodeId, TextField field, int start, int end) { }
