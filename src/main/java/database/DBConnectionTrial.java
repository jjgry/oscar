package database;

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
      db = new DBInterface("127.0.0.1", username, password);
    } catch (DBInitializationException e) {
      e.printStackTrace();
    }

    // open a connection and run a basic command
    if (db != null) {
      db.openConnection();
      db.getApp(1);
      db.closeConnection();
    }
  }
}
