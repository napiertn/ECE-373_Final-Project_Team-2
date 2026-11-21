package command;

import java.io.BufferedReader;
import java.io.IOException;
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

    public void processScript(Reader lines){

        if (lines == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }

        BufferedReader reader = new BufferedReader(lines);

        try {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                CommandResult result = processLine(line);
                System.out.println(result.message);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error reading command script", e
            );
        }

    }

}
