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

    //TODO: this constructor will take a combined datetime and separate them out.
    public OutgoingEmailMessage( String patientEmailAddress, String patientName, String doctorName, String appointmentDateTime, EmailMessageType messageType ) {
        this.patientEmailAddress = patientEmailAddress;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDateTime;//TODO
        this.appointmentTime = appointmentDateTime;//TODO
        this.messageType = messageType;
    }

    public OutgoingEmailMessage( Patient p, Appointment a, EmailMessageType messageType ) {
        this.patientEmailAddress = p.getEmail();
        this.patientName = p.getName();
        this.doctorName = a.getDoctorName();
        this.appointmentDate = a.getDatetime();//TODO
        this.appointmentTime = a.getDatetime();//TODO
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
