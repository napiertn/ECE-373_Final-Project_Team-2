package editor.model;

/*
Observer interface is notified whenever a linked document is modified
its implemented by presentation components such as code statistic service 
*/
public interface DocumentChangeListener{
  /*
called after any document-changing operation completes.
@param doc the document that changed
  */
  void onDocumentChanged(Docuement doc);
}
