package editor.model;

  /*
  Partial formatting update whose non null fields override the corresponding
  attributes of an existing {link textstyle}
  
  @param optionalBold        new bold flag, or {@code null} to leave unchanged
  @param optionalItalic      new italic flag, or {@code null} to leave unchanged
  @param optionalFontName    new font family name, or {@code null} to leave unchanged
  @param optionalFontSize    new positive font size, or {@code null} to leave unchanged
  @param optionalColor       new color value, or {@code null} to leave unchanged
  */

public record StylePatch(
        Boolean optionalBold,
        Boolean optionalItalic,
        String optionalFontName,
        Integer optionalFontSize,
        String optionalColor
){
    // validates any provided non null patch values immediately.
    public StylePatch{
        if (optionalFontName != null && optionalFontName.isBlank()){
            throw new IllegalArgumentException("Font name must not be blank.");
        }
        if (optionalFontSize != null && optionalFontSize <= 0){
            throw new IllegalArgumentException("Font size must be positive.");
        }
        if (optionalColor != null && optionalColor.isBlank()){
            throw new IllegalArgumentException("Color must not be blank.");
        }
    }
  
  /* 
  produces a new {link textstyle} by applying all non null fields of this patch to {code style}
  
  param style base style to update
  return updated immutable {link textstyle}
  */
  public TextStyle applyTo(TextStyle style) {
      if (style == null) {
          throw new IllegalArgumentException("Base style cannot be null.");
      }

      // Start with the existing style's values
      boolean newBold = style.bold();
      boolean newItalic = style.italic();
      String newFontName = style.fontName();
      int newFontSize = style.fontSize();
      String newColor = style.color();

      // Only replace the fields that this patch actually set
      if (optionalBold != null) {
          newBold = optionalBold;
      }
      if (optionalItalic != null) {
          newItalic = optionalItalic;
      }
      if (optionalFontName != null) {
          newFontName = optionalFontName;
      }
      if (optionalFontSize != null) {
          newFontSize = optionalFontSize;
      }
      if (optionalColor != null) {
          newColor = optionalColor;
      }

      return new TextStyle(newBold, newItalic, newFontName, newFontSize, newColor);
  }
}
