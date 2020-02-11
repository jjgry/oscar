//Started by Shaun. Inform me of desired edits please!
package oscar;

public abstract class OutgoingMessage {

    private String msgType;//The type of message to be sent (also represented by the class itself when inherited)
    //Should be defined in each inheriting type.

    private String email;
    private boolean sendByEmail;
    private String telephoneNumber;//Use this only when !SendByEmail.
    private String doctorName;
    //TODO: add the elements that are need for the outgoing message templates. What info does the sender need?
    //TODO: The different types of emails to send will each have their own class and

    public OutgoingMessage(String Email, boolean SendByEmail, String TelephoneNumber, String DoctorName){

        this.email = Email;
        this.sendByEmail = SendByEmail;
        this.telephoneNumber = TelephoneNumber;
        this.doctorName = DoctorName;
    }

    public String getMsgType(){return msgType;}

    public String getEmail(){return email;}

    public boolean SendByEmail(){return sendByEmail;}

    public String getTelNumber() {return telephoneNumber;}

    public String getDoctorName() {return doctorName;}
}
