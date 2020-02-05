import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class DBConnectionTrial {

  public static void main(String[] args) {
    String CONNECTION = "jdbc:mysql://db5000288829.hosting-data.io:3306/dbs282068?characterEncoding=utf8";
    String USERNAME = "dbu102546";
    String PASSWORD = "TeamOscar6??";
//    String TRIALCOMMAND = "select app_id, name, patient_email, doctor_name, timeslot from SurgeryAssistant.Appointments LEFT JOIN SurgeryAssistant.Patients on patient_email=email_id LEFT JOIN SurgeryAssistant.Doctors on doctor=doctor_id LEFT JOIN SurgeryAssistant.Timeslots on SurgeryAssistant.Appointments.timeslot_id=SurgeryAssistant.Timeslots.timeslot_id WHERE DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 10 DAY) OR DATE(timeslot)=DATE_ADD(CURDATE(), INTERVAL 7 DAY);";
    String TRIALCOMMAND = "SELECT * from db777778088";

    Connection con = null;

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    try {
      con = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);

      Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs =  stmt.executeQuery(TRIALCOMMAND);

      rs.last();
      System.out.println(rs.getRow());

    } catch (SQLException e) {
      System.err.println("Error opening connection: " + e.getMessage());
    } finally {
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          System.err.println("Error closing connection: " + e.getMessage());
        }
      }
    }
  }

}

