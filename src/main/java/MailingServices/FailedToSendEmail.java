package MailingServices;

public class FailedToSendEmail extends MailingError {
    public FailedToSendEmail(String errorMessage) {
        super(errorMessage);
    }
}
