package oscar;

public class InvalidPhoneNumberMessage extends OutgoingMessage {

    public InvalidPhoneNumberMessage( String TelephoneNumber) {
        super("", false, TelephoneNumber,"");
    }
}
