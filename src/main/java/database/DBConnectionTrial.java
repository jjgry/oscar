package database;

/**
 * Simple class for testing accessing the database.
 */
class DBConnectionTrial {

  public static void main(String[] args) {
    connectionTesting();
  }

  public static void connectionTesting() {

    // initialize database interface
    DBInterface db;
    try {
      db = new DBInterface();
    } catch (DBInitializationException e) {
      e.printStackTrace();
      db = null;
    }

    // open a connection and run a basic command
    if (db != null) {
      db.openConnection();

      Appointment app = db.getApp(1);
      System.out.println(app.getPatientName());

      db.closeConnection();
    }
  }
}
