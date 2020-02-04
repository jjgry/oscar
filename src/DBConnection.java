import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DBConnection {

  private static final String USERNAME = "jj";
  private static final String PASSWORD = "teamoscar";
  private static final String CONNECTION = "jdbc:mysql://10.248.114.7";


  public static void main(String[] args) throws Exception{
    Connection con = null;
    try {
      Class.forName("com.mysql.jdbc.Driver");
      con = DriverManager.getConnection(CONNECTION, USERNAME, PASSWORD);
      System.out.println("Worked");
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }
}

