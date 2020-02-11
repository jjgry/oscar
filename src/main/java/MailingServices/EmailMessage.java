package MailingServices;

public class EmailMessage {
    private String sender;
    private String receiver;
    private String subject;
    private String message;

    public EmailMessage( String sender, String receiver, String subject, String message ){
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.message = message;
    }

    public String getSender() {return sender;}
    public String getReceiver() {return receiver;}
    public String getSubject() {return subject;}
    public String getMessage() {return message;}
}
