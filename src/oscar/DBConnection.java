package oscar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Maintains a connection to the database and executes commands on request
 */
class DBConnection {

  private String CONNECTION;
  private String USERNAME;
  private String PASSWORD;
//  private String IP = "10.248.114.7";
  private Connection con;

  /**
   * @param ip IP address we wish to connect to, can include port and database name
   * @param username username to login to the database
   * @param password password for the database
   */
  public DBConnection(String ip, String username, String password) {
    USERNAME = username;
    PASSWORD = password;
    CONNECTION = "jdbc:mysql://" + ip + "?characterEncoding=utf8";

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Opens a connection to the database and executes an SQL command.
   *
   * @param command an SQL command to be executed on the database
   * @return a ResultSet object containing return table of the SQL query
   * @throws SQLException when there is an error executing the SQL query
   */
  public ResultSet execute(String command) throws SQLException {
    ResultSet rs = null;
    try {
      con = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
      Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      rs =  stmt.executeQuery(command);

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
    return rs;
  }

}

