package oscar;

public class ReminderMessage extends OutgoingMessage {
/// The email type that is sent routinely 24 hours before an appointment is due to happen.

    public ReminderMessage(String Email, boolean SendByEmail, String TelephoneNumber, String DoctorName) {
        super(Email, SendByEmail, TelephoneNumber, DoctorName);
        //TODO: Add extra state needed for such emails



    }
}
