public class Queries {

    public static final String GET_APP_TO_REMIND_1_DAY = "select app_id, name, patient_email, doctor_name, timeslot, conversation_state_id from SurgeryAssistant.Appointments\n" +
            "LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id\n" +
            "LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id\n" +
            "LEFT JOIN `SurgeryAssistant`.`Conversation State` on conversation_state_id = conversation_state_id\n" +
            "LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id\n" +
            "WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND 1_day_reminder_sent = 0)";

    public static final String GET_APP_TO_REMIND_7_DAY = "select app_id, name, patient_email, doctor_name, timeslot from SurgeryAssistant.Appointments\n" +
            "LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id\n" +
            "LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id\n" +
            "LEFT JOIN `SurgeryAssistant`.`Conversation State` on conversation_state_id = conversation_state_id\n" +
            "LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id\n" +
            "WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 9 DAY) or 7_day_reminder_sent = 0);";
    
    public static final String MARK_REMINDED = "UPDATE `SurgeryAssistant`.`Conversation State`\n" +
            "SET %1$s_day_reminder_sent = 1 \n" +
            "where conversation_state_id = %2$s";
    public static final String GET_APPS = "SELECT timeslot, doctor_id, location FROM SurgeryAssistant.Timeslots \n" +
            "where (timeslot between \"%1$s\" AND \"%2$s\") AND (doctor_id = %3$s);";

    public static final String REJECT_APP = "UPDATE SurgeryAssistant.Timeslots \n" +
            "SET \n" +
            "\tavailable = 1\n" +
            "where timeslot_id = ( SELECT timeslot_id from SurgeryAssistant.Appointments where app_id = %s) ;";

    public static final String GET_APP_FROM_ID = "SELECT app_id, name, patient_email, doctor_name, timeslot from SurgeryAssistant.Appointments\n" +
        "LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id\n" +
        "LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id\n" +
        "LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id\n" +
        "WHERE app_id = %s";

    public static final String GET_NAME = "SELECT name from SurgeryAssitant.Patients WHERE email_id = %s";
}
