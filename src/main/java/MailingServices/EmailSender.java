package MailingServices;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import oscar.SegmentQueue;

import java.io.IOException;

public class EmailSender {
    //TODO change to ENV variables
    private static final String apiKey = "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";
    private static final String applicationEmailAddress = "nhs.appointment.reminder@gmail.com";
    private static EmailSender uniqueSender;
    private SegmentQueue<OutgoingEmailMessage> messagesToSend;

    private static final String footer = "\n" + "-----------------------------------------------------" + "\n" +
            "This is an automated email assistant system helping you remember and confirm/reschedule/cancel your GP appointment. This email system can't provide you with medical advice and should not be used in case of an emergency. Please DON'T disclose any personal information other than your availability.  If you would like to talk to a human assistant, please find attached the following contact information:\n" +
            "\n" +
            "Surgery contact number: phone number\n" +
            "Address: location address"; //TODO include location address and phone number

    //TODO make use of queue
    public EmailSender(SegmentQueue<OutgoingEmailMessage> messagesToSend){
        this.messagesToSend = messagesToSend;
    }
    public EmailSender() {}

    //TODO: make use of a queue
    public static EmailSender getSender(SegmentQueue<OutgoingEmailMessage> messagesToSend){
        if(null == uniqueSender){
            uniqueSender = new EmailSender(messagesToSend);
        }
        return uniqueSender;
    }

    private void sendEmail(
        String senderEmailAddress,
        String receiverEmailAddress,
        String subject,
        String messageText) throws FailedToSendEmail
    {
        Email sendersEmail = new Email(senderEmailAddress);
        Email receiversEmail = new Email(receiverEmailAddress);
        Content content = new Content("text/plain", messageText + footer);

        Mail mail = new Mail (sendersEmail, subject, receiversEmail, content);

        SendGrid sendgrid = new SendGrid(apiKey);
        Request request = new Request();

        try{
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendgrid.api(request);
            if(response.getStatusCode() != 202) {
                throw new FailedToSendEmail(
       "STATUS: " + response.getStatusCode() +
                    " .BODY:" + response.getBody() +
                    " .HEADERS: " + response.getHeaders());
            }
        } catch (IOException ex){
            throw new FailedToSendEmail(ex.getMessage());
        }
    }

    public void sendInitialReminderEmail(
         String patientEmailAddress,
         String patientName,
         String doctorName,
         String appointmentDate,
         String appointmentTime) throws FailedToSendEmail {
        sendEmail(applicationEmailAddress, patientEmailAddress, "GP Appointment Reminder",
                "Dear " + patientName +",\n" +
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
                "Thank you and have a nice day!\n" );
    }

    public void sendUnexpectedSenderEmail(String patientEmailAddress) throws FailedToSendEmail {
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

    public void sendResponseWasUnclassifiedEmail(
             String patientEmailAddress,
             String patientName,
             String doctorName,
             String appointmentDate,
             String appointmentTime) throws FailedToSendEmail {
        sendEmail(
            applicationEmailAddress,
            patientEmailAddress,
            "We couldn't process your response",
                "Dear " + patientName +",\n"
                + "We were unable to understand your decision regarding your appointment."
                + "Your appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + " remains unchanged. \n"
                + "\n"
                + "If you have any questions, use the contact details below to get in touch with us.\n"
                + "\n"
                + "Thank you and have a nice day!\n");
    }

    public void sendCancelationEmail(
         String patientEmailAddress,
         String patientName,
         String doctorName,
         String appointmentDate,
         String appointmentTime) throws FailedToSendEmail {
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

    public void sendEmailAskingToPickAnotherTimeSlots(
            String patientEmailAddress,
            String patientName) throws FailedToSendEmail {
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

    public void sendNewAppointmentDetailsEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime) throws FailedToSendEmail {
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

    public static void main(String[] args) {
        EmailSender sender = new EmailSender();
        try {
            sender.sendInitialReminderEmail(
                    "sm2354@cam.ac.uk",
                    "Simon",
                    "Mr. John",
                    "27-02-2020",
                    "11:00 AM");

            sender.sendCancelationEmail(
                    "sm2354@cam.ac.uk",
                    "Simon",
                    "Mr. John",
                    "27-02-2020",
                    "11:00 AM");

            sender.sendEmailAskingToPickAnotherTimeSlots(
                    "sm2354@cam.ac.uk",
                    "Simon");

            sender.sendResponseWasUnclassifiedEmail(
                    "sm2354@cam.ac.uk",
                    "Simon",
                    "Mr. John",
                    "27-02-2020",
                    "11:00 AM");

            sender.sendUnexpectedSenderEmail("sm2354@cam.ac.uk");

            System.out.println("Successfully sent emails");
        } catch (FailedToSendEmail failedToSendEmail) {
            System.out.println("Failed to send");
            failedToSendEmail.printStackTrace();
        }
    }
}

