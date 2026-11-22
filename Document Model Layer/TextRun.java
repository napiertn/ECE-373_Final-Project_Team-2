package editor.model;
import java.util.Objects;

/*
contigous run of characters sharing a single {@link textstyle}

param hcharacters non null sequence of characters
param style       non null formatting style applied to those characters
*/

public record TextRun(String characters, TextStyle style){
// validates non null components
  public TextRun{
Objects.requireNonNull(characters, "Run characters must not be null.");
    Objects.requireNonNull(style, "Run style must not be null.");
  }

  //returns the number of characters in the run
  //@returns caracter count
  public int length(){
return characters.length();
  }
}
