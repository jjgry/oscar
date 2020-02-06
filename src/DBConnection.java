import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Stores login information for the database and on command,
 * establishes a connection and executes a query
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
  DBConnection(String ip, String username, String password) {
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
   * @param query an SQL query to be executed on the database
   * @return a ResultSet object containing return table of the SQL query
   */
  ResultSet execute(String query) {
    ResultSet rs = null;
    try {
      con = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
      Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      rs =  stmt.executeQuery(query);
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