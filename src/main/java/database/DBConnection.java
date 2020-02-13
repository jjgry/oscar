package database;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Retrieves and stores login information for the database and on command establishes a connection
 * to the database and executes a query
 */
class DBConnection {

  private static int LPORT = 9876;
  private static String RHOST = "localhost";
  private static int RPORT = 3306;

  private String CONNECTION;
  private String SSH_USERNAME;
  private String SSH_PASSWORD;
  private String DB_USERNAME;
  private String DB_PASSWORD;
  private Connection con;

  /**
   * Constructor which will fetch it's own paramaters
   */
  DBConnection() throws DBInitializationException {
    // get ssh login credentials
    Scanner scanner = new Scanner(System.in);
    System.out.print("crsid: ");
    SSH_USERNAME = scanner.nextLine();
    System.out.print("srcf password: ");
    SSH_PASSWORD = scanner.nextLine();
    System.out.print("database username: ");
    DB_USERNAME = scanner.nextLine();
    System.out.print("database password: ");
    DB_PASSWORD = scanner.nextLine();

    CONNECTION = "jdbc:mysql://127.0.0.1:" + LPORT + "?characterEncoding=utf8";
    try {
      Class.forName("com.mysql.jdbc.Driver");
      startPortForwarding();
      System.out.println("Connection established successfully");
    } catch (ClassNotFoundException | JSchException e) {
      throw new DBInitializationException(e.getMessage());
    }
  }

  /**
   * @param db_username the username for the database
   * @param db_password the password for the databse
   * @param ssh_username the CRSID of the user attempting to connect to the remote host
   * @param ssh_password the password of the remote host user
   * @throws DBInitializationException if there is an issue with establishing port forwarding or
   * importing relevant classes
   */
  DBConnection(String db_username, String db_password, String ssh_username, String ssh_password)
      throws DBInitializationException {
    DB_USERNAME = db_username;
    DB_PASSWORD = db_password;
    SSH_USERNAME = ssh_username;
    SSH_PASSWORD = ssh_password;
    CONNECTION = "jdbc:mysql://127.0.0.1:" + LPORT + "?characterEncoding=utf8";
    try {
      Class.forName("com.mysql.jdbc.Driver");
      startPortForwarding();
      System.out.println("Connection established successfully");
    } catch (ClassNotFoundException | JSchException e) {
      throw new DBInitializationException(e.getMessage());
    }
  }

  private void startPortForwarding() throws JSchException {
    int ssh_port = 22;
    String hostname = "shell.srcf.net";
    JSch jsch = new JSch();
    Session session = jsch.getSession(SSH_USERNAME, hostname, ssh_port);
    session.setPassword(SSH_PASSWORD);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();
    session.setPortForwardingL(LPORT, RHOST, RPORT);
  }

  /**
   * @return whether a new connection has been created successfully
   */
  boolean newConnection() {
    try {
      con = DriverManager.getConnection(CONNECTION, DB_USERNAME, DB_PASSWORD);
    } catch (SQLException e) {
      System.err.println("Error opening connection:");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * @return whether terminating the connection has been successful
   */
  boolean endConnection() {
    if (con == null) {
      return false;
    }

    try {
      con.close();
    } catch (SQLException e) {
      System.err.println("Error closing connection: ");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * @param query an SQL query to be executed on the database
   * @return a ResultSet object containing return table of the SQL query
   */
  ResultSet execute(String query) {
    ResultSet rs = null;
    try {
      Statement stmt = con
          .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      rs = stmt.executeQuery(query);
    } catch (SQLException e) {
      System.err.println("Error executing query: ");
      e.printStackTrace();
    }
    return rs;
  }

  /**
   * @param query an SQL query to be executed on the database
   * @return whether the query has been successful
   */
  boolean executeUpdate(String query) {
    boolean success;
    try {
      Statement stmt = con
          .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      success = stmt.execute(query);
    } catch (SQLException e) {
      System.err.println("Error executing query: ");
      e.printStackTrace();
      return false;
    }
    return !success;
  }

}