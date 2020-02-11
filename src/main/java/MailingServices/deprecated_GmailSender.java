package MailingServices;

import java.util.*;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class deprecated_GmailSender {
    //TODO improve authentication
    private String sendersEmailAddress;
    private String password;
    private String sendingHost;
    private int sendingPort;

    private String receivingHost;

    /*
    This class expects that sender's email uses Gmail.
     */
    public deprecated_GmailSender( String emailAddress, String emailPassword) throws IncorrectArgument {
        if(!emailAddress.contains("@gmail.com")) {
            System.out.println("Sender's email should use Gmail.");
            throw new IncorrectArgument("Sender's email should use Gmail.");
        }
        this.sendersEmailAddress = emailAddress;
        this.password = emailPassword;
        this.sendingHost = "smtp.gmail.com";
        this.sendingPort = 465;
        this.receivingHost = "imap.gmail.com";
    }

    public void sendEmail(String to, String subject, String text){
        Properties properties = new Properties();

        properties.put("mail.smtp.host", sendingHost);
        properties.put("mail.smtp.port", String.valueOf(sendingPort));
        properties.put("mail.smtp.user", sendersEmailAddress);
        properties.put("mail.smtp.password", password);

        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties);
        Message message = new MimeMessage(session);

        InternetAddress sendersAddress = null;
        InternetAddress receiversAddress = null;
        try {
            sendersAddress = new InternetAddress(sendersEmailAddress);
            receiversAddress = new InternetAddress(to);
        } catch (AddressException e) {
            e.printStackTrace();
            System.out.println("ERROR while sending a message to " + to + ".");
            //TODO decide what to do with erroneous emails
        }
        try {
            message.setFrom(sendersAddress);
            message.setRecipient(RecipientType.TO, receiversAddress);
            //TODO send emails to sys admin
            // message.setRecipient(RecipientType.BCC, new InternetAddress("sm2354@cam.ac.uk"));

            message.setSubject(subject);
            message.setText(text);

            Transport transport = session.getTransport("smtps");
            transport.connect (sendingHost,sendingPort, sendersEmailAddress, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            System.out.println("Message to " + to + " was sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("ERROR while sending a message to " + to + ".");
        }
    }
    public void readEmails(){
        Properties properties = System.getProperties();
        properties.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(properties, null);

        try {
            Store store = session.getStore("imaps");
            store.connect(receivingHost, sendersEmailAddress, password);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();


//            for(Message message:messages) {
//
//            }


            for(Message message : messages){
                MimeMessage msg = (MimeMessage) message;
                Scanner s = new Scanner(msg.getInputStream()).useDelimiter("\\A");

                System.out.println("---------------------------");
                System.out.println("Subject: " + message.getSubject());
                System.out.println("Sender: " + msg.getSender().toString());
                System.out.println("Date of receiving : " + msg.getReceivedDate().toString());

                String result = s.hasNext() ? s.next() : "";
                System.out.println("Contents: " + result);
                System.out.println("---------------------------");

//                if(MimeMessage.class.isInstance(message)){
//                    MimeMessage parts = (MimeMessage)message.getContent();
//                    System.out.println("OPA");
//                    for(int i = 0 ; i < parts.get ; i++){
//                        javax.mail.BodyPart p = parts.getBodyPart(i);
//                        if("text/html".equals(p.getContentType())){
//                            // now you can read out the contents from p.getContent()
//                            // (which is typically an InputStream, but depending on your javamail
//                            // libraries may be something else
//                            System.out.println(p.getContent().toString());
//                        }
//                    }
//                }


            }

            folder.close(true);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //TODO change to ENV variables
        String sendersEmailAddress = "nhs.appointment.reminder@gmail.com";
        String senderPassword = "Team0scarIsBest";

        String emailSubject = "NHS Reminder";
        String emailText = "Good day!";
        String receiversEmail ="mulevicius.simonas@gmail.com";

        deprecated_GmailSender gmailSender = null;
        try {
            gmailSender = new deprecated_GmailSender(sendersEmailAddress, senderPassword);
        } catch (IncorrectArgument incorrectArgument) {
            incorrectArgument.printStackTrace();
        }
        //gmailSender.sendEmail(receiversEmail, emailSubject, emailText);
        gmailSender.readEmails();
    }
}