package MailingServices;

import org.apache.commons.mail.util.MimeMessageParser;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailReceiver {
  //TODO move to env variables
  private static final String applicationEmailAddress = "nhs.appointment.reminder@gmail.com";

  private static EmailReceiver singletonReceiver;
  private EmailReceiver() {}

  public static EmailReceiver getEmailReceiver(){
    if(null == singletonReceiver){
      singletonReceiver = new EmailReceiver();
    }
    return singletonReceiver;
  }

  //TODO integrate using a queue

//    Thread Major = new Thread() {
//      @Override
//      public void run() {
//        //Main Loop:
//        LocalDateTime NextNewApptCheck = LocalDateTime.now();
//
//        while (true) {
//        }
//      }
//    };
//    Major.start();


  public static List<IncomingEmailMessage> getUnreadEmails() {
    Folder folder = null;
    Store store = null;
    List<IncomingEmailMessage> emailMessages = new LinkedList<>();
    try {
      Properties properties = System.getProperties();
      properties.setProperty("mail.store.protocol", "imaps");
      properties.setProperty("mail.imaps.port", "993");
      properties.setProperty("mail.imaps.connectiontimeout", "5000");
      properties.setProperty("mail.imaps.timeout", "5000");

      Session session = Session.getDefaultInstance(properties, null);
      store = session.getStore("imaps");

      // TODO move password and email to environment variables
      store.connect("imap.gmail.com", "nhs.appointment.reminder@gmail.com", "Team0scarIsBest");

      folder = store.getFolder("INBOX");
      folder.open(Folder.READ_WRITE);
      Message messages[] = folder.getMessages();

      System.out.println("Number of messages in INBOX: " + folder.getMessageCount());
      System.out.println("Number of unread messages in INBOX: " + folder.getUnreadMessageCount());

      for (Message message : messages) {
        IncomingEmailMessage emailMessage = parseEmail(message);
        if (null != emailMessage) emailMessages.add(emailMessage);
      }
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    }

    try {
      if (null != folder) folder.close(true);
      if (store != null) store.close();
    } catch (MessagingException e) {
      e.printStackTrace();
    }

    return emailMessages;
  }

  private static IncomingEmailMessage parseEmail(Message message) throws MessagingException {
    System.out.println("---------MESSAGE-----------");
    // TODO UNDO if (message.isSet(Flags.Flag.SEEN)) return null

    // Read only unseen emails
    message.setFlag(Flags.Flag.SEEN, true);

    if (!(message instanceof MimeMessage)) {
      System.err.println("Unidentified Email Format: " + message.getClass().toString());
      return null;
    }
    MimeMessage mimeMessage = (MimeMessage) message;

    IncomingEmailMessage emailMessage;
    try {
      MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();

      Address senderAddress = mimeMessage.getSender();
      if (null == senderAddress) {
        System.err.println("Suspicious email with no or multiple recipient email addresses.");
        return null;
      }
      String senderEmailAddress = parser.getFrom();
      System.out.println("FROM: " + senderEmailAddress);

      String receiverEmailAddress = "unknownAddress";
      Address[] allRecipients = mimeMessage.getAllRecipients();
      for (Address recipient : allRecipients) {
        if (recipient.toString().contains(applicationEmailAddress)) {
          receiverEmailAddress = applicationEmailAddress;
        }
      }
      if (receiverEmailAddress.equals("unknownAddress")) {
        System.err.println(
            "Unidentified receiver email address. We shouldn't have received this email.");
        return null;
      }
      System.out.println("TO: " + receiverEmailAddress);

      String subject = parser.getSubject();
      System.out.println("SUBJECT: " + subject);

      // Ignore emails which don't have textual representation
      if (!parser.hasPlainContent()) return null;
      String messageContents = readPlainContent(mimeMessage);
      System.out.println("PLAIN TEXT: " + messageContents);

      emailMessage =
          new IncomingEmailMessage(senderEmailAddress, receiverEmailAddress, subject, messageContents);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return emailMessage;
  }

  private static String readPlainContent(MimeMessage message) throws Exception {
    return new MimeMessageParser(message).parse().getPlainContent();
  }

  public static void main(String args[]) {
    EmailReceiver.getUnreadEmails();
  }
}
