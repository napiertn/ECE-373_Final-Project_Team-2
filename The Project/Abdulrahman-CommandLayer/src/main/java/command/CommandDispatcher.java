package command;

public class CommandDispatcher {

    public CommandResult dispatch(ParsedCommand request){
        if (request == null){
            return new CommandResult(false, "Command cannot be null");

        }
        return new CommandResult(false, "Command not implemented yet: " + request.verb);
    }
    public CommandResult replayMacro(String name){
        return new CommandResult(false, "Macro replay not implemented yet: " + name);
    }
}
