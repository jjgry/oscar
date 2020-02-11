package oscar;

import java.lang.String;
import java.time.LocalDateTime;
import java.time.*;

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

        //TODO:Initialise the Sender

        //Sender Sender = new Sender(OutQ);

        //TODO: Initialise the Receiver

        //Receiver Rec = new Receiver(InQ);

        //TODO: Initialise the training system of the classifier, if necessary


        //TODO: Initialise the DBMS port thread, if necessary

        //TODO: Create a thread, the major loop, named Major.
        Thread Major = new Thread() {
            @Override
            public void run() {


                //Main Loop:
                LocalDateTime NextNewApptCheck = LocalDateTime.now();

                while (true) {
                    // TODO: 1. Poll periodically for any new emails to send - so track last time checked. Check every 5 mins.
//                    if (LocalDateTime.now().IsAfter(NextNewApptCheck)) {
//                        NextNewApptCheck = LocalDateTime.now().plusHours(1);
//
//
//                        List<Appointment> newAppts = DB.remindersToSendToday();
//
//                        // TODO: 1a. Send any emails that are now shown as required by the database state.
//
//                        foreach(Appointment A :newAppts){
//                            //IntroMessage nextIntro = new IntroMessage(*);
//                            OutQ.put(nextIntro);
//                        }
//                    }
//
//                    // TODO: 2. Deal with received emails one at a time until the queue is empty:
//                    if (InQ.NumWaiting() > 0) {
//                        IncomingMessage PatientResponse = InQ.take();
//
//                        // 2a. Is the received email valid? check it's a patient on the database.
//                        int PatientID = DB.getPatientsID(PatientResponse.getEmail());
//                        if (PatientID > 0) {//if a valid email....
//                            // 2b. Fetch information on the history of this conversation, ie last response type, appointment time, doctor name, patient name.
//                            //TODO: Appointment bookedAppointment = DB.getAppointmentID();
//
//                            // 2c. Hand off to classifier: what type of message was it?
//                            Classification C = Classifier.Classify( *)
//                            if (C instanceof CONFIRM) {
//
//                                // TODO: Confirm Appt in database
//
//                                //Send Email
//                                OutQ.put(new ConfirmationMessage(patientResponse.getEmail(), true, "", ));
//                                // 2d. update database with any new developments.
//                                // 2e. Send another email based on this as required.
//
//                            } else if (C instanceof CANCEL) {
//
//                                //Cancel Appt in database
//
//                                //Send Email
//
//
//                            } else if (C instanceof RESCHEDULE) {
//
//                                // RESCHEDULE **** complicated
//
//                                //Add this appointment to list of those that cannot be attended in DB
//
//                                //Poll database for available appointments in given time slots
//
//                                // Suggest one. Set it as being attended in database
//
//                                // Send SuggestedAppt email to patient to suggest the new time.
//                            } else if (C instanceof OTHER) {
//
//                                // OTHER
//
//                                // 2d. update database with any new developments.
//                                // 2e. Send another email based on this as required.
//
//                            } else if (C instanceof AUTOMATED_RESPONSE){
//
//                            }
//                            else{ // a new classification that is unsupported.
//
//                            }
//                            // AUTOMATED_RESPONSE
//
//                        } else {//invalid patientID...
//                            OutQ.put(new InvalidEmailMessage(PatientResponse.getEmail()));
//                        }

                }
            }

        };
        Major.start();

        // If the DBMS port is a thread in this, put it here. If it is a system with (another) producer consumer queue, check it between every handle of an incoming, and at the end of all of these.
    }
}