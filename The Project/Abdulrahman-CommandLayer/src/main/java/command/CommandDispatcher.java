package command;

public class CommandDispatcher {

    private final EditCommandFactory editFactory;

    public CommandDispatcher() {
        this.editFactory = new EditCommandFactory();
    }

    public CommandResult dispatch(ParsedCommand request) {

        if (request == null) {
            return new CommandResult(
                    false, "Command cannot be null");
        }

        if (editFactory.isEditCommand(request)) {
            return new CommandResult(
                    false,
                    "Editing service not connected: "
                            + editFactory.getEditCommandType(request));
        }

        return switch (request.verb) {

            case "undo", "redo" ->
                    unavailable("HistoryManager", request.verb);

            case "save", "restore" ->
                    unavailable("CheckpointManager", request.verb);

            case "find", "replacefirst", "replaceall" ->
                    unavailable("SearchService", request.verb);

            case "render", "width", "view", "breaker" ->
                    unavailable("PresentationService", request.verb);

            case "status" ->
                    unavailable("StatisticsService", request.verb);

            case "macro" ->
                    unavailable("MacroManager", request.verb);

            default -> new CommandResult(
                    false, "Unsupported command: " + request.verb);
        };
    }

    private CommandResult unavailable(String service, String command) {
        return new CommandResult(
                false,
                service + " not connected for command: " + command
        );
    }

    public CommandResult replayMacro(String name) {

        if (name == null || name.isBlank()) {
            return new CommandResult(
                    false, "Macro name cannot be empty");
        }

        return unavailable("MacroManager", "replay " + name);
    }
}
