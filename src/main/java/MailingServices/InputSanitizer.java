package MailingServices;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Solution was taken from
https://stackoverflow.com/questions/2385347/how-to-remove-the-quoted-text-from-an-email-and-only-show-the-new-text
 */
public class InputSanitizer {
    /** general spacers for time and date */
    private static final String spacers = "[\\s,/\\.\\-]";

    /** matches times */
    private static final String timePattern  = "(?:[0-2])?[0-9]:[0-5][0-9](?::[0-5][0-9])?(?:(?:\\s)?[AP]M)?";

    /** matches day of the week */
    private static final String dayPattern   = "(?:(?:Mon(?:day)?)|(?:Tue(?:sday)?)|(?:Wed(?:nesday)?)|(?:Thu(?:rsday)?)|(?:Fri(?:day)?)|(?:Sat(?:urday)?)|(?:Sun(?:day)?))";

    /** matches day of the month (number and st, nd, rd, th) */
    private static final String dayOfMonthPattern = "[0-3]?[0-9]" + spacers + "*(?:(?:th)|(?:st)|(?:nd)|(?:rd))?";

    /** matches months (numeric and text) */
    private static final String monthPattern = "(?:(?:Jan(?:uary)?)|(?:Feb(?:uary)?)|(?:Mar(?:ch)?)|(?:Apr(?:il)?)|(?:May)|(?:Jun(?:e)?)|(?:Jul(?:y)?)" +
            "|(?:Aug(?:ust)?)|(?:Sep(?:tember)?)|(?:Oct(?:ober)?)|(?:Nov(?:ember)?)|(?:Dec(?:ember)?)|(?:[0-1]?[0-9]))";

    /** matches years (only 1000's and 2000's, because we are matching emails) */
    private static final String yearPattern  = "(?:[1-2]?[0-9])[0-9][0-9]";

    /** matches a full date */
    private static final String datePattern     = "(?:" + dayPattern + spacers + "+)?(?:(?:" + dayOfMonthPattern + spacers + "+" + monthPattern + ")|" +
            "(?:" + monthPattern + spacers + "+" + dayOfMonthPattern + "))" +
            spacers + "+" + yearPattern;

    /** matches a date and time combo (in either order) */
    private static final String dateTimePattern = "(?:" + datePattern + "[\\s,]*(?:(?:at)|(?:@))?\\s*" + timePattern + ")|" +
            "(?:" + timePattern + "[\\s,]*(?:on)?\\s*"+ datePattern + ")";

    /** matches a leading line such as
     * ----Original Message----
     * or simply
     * ------------------------
     */
    private static final String leadInLine    = "-+\\s*(?:Original(?:\\sMessage)?)?\\s*-+\n";

    /** matches a header line indicating the date */
    private static final String dateLine    = "(?:(?:date)|(?:sent)|(?:time)):\\s*"+ dateTimePattern + ".*\n";

    /** matches a subject or address line */
    private static final String subjectOrAddressLine    = "((?:from)|(?:subject)|(?:b?cc)|(?:to))|:.*\n";

    /** matches gmail style quoted text beginning, i.e.
     * On Mon Jun 7, 2010 at 8:50 PM, Simon wrote:
     */
    private static final String gmailQuotedTextBeginning = "(On\\s+" + dateTimePattern + ".*wrote:\n)";


    /** matches the start of a quoted section of an email */
    private static final Pattern QUOTED_TEXT_BEGINNING = Pattern.compile("(?i)(?:(?:" + leadInLine + ")?" +
            "(?:(?:" +subjectOrAddressLine + ")|(?:" + dateLine + ")){2,6})|(?:" +
            gmailQuotedTextBeginning + ")"
    );

    public static String removeOldEmailContents(String rawEmailWithOldMessages){
        String[] emails = rawEmailWithOldMessages.split("(?i)(?:(?:" + leadInLine + ")?" +
                "(?:(?:" +subjectOrAddressLine + ")|(?:" + dateLine + ")){2,6})|(?:" +
                gmailQuotedTextBeginning + ")"
        );

        if(null == emails){
            System.out.println("Input sanitizer: didn't detect contents of an old email");
            return rawEmailWithOldMessages;
        }

        System.out.println("Input sanitizer: the latest email: " + emails[0]);
        return emails[0];
    }

    public static void main(String[] args){
        String tipicalEmailBody = "Hi, I will not come.\n" +
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

        InputSanitizer.removeOldEmailContents(tipicalEmailBody);
    }
}
