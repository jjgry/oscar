package database;

import java.util.List;
import java.util.Scanner;

/**
 * Simple class for testing accessing the database.
 */
class DBConnectionTrial {

  public static void main(String[] args) {
    String username;
    String password;

    // get login credentials
    Scanner scanner = new Scanner(System.in);
    System.out.print("Username: ");
    username = scanner.nextLine();
    System.out.print("Password: ");
    password = scanner.nextLine();

    // initialize database interface
    DBInterface db = null;
    try {
      db = new DBInterface("127.0.0.1:9876", username, password);
    } catch (DBInitializationException e) {
      e.printStackTrace();
      db = null;
    }

    // open a connection and run a basic command
    if (db != null) {
      db.openConnection();

      Appointment app = db.getApp(1);
      System.out.println(app.getPatientName());

//      List<Appointment> apps = db.remindersToSendToday();
//      for(Appointment a : apps) {
//        System.out.println(a.getPatientName());
//      }

      db.closeConnection();
    }
  }
}
