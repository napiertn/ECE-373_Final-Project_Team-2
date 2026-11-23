package model;

/** Character offset in a text-bearing node field. */
public record TextLocation(String nodeId, TextField field, int offset) { }
