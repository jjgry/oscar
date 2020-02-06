import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides functions for the kernel to call to access and modify the database
 */
public class DBInterface {

  private DBConnection database;
  private String username;
  private String password;
  private String ip;


  /**
   * Initialises the database connection object
   */
  public DBInterface (String username, String password) {
    database = new DBConnection("10.248.114.7", "jj", "teamoscar");
  }

  /**
   * @return whether a new connection has been made successfully
   */
  public boolean openConnection() {
    return database.newConnection();
  }

  /**
   * @return whether the connection to the database has been terminated successfully
   */
  public boolean closeConnection() {
    return database.endConnection();
  }


  /**
   * @return a representation of the email addresses and appointment information.
   */
  public List<Appointment> remindersToSendToday() {

    ResultSet rs = database.execute(Queries.GET_APP_TO_REMIND);
    List<Appointment> appointmentList = new ArrayList<>();
    try {
      while (rs.next())
      {
        Appointment app = new Appointment();
        app.setAppID(rs.getInt("app_id"));
        app.setDatetime(rs.getString("timeslot"));
        app.setDoctorName(rs.getString("doctor_name"));
        app.setPatientEmail(rs.getString("patient_email"));
        app.setPatientName(rs.getString("name"));
        appointmentList.add(app);
      }
      return appointmentList;
    }
    catch (SQLException e)
    {
      System.out.println("SQLException occured.");
    }


    // must deal with runtime exceptions such as timeout somewhere

    // make data into readable form for kernel

    return null;
  }

  /**
   * @param doctor the doctor we want appointments for
   * @param startDate look for dates after
   * @param endDate look for dates before
   * @return a representation the available appointments for the patient
   */
  public List<Appointment> getAppointments(String doctor, String startDate, String endDate) {
    return null;
  }

  /**
   * Update the DB to confirm the new appointment time
   *
   * @param appointmentID the appointment to be confirmed
   * @return true if the update was successful
   */
  public boolean confirmNewTime(String appointmentID) {
    return false;
  }

  /**
   * Update the DB to reject the new appointment time
   *
   * @param appointmentID the appointment to be rejected
   * @return true if the update was successful
   */
  public boolean rejectNewTime(String appointmentID) {
    return false;
  }

  /**
   * After a patient has booked an appointment with the surgery, this is how it is added
   *
   * @param time time of the appointment
   * @param doctor doctor the appointment is with
   * @param patientID patient who's appointment it is
   * @return true if update was successful
   */
  public boolean addNewAppointment(String time, String doctor, String patientID){
    return false;
  }

  /**
   * @param emailID the email address of the patient
   * @return the patient ID associated with the email address
   */
  public String getPatientID(String emailID) {
    return null;
  }

  /**
   * Allows kernel to confirm a patient has an appointment with the given ID
   *
   * @param patientID the identifier used for the patient
   * @param appointmentID the identifier used for thr appointment
   * @return true if said patient has the given appointment
   */
  public boolean confirmAppointmentExists(String patientID, String appointmentID) {
    return false;
  }


}
