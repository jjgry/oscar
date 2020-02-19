package MailingServices;

public class OutgoingEmailMessage {
    private String patientEmailAddress;
    private String patientName;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private EmailMessageType messageType;

    //Leave blank fields for unused variables
    public OutgoingEmailMessage( String patientEmailAddress, String patientName, String doctorName, String appointmentDate, String appointmentTime, EmailMessageType messageType ) {
        this.patientEmailAddress = patientEmailAddress;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
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
}
