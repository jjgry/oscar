package MailingServices;

import org.apache.commons.mail.util.MimeMessageParser;
import oscar.SegmentQueue;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for sending emails to patients
 */
public class EmailReceiver {
    private static final String applicationEmailAddress = System.getenv("GMAIL_ACCOUNT_EMAIL_ADDRESS");
    private static final String applicationGmailPassword = System.getenv("GMAIL_ACCOUNT_PASSWORD");
    private static final int secondsToWaitBetweenEmails = 1;
    private static EmailReceiver singletonReceiver;
    private SegmentQueue<IncomingEmailMessage> receivedEmails;

    /**
     * @param receivedEmails
     * @throws FailedToInstantiateComponent
     */
    private EmailReceiver( SegmentQueue<IncomingEmailMessage> receivedEmails ) throws FailedToInstantiateComponent {
        if (null == applicationEmailAddress) {
            System.err.println(
                    "Receiver: Can't receive emails because Receiver doesn't know application email address. Try checking system variables");
            throw new FailedToInstantiateComponent("EmailReceiver couldn't be instantiated because applicationEmailAddress is null");
        }
        System.out.println("Receiver: EMAIL RECEIVER WILL GET EMAILS FROM: " + applicationEmailAddress);
        this.receivedEmails = receivedEmails;
    }

    /**
     * This is an indempotent function
     * Only a singleton Receiver will be created
     */
    public static EmailReceiver getEmailReceiver( SegmentQueue<IncomingEmailMessage> receivedEmails )
            throws FailedToInstantiateComponent {
        if (null == singletonReceiver) {
            singletonReceiver = new EmailReceiver(receivedEmails);

            Thread RecThread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            List<IncomingEmailMessage> emails = EmailReceiver.getUnreadEmails();

                            if (null == emails) break;

                            System.out.println("Receiver: RECEIVED " + emails.size() + " EMAILS!");
                            for (IncomingEmailMessage email : emails) {
                                receivedEmails.put(email);
                            }

                            try {
                                TimeUnit.SECONDS.sleep(secondsToWaitBetweenEmails);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } catch (AuthenticationFailedException e) {
                        //TODO how to deal with AuthenticationFailedException
                        System.err.println("Receiver: Couldn't connect to email so stop receiving");
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            };
            RecThread.setDaemon(true);
            RecThread.start();
        }
        return singletonReceiver;
    }

    /**
     * Pool this function regularly to check if there are new unseen emails in the inbox
     *
     * @return a list of emails from inbox and mark them SEEN
     * @throws MessagingException if there are problems with API
     */
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

        System.out.println("Receiver: Number of messages in INBOX: " + folder.getMessageCount());
        System.out.println("Receiver: Number of unread messages in INBOX: " + folder.getUnreadMessageCount());

        for (Message message : messages) {
            IncomingEmailMessage emailMessage = parseEmail(message);
            if (null != emailMessage) emailMessages.add(emailMessage);
        }

        try {
            if (null != folder) folder.close(true);
            if (null != store) store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return emailMessages;
    }

    private static IncomingEmailMessage parseEmail( Message message ) throws MessagingException {
        // Read only unseen emails
        if (message.isSet(Flags.Flag.SEEN)) {
            //TODO delete seen emails after a week
//            Date today = new Date();
//            System.out.println("TODAY: " + today);
//            System.out.println("EMAIL RECEIVED: " + message.getSentDate());
//            System.out.println("DIFFERENCE between days: " + today.compareTo(message.getSentDate()));

            return null;
        }

        System.out.println("Receiver: NEW MESSAGE RECEIVED!");
        message.setFlag(Flags.Flag.SEEN, true);

        if (!(message instanceof MimeMessage)) {
            System.err.println("Receiver: Unidentified Email Format: " + message.getClass().toString());
            return null;
        }
        MimeMessage mimeMessage = (MimeMessage) message;

        IncomingEmailMessage emailMessage;
        try {
            MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();

            Address senderAddress = mimeMessage.getSender();
            if (null == senderAddress) {
                System.err.println("Receiver: Suspicious email with no or multiple recipient email addresses.");
                return null;
            }
            String senderEmailAddress = parser.getFrom();
            System.out.println("Receiver: FROM: " + senderEmailAddress);

            String receiverEmailAddress = "unknownAddress";
            Address[] allRecipients = mimeMessage.getAllRecipients();
            for (Address recipient : allRecipients) {
                if (recipient.toString().contains(applicationEmailAddress)) {
                    receiverEmailAddress = applicationEmailAddress;
                }
            }
            if (receiverEmailAddress.equals("unknownAddress")) {
                System.err.println(
                        "Receiver: Unidentified receiver email address. We shouldn't have received this email.");
                return null;
            }
            System.out.println("Receiver: TO: " + receiverEmailAddress);

            String subject = parser.getSubject();
            System.out.println("Receiver: SUBJECT: " + subject);

            int startIndex = subject.indexOf('[');
            int endIndex = subject.indexOf(']');
            if (startIndex >= endIndex) {
                System.err.println("Receiver: malformed appointment ID.");
                return null;
            }

            //TODO improve security
            String appointmentId = subject.substring(startIndex + 1, endIndex);
            System.out.println("Receiver: APPOINTMENT ID: " + appointmentId);

            // Ignore emails which don't have textual representation
            if (!parser.hasPlainContent()) {
                System.err.println("Receiver: email doesn't have text in it so ignore this email.");
                return null;
            }
            String messageContents = readPlainContent(mimeMessage);
            System.out.println("Receiver: PLAIN TEXT: " + messageContents);

            emailMessage =
                    new IncomingEmailMessage(
                            senderEmailAddress,
                            receiverEmailAddress,
                            subject,
                            messageContents,
                            appointmentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return emailMessage;
    }

    private static String readPlainContent( MimeMessage message ) throws Exception {
        return new MimeMessageParser(message).parse().getPlainContent();
    }

    /**
     * Example how to use EmailReceiver
     */
    public static void main( String args[] ) {
        SegmentQueue<IncomingEmailMessage> InQ = new SegmentQueue<>();
        try {
            EmailReceiver receiver = EmailReceiver.getEmailReceiver(InQ);
        } catch (FailedToInstantiateComponent failedToInstantiateComponent) {
            failedToInstantiateComponent.printStackTrace();
        }

        System.out.println("Main thread will sleep to allow receiver to receive some emails");
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
