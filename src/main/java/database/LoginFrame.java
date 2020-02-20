package database;

import com.jcraft.jsch.JSchException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JDialog implements ActionListener {

  Container container = getContentPane();
  JLabel sshUserLabel = new JLabel("SRCF USERNAME");
  JLabel sshPasswordLabel = new JLabel("SRCF PASSWORD");
  JLabel dbUserLabel = new JLabel("DATABASE USERNAME");
  JLabel dbPasswordLabel = new JLabel("DATABASE PASSWORD");
  JTextField sshUsernameField = new JTextField();
  JTextField dbUsernameField = new JTextField();
  JPasswordField sshPasswordField = new JPasswordField();
  JPasswordField dbPasswordField = new JPasswordField();
  JButton loginButton = new JButton("LOGIN");
  JButton resetButton = new JButton("RESET");
  JCheckBox showPasswords = new JCheckBox("Show Passwords");

  private DBConnection dbConnection;
  public String sshUsername;
  public String sshPassword;
  public String dbUsername;
  public String dbPassword;

  public LoginFrame(DBConnection dbConnection) {
    this.dbConnection = dbConnection;
    setLayoutManager();
    setLocationAndSize();
    addComponentsToContainer();
    addActionEvent();
  }

  private void setLayoutManager() {
    container.setLayout(null);
  }

  private void setLocationAndSize() {
    sshUserLabel.setBounds(50, 50, 100, 30);
    sshPasswordLabel.setBounds(50, 120, 100, 30);
    sshUsernameField.setBounds(180, 50, 150, 30);
    sshPasswordField.setBounds(180, 120, 150, 30);

    dbUserLabel.setBounds(50, 190, 100, 30);
    dbPasswordLabel.setBounds(50, 260, 100, 30);
    dbUsernameField.setBounds(180, 190, 150, 30);
    dbPasswordField.setBounds(180, 260, 150, 30);

    showPasswords.setBounds(150, 290, 150, 30);
    loginButton.setBounds(50, 340, 100, 30);
    resetButton.setBounds(200, 340, 100, 30);
  }

  private void addComponentsToContainer() {
    container.add(sshUserLabel);
    container.add(sshPasswordLabel);
    container.add(sshUsernameField);
    container.add(sshPasswordField);

    container.add(dbUserLabel);
    container.add(dbPasswordLabel);
    container.add(dbUsernameField);
    container.add(dbPasswordField);

    container.add(showPasswords);
    container.add(loginButton);
    container.add(resetButton);
  }

  private void addActionEvent() {
    loginButton.addActionListener(this);
    resetButton.addActionListener(this);
    showPasswords.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //Coding Part of LOGIN button
    if (e.getSource() == loginButton) {
      sshUsername = sshUsernameField.getText();
      sshPassword = sshPasswordField.getText();
      dbUsername = dbUsernameField.getText();
      dbPassword = dbPasswordField.getText();
      dbConnection.setLoginDetails(dbUsername, dbPassword, sshUsername, sshPassword);
      try {
        dbConnection.startPortForwarding();
      } catch (JSchException jsche) {
        jsche.printStackTrace();
      }
      this.dispose();
    }

    //Coding Part of RESET button
    if (e.getSource() == resetButton) {
      sshUsernameField.setText("");
      sshPasswordField.setText("");
      dbUsernameField.setText("");
      dbPasswordField.setText("");
    }

    //Coding Part of showPassword JCheckBox
    if (e.getSource() == showPasswords) {
      if (showPasswords.isSelected()) {
        sshPasswordField.setEchoChar((char) 0);
        dbPasswordField.setEchoChar((char) 0);
      } else {
        sshPasswordField.setEchoChar('*');
        dbPasswordField.setEchoChar('*');
      }
    }
  }
}
