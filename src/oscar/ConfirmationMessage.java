package oscar;

public class ConfirmationMessage extends OutgoingMessage {
    /// The type of message to sent to confirm that the appointment is still theirs, and we look forward to seeing them.

    public ConfirmationMessage(String Email, boolean SendByEmail, String TelephoneNumber, String DoctorName) {
        super(Email, SendByEmail, TelephoneNumber, DoctorName);
        //TODO: add extra state for this email type
    }
}
