class DBConnectionTrial {

  public static void main(String[] args) {

    DBInterface db = new DBInterface("10.248.106.159", "jj", "teamoscar");
    db.openConnection();
    boolean success = db.rejectTime(1);

    db.closeConnection();


    if (success) {
      System.out.println("Yay!");
    }
  }
}
