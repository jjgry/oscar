package MailingServices;

import database.Appointment;
import database.Patient;

public class OutgoingEmailMessage {
    private String patientEmailAddress;
    private String patientName;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private EmailMessageType messageType;
    private String appointmentID;

    //Leave blank fields for unused variables
    public OutgoingEmailMessage(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime,
            EmailMessageType messageType,
            String appointmentID) {
        this.patientEmailAddress = patientEmailAddress;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.messageType = messageType;
        this.appointmentID = appointmentID;
    }

    // this constructor will take a combined datetime and separate them out.
    public OutgoingEmailMessage(String patientEmailAddress, String patientName, String doctorName, String appointmentDateTime, EmailMessageType messageType, String appointmentID) {
        this.patientEmailAddress = patientEmailAddress;
        this.patientName = patientName;
        this.doctorName = doctorName;
        String[] datetimesplit = appointmentDateTime.split(" ");
        if (datetimesplit.length >= 2) {
            this.appointmentDate = datetimesplit[0];
            this.appointmentTime = datetimesplit[1];
            System.out.println("Sender<DateFormatting>: split string \"" + appointmentDateTime + "\" into \"" + this.appointmentDate + "\" and \"" + this.appointmentTime + "\"");
        } else {
            this.appointmentDate = appointmentDateTime;
            this.appointmentTime = appointmentDateTime;
        }
        this.messageType = messageType;
        this.appointmentID = appointmentID;
    }

    public OutgoingEmailMessage(Patient p, Appointment a, EmailMessageType messageType) {
        this.patientEmailAddress = p.getEmail();
        this.patientName = p.getName();
        this.doctorName = a.getDoctorName();
        String[] datetimesplit = a.getDatetime().split(" ");
        if (datetimesplit.length >= 2) {
            this.appointmentDate = datetimesplit[0];
            this.appointmentTime = datetimesplit[1];
            System.out.println("Sender<DateFormatting>: split string \"" + a.getDatetime() + "\" into \"" + this.appointmentDate + "\" and \"" + this.appointmentTime + "\"");
        } else {
            this.appointmentDate = a.getDatetime();
            this.appointmentTime = a.getDatetime();
        }

        this.appointmentID = Integer.toString(a.getAppID());
        this.messageType = messageType;
    }


    public String getPatientEmailAddress() {
        return patientEmailAddress;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public EmailMessageType getMessageType() {
        return messageType;
    }

    public String getAppointmentID() {
        return appointmentID;
    }
}
