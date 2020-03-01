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

import java.util.concurrent.TimeUnit;
import opennlp.tools.doccat.DoccatModel;

import static java.util.concurrent.TimeUnit.*;


public class Kernel {//
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

  private DBInterface DB;
  private SegmentQueue<OutgoingEmailMessage> OutQ;
  private SegmentQueue<IncomingEmailMessage> InQ;
  private boolean Sender_ON;
  private LinkedList<Appointment> PendingEmailOutbox;


  DoccatModel model;

  {
    try {
      model = EmailClassifier.trainCategorizerModel();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // static variable single_instance of type Singleton
  private static Kernel single_instance = null;

  // private constructor restricted to this class itself

  // static method to create instance of Singleton class
  public static Kernel getInstance() {
    if (single_instance == null) {
      single_instance = new Kernel();
    }

    return single_instance;
  }

  private Kernel() {
    //SETUP
    //Initialise the queues for the Receiver -> Kernel and the Kernel -> Sender

    OutQ = new SegmentQueue<>();
    InQ = new SegmentQueue<>();
    PendingEmailOutbox = new LinkedList<>();
    Sender_ON = false;
  }


  private void run() throws DBInitializationException {

    //  Establish the Interface to the database. REMOVE credentials from hardcoding.
    try {
      DB = new DBInterface();
    } catch (DBInitializationException e) {
      //try again 3 times. If connection is impossible, end program in error.
      System.out.println("Failure 1 in attempting DB connection");
      try {
        DB = new DBInterface();
      } catch (DBInitializationException e1) {
        System.out.println("Failure 2 in attempting DB connection");
        try {
          DB = new DBInterface();
        } catch (DBInitializationException e2) {
          e.printStackTrace();
          throw new DBInitializationException(
              "After trying 3 times, no connection can be established.");
        }
      }
    }

    //Initialise the Sender
    try {
      EmailSender Sender = EmailSender.getEmailSender(OutQ);
      Sender_ON = true;
      EmailReceiver Rec = EmailReceiver.getEmailReceiver(InQ);
    } catch (FailedToInstantiateComponent failedToInstantiateComponent) {
      failedToInstantiateComponent.printStackTrace();
      Sender_ON = false;
      //This puts our system into a failed mode where sender is off, so only incoming emails are handled in some limited form.
    }

    //TODO: Initialise the DBMS port thread, if necessary

    System.out.println("Kernel: Receiver and Sender initialised.");
    // Create a thread, the major loop, named Major.
    DBInterface finalDB = DB;

    //  1. Poll periodically for any new emails to send - so track last time checked. Check every 5 mins.
    Thread DBPoll = new Thread() {
      @Override
      public void run() {
        while (true) {
          SendNewReminders();
          try {//TODO: Every 5 minutes when implemented, not 1
            MINUTES.sleep(1);
          } catch (InterruptedException e) {
            //ignore the exception.
          }
        }
      }

    };

    Thread Major = new Thread() {
      @Override
      public void run() {
        //Main Loop:

        while (true) {
          // 2. Deal with received emails one at a time until the queue is empty;
          if (InQ.NumWaiting() > 0) {
            System.out.println("Kernel<major>: email received, handling....");
            System.out.println("Kernel<major>: opening DB connection");
            synchronized (Kernel.class) {
              finalDB.openConnection();
              while (InQ.NumWaiting() > 0) {
                IncomingEmailMessage PatientResponse = InQ.take();
                System.out.println("\n\nKernel<major>: Patient email response taken from Rec.");
                //Work out which appointment ID this email is about from the header
                int appointmentID = -1;
                try {
                  appointmentID = Integer.parseInt(PatientResponse.getAppointmentID());
                } catch (Exception e) {//catching parsing exception
                  //no action, leave the apptID invalid at -1.
                }
                // 2a. Is the received email valid? check it's a patient on the database.
                System.out.println("    Appointment ID: " + appointmentID);
                System.out.println("    Sender Email: " + PatientResponse.getSenderEmailAddress());
                if (finalDB.confirmAppointmentExists(PatientResponse.getSenderEmailAddress(),
                    appointmentID)) {
                  //if a valid appointment....
                  // now check the sending email address is correct

                  // 2b. Fetch information on the history of this conversation, ie last response type, appointment time, doctor name, patient name.
                  System.out.println("    <Appointment Exists>");
                  Appointment bookedAppointment = finalDB.getApp(appointmentID);
                  Patient p = finalDB.getPatient(appointmentID);
                  if (p.getEmail().equals(PatientResponse.getSenderEmailAddress())) {
                    // 2b.i Add the email  to logging system
                    finalDB.addLog(appointmentID, PatientResponse.getMessage());

                    // 2c. Hand off to classifier: what type of message was it?
                    Classification C = null;
                    try {
                      C = new Classification(PatientResponse.getMessage(), model);
                    } catch (IOException e) {
                      //TODO: Handle this error properly. Why is it thrown?

                    }

                    assert (bookedAppointment.getAppID() == appointmentID);
                    switch (C.getDecision()) {
                      case CONFIRM:
                        //  Confirm Appt in database
                        System.out.println("Kernel<major>: message classified as CONFIRM");
                        finalDB.confirmNewTime(appointmentID);
                        OutQ.put(new OutgoingEmailMessage(p, bookedAppointment,
                            EmailMessageType.ConfirmationMessage));
                        System.out.println(
                            "Kernel<major>: email sent to show confirmation; DBMS informed of confirmation.");
                        break;
                      case CANCEL:
                        //Cancel Appt in database
                        System.out.println("Kernel<major>: message classified as CANCEL");
                        finalDB.rejectTime(appointmentID);
                        OutQ.put(new OutgoingEmailMessage(p, bookedAppointment,
                            EmailMessageType.CancellationMessage));
                        System.out.println(
                            "Kernel<major>: cancellation message sent; slot cancelled on database.");

                        break;
                      case RESCHEDULE:
                        // RESCHEDULE
                        //Poll database for available appointments in given time slots
                        System.out
                            .println("Kernel<major>: message classified as RESCHEDULE");
                        String[] availableDates = C.getDates();
                        //TODO: Ends of the timeslot are not implemented in the database, so should be targeted if posssible
                        List<Timeslot> all_available_timeslots = pollForAvailableSlots(
                            finalDB,
                            bookedAppointment.getDoctorID(), availableDates);
                        System.out.println("DB--> Available timeslots:");
                        for (Timeslot T : all_available_timeslots) {
                          System.out.println("      starting at: " + T.getStartTime());
                        }

                        if (all_available_timeslots.size() < 1) {
                          System.out.println("      <no available timeslots>");
                          //  Send email asking for new timeslots (the ones we were given do not work).
                          OutQ.put(new OutgoingEmailMessage(p.getEmail(), p.getName(),
                              bookedAppointment.getDoctorName(), "", "",
                              EmailMessageType.AskToPickAnotherTimeSlotMessage,
                              Integer.toString(
                                  appointmentID)));//We have empty strings given as time info as we have no time info!

                        } else {// we have a collection of >= 1 to choose from.
                          int selectedTimeslotID = all_available_timeslots.get(0).getID();
                          //cancel last one.
                          finalDB.rejectTime(appointmentID);
                          // block selected new appointment
                          finalDB.blockTimeSlot(selectedTimeslotID, appointmentID);
                          bookedAppointment = finalDB.getApp(appointmentID);
                          // Send SuggestedAppt email to patient to suggest the new time.
                          System.out.println("Kernel: new Appointment for the ID:");

                          OutQ.put(
                              new OutgoingEmailMessage(p, finalDB.getApp(appointmentID),
                                  EmailMessageType.NewAppointmentDetailsMessage));
                          System.out.println(
                              "Kernel<major>: Times updated in database; new appt details email sent.");
                        }
                        break;
                      case OTHER:
                        //No action taken on these emails.
                        System.out.println("Kernel<major>: message classified as OTHER");
                        break;
                      default:
                        System.out
                            .println("This new classification type should not exist.");
                        break;

                    }

                  } else {//appointment ID does not match patient listed email address - Malicious attack?
                    System.out
                        .println(
                            "    <Possible Phishing - apptID does not match patient email>");
                    //  Send invalidEmailAddress  Email.
                    OutQ.put(new OutgoingEmailMessage(
                        PatientResponse.getSenderEmailAddress(), "", "",
                        "", "",
                        EmailMessageType.InvalidEmailMessage, "-/-"));
                    System.out.println(
                        "Kernel<major>: message sent to unknown user to inform them we cannot take their emails.");
                  }
                } else {//invalid appointmentID...
                  System.out.println("    <Appointment Does Not Exist>");
                  //  Send invalidEmailAddress  Email.
                  OutQ.put(new OutgoingEmailMessage(
                      PatientResponse.getSenderEmailAddress(), "", "",
                      "", "",
                      EmailMessageType.InvalidEmailMessage, "-/-"));
                  System.out.println(
                      "Kernel<major>: message sent to unknown user to inform them we cannot take their emails.");
                }
                finalDB.closeConnection();
                System.out.println("Kernel<major>: closing connection on main thread.");
              }
            }
          } else {
            System.out.println("Kernel<major>: no new received emails");
            System.out.println("Kernel<major>: inQ length: " + InQ.NumWaiting());
            System.out.println(("Kernel<major>: outQ length: " + OutQ.NumWaiting()));
          }
          try {
            SECONDS.sleep(15);
          } catch (InterruptedException e) {
            //ignore
          }

        }
      }
    };

    if (Sender_ON) {
      DBPoll.start();
      System.out.println("Kernel: Database polling system set up.");
      Major.start();
    } else {
      System.err.println(
          "Without a validly set-up sender and/or receiver, this system should not attempt to" +
              "handle any current emails due to the likelihood of lost messages.");
    }
    // If the DBMS port is a thread in this, put it here. If it is a system with (another) producer consumer queue, check it between every handle of an incoming, and at the end of all of these.
  }

  private static List<Timeslot> pollForAvailableSlots(DBInterface connectedDB, int DoctorID,
      String[] GivenSlots) {
    LinkedList<Timeslot> all_available_timeslots = new LinkedList<>();

    System.out.println("Patient-suggested times:");
    System.out.println("    " + GivenSlots[0] + " to " + GivenSlots[1]);
    System.out.println("    " + GivenSlots[2] + " to " + GivenSlots[3]);
    System.out.println("    " + GivenSlots[4] + " to " + GivenSlots[5]);
    if (!GivenSlots[0].equals("") && !GivenSlots[1].equals("")) {
      all_available_timeslots
          .addAll(connectedDB.getAppointments(DoctorID, GivenSlots[0], GivenSlots[1]));
    }
    if (!GivenSlots[2].equals("") && !GivenSlots[3].equals("")) {
      all_available_timeslots
          .addAll(connectedDB.getAppointments(DoctorID, GivenSlots[2], GivenSlots[3]));
    }
    if (!GivenSlots[4].equals("") && !GivenSlots[5].equals("")) {
      all_available_timeslots
          .addAll(connectedDB.getAppointments(DoctorID, GivenSlots[4], GivenSlots[5]));
    }
    return all_available_timeslots;
  }


  private void SendNewReminders() {
    if (DB != null) {
      System.out.println("Kernel<pollDB>: beep, at " + LocalDateTime.now());
      synchronized (Kernel.class) {
        DB.openConnection();
        System.out.println("Kernel<pollDB>: DB Connection established");
        List<Appointment> newAppts = DB.remindersToSendToday();
        newAppts.removeAll(PendingEmailOutbox);
        System.out.println("Kernel<pollDB>: Found " + newAppts.size()
            + " new appointments to remind about in latest DB poll.");
        LinkedList<Appointment> sent_Apps = new LinkedList<>();
        for (Appointment A : newAppts) {
          //  1a. Send any initial reminder emails that are now shown as required by the database state, and not already on the pending queue.
          if (A != null) {
            System.out.println("Kernel<pollDB>: Appointment ID: " + A.getAppID());
            Patient p = DB.getPatient(A.getAppID());
            //TODO: remove this console print once shown to work:
            System.out.println("Trying to add new email to send queue, pending outbox length: "+PendingEmailOutbox.size());
            if (Sender_ON){
              if(IsAboutNewAppointment(A)) {
                OutQ.put(
                        new OutgoingEmailMessage(p, A, EmailMessageType.InitialReminderMessage));
                System.out.println(
                        "Kernel<pollDB>: Sent initial reminder message about appointment " + A
                                .getAppID());
                //sent_Apps.add(A);
                PendingEmailOutbox.add(A);
              }
              else System.out.println("Email on pending outbox list, not re-sent.");
            } else {
              System.err.println("Kernel<pollDB>: no sender, so email unsent.");
            }
          } else {
            System.err.println(
                "Kernel<pollDB>: appointment given by database is a NULL pointer***");
          }
        }
        //DB.RemindersSent(sent_Apps);
        DB.closeConnection();
        System.out.println("Kernel<pollDB>: DB Connection ended");
      }
    } else {
      System.out.println("Kernel<pollDB>: Database pointer is null.");
    }
  }
  private  boolean IsAboutNewAppointment(Appointment A){
    for(Appointment B : PendingEmailOutbox){
      if(A.getAppID() == B.getAppID()){
        return false;
      }
    }
    return true;
  }

  public static void Confirm_Intro_Email_Sent(String AppointmentID) {
    Kernel k = Kernel.getInstance();
    k.CIES(AppointmentID);
  }


  private void CIES(Appointment A) {
    System.out.println("Kernel: removed Appointment from pending outbox queue: No."+A.getAppID());
    if (!IsAboutNewAppointment(A)) {
      for(int I = 0; I< PendingEmailOutbox.size(); I++){ //remove all instances of this appointment in the outbox queue.
        if(PendingEmailOutbox.get(I).getAppID() == A.getAppID()){
          PendingEmailOutbox.remove(I);
          I--;
        }//else continue
      }
      LinkedList<Appointment> ConfirmedSent = new LinkedList<>();
      ConfirmedSent.add(A);
      synchronized (Kernel.class) {
          DB.openConnection();
          DB.RemindersSent(ConfirmedSent);
          DB.closeConnection();
      }
      System.out.println(
          "Kernel: Confirmed initial message about appointment No." + A.getAppID() + " was sent.");
    } else {
      System.err.println("Trying to confirm an intro email not in the pending outbox.");
    }
  }

  private void CIES(String AppointmentID) {
    Appointment A = null;
    for (Appointment Appt : PendingEmailOutbox) {
      try {
        if (Appt.getAppID() == Integer.parseInt(AppointmentID)) {
          A = Appt;
        }
      } catch (Exception e) {//from parsing invalid string, ie not a number
        System.err.println(
            "Kernel: Failed to parse appointment ID given by Sender to confirm email sent.");
      }
    }
    if (A != null) {
      CIES(A);
    }
  }


  public static void main(String[] args) throws DBInitializationException {
    Kernel k = Kernel.getInstance();
    k.run();
  }
}