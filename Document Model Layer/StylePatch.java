  package editor.model;
  
  import java.util.Objects;
  import static java.util.Objects.requireNonNullElse;
  
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
  
    public TextStyle applyTo(TextStyle style){
  Objects.requireNonNull(style, "Base style must not be null.");
    
    return new TextStyle{
    requireNonNullElse(optionalBold, style.bold()),
    requireNonNullElse(optionalItalic, style.italic()),
    requireNonNullElse(optionalFontName, style.fontName()),
    requireNonNullElse(optionalFontSize, style.fontSize()),
    requireNonNullElse(optionalColor, style.color())
      );
      }
  }
