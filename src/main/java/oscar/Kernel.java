package oscar;

import MailingServices.EmailReceiver;
import MailingServices.EmailSender;

import java.lang.String;

public class Kernel {
    /*
    / (FROM FUNCTIONAL SPEC) Functions of the Kernel:
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

        SegmentQueue<OutgoingMessage> OutQ = new SegmentQueue<>();
        SegmentQueue<IncomingMessage> InQ = new SegmentQueue<>();

        // TODO: Establish the Interface to the database.

        //DBInterface DB = new DBInterface();

        EmailSender sender = EmailSender.getSender(OutQ);

        //TODO initialise receiver
        // EmailReceiver receiver = new EmailReceiver(InQ);

        //TODO: Initialise the training system of the classifier, if necessary


        //TODO: Initialise the DBMS port thread, if necessary

        //TODO: Create a thread

        //Main Loop:
//        while (true) {
//            // TODO: 1. Poll periodically for any new emails to send - so track last time checked. Check every 5 mins.
//            // __________ = DB.remindersToSendToday();
//
//            // TODO: 1a. Send any emails that are now shown as required by the database state.
//
//            //foreach(    ^^^^___________        ){
//            // IntroMessage nextIntro = new IntroMessage(*);
//            // OutQ.put(nextIntro);
//            //}
//
//            // TODO: 2. Deal with received emails one at a time until the queue is empty:
//            if (InQ.NumWaiting() > 0) {
//                IncomingMessage PatientResponse = InQ.take();
//
//                // 2a. Is the received email valid? check it's a patient on the database.
//                int PatientID = DB.getPatientsID(PatientRepsonse.getEmail());
//                if (PatientID > 0) {
//                    // 2b. Fetch information on the history of this conversation, ie last response type, appointment time, doctor name, patient name.
//
//                    // 2c. Hand off to classifier: what type of message was it?
//                    Classification C = Classifier.Classify(*)
//                    switch(C.getType()){
//                      //CONFIRM
//                       case CONFIRM:
//                           break;
//                    // CANCEL
//                        case CANCEL:
//                            break;
//                    // RESCHEDULE **** complicated
//                        case RESCHEDULE:
//                            break;
//                    // OTHER
//                         case OTHER:
//                             break;
//                    // AUTOMATED_RESPONSE
//                         case AUTOMATED_RESPONSE:
//                                break;
//                        }
//
//
//                    // 2d. update database with any new developments.
//                    // 2e. Send another email based on this as required.
//
//                } else {
//                    // Invalid email. Send response to this system saying we can't accept this address.
//                    InvalidAddressMessage resp = new InvalidAddressMessage();
//                    OutQ.put(resp);
//                }
//            }
//        }

        // If the DBMS port is a thread in this, put it here. If it is a system with (another) producer consumer queue, check it between every handle of an incoming, and at the end of all of these.
    }
}