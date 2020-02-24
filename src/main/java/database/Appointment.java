package database;

public class Appointment {

  private int appID;
  private String patientEmail;
  private String patientName;
  private String doctorName;
  private String datetime;
  private int doctorID;


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
}
