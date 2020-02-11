package oscar;

public class IntroMessage extends OutgoingMessage {
/// The IntroMessage is the message to send for the first time, ie to start a new conversation with a patient.
/// It is the first email they receive. A similar type exists for the message sent 24hrs before an appointment, a
/// 'ReminderMessage'.

    public IntroMessage(String Email, boolean SendByEmail, String TelephoneNumber, String DoctorName) {
        super(Email, SendByEmail, TelephoneNumber, DoctorName);
        //TODO; add extras here



    }
}
