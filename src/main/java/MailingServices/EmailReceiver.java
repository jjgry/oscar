package MailingServices;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailReceiver {

    public EmailReceiver() {}

    public static List<EmailMessage> getUnreadEmails() throws MessagingException, IOException {
        Folder folder = null;
        Store store = null;
        List<EmailMessage> emailMessages;
        try {
            Properties properties = System.getProperties();
            properties.setProperty("mail.store.protocol", "imaps");
            properties.setProperty("mail.imaps.port", "993");
            properties.setProperty("mail.imaps.connectiontimeout", "5000");
            properties.setProperty("mail.imaps.timeout", "5000");

            Session session = Session.getDefaultInstance(properties, null);
            store = session.getStore("imaps");

            //TODO move password and email to environment variables
            store.connect("imap.gmail.com","nhs.appointment.reminder@gmail.com", "Team0scarIsBest");

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            Message messages[] = folder.getMessages();

            System.out.println("Number of messages in INBOX: " + folder.getMessageCount());
            System.out.println("Number of unread messages in INBOX: " + folder.getUnreadMessageCount());

            emailMessages = parseEmails(messages);
        }
        finally {
            if (folder != null) { folder.close(true); }
            if (store != null) { store.close(); }
        }

        return emailMessages;
    }

    private static List<EmailMessage> parseEmails( Message[] messages) throws IOException, MessagingException {
        List<EmailMessage> emailMessages = new LinkedList<>();
        //TODO parsing

        for (Message message : messages) {
            System.out.println("---------MESSAGE-----------");

            //TODO UNDO if (msg.isSet(Flags.Flag.SEEN)) continue;

            String sendersEmailAddress = "unknownAddress";
            String receiversEmailAddress = "unknownAddress";

            if (message.getFrom().length > 0) {
                sendersEmailAddress = message.getFrom()[0].toString();
                System.out.println("FROM:" + sendersEmailAddress);
            }

            if (message.getAllRecipients().length > 0){
                receiversEmailAddress = message.getAllRecipients()[0].toString();
                System.out.println("TO:" + receiversEmailAddress);
            }

            String subject = message.getSubject();
            System.out.println("SUBJECT: " + subject);

            String messageContents = "";
            Object content = message.getContent();

            InputStream inputStream = null;
            try {
                if (content instanceof Multipart) {
                    Multipart multi = ((Multipart)content);
                    int parts = multi.getCount();
                    for (int j=0; j < parts; ++j) {
                        MimeBodyPart part = (MimeBodyPart)multi.getBodyPart(j);
                        if (part.getContent() instanceof Multipart) {
                            // part-within-a-part, do some recursion...

                            System.out.println("NEED TO RECURSE");
                            //saveParts(part.getContent(), filename);
                        }
                        else {
                            String extension = "";
                            if (part.isMimeType("text/html")) {
                                extension = "html";
                            }
                            else if (part.isMimeType("text/plain")) {
                                extension = "txt";
                            }
                            else {
                                //  Try to get the name of the attachment
                                extension = part.getDataHandler().getName();
                            }

                            System.out.println("EXTENSION: " + extension);
                            inputStream = part.getInputStream();

                            messageContents = CharStreams.toString(new InputStreamReader(
                                    inputStream, Charsets.UTF_8));
                            System.out.println("TEXT:"  + messageContents);
                        }
                    }
                } else {
                    System.out.println("Unidentified Email type");
                }
            }
            finally {
                if (inputStream != null) { inputStream.close(); }
            }


            EmailMessage emailMessage = new EmailMessage(sendersEmailAddress, receiversEmailAddress, subject, messageContents);
            message.setFlag(Flags.Flag.SEEN,true);
        }

        //TODO return emails
        return emailMessages;
    }

    public static void main(String args[]) throws Exception {
        EmailReceiver.getUnreadEmails();
    }
}