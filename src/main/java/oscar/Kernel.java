package oscar;

import database.Appointment;
import database.DBInitializationException;
import database.DBInterface;

import java.lang.String;
import java.time.LocalDateTime;
import  java.lang.Thread;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;




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

    DBInterface DB;
    SegmentQueue<OutgoingMessage> OutQ;
    SegmentQueue<IncomingMessage> InQ;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public Kernel() {
        //SETUP
        //Initialise the queues for the Receiver -> Kernel and the Kernel -> Sender

         OutQ = new SegmentQueue<>();
         InQ = new SegmentQueue<>();

        // TODO: Establish the Interface to the database. REMOVE credentials from hardcoding.

        //DBInterface DB = new DBInterface();

        String username;
        String password;

        // get login credentials

        //System.out.print("Username: ");
        username = "jjag3";
        //System.out.print("Password: ");
        password = "JixOondEta";

        // initialize database interface
        DB = null;
        try {
            DB= new DBInterface("127.0.0.1:9876", username, password);
        } catch (DBInitializationException e) {
            e.printStackTrace();
            DB = null;
        }

        //TODO:Initialise the Sender

        //EmailSender Sender = new EmailSender.getSender(OutQ);

        //TODO: Initialise the Receiver

        //Receiver Rec = new Receiver(InQ);

        //TODO: Initialise the training system of the classifier, if necessary


        //TODO: Initialise the DBMS port thread, if necessary


        // Create a thread, the major loop, named Major.
        Thread Major = new Thread() {
            @Override
            public void run() {


                //Main Loop:
                LocalDateTime NextNewApptCheck = LocalDateTime.now();
                // TODO: 1. Poll periodically for any new emails to send - so track last time checked. Check every 5 mins.
                SendNewReminders();//SEE BELOW. This makes a system that should send the new reminders found on the database on demand, every 10 minutes.

                while (true) {


//
//                    // TODO: 2. Deal with received emails one at a time until the queue is empty;
//
//                    if (InQ.NumWaiting() > 0) {
//                        DB.openConnection();
//                        while (InQ.NumWaiting() > 0) {
//                            IncomingMessage PatientResponse = InQ.take();
//
//                            // 2a. Is the received email valid? check it's a patient on the database.
//                            int PatientID = DB.getPatientsID(PatientResponse.getEmail());
//                            if (PatientID > 0) {//if a valid email....
//                                // 2b. Fetch information on the history of this conversation, ie last response type, appointment time, doctor name, patient name.
//                                //TODO: Appointment bookedAppointment = DB.getAppointmentID();
//
//                                // 2c. Hand off to classifier: what type of message was it?
//                                Classification C = Classifier.Classify( *)
//                                if (C instanceof CONFIRM) {
//
//                                    // TODO: Confirm Appt in database
//
//                                    //Send Email
//                                    OutQ.put(new ConfirmationMessage(patientResponse.getEmail(), true, "", ));
//                                    // 2d. update database with any new developments.
//                                    // 2e. Send another email based on this as required.
//
//                                } else if (C instanceof CANCEL) {
//
//                                    //Cancel Appt in database
//
//                                    //Send Email
//
//
//                                } else if (C instanceof RESCHEDULE) {
//
//                                    // RESCHEDULE **** complicated
//
//                                    //Add this appointment to list of those that cannot be attended in DB
//
//                                    //Poll database for available appointments in given time slots
//
//                                    // Suggest one. Set it as being attended in database
//
//                                    // Send SuggestedAppt email to patient to suggest the new time.
//                                } else if (C instanceof OTHER) {
//
//                                    // OTHER
//
//                                    // 2d. update database with any new developments.
//                                    // 2e. Send another email based on this as required.
//
//                                } else if (C instanceof AUTOMATED_RESPONSE) {
//
//                                } else { // a new classification that is unsupported.
//
//                                }
//                                // AUTOMATED_RESPONSE
//
//                            } else {//invalid patientID...
//                                OutQ.put(new InvalidEmailMessage(PatientResponse.getEmail()));
//                            }
//                        }
//                        DB.closeConnection();
                    }
                }
            };
        Major.start();

        // If the DBMS port is a thread in this, put it here. If it is a system with (another) producer consumer queue, check it between every handle of an incoming, and at the end of all of these.
    }


    public void SendNewReminders() {
        final Runnable reminderBatchSender = new Runnable() {
            public void run() {

                System.out.println("beep"+LocalDateTime.now());
//                        DB.openConnection();
//                        List<Appointment> newAppts = DB.remindersToSendToday();
//                        DB.closeConnection();
//                        // TODO: 1a. Send any emails that are now shown as required by the database state.
//
//                        for(Appointment A :newAppts){
//                            IntroMessage nextIntro = new IntroMessage(A.getPatientEmail(),true,"",A.getDoctorName());
//                            OutQ.put(nextIntro);
//                        }
//                    }

            }
        };
        final ScheduledFuture<?> BatchHandle =
                //Schedule the check for every 1 minute.
                scheduler.scheduleAtFixedRate(reminderBatchSender, 0, 1, MINUTES);
        scheduler.schedule(new Runnable() {
            public void run() { BatchHandle.cancel(true); }
        }, 60 * 60, SECONDS);
    }


    public static void main (String[] args){
        new Kernel();
    }
}