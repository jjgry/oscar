package MailingServices;

public class FailedToSendEmail extends Exception {
    public FailedToSendEmail(String errorMessage) {
        super(errorMessage);
    }
}
