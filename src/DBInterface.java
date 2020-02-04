public class DBInterface {

  private DBConnection connection;

  public DBInterface () {
    connection = new DBConnection();
  }

  public void remindersToSendToday() {

  }

  public void getAppointments(String doctor, String startDate, String endDate) {

  }

  public void confirmNewTime(String appointmentID) {

  }

  public void rejectNewTime(String appointmentID) {

  }

  public void addNewAppointment(String time, String doctor, String patientID){

  }

  public void getPatientID(String emailID) {

  }

  public void confirmAppointmentExists(String patientID, String appointmentID) {

  }


}
