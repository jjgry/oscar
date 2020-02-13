package database;

import com.jcraft.jsch.*;
import java.awt.*;
import javax.swing.*;

/**
 * This program will demonstrate the port forwarding like option -L of ssh command; the given port
 * on the local host will be forwarded to the given remote host and port on the remote side. $
 * CLASSPATH=.:../build javac PortForwardingL.java $ CLASSPATH=.:../build java PortForwardingL You
 * will be asked username, hostname, port:host:hostport and passwd. If everything works fine, you
 * will get the shell prompt. Try the port on localhost.
 */
public class PortForwardingL {

  public static void main(String[] arg) {

    int lport;
    String rhost;
    int rport;

    try {
      JSch jsch = new JSch();

      String user = "jjag3";
      String host = "shell.srcf.net";

      Session session = jsch.getSession(user, host, 22);

      lport = 3306;
      rhost = "shell.srcf.net";
      rport = 3306;

      // username and password will be given via UserInfo interface.
      UserInfo ui = new MyUserInfo();
      session.setUserInfo(ui);

      session.connect();

      //Channel channel=session.openChannel("shell");
      //channel.connect();

      int assigned_port = session.setPortForwardingL(lport, rhost, rport);
      System.out.println("localhost:" + assigned_port + " -> " + rhost + ":" + rport);

      // DBConnectionTrial.connectionTesting();

    } catch (JSchException e) {
      e.printStackTrace();
    }
  }

  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {

    String passwd;
    JTextField passwordField = new JPasswordField(20);

    public String getPassword() {
      return passwd;
    }

    public boolean promptYesNo(String str) {
      return true;
    }

    public String getPassphrase() {
      return null;
    }

    public boolean promptPassphrase(String message) {
      return true;
    }

    public boolean promptPassword(String message) {
      Object[] ob = {passwordField};
      int result =
          JOptionPane.showConfirmDialog(null, ob, message,
              JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        passwd = passwordField.getText();
        return true;
      } else {
        return false;
      }
    }

    public void showMessage(String message) {
      JOptionPane.showMessageDialog(null, message);
    }

    public String[] promptKeyboardInteractive(
        String destination,
        String name,
        String instruction,
        String[] prompt,
        boolean[] echo) {
      return null;
    }
  }
}
