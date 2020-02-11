package database;

public class Appointment {

  private int appID;
  private String patientEmail;
  private String patientName;
  private String doctorName;
  private String datetime;

  public void setPatientEmail(String e) {
    patientEmail = e;
  }

  public void setAppID(int id) {
    appID = id;
  }

  public void setPatientName(String n) {
    patientName = n;
  }

  public void setDoctorName(String n) {
    doctorName = n;
  }

  public void setDatetime(String d) {
    datetime = d;
  }

  public int getAppID() {
    return appID;
  }

  public String getDatetime() {
    return datetime;
  }

  public String getDoctorName() {
    return doctorName;
  }

  public String getPatientEmail() {
    return patientEmail;
  }

  public String getPatientName() {
    return patientName;
  }

  static class Queries {

    static final String GET_APP_TO_REMIND_1_DAY =
        "select app_id, name, patient_email, doctor_name, timeslot, conversation_state_id from SurgeryAssistant.Appointments\n" +
            "LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id\n" +
            "LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id\n" +
            "LEFT JOIN `SurgeryAssistant`.`Conversation State` on conversation_state_id = conversation_state_id\n" +
            "LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id\n" +
            "WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND 1_day_reminder_sent = 0)";

    static final String GET_APP_TO_REMIND_7_DAY =
        "select app_id, name, patient_email, doctor_name, timeslot from SurgeryAssistant.Appointments\n" +
            "LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id\n" +
            "LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id\n" +
            "LEFT JOIN `SurgeryAssistant`.`Conversation State` on conversation_state_id = conversation_state_id\n" +
            "LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id\n" +
            "WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 9 DAY) or 7_day_reminder_sent = 0);";

    static final String MARK_REMINDED =
        "UPDATE `SurgeryAssistant`.`Conversation State`\n" +
            "SET %1$s_day_reminder_sent = 1 \n" +
            "WHERE conversation_state_id = %2$s";

    static final String GET_APPS =
        "SELECT timeslot, doctor_id, location FROM SurgeryAssistant.Timeslots \n" +
            "WHERE (timeslot between \"%1$s\" AND \"%2$s\") AND (doctor_id = %3$s);";

    static final String REJECT_APP =
        "UPDATE SurgeryAssistant.Timeslots \n" +
            "SET \n" +
            "\tavailable = 1\n" +
            "WHERE timeslot_id = ( SELECT timeslot_id from SurgeryAssistant.Appointments where app_id = %s) ;";

    static final String GET_APP_FROM_ID =
        "SELECT app_id, name, patient_email, doctor_name, timeslot from jjag3/SurgeryAssistant.Appointments \n" +
            "LEFT JOIN jjag3/SurgeryAssistant.Patients on patient_email=email_id \n" +
            "LEFT JOIN jjag3/SurgeryAssistant.Doctors on doctor=doctor_id \n" +
            "LEFT JOIN jjag3/SurgeryAssistant.Timeslots on jjag3/SurgeryAssistant.Appointments.timeslot_id=jjag3/SurgeryAssistant.Timeslots.timeslot_id \n" +
            "WHERE app_id = %s";

    static final String GET_NAME = "SELECT name from SurgeryAssitant.Patients WHERE email_id = %s";
  }
}
