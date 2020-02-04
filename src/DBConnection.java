import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class DBConnection {

  private static final String USERNAME = "jj";
  private static final String PASSWORD = "teamoscar";
  private static final String CONNECTION = "jdbc:mysql://10.248.114.7?characterEncoding=utf8";

  Connection con;
  Statement stmt;
  ResultSet rs;

  public DBConnection() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    try {
      makeConnection();
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  private void makeConnection() throws SQLException {
    try {
      con = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  public ResultSet execute(String command) throws SQLException {
    stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    rs = stmt.executeQuery(command);
    return rs;
  }

}

