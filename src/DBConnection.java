import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Maintains a connection to the database and executes commands on request
 */
class DBConnection {

  private static final String CONNECTION = "jdbc:mysql://10.248.114.7?characterEncoding=utf8";

  private Connection con;

  /**
   * Initialises a connection to the database using the provided information
   */
  public DBConnection(String username, String password) {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    try {
      con = DriverManager.getConnection(CONNECTION, username, password);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    } finally {
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * @param command an SQL command to be executed on the database
   * @return a ResultSet object containing return table of the SQL query
   * @throws SQLException when there is an error executing the SQL query
   */
  public ResultSet execute(String command) throws SQLException {
    Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    return stmt.executeQuery(command);
  }

}

