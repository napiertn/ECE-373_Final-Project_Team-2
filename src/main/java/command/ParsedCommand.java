package command;

import java.util.Map;

public class ParsedCommand {

    public final String verb;
    public final Map<String, String> arguments;

    public ParsedCommand(String verb, Map<String, String> arguments){
        this.verb = verb;
        this.arguments = Map.copyOf(arguments);
    }

    public String argument(String name) {
        return arguments.get(name);
    }
}
