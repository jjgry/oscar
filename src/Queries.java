public class Queries {

    public static final String GET_APP_TO_REMIND = "select app_id, name, patient_email, doctor_name, timeslot from SurgeryAssistant.Appointments\n" +
            "LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id\n" +
            "LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id\n" +
            "LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id\n" +
            "WHERE DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 1 DAY) OR DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 9 DAY);";

    public static final String GET_APPS = "";

    public static final String REJECT_APP = "";

}
