package database;

public class Timeslot {

  private int ID;
  private int doctorID;
  private String startTime;
  private String location;

  public void setID(int ID) {
    this.ID = ID;
  }

  public void setDoctorID(int doctorID) {
    this.doctorID = doctorID;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public int getDoctorID() {
    return doctorID;
  }

  public String getLocation() {
    return location;
  }

  public String getStartTime() {
    return startTime;
  }

  public int getID() {
    return ID;
  }

}
