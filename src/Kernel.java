import java.lang.String;

public class Kernel{
    /*
    / Functions of the Kernel:
    /
    /     The kernel has four main responsibilities:
    /
    /     Sending reminders to patients prior to their appointments and asking them to confirm attendance.
    /     Sanitising and storing received emails in the database
    /     Invoking the classifier to interpret an email it has been passed by the receiver.
    /     Handling the consequent rescheduling of appointments
    /
    /     The kernel will poll the database at regular intervals in order to determine which messages need to be sent.
    /       Rescheduling information comes from the classifier. The kernel uses the information from the classifier and
    /       the current conversation state which is held in the database to determine what action needs to be taken
    /       (cancellation/booking), updates the database accordingly and instructs the email sending handler to inform
    /       the patient of the changes.
    /
    /      If an email address provided by the receiver is not recognised, inform the kernel to send a response to the
    /      email informing them to use the registered email address.
    /
    */

    public static void main(String[] args) {
        //SETUP
        //Initialise the queues for the Receiver -> Kernel and the Kernel -> Sender
        //Initialise the Sender
        //Initialise the Reciever
        //Initialise the training system of the classifier, if necessary
        //Initialise the DBMS port thread, if necessary

        //Create a thread

        //Main Loop:
        // 1. Poll periodically for any new emails to send - so track last time checked. Check every 5 mins.

        // 1a. Send any emails that are now shown as required by the database state.

        // 2. Deal with recieved emails one at a time until the queue is empty:

        // 2a. Is the received email valid? check it's a patient on the database.

        // 2b. Fetch information on the history of this conversation, ie last response type, appointment time, doctor name, patient name.

        // 2c. Hand off to classifier: what type of message was it?

            // CONFIRM
            // CANCEL
            // RESCHEDULE **** complicated
            // OTHER
            // AUTOMATED_RESPONSE

        // 2d. update database with any new developments.
        // 2e. Send another email based on this as required.




        // If the DBMS port is a thread in this, put it here. If it is a system with (another) producer consumer queue, check it between every handle of an incoming, and at the end of all of these.
    }


}