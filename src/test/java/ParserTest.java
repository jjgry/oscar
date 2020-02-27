public class ParserTest {
    public static String removeContentsOfLastEmail(String unparsedEmail){
        int indexUnderScores = unparsedEmail.indexOf("_______");
        int indexDashes = unparsedEmail.indexOf("-------");
        int index = Math.max(indexUnderScores, indexDashes);

        if (index != -1) {
            unparsedEmail = unparsedEmail.substring(0, index);
        }

        return unparsedEmail;
    }

    public static void main( String[] args ) {
        String sentence = "i confirm\n" +
                "________________________________\n" +
                "From: nhs.appointment.reminder@gmail.com <nhs.appointment.reminder@gmail.com>\n" +
                "Sent: Thursday, February 27, 2020 11:49:02 AM\n" +
                "To: John-Joseph Gray <jjag3@cam.ac.uk>\n" +
                "Subject: [2] GP Appointment Reminder\n" +
                "\n" +
                "Dear JJ,\n" +
                "\n" +
                "You have an appointment with Dr Barbara on 2020-03-05 at 07:00:00.0.\n" +
                "\n" +
                "Please reply to this email stating whether you would like to confirm,\n" +
                "cancel or reschedule your appointment.\n" +
                "\n" +
                "In case you want a new appointment, please provide 3 one hour slots\n" +
                "over the next 14 days when you are available, in the following format:\n" +
                "DD-MM-YYYY, from hh:mm AM/PM to hh:mm AM/PM.\n" +
                "\n" +
                "Example:\n" +
                "22-02-2019 from 11:00 AM to 12:00 PM\n" +
                "22-02-2019 from 03:00 PM to 04:00 PM\n" +
                "25-02-2019 from 03:00 PM to 04:00 PM\n" +
                "\n" +
                "If you have any questions, use the contact details below to get in\n" +
                "touch with us.\n" +
                "\n" +
                "Thank you and have a nice day!\n" +
                "Oscar\n" +
                "\n" +
                "_______________________________\n" +
                "Oscar is an automated email assistant system helping you remember and\n" +
                "confirm/reschedule/cancel your GP appointment. This email system can't\n" +
                "provide you with medical advice and should not be used in case of an\n" +
                "emergency. Please DON'T disclose any personal information other than\n" +
                "your availability.  If you would like to talk to a human assistant,\n" +
                "please find attached the following contact information:\n" +
                "\n" +
                "Surgery contact number: phone number\n" +
                "Address: location address";


        String stripedEmail = removeContentsOfLastEmail(sentence);

        System.out.println(sentence);
    }
}
