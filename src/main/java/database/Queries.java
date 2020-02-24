package database;

class Queries {

  static final String GET_APP_TO_REMIND_1_DAY =
      "SELECT app_id,patient_name, patient_email, doctor_name, timeslot, `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id from `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Appointments`.patient_id = `jjag3/SurgeryAssistant`.`Patients`.patient_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Doctors` on doctor=doctor_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Conversation State` on `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id = `jjag3/SurgeryAssistant`.`Conversation State`.conversation_state_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Timeslots` on `jjag3/SurgeryAssistant`.`Appointments`.timeslot_id= `jjag3/SurgeryAssistant`.`Timeslots`.timeslot_id\n" +
      "WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND 1_day_reminder_sent = 0);";

  static final String GET_APP_TO_REMIND_7_DAY =
      "SELECT app_id,patient_name, patient_email, doctor_name, timeslot, `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id from `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Appointments`.patient_id = `jjag3/SurgeryAssistant`.`Patients`.patient_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Doctors` on doctor=doctor_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Conversation State` on `jjag3/SurgeryAssistant`.`Appointments`.conversation_state_id = `jjag3/SurgeryAssistant`.`Conversation State`.conversation_state_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Timeslots` on `jjag3/SurgeryAssistant`.`Appointments`.timeslot_id= `jjag3/SurgeryAssistant`.`Timeslots`.timeslot_id\n" +
      "WHERE (DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND 7_day_reminder_sent = 0);";

  static final String MARK_REMINDED =
      "UPDATE `jjag3/SurgeryAssistant`.`Conversation State`\n" +
      "SET %1$s_day_reminder_sent = 1 \n" +
      "WHERE conversation_state_id = %2$s";

  static final String GET_APPS =
      "SELECT timeslot, doctor_id, location FROM  `jjag3/SurgeryAssistant`.`Timeslots` \n" +
      "WHERE (timeslot between ? AND ?) AND (doctor_id = ?);";

  static final String REJECT_APP =
      "UPDATE `jjag3/SurgeryAssistant`.`Timeslots` \n" +
      "SET available = 1\n" +
      "WHERE timeslot_id = ( SELECT timeslot_id from `jjag3/SurgeryAssistant`.`Appointments` where app_id = ?) ;";

  static final String GET_APP_FROM_ID =
      "SELECT app_id, patient_name, patient_email, `jjag3/SurgeryAssistant`.`Appointments`.doctor, doctor_name, timeslot from `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Patients`.patient_id= `jjag3/SurgeryAssistant`.`Appointments`.patient_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Doctors` on doctor=doctor_id\n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Timeslots` on `jjag3/SurgeryAssistant`.`Appointments`.timeslot_id=`jjag3/SurgeryAssistant`.`Timeslots`.timeslot_id\n" +
      "WHERE app_id = ?";

  static final String CONFIRM_APP_FOR_PATIENT =
      "SELECT app_id  from `jjag3/SurgeryAssistant`.`Appointments` \n" +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Appointments`.patient_id = `jjag3/SurgeryAssistant`.`Patients`.patient_id\n" +
      "WHERE patient_email = ? AND app_id =?";

  static final String GET_NAME =
      "SELECT patient_name from `jjag3/SurgeryAssistant`.`Patients` WHERE patient_id = ?";

  static final String ADD_LOG =
      "INSERT INTO `jjag3/SurgeryAssistant`.`Logs` VALUES(?, ?)";

  public static final String GET_PATIENTS =
      "SELECT patient_id, patient_name from `jjag3/SurgeryAssistant`.`Patients`\n" +
      "WHERE patient_email =?;";

  static final String GET_PATIENT =
      "SELECT `jjag3/SurgeryAssistant`.`Appointments`.patient_id, patient_name, patient_email from `jjag3/SurgeryAssistant`.`Appointments` " +
      "LEFT JOIN `jjag3/SurgeryAssistant`.`Patients` on `jjag3/SurgeryAssistant`.`Patients`.patient_id= `jjag3/SurgeryAssistant`.`Appointments`.patient_id WHERE app_id = ?";

  static final String BLOCK_TIMESLOT =
      "UPDATE `jjag3/SurgeryAssistant`.`Appointments`\n" +
      "SET timeslot_id = %1$s\n" +
      "WHERE app_id = %2$s";

  static final String REMOVE_TIMESLOTS =
      "DELETE FROM `jjag3/SurgeryAssistant`.`Timeslots` WHERE (timeslot  < DATE_ADD(CURDATE(), INTERVAL -1 MONTH))\n";

  static final String REMOVE_CONVERSATION_STATES = "DELETE FROM `jjag3/SurgeryAssistant`.`Conversation State`\n" +
      "WHERE conversation_state_id NOT IN (SELECT a.conversation_state_id\n" +
      "FROM `jjag3/SurgeryAssistant`.`Appointments` a)\n";

  static final String REMOVE_LOGS = "DELETE FROM `jjag3/SurgeryAssistant`.`Logs`\n" +
      "WHERE app_id NOT IN (SELECT a.app_id\n" +
      "FROM `jjag3/SurgeryAssistant`.`Appointments` a)\n";
}
