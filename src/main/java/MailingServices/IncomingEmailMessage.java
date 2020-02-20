package MailingServices;

public class IncomingEmailMessage {
    private String senderEmailAddress;
    private String receiverEmailAddress;
    private String subject;
    private String message;
    private String appointmentID;

    public IncomingEmailMessage(
            String senderEmailAddress,
            String receiverEmailAddress,
            String subject,
            String message,
            String appointmentID ) {
        this.senderEmailAddress = senderEmailAddress;
        this.receiverEmailAddress = receiverEmailAddress;
        this.subject = subject;
        this.message = message;
        this.appointmentID = appointmentID;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public String getReceiverEmailAddress() {
        return receiverEmailAddress;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getAppointmentID() {
        return appointmentID;
    }
}
