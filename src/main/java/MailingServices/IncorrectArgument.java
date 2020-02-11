package MailingServices;

public class IncorrectArgument extends Exception {
    public IncorrectArgument(String errorMessage) {
        super(errorMessage);
    }
}