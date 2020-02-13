package oscar;

import MailingServices.OutgoingEmailMessage;

public class MessageToPatient extends OutgoingEmailMessage {
    private String patientName;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private EmailMessageType messageType;

    //Leave blank field for unused variables
    public MessageToPatient( String email, String patientName, String doctorName, String appointmentDate, String appointmentTime, EmailMessageType messageType) {
        super(email);
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
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
