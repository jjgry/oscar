import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides functions for the kernel to call to access and modify the database
 */
public class DBInterface {

  private DBConnection database;

  /**
   * Initialises the database connection object
   */
  public DBInterface (String ip, String username, String password) {
    database = new DBConnection(ip, username, password);
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
    ResultSet rs1 = database.execute(Queries.GET_APP_TO_REMIND_1_DAY);
    ResultSet rs2 = database.execute(Queries.GET_APP_TO_REMIND_7_DAY);
    List<Appointment> appointmentList = new ArrayList<>();
    List<Integer> conv_id = new ArrayList<>();
    try {
      while (rs1.next()) {
        Appointment app = new Appointment();

        conv_id.add(rs1.getInt("conversation_state_id"));
        app.setAppID(rs1.getInt("app_id"));
        app.setDatetime(rs1.getString("timeslot"));
        app.setDoctorName(rs1.getString("doctor_name"));
        app.setPatientEmail(rs1.getString("patient_email"));
        app.setPatientName(rs1.getString("name"));
        appointmentList.add(app);
      }
      for(Integer i : conv_id)
      {
        database.executeUpdate(String.format(Queries.MARK_REMINDED, 1, i));
      }
      conv_id = new ArrayList<>();
      while(rs2.next()) {
        Appointment app = new Appointment();

        conv_id.add(rs2.getInt("conversation_state_id"));
        app.setAppID(rs2.getInt("app_id"));
        app.setDatetime(rs2.getString("timeslot"));
        app.setDoctorName(rs2.getString("doctor_name"));
        app.setPatientEmail(rs2.getString("patient_email"));
        app.setPatientName(rs2.getString("name"));
        appointmentList.add(app);
      }
      for(Integer i : conv_id)
      {
        database.executeUpdate(String.format(Queries.MARK_REMINDED, 7, i));
      }
      return appointmentList;
    }

    catch (SQLException e) {
      System.out.println("Exception in iterating over ResultSet: " + e.getMessage());
    }
    return null;
  }

  /**
   * @param doctorID the doctor we want appointments for
   * @param startDatetime look for dates after
   * @param endDatetime look for dates before
   * @return a representation the available appointments for the patient
   */
  public List<Timeslot> getAppointments(int doctorID, String startDatetime, String endDatetime) {
    ResultSet rs = database.execute(String.format(Queries.GET_APPS, startDatetime, endDatetime, doctorID));
    List<Timeslot> appointmentList = new ArrayList<>();
    try {
      while (rs.next()) {
        Timeslot app = new Timeslot();

       String startime = rs.getString("timeslot");
       startime = startime.substring(0, startime.length()-2);
       app.setStartTime(startime);
        app.setDoctorID(rs.getInt("doctor_id"));
        app.setLocation(rs.getString("location"));

        appointmentList.add(app);
      }
      return appointmentList;
    }
    catch (SQLException e) {
      System.out.println("Exception in iterating over ResultSet: " + e.getMessage());
    }
    return null;
  }

  /**
   * Update the DB to confirm the new appointment time
   *
   * @param appointmentID the appointment to be confirmed
   * @return true if the update was successful
   */
  public boolean confirmNewTime(String appointmentID) {
    // nothing changes in the database so do nothing
    return true;
  }

  /**
   * Update the DB to reject the appointment time
   *
   * @param appointmentID the appointment to be rejected
   * @return true if the update was successful
   */
  public boolean rejectTime(int appointmentID) {

    return database.executeUpdate(String.format(Queries.REJECT_APP, appointmentID));

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
    // Do for phone number too in extended
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

  public Appointment getApp(int appointmentID) {
    ResultSet rs = database.execute(String.format(Queries.GET_APP_FROM_ID, appointmentID));
    List<Appointment> appointmentList = new ArrayList<>();
    try {
      while (rs.next()) {
        Appointment app = new Appointment();
        app.setAppID(rs.getInt("app_id"));
        app.setDatetime(rs.getString("timeslot"));
        app.setDoctorName(rs.getString("doctor_name"));
        app.setPatientEmail(rs.getString("patient_email"));
        app.setPatientName(rs.getString("name"));
        appointmentList.add(app);
      }
      if (appointmentList.size() == 1) {
        return appointmentList.get(0);
      } else {
        return null;
      }
    }
    catch (SQLException e) {
      System.out.println("Exception in iterating over ResultSet: " + e.getMessage());
    }
    return null;
  }

  public String getPatientName(String patientEmail) {
    ResultSet rs = database.execute(String.format(Queries.GET_NAME, patientEmail));
    String name = null;
    try {
      name = rs.getString("name");
    } catch (SQLException e) {
      System.out.println("Excaption in reading name from ResultSet: " + e.getMessage());
    }
    return name;
  }

  public boolean addLog(String sender, String receiver, String subject, String body) {
    return false;
  }

}
