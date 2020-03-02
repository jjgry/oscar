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
        formatDate(appointmentDateTime);
        this.messageType = messageType;
        this.appointmentID = appointmentID;
    }

    public OutgoingEmailMessage(Patient p, Appointment a, EmailMessageType messageType) {
        this.patientEmailAddress = p.getEmail();
        this.patientName = p.getName();
        this.doctorName = a.getDoctorName();
        formatDate(a.getDatetime());
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

    private void formatDate(String input_DateTime) {
        String[] datetimesplit = input_DateTime.split(" ");
        if (datetimesplit.length >= 2) {
            this.appointmentDate = datetimesplit[0];
            this.appointmentTime = datetimesplit[1];
            System.out.println("Sender<DateFormatting>: split string \"" + input_DateTime + "\" into \"" + this.appointmentDate + "\" and \"" + this.appointmentTime + "\"");

            //format Date
            String[] sections = this.appointmentDate.split("-");
            if (sections.length >= 3) {
                String year = sections[0];//TODO: this is a bit hacky
                String month = sections[1];
                switch (sections[1]) {
                    case "01":
                        month = "January";
                        break;
                    case "02":
                        month = "February";
                        break;
                    case "03":
                        month = "March";
                        break;
                    case "04":
                        month = "April";
                        break;
                    case "05":
                        month = "May";
                        break;
                    case "06":
                        month = "June";
                        break;
                    case "07":
                        month = "July";
                        break;
                    case "08":
                        month = "August";
                        break;
                    case "09":
                        month = "September";
                        break;
                    case "10":
                        month = "October";
                        break;
                    case "11":
                        month = "November";
                        break;
                    case "12":
                        month = "December";
                }
                this.appointmentDate = sections[2] + " " + month + " " + year;
            }
            //format Time
            String[] timeSections = this.appointmentTime.split(".");
            if (timeSections.length > 1) {
                this.appointmentTime = timeSections[0];//cut anything after a decimal point
            }

            timeSections = this.appointmentTime.split(":");
            if (timeSections.length >= 3) {
                this.appointmentTime = timeSections[0] + ":" + timeSections[1];//cut anything after an initial HH:MM
            }

        } else {
            this.appointmentDate = input_DateTime;
            this.appointmentTime = input_DateTime;
        }
    }
}
