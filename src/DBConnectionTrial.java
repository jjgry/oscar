import java.util.List;

class DBConnectionTrial {

  public static void main(String[] args) {

    DBInterface db = new DBInterface("10.248.106.159", "jj", "teamoscar");
    db.openConnection();
    List<Appointment> apps = db.remindersToSendToday();
    db.closeConnection();

    for (Appointment app : apps) {
      System.out.println(app.getPatientEmail());
    }
  }
}
