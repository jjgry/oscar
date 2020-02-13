package MailingServices;

public abstract class OutgoingEmailMessage {
    private String email;

    public OutgoingEmailMessage(String Email){
        this.email = Email;
    }

    public String getEmail(){return email;}
}
