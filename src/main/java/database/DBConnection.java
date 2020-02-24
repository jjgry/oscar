package database;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.awt.EventQueue;
import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import sun.rmi.runtime.Log;

/**
 * Retrieves and stores login information for the database and on command establishes a connection
 * to the database and executes a query
 */
class DBConnection {

  private static int LPORT = 9876;
  private static String RHOST = "localhost";
  private static int RPORT = 3306;

  private boolean fetchedLoginDetails;
  private String CONNECTION;
  private String SSH_USERNAME;
  private String SSH_PASSWORD;
  private String DB_USERNAME;
  private String DB_PASSWORD;
  private Connection con;

  /**
   * Constructor which will fetch it's own parameters
   */
  DBConnection() throws DBInitializationException {
    // get ssh login credentials

    LoginFrame frame = new LoginFrame(this);
    frame.setTitle("Login Form");
    frame.setVisible(true);
    frame.setBounds(10, 10, 370, 500);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setResizable(false);

    DB_USERNAME = frame.dbUsername;
    DB_PASSWORD = frame.dbPassword;
    SSH_USERNAME = frame.sshUsername;
    SSH_PASSWORD = frame.sshPassword;

    CONNECTION = "jdbc:mysql://127.0.0.1:" + LPORT + "?characterEncoding=utf8";
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new DBInitializationException(e.getMessage());
    }
  }

  /**
   * @param db_username the username for the database
   * @param db_password the password for the database
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

  /**
   * @throws JSchException if there is an error in setting up port forwarding
   */
  void startPortForwarding() throws JSchException {
    int ssh_port = 22;
    String hostname = "shell.srcf.net";
    JSch jsch = new JSch();
    Session session = jsch.getSession(SSH_USERNAME, hostname, ssh_port);
    session.setPassword(SSH_PASSWORD);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();
    session.setPortForwardingL(LPORT, RHOST, RPORT);
  }

  void setLoginDetails(String dbUsername, String dbPassword, String sshUsername, String sshPassword) {
    DB_USERNAME = dbUsername;
    DB_PASSWORD = dbPassword;
    SSH_USERNAME = sshUsername;
    SSH_PASSWORD = sshPassword;
    fetchedLoginDetails = true;
  }

  /**
   * @return whether a new connection has been created successfully
   */
  boolean newConnection() {
    while(!fetchedLoginDetails)
    {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    try {
      System.out.println(DB_USERNAME);
      System.out.println(DB_PASSWORD);
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

  public Connection getCon() {
    return con;
  }

  /**
   * @param query an SQL query to be executed on the database
   * @return a ResultSet object containing return table of the SQL query
   */
  ResultSet execute(String query) {
    ResultSet rs = null;
    try {
//      Statement stmt = con
//          .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//      rs = stmt.executeQuery(query);

      PreparedStatement stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
    } catch (SQLException e) {
      System.err.println("Error executing query: ");
      e.printStackTrace();
    }
    return rs;
  }

  ResultSet execute(PreparedStatement ps) {
    ResultSet rs = null;
    try {

      rs = ps.executeQuery();
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
      //Statement stmt = con
      //    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      //success = stmt.execute(query);

      PreparedStatement stmt = con.prepareStatement(query);
      success = stmt.execute();
    } catch (SQLException e) {
      System.err.println("Error executing query: ");
      e.printStackTrace();
      return false;
    }
    return !success;
  }

  boolean executeUpdate(PreparedStatement ps) {
    boolean success;
    try {

      success = ps.execute();
    } catch (SQLException e) {
      System.err.println("Error executing query: ");
      e.printStackTrace();
      return false;
    }
    return !success;
  }

}