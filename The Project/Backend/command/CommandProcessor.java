package command;

import java.io.Reader;

public class CommandProcessor {
    private final CommandParser  parser;
    private final CommandDispatcher dispatcher;

    public CommandProcessor(){
        this.parser = new CommandParser();
        this.dispatcher = new CommandDispatcher();
    }

    public CommandResult processLine(String line){
        try {
            ParsedCommand command = parser.parse(line);
            return dispatcher.dispatch(command);
        }

        catch (IllegalArgumentException e) {
            return new CommandResult(false, e.getMessage());
        }
    }

    public void processScript(Reader line){

    }

}
