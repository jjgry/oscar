package MailingServices;

public class MailingError extends Exception {
    public MailingError(String errorMessage) {
        super(errorMessage);
    }
}