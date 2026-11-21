package command;

public class EditCommandFactory {
    public void validateRequest(ParsedCommand request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Command request cannot be null"
            );
        }

        if (request.verb == null || request.verb.isBlank()) {
            throw new IllegalArgumentException(
                    "Command name cannot be empty"
            );
        }
    }
}
