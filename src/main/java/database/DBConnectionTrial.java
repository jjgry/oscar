package database;

import javax.swing.JFrame;

/**
 * Simple class for testing accessing the database.
 */
class DBConnectionTrial {

  private DBInterface DB;

  private DBConnectionTrial() throws DBInitializationException {
    DB = new DBInterface();
  }

  public static void main(String[] args) {
    // initialize database interface
    DBConnectionTrial dbTrial;
    try {
      dbTrial = new DBConnectionTrial();
    } catch (DBInitializationException e) {
      return;
    }
    System.out.println("success");
    dbTrial.connectionTesting();
//    dbTrial.progressMeetingDemo();
  }

  private void connectionTesting() {
    DB.openConnection();

    Appointment app = DB.getApp(1);
    System.out.println(app.getPatientName());

    DB.closeConnection();
  }

  private void progressMeetingDemo() {
//    DB.addNewAppointment("2020/02/16 12:00:00", "Dr E Jackson", "12");
  }


}
