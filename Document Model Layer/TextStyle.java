package editor.model;

import java.util.Objects;

/*
immutable value object representation character formatting attributes.

param bold: wheter the text is bold
param italic: whether the text is italic
param fontName: non blank font family name
param fontSize: positive font size
param color: non blank color name or code
*/

public record TextStyle(
boolean bold,
  boolean italic,
  String fontName,
  int fontSize,
  String color
   ){
//default style applied to unformatted plain text
  public static final TextStyle DEFAULT = new TextStyle(false, false, "Arial", 12, "black");

  //validates style invariants
  public TextStyle{
    Objects.requireNonNull(fontName, "Font name must not be null.");
    Objects.requireNonNull(color, "Color must not be null.");
    if (fontName.isBlank()){
      throw new IllegalArgumentException("Font name must not be blank.");
    }
    if (fontSize <= 0){
      throw new IllegalArgumentException("Font Size must be Positive.");
    }
    if (color.isBlank()){
      throw new IllegalArgumentException("Color must not be blank.");
    }
  }
}
