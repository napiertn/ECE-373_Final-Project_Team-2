package editor.model;
import java.util.Objects;

//leaf node in the composite hierarchy containing formatting body text
public class Paragraph extends DocumentNode{
private final StyledText body;

  
  //creates paragraph with the given {@link StyledText} body
  //@param id   unique paragraph ID
  //@param body formatted paragraph body
  public Paragraph(String id, StyledText body){
super(id);
    this.body = Objects.requireNonNull(body, "Paragraph body must not be null.");
  }

  
//Convenience constructor creating a paragraph from plain text. 
  //@param id         unique paragraph ID
  //@param plainText  initial plain text
public Paragraph(String id, String plainText) {
        this(id, StyledText.ofPlainText(plainText));
    }

  //returns the editable {@link StyledText} body of this paragraph
  //@return paragraph body
public StyledText getBody() {
        return body;
    }

    @Override
    public Paragraph copy() {
        return new Paragraph(getId(), body.copy());
    }
}
