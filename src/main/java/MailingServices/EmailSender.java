package MailingServices;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import oscar.SegmentQueue;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static MailingServices.EmailMessageType.InitialReminderMessage;

public class EmailSender {
    private static final String apiKey = System.getenv("SENDGRID_API_KEY");
    private static final String applicationEmailAddress = System.getenv("GMAIL_ACCOUNT_EMAIL_ADDRESS");
    private static final int secondsToWaitBetweenEmails = 10;
    private static EmailSender uniqueSender;
    private SegmentQueue<OutgoingEmailMessage> messagesToSend;

    private static final String footer = "\n" + "-----------------------------------------------------" + "\n" +
            "This is an automated email assistant system helping you remember and confirm/reschedule/cancel your GP appointment. This email system can't provide you with medical advice and should not be used in case of an emergency. Please DON'T disclose any personal information other than your availability.  If you would like to talk to a human assistant, please find attached the following contact information:\n" +
            "\n" +
            "Surgery contact number: phone number\n" +
            "Address: location address"; //TODO include location address and phone number

    private EmailSender( SegmentQueue<OutgoingEmailMessage> messagesToSend ) {
        //TODO how to deal with this behaviour?
        if (null == applicationEmailAddress) System.err.println(
                "Can't send emails because Sender doesn't know application email address. Try checking system variables");
        System.out.println("EMAIL SENDER WILL SEND EMAILS FROM: " + applicationEmailAddress);
        this.messagesToSend = messagesToSend;
    }

    //This is an indempotent function
    //Only a singleton Sender will be created
    public static EmailSender getSender( SegmentQueue<OutgoingEmailMessage> messagesToSend ) {
        if (null == uniqueSender) {
            uniqueSender = new EmailSender(messagesToSend);

            Thread Major = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if (0 == messagesToSend.NumWaiting()) continue;

                        System.out.println("NEED TO SEND " + messagesToSend.NumWaiting() + " EMAILS!");
                        OutgoingEmailMessage emailToSend = messagesToSend.take();
                        if (null == emailToSend) continue;

                        EmailMessageType type = emailToSend.getMessageType();
                        String patientEmailAddress = emailToSend.getPatientEmailAddress();
                        String patientName = emailToSend.getPatientName();
                        String doctorName = emailToSend.getDoctorName();
                        String appointmentDate = emailToSend.getAppointmentDate();
                        String appointmentTime = emailToSend.getAppointmentTime();

                        try {
                            //TODO check if all scenarios are implemented
                            switch (type) {
                                case InitialReminderMessage:
                                    sendInitialReminderEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime);
                                    break;
                                case ConfirmationMessage:
                                    //TODO ???
                                    break;
                                case AskToPickAnotherTimeSlotMessage:
                                    //TODO ?? Need to pass info about time slots
                                    break;
                                case NewAppointmentDetailsMessage:
                                    sendNewAppointmentDetailsEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime);
                                    break;
                                case InvalidEmailMessage:
                                    sendUnexpectedSenderEmail(patientEmailAddress);
                                    break;
                                case ResponseWasUnclassifiedMessage:
                                    sendResponseWasUnclassifiedEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime);
                                    break;
                                default:
                                    System.err.println("Unimplemented email type");
                            }
                        } catch (FailedToSendEmail failedToSendEmail) {
                            System.err.println("We couldn't send email to " + patientEmailAddress);
                            failedToSendEmail.printStackTrace();
                        }

                    }
                }
            };
            Major.start();
        }
        return uniqueSender;
    }

    private static void sendEmail(
            String senderEmailAddress,
            String receiverEmailAddress,
            String subject,
            String messageText ) throws FailedToSendEmail {
        Email sendersEmail = new Email(senderEmailAddress);
        Email receiversEmail = new Email(receiverEmailAddress);
        Content content = new Content("text/plain", messageText + footer);

        Mail mail = new Mail(sendersEmail, subject, receiversEmail, content);

        SendGrid sendgrid = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendgrid.api(request);
            if (response.getStatusCode() != 202) {
                throw new FailedToSendEmail(
                        "STATUS: " + response.getStatusCode() +
                                " .BODY:" + response.getBody() +
                                " .HEADERS: " + response.getHeaders());
            }
            System.out.println("Sent email to: " + receiverEmailAddress);
        } catch (IOException ex) {
            throw new FailedToSendEmail(ex.getMessage());
        }

        try {
            TimeUnit.SECONDS.sleep(secondsToWaitBetweenEmails);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sendInitialReminderEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime ) throws FailedToSendEmail {
        sendEmail(applicationEmailAddress, patientEmailAddress, "GP Appointment Reminder",
                "Dear " + patientName + ",\n" +
                        "\n" +
                        "You have an appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + ". \n" +
                        "\n" +
                        "Please reply to this email stating whether you would like to confirm, cancel or reschedule your appointment. \n" +
                        "\n" +
                        "In case you want a new appointment, please provide 3 one hour slots over the next 14 days when you are available, in the following format: DD-MM-YYYY, from hh:mm AM/PM to hh:mm AM/PM.\n" +
                        "\n" +
                        "Example:\n" +
                        "22-02-2019 from 11:00 AM to 12:00 PM\n" +
                        "22-02-2019 from 03:00 PM to 04:00 PM\n" +
                        "25-02-2019 from 03:00 PM to 04:00 PM\n" +
                        "\n" +
                        "If you have any questions, use the contact details below to get in touch with us.\n" +
                        "\n" +
                        "Thank you and have a nice day!\n");
    }

    public static void sendUnexpectedSenderEmail( String patientEmailAddress ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "Do you have the right address?",
                "Dear patient,\n"
                        + "\n"
                        + "Please ensure:\n"
                        + "You've received an email reminder for your appointment from us. \n"
                        + "You are emailing us from the email account you have registered with our GP surgery.\n"
                        + "\n"
                        + "Otherwise, this email system can't help you and we advise you to contact the GP surgery with the contact information below. \n");
    }

    public static void sendResponseWasUnclassifiedEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "We couldn't process your response",
                "Dear " + patientName + ",\n"
                        + "We were unable to understand your decision regarding your appointment."
                        + "Your appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + " remains unchanged. \n"
                        + "\n"
                        + "If you have any questions, use the contact details below to get in touch with us.\n"
                        + "\n"
                        + "Thank you and have a nice day!\n");
    }

    public static void sendCancelationEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "Your appointment was cancelled\n",
                "Dear "
                        + patientName
                        + ",\n"
                        + "Your appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + " has been cancelled. \n"
                        + "\n"
                        + "If you have any questions, use the contact details below to get in touch with us.\n"
                        + "\n"
                        + "Thank you and have a nice day!\n");
    }

    public static void sendEmailAskingToPickAnotherTimeSlots(
            String patientEmailAddress,
            String patientName ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "Pick another time for appointment",
                "Dear "
                        + patientName
                        + ",\n"
                        + "We were unable to find an appointment in the preferred time slots you provided or you didn't use the required format. \n"
                        + "\n"
                        + "Please provide 3 new one hour slots over the next 14 days of when you are available, in the following format: DD-MM-YYYY, from hh:mm AM/PM to hh:mm AM/PM.\n"
                        + "\n"
                        + "Example:\n"
                        + "27-02-2020 from 11:00 AM to 12:00 PM\n"
                        + "27-02-2020 from 03:00 PM to 04:00 PM\n"
                        + "28-02-2020 from 03:00 PM to 04:00 PM\n"
                        + "\n"
                        + "If you have any questions, use the contact details below to get in touch with us.\n"
                        + "\n"
                        + "Thank you and have a nice day!\n");
    }

    public static void sendNewAppointmentDetailsEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "Your appointment was cancelled\n",
                "Dear "
                        + patientName
                        + ",\n"
                        + "Your appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + " has been cancelled. \n"
                        + "\n"
                        + "If you have any questions, use the contact details below to get in touch with us.\n"
                        + "\n"
                        + "Thank you and have a nice day!\n");
    }

    public static void main( String[] args ) {
        SegmentQueue OutQ = new SegmentQueue<>();
        EmailSender sender = EmailSender.getSender(OutQ);
        OutgoingEmailMessage emailToSimon = new OutgoingEmailMessage(
                "sm2354@cam.ac.uk",
                "Mr. Simon",
                "Dr. John",
                "27-02-2021",
                "11:00 AM",
                InitialReminderMessage);
        OutQ.put(emailToSimon);
    }
}

