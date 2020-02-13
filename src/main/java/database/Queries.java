package database;

class Queries {

  static final String GET_APP_TO_REMIND_1_DAY = "select app_id,patient_name, patient_email, doctor_name, timeslot, `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id from `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Appointments`.patient_id = `jjag3/SurgeryAssistant`.`Patients`.patient_id\n" +
      "            LEFT JOIN `jjag3/SurgeryAssistant`.`Doctors` on doctor=doctor_id\n" +
      "            LEFT JOIN `jjag3/SurgeryAssistant`.`Conversation State` on `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id = `jjag3/SurgeryAssistant`.`Conversation State`.conversation_state_id\n" +
      "            LEFT JOIN `jjag3/SurgeryAssistant`.`Timeslots` on `jjag3/SurgeryAssistant`.`Appointments`.timeslot_id= `jjag3/SurgeryAssistant`.`Timeslots`.timeslot_id\n" +
      "            WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND 1_day_reminder_sent = 0);";

  static final String GET_APP_TO_REMIND_7_DAY = "select app_id,patient_name, patient_email, doctor_name, timeslot, `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id from `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Appointments`.patient_id = `jjag3/SurgeryAssistant`.`Patients`.patient_id\n" +
      "            LEFT JOIN `jjag3/SurgeryAssistant`.`Doctors` on doctor=doctor_id\n" +
      "            LEFT JOIN `jjag3/SurgeryAssistant`.`Conversation State` on `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id = `jjag3/SurgeryAssistant`.`Conversation State`.conversation_state_id\n" +
      "            LEFT JOIN `jjag3/SurgeryAssistant`.`Timeslots` on `jjag3/SurgeryAssistant`.`Appointments`.timeslot_id= `jjag3/SurgeryAssistant`.`Timeslots`.timeslot_id\n" +
      "            WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND 7_day_reminder_sent = 0);";

  static final String MARK_REMINDED = "UPDATE `jjag3/SurgeryAssistant`.`Conversation State`\n" +
      "SET %1$s_day_reminder_sent = 1 \n" +
      "where conversation_state_id = %2$s";
  static final String GET_APPS = "SELECT timeslot, doctor_id, location FROM  `jjag3/SurgeryAssistant`.`Timeslots` \n" +
      "where (timeslot between \"%1$s\" AND \"%2$s\") AND (doctor_id = %3$s);";

  static final String REJECT_APP = "UPDATE `jjag3/SurgeryAssistant`.`Timeslots` \n" +
      "SET \n" +
      "\tavailable = 1\n" +
      "where timeslot_id = ( SELECT timeslot_id from `jjag3/SurgeryAssistant`.`Appointments` where app_id = %s) ;";

  static final String GET_APP_FROM_ID = "SELECT app_id, patient_name, patient_email, doctor_name, timeslot from `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Patients`.patient_id= `jjag3/SurgeryAssistant`.`Appointments`.patient_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Doctors` on doctor=doctor_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Timeslots` on `jjag3/SurgeryAssistant`.`Appointments`.timeslot_id=`jjag3/SurgeryAssistant`.`Timeslots`.timeslot_id\n" +
      "WHERE app_id = %s";
  
  static final String CONFIRM_APP_FOR_PATIENT = "SELECT app_id  from `jjag3/SurgeryAssistant`.`Appointments` \n" +
            "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Appointments`.patient_id = `jjag3/SurgeryAssistant`.`Patients`.patient_id\n" +
            "WHERE patient_email = \"%1$s\" AND app_id = %2$s;";

  static final String GET_NAME = "SELECT patient_name from `jjag3/SurgeryAssistant`.`Patients` WHERE patient_id = %s";
  static final String ADD_LOG = "INSERT INTO `jjag3/SurgeryAssistant`.`Logs` VALUES(%1$s, \"%2$s\")";
}
