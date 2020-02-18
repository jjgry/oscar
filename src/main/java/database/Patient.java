package database;

public class Patient {
  int patient_id;
  String name;
  String email;

  public void setEmail(String email) {
    this.email = email;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPatient_id(int patient_id) {
    this.patient_id = patient_id;
  }

  public int getPatient_id() {
    return patient_id;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }
}