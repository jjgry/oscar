package database;

import database.Appointment.ReminderType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides functions for the kernel to call to access and modify the database
 */
public class DBInterface {

    private DBConnection database;

    /**
     * Default constructor. Usernames and passwords will be retrieved automatically from the user
     *
     * @throws DBInitializationException if there is an issue with establishing port forwarding or
     *                                   importing relevant classes
     */
    public DBInterface() throws DBInitializationException {
        database = new DBConnection();
    }

    /**
     * To be used when the login credentials are already used, else use the constructor with no
     * arguments.
     *
     * @param db_username  the username for the database
     * @param db_password  the password for the databse
     * @param ssh_username the CRSID of the user attempting to connect to the remote host
     * @param ssh_password the password of the remote host user
     * @throws DBInitializationException if there is an issue with establishing port forwarding or
     *                                   importing relevant classes
     */
    public DBInterface(String db_username, String db_password, String ssh_username,
                       String ssh_password) throws DBInitializationException {
        database = new DBConnection(db_username, db_password, ssh_username, ssh_password);
    }

    /**
     * @return whether a new connection has been made successfully
     */
    public boolean openConnection() {
        return database.newConnection();
    }

    /**
     * @return whether the connection to the database has been terminated successfully
     */
    public boolean closeConnection() {
        return database.endConnection();
    }

    /**
     * @return a list of appointments for which reminders have to be sent
     */
    public List<Appointment> remindersToSendToday() {
        ResultSet rs1 = database.execute(Queries.GET_APP_TO_REMIND_1_DAY);
        ResultSet rs2 = database.execute(Queries.GET_APP_TO_REMIND_7_DAY);
        List<Appointment> appointmentList = new ArrayList<>();
        try {
            while (rs1.next()) {
                Appointment app = new Appointment();

                app.setConversationStateID(rs1.getInt("conversation_state_id"));
                app.setAppID(rs1.getInt("app_id"));
                app.setDatetime(rs1.getString("timeslot"));
                app.setDoctorName(rs1.getString("doctor_name"));
                app.setPatientEmail(rs1.getString("patient_email"));
                app.setPatientName(rs1.getString("patient_name"));
                app.setReminderType(ReminderType.OneDay);
                appointmentList.add(app);
            }
            while (rs2.next()) {
                Appointment app = new Appointment();

                app.setConversationStateID(rs2.getInt("conversation_state_id"));
                app.setAppID(rs2.getInt("app_id"));
                app.setDatetime(rs2.getString("timeslot"));
                app.setDoctorName(rs2.getString("doctor_name"));
                app.setPatientEmail(rs2.getString("patient_email"));
                app.setPatientName(rs2.getString("patient_name"));
                app.setReminderType(ReminderType.SevenDay);
                appointmentList.add(app);
            }
            return appointmentList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param appointmentList a list of appointments for which reminders have been sent
     */
    public void RemindersSent(List<Appointment> appointmentList) {
        for (Appointment appointment : appointmentList) {
            switch (appointment.getReminderType()) {
                case OneDay:
                    database.executeUpdate(
                        String.format(Queries.MARK_REMINDED, 1, appointment.getConversationStateID()));
                    break;
                case SevenDay:
                    database.executeUpdate(
                        String.format(Queries.MARK_REMINDED, 7, appointment.getConversationStateID()));
                    break;
                default:
                    // this should never happen
                    System.err.println("DBInterface: Appointment has no ReminderType");
            }
        }
    }

    /**
     * @param doctorID      the doctor we want appointments for
     * @param startDatetime look for timeslots after
     * @param endDatetime   look for timeslots before
     * @return a representation the available timeslots for the patient
     */
    public List<Timeslot> getAppointments(int doctorID, String startDatetime, String endDatetime) {

        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.GET_APPS);
            ps.setString(1, startDatetime);
            ps.setString(2, endDatetime);
            ps.setInt(3, doctorID);
            ResultSet rs = database
                    .execute(ps);
            List<Timeslot> appointmentList = new ArrayList<>();
            while (rs.next()) {
                Timeslot app = new Timeslot();

                String startime = rs.getString("timeslot");
                startime = startime.substring(0, startime.length() - 2);
                app.setStartTime(startime);
                app.setDoctorID(rs.getInt("doctor_id"));
                app.setLocation(rs.getString("location"));

                appointmentList.add(app);
            }
            return appointmentList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update the DB to confirm the new appointment time
     *
     * @param appointmentID the appointment to be confirmed
     * @return true if the update was successful
     */
    public boolean confirmNewTime(int appointmentID) {
        // nothing changes in the database so do nothing
        return true;
    }

    /**
     * Update the DB to reject the appointment time
     *
     * @param appointmentID the appointment to be rejected
     * @return true if the update was successful
     */
    public boolean rejectTime(int appointmentID) {
        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.REJECT_APP);
            ps.setInt(1, appointmentID);
            return database.executeUpdate(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Allows kernel to confirm a patient has an appointment with the given ID
     *
     * @param patientEmail  the identifier used for the patient
     * @param appointmentID the identifier used for thr appointment
     * @return true if said patient has the given appointment
     */
    public boolean confirmAppointmentExists(String patientEmail, int appointmentID) {

        try {
            Connection con = database.getCon();
            PreparedStatement ps = con.prepareStatement(Queries.CONFIRM_APP_FOR_PATIENT);
            ps.setString(1, patientEmail);
            ps.setInt(2, appointmentID);
            ResultSet rs = database
                    .execute(ps);
            rs.next();
            int app_id = rs.getInt("app_id");
            return appointmentID == app_id;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param appointmentID the appointment ID
     * @return an Appointment object containing details of the appointment
     */
    public Appointment getApp(int appointmentID) {

        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.GET_APP_FROM_ID);
            ps.setInt(1, appointmentID);
            ResultSet rs = database.execute(ps);
            List<Appointment> appointmentList = new ArrayList<>();
            while (rs.next()) {
                Appointment app = new Appointment();
                app.setAppID(rs.getInt("app_id"));
                app.setDatetime(rs.getString("timeslot"));
                app.setDoctorName(rs.getString("doctor_name"));
                app.setPatientEmail(rs.getString("patient_email"));
                app.setPatientName(rs.getString("patient_name"));
                app.setDoctorID(rs.getInt("doctor"));

                appointmentList.add(app);
            }
            if (appointmentList.size() == 1) {
                return appointmentList.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param appointmentID the appointment ID
     * @return a Patient object of the patient associated with the appointment
     */
    public Patient getPatient(int appointmentID) {

        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.GET_PATIENT);
            ps.setInt(1, appointmentID);
            ResultSet rs = database.execute(ps);
            List<Patient> patientList = new ArrayList<>();
            while (rs.next()) {
                Patient p = new Patient();
                p.setEmail(rs.getString("patient_email"));
                p.setName(rs.getString("patient_name"));
                p.setPatient_id(rs.getInt("patient_id"));
                patientList.add(p);
            }
            if (patientList.size() == 1) {
                return patientList.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param emailID the email address of the patient
     * @return the list of patients associated with the email address
     */
    public List<Patient> getPatients(String emailID) {

        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.GET_PATIENTS);
            ps.setString(1, emailID);
            ResultSet rs = database.execute(ps);
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                Patient p = new Patient();
                p.setEmail(emailID);
                p.setName(rs.getString("patient_name"));
                p.setPatient_id(rs.getInt("patient_id"));
                patients.add(p);

            }
            if (patients.size() > 0) {
                return patients;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean blockTimeSlot(int timeslotID, int appID) {
        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.BLOCK_TIMESLOT);
            ps.setInt(1, timeslotID);
            ps.setInt(2, appID);
            database.executeUpdate(ps);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param app_id      the appointment id associated with the email
     * @param messageBody the content of the message
     */
    public void addLog(int app_id, String messageBody) {
        try {
            Connection c = database.getCon();
            PreparedStatement ps = c.prepareStatement(Queries.ADD_LOG);
            ps.setInt(1, app_id);
            ps.setString(2, messageBody);
            database.executeUpdate(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

  /**
   * Remove appointments, logs, and timeslots which are more than 1 month old
   */
  public void removeOldData() {
    // remove timeslots (cascade delete will remove associated appointments)
    database.execute(Queries.REMOVE_TIMESLOTS);

    // remove conversation states with app_id not in appointments
    database.executeUpdate(Queries.REMOVE_CONVERSATION_STATES);

    // remove logs with app_id not in appointments
    database.executeUpdate(Queries.REMOVE_LOGS);

    // keep doctors and patients in system
  }
}
