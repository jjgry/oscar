package MailingServices;

public class FailedToInstantiateComponent extends Exception {
    public FailedToInstantiateComponent(String errorMessage) {
        super(errorMessage);
    }
}
