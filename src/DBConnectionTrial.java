class DBConnectionTrial {

  public static void main(String[] args) {

    DBInterface db = new DBInterface("10.248.99.3", "jj", "teamoscar");
    db.openConnection();
    db.getApp(1);

    db.closeConnection();


//    if (success) {
//      System.out.println("Yay!");
//    }
  }
}
