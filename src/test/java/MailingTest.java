import MailingServices.*;
import oscar.Classification;
import oscar.SegmentQueue;

import java.util.concurrent.TimeUnit;

import static MailingServices.EmailMessageType.InitialReminderMessage;

public class MailingTest {
    private static boolean EmailSenderAndReceiver_WhenSenderSendsEmail_EmailReceiverGetEmailWithinTwoMinutes(){
        SegmentQueue OutQ = new SegmentQueue<>();
        try {
            EmailSender sender = EmailSender.getEmailSender(OutQ);
        } catch (FailedToInstantiateComponent failedToInstantiateComponent) {
            failedToInstantiateComponent.printStackTrace();
            return false;
        }

        OutgoingEmailMessage emailToSystem = new OutgoingEmailMessage(
                "nhs.appointment.reminder@gmail.com",
                "Mr. Oscar",
                "Dr. Oscarino",
                "27-02-2021",
                "11:00 AM",
                InitialReminderMessage,
                "9876543210");
        OutQ.put(emailToSystem);

        System.out.println("Main thread will sleep to allow sender to send email");
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }




        SegmentQueue<IncomingEmailMessage> InQ = new SegmentQueue<>();
        try {
            EmailReceiver receiver = EmailReceiver.getEmailReceiver(InQ);
        } catch (FailedToInstantiateComponent failedToInstantiateComponent) {
            failedToInstantiateComponent.printStackTrace();
            return false;
        }

        System.out.println("Main thread will sleep to allow receiver to receive some emails");
        try {
            TimeUnit.MINUTES.sleep(60);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        IncomingEmailMessage email = InQ.take();
        System.out.println(email.getMessage()); //Need to inspect email contents
        return  (null != email.getMessage());
    }


    public static boolean ClassificationRemoveContentsOfLastEmail_WhenPatientReplies_ContentsOfOlderMessagesShouldBeRemoved(){
        String rawEmailMessageString = "Hi, I will not come.\n" +
                "Thanks\n" +
                "\n" +
                "On Sat, 29 Feb 2020 at 23:49, <nhs.appointment.reminder@gmail.com> wrote:\n" +
                "----------\n" +
                "Dear Mr. Oscar,\n" +
                "\n" +
                "You have an appointment with Dr. Oscarino on 27-02-2021 at 11:00 AM. \n" +
                "\n" +
                "Please reply to this email stating whether you would like to confirm, cancel or reschedule your appointment. \n" +
                "\n" +
                "In case you want a new appointment, please provide 3 one hour slots when you are available in the next few weeks in the following format: DD-MM-YYYY, from hh:mm AM/PM to hh:mm AM/PM.\n" +
                "\n" +
                "For example:\n" +
                "22-02-2019 from 11:00 AM to 12:00 PM\n" +
                "22-02-2019 from 03:00 PM to 04:00 PM\n" +
                "25-02-2019 from 03:00 PM to 04:00 PM\n" +
                "\n" +
                "If you have any questions, use the contact details below to get in touch with my human supervisor.\n" +
                "\n" +
                "Thank you and have a nice day!\n" +
                "Oscar\n" +
                "\n" +
                "\n" +
                "\n" +
                "----------\n" +
                "Oscar is an automated email assistant system helping you remember and manage your GP appointment. This email system can't provide you with medical advice and should not be used in case of an emergency. Please DON'T disclose any personal information other than your availability.  If you would like to talk to a human assistant, please find attached the following contact information:\n";


        String newEmailMessage = Classification.removeContentsOfLastEmail(rawEmailMessageString);
        return newEmailMessage.equals("Hi, I will not come.\n" +
                "Thanks\n" +
                "\n" +
                "On Sat, 29 Feb 2020 at 23:49, <nhs.appointment.reminder@gmail.com> wrote:\n");
    }

    public static void main( String[] args ) {
        System.out.println("TEST EmailSenderAndReceiver_WhenSenderSendsEmail_EmailReceiverGetEmailWithinTwoMinutes RESULT:"
                + EmailSenderAndReceiver_WhenSenderSendsEmail_EmailReceiverGetEmailWithinTwoMinutes());
        System.out.println("TEST ClassificationRemoveContentsOfLastEmail_WhenPatientReplies_ContentsOfOlderMessagesShouldBeRemoved RESULT: "
                + ClassificationRemoveContentsOfLastEmail_WhenPatientReplies_ContentsOfOlderMessagesShouldBeRemoved());
    }
}
