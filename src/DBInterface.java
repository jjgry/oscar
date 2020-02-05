/**
 * Provides functions for the kernel to call to access and modify the database
 */
public class DBInterface {

  private DBConnection connection;

  /**
   * Initialises the connection to the database
   */
  public DBInterface (String username, String password) {
    connection = new DBConnection("test", "jj", "teamoscar");
  }

  /**
   * Returns a JSON representation of the reminders that are to be sent
   */
  public void remindersToSendToday() {

  }

  /**
   * Returns a JSON representation of the available appointments
   *
   * @param doctor the doctor we want appointments for
   * @param startDate look for dates after
   * @param endDate look for dates before
   */
  public void getAppointments(String doctor, String startDate, String endDate) {

  }

  /**
   * Update the DB to confirm the new appintment time
   *
   * @param appointmentID the appointment to be confirmed
   */
  public void confirmNewTime(String appointmentID) {

  }

  /**
   * Update the DB to reject the new appintment time
   *
   * @param appointmentID the appointment to be rejected
   */
  public void rejectNewTime(String appointmentID) {

  }

  /**
   * After a patient has booked an appointment with the suegery, this is how it is added
   *
   * @param time time of the appointment
   * @param doctor doctor the appointment is with
   * @param patientID patient who's appointment it is
   */
  public void addNewAppointment(String time, String doctor, String patientID){

  }

  /**
   * Get the patientID associated with the given email address
   *
   * @param emailID the email address of the patient
   */
  public void getPatientID(String emailID) {

  }

  /**
   * Not sure what this does?
   *
   * @param patientID the patient associated with the appointment
   * @param appointmentID the appointment identifier
   */
  public void confirmAppointmentExists(String patientID, String appointmentID) {

  }

}
