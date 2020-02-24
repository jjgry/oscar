package oscar;

import MailingServices.*;
import classifier.EmailClassifier;
import database.*;

import java.io.IOError;
import java.io.IOException;
import java.lang.String;
import java.time.LocalDateTime;
import java.lang.Thread;
import java.util.LinkedList;
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
    */

    DBInterface DB;
    SegmentQueue<OutgoingEmailMessage> OutQ;
    SegmentQueue<IncomingEmailMessage> InQ;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public Kernel() throws DBInitializationException, ClassificationTypeException {
        //SETUP
        //Initialise the queues for the Receiver -> Kernel and the Kernel -> Sender

        OutQ = new SegmentQueue<>();
        InQ = new SegmentQueue<>();

        //  Establish the Interface to the database. REMOVE credentials from hardcoding.
        DBInterface DB;
        try {
            DB = new DBInterface();
        } catch (DBInitializationException e) {
            //try again 3 times. If connection is impossible, end program in error.
            try {
                DB = new DBInterface();
            } catch (DBInitializationException e1) {
                try {
                    DB = new DBInterface();
                } catch (DBInitializationException e2) {
                    e.printStackTrace();
                    throw new DBInitializationException("After trying 3 times, no connection can be established.");
                }
            }
        }


        //Initialise the Sender
        EmailSender Sender = EmailSender.getSender(OutQ);

        // Initialise the Receiver
        EmailReceiver Rec = EmailReceiver.getEmailReceiver(InQ);

        //TODO: Initialise the DBMS port thread, if necessary

        System.out.println("Kernel: Receiver and Sender initialised.");
        // Create a thread, the major loop, named Major.
        DBInterface finalDB = DB;
        Thread Major = new Thread() {
            @Override
            public void run() {
                //Main Loop:
                LocalDateTime NextNewApptCheck = LocalDateTime.now();
                //  1. Poll periodically for any new emails to send - so track last time checked. Check every 5 mins.
                SendNewReminders(5);//SEE BELOW. This makes a system that should send the new reminders found on the database on demand, every 10 minutes.
                System.out.println("Kernel: Database polling system set up.");
                while (true) {

                    // 2. Deal with received emails one at a time until the queue is empty;
                    if (InQ.NumWaiting() > 0) {
                        finalDB.openConnection();
                        while (InQ.NumWaiting() > 0) {
                            IncomingEmailMessage PatientResponse = InQ.take();
                            System.out.println("\n===\nKernel: Patient email response taken from Rec.");
                            //TODO: Appointment ID should be given by receiver system, not always be -1 in line below
                            int appointmentID = -1;
                            try {
                                appointmentID = Integer.parseInt(PatientResponse.getAppointmentID());
                            }
                            catch(Exception e){//catching parsing exception
                                //no action, leave the apptID invalid at -1.
                            }
                            // 2a. Is the received email valid? check it's a patient on the database.
                            System.out.println("    Appointment ID: "+appointmentID);
                            if (finalDB.confirmAppointmentExists(PatientResponse.getSenderEmailAddress(), appointmentID)) {//if a valid email....
                                // 2b. Fetch information on the history of this conversation, ie last response type, appointment time, doctor name, patient name.
                                System.out.println("    <Appointment Exists>");
                                Appointment bookedAppointment = finalDB.getApp(appointmentID);
                                Patient p = finalDB.getPatient(appointmentID);
                                // 2c. Hand off to classifier: what type of message was it?

                                //String C = EmailClassifier.getCategory(PatientResponse.getMessage());
                                Classification C = null;
                                try {
                                    C = new Classification(PatientResponse.getMessage());
                                } catch (IOException e) {
                                    //TODO: Handle this error properly. Why is it thrown?

                                }
                                assert (p.getEmail() == PatientResponse.getSenderEmailAddress());// these really should be the same and if not our system is not designed correctly.
                                assert (bookedAppointment.getAppID() == appointmentID);
                                switch (C.getDecision()) {
                                    case CONFIRM:
                                        //  Confirm Appt in database
                                        System.out.println("Kernel: message classified as CONFIRM");
                                        finalDB.confirmNewTime(appointmentID);
                                        OutQ.put(new OutgoingEmailMessage(p, bookedAppointment, EmailMessageType.ConfirmationMessage));
                                        System.out.println("Kernel: email sent to show confirmation; DBMS informed of confirmation.");
                                        break;
                                    case CANCEL:
                                        //Cancel Appt in database
                                        System.out.println("Kernel: message classified as CANCEL");
                                        finalDB.rejectTime(appointmentID);
                                        OutQ.put(new OutgoingEmailMessage(p, bookedAppointment, EmailMessageType.CancellationMessage));
                                        System.out.println("Kernel: cancellation message sent; slot cancelled on database.");

                                        break;
                                    case RESCHEDULE:
                                        // RESCHEDULE **** complicated

                                        //Add this appointment to list of those that cannot be attended in DB
                                        // ^^^ decided as redundant given the current timestamp system
                                        //Poll database for available appointments in given time slots
                                        System.out.println("Kernel: message classified as RESCHEDULE");
                                        LinkedList<Timeslot> all_available_timeslots = new LinkedList<>();
                                        String[] availableDates = C.getDates();
                                        all_available_timeslots.addAll(finalDB.getAppointments(bookedAppointment.getDoctorID(), availableDates[0], availableDates[1]));
                                        all_available_timeslots.addAll(finalDB.getAppointments(bookedAppointment.getDoctorID(), availableDates[2], availableDates[3]));
                                        all_available_timeslots.addAll(finalDB.getAppointments(bookedAppointment.getDoctorID(), availableDates[4], availableDates[5]));
                                        //TODO: Ends of the timeslot are not implemented in the database, so should be targeted if posssible

                                        System.out.println("    Available timeslots:");
                                        for(Timeslot T : all_available_timeslots){
                                            System.out.println("      starting at: "+T.getStartTime());
                                        }

                                        if (all_available_timeslots.size() < 1) {
                                            System.out.println("      <no available timeslots>");
                                            //  Send email asking for new timeslots (the ones we were given do not work).
                                            OutQ.put(new OutgoingEmailMessage(p.getEmail(), p.getName(),
                                                    bookedAppointment.getDoctorName(), "", "",
                                                    EmailMessageType.AskToPickAnotherTimeSlotMessage,
                                                    Integer.toString(appointmentID)));//We have empty strings given as time info as we have no time info!

                                        } else {// we have a collection of >= 1 to choose from.
                                            //TODO: select one to suggest based on a more sensible criteria.
                                            int selectedTimeslotID = all_available_timeslots.getFirst().getID();
                                            //cancel last one.
                                            finalDB.rejectTime(appointmentID);
                                            // block selected new appointment
                                            finalDB.blockTimeSlot(appointmentID, selectedTimeslotID);
                                            // Send SuggestedAppt email to patient to suggest the new time.
                                            OutQ.put(new OutgoingEmailMessage(p, bookedAppointment,
                                                    EmailMessageType.NewAppointmentDetailsMessage));
                                            System.out.println("Kernel: Times updated in databsase; new appt details email sent.");
                                        }
                                        break;
                                    case OTHER:
                                        //No action taken on these emails.
                                        System.out.println("Kernel: message classified as OTHER");
                                        break;
                                    default:
                                        System.out.println("This new classification type should not exist.");
                                        break;

                                }

                            } else {//invalid patientID...
                                System.out.println("    <Appointment Does Not Exist>");
                                //  Send invalidEmailAddress  Email.
                                OutQ.put(new OutgoingEmailMessage(
                                        PatientResponse.getSenderEmailAddress(), "", "",
                                        "", "",
                                        EmailMessageType.InvalidEmailMessage, "NO APPOINTMENT EXISTS"));
                                System.out.println(
                                        "Kernel: message sent to unknown user to inform them we cannot take their emails.");
                            }
                        }
                        finalDB.closeConnection();
                    }
                }
            }
        };
        Major.start();

        // If the DBMS port is a thread in this, put it here. If it is a system with (another) producer consumer queue, check it between every handle of an incoming, and at the end of all of these.
    }

    //TODO: Simonas suggested a better system for this
    public void SendNewReminders( int xMinutes ) {
        final Runnable reminderBatchSender = new Runnable() {
            public void run() {

                System.out.println("Kernel: beep, at " + LocalDateTime.now());
                DB.openConnection();
                List<Appointment> newAppts = DB.remindersToSendToday();
                System.out.println("Kernel: Found "+newAppts.size()+" new appointments to remind about in latest DB poll.");

                for (Appointment A : newAppts) {
                    //  1a. Send any initial reminder emails that are now shown as required by the database state.
                    Patient p = DB.getPatient(A.getAppID());
                    OutQ.put(new OutgoingEmailMessage(p, A, EmailMessageType.InitialReminderMessage));
                    System.out.println("Kernel: Sent initial reminder message about appointment "+A.getAppID());
                }
                DB.closeConnection();
            }
        };//TODO: this should be minutes, not seconds.
        final ScheduledFuture<?> BatchHandle =
                //Schedule the check for every x minutes.
                scheduler.scheduleAtFixedRate(reminderBatchSender, xMinutes, 0, SECONDS);
    }


    public static void main( String[] args ) throws DBInitializationException, ClassificationTypeException {
        new Kernel();
    }
}