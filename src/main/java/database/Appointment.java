package database;

public class Appointment {

  enum ReminderType {
    OneDay,
    SevenDay
  }

  private int appID;
  private String patientEmail;
  private String patientName;
  private String doctorName;
  private String datetime;
  private int doctorID;
  private int conversationStateID;
  private ReminderType reminderType;


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

  public void setDoctorID(int id) {
    doctorID = id;
  }

  public void setConversationStateID(int conversationStateID) {
    this.conversationStateID = conversationStateID;
  }

  public void setReminderType(ReminderType reminderType) {
    this.reminderType = reminderType;
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

  public int getDoctorID() {
    return doctorID;
  }

  public String getPatientEmail() {
    return patientEmail;
  }

  public String getPatientName() {
    return patientName;
  }

  public int getConversationStateID() {
    return conversationStateID;
  }

  public ReminderType getReminderType() {
    return reminderType;
  }
}
