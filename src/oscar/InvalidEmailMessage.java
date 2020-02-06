package oscar;

public class InvalidEmailMessage extends OutgoingMessage {
    public InvalidEmailMessage(String Email) {
        super(Email, true, "", "");
    }
}
