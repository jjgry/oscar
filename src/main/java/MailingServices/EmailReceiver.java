package MailingServices;

import org.apache.commons.mail.util.MimeMessageParser;
import oscar.SegmentQueue;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

//TODO add mechanism to retrieve Appointment ID
//TODO deal with the case when it is impossible to retrieve appointment ID

public class EmailReceiver {
    private static final String applicationEmailAddress = System.getenv("GMAIL_ACCOUNT_EMAIL_ADDRESS");
    private static final String applicationGmailPassword = System.getenv("GMAIL_ACCOUNT_PASSWORD");
    private static EmailReceiver singletonReceiver;
    private SegmentQueue<IncomingEmailMessage> receivedEmails;

    private EmailReceiver( SegmentQueue<IncomingEmailMessage> receivedEmails ) {
        System.out.println("EMAIL RECEIVER WILL GET EMAILS FROM: " + applicationEmailAddress);
        this.receivedEmails = receivedEmails;
    }

    //This is an indempotent function
    //Only a singleton Receiver will be created
    public static EmailReceiver getEmailReceiver( SegmentQueue<IncomingEmailMessage> receivedEmails ) {
        if (null == singletonReceiver) {
            singletonReceiver = new EmailReceiver(receivedEmails);

            Thread Major = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            List<IncomingEmailMessage> emails = EmailReceiver.getUnreadEmails();

                            if (null == emails) break;

                            System.out.println("RECEIVED " + emails.size() + " EMAILS!");
                            for (IncomingEmailMessage email : emails) {
                                receivedEmails.put(email);
                            }
                        }
                    } catch (AuthenticationFailedException e) {
                        //TODO how to deal with AuthenticationFailedException
                        System.err.println("Couldn't connect to email so stop receiving");
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            };
            Major.start();
        }
        return singletonReceiver;
    }

    public static List<IncomingEmailMessage> getUnreadEmails() throws MessagingException {
        List<IncomingEmailMessage> emailMessages = new LinkedList<>();
        Properties properties = System.getProperties();
        properties.setProperty("mail.store.protocol", "imaps");
        properties.setProperty("mail.imaps.port", "993");
        properties.setProperty("mail.imaps.connectiontimeout", "5000");
        properties.setProperty("mail.imaps.timeout", "5000");

        Session session = Session.getDefaultInstance(properties, null);
        Store store = session.getStore("imaps");

        store.connect("imap.gmail.com", applicationEmailAddress, applicationGmailPassword);

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        Message messages[] = folder.getMessages();

        System.out.println("Number of messages in INBOX: " + folder.getMessageCount());
        System.out.println("Number of unread messages in INBOX: " + folder.getUnreadMessageCount());

        for (Message message : messages) {
            IncomingEmailMessage emailMessage = parseEmail(message);
            if (null != emailMessage) emailMessages.add(emailMessage);
        }

        try {
            if (null != folder) folder.close(true);
            if (store != null) store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return emailMessages;
    }

    private static IncomingEmailMessage parseEmail( Message message ) throws MessagingException {
        if (message.isSet(Flags.Flag.SEEN)) return null; //TODO delete seen emails after a week

        System.out.println("---------NEW MESSAGE RECEIVED-----------");
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
                    new IncomingEmailMessage(
                            senderEmailAddress,
                            receiverEmailAddress,
                            subject,
                            messageContents,
                            "UNIDENTIFIED YET");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return emailMessage;
    }

    private static String readPlainContent( MimeMessage message ) throws Exception {
        return new MimeMessageParser(message).parse().getPlainContent();
    }

    public static void main( String args[] ) {
        SegmentQueue<IncomingEmailMessage> InQ = new SegmentQueue<>();
        EmailReceiver receiver = EmailReceiver.getEmailReceiver(InQ);
    }
}
