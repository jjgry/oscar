package MailingServices;

public class IncomingEmailMessage {
    private String senderEmailAddress;
    private String receiverEmailAddress;
    private String subject;
    private String message;


    public IncomingEmailMessage(String senderEmailAddress, String receiverEmailAddress, String subject, String message, String emailID){
        this.senderEmailAddress = senderEmailAddress;
        this.receiverEmailAddress = receiverEmailAddress;
        this.subject = subject;
        this.message = message;
    }

    public String getSenderEmailAddress() {return senderEmailAddress;}
    public String getReceiverEmailAddress() {return receiverEmailAddress;}
    public String getSubject() {return subject;}
    public String getMessage() {return message;}
}
