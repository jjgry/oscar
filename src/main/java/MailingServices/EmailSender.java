package MailingServices;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import oscar.Kernel;
import oscar.SegmentQueue;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static MailingServices.EmailMessageType.InitialReminderMessage;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *  This class is responsible for receiving emails from patients
 */
public class EmailSender {
    private static final String apiKey = System.getenv("SENDGRID_API_KEY");
    private static final String applicationEmailAddress = System.getenv("GMAIL_ACCOUNT_EMAIL_ADDRESS");
    private static final int secondsToWaitBetweenEmails = 10;
    private static final int secondsToWaitBetweenCheckingQueue = 10;
    private static EmailSender uniqueSender;
    private SegmentQueue<OutgoingEmailMessage> messagesToSend;

    private static final String identificationHeader = "----------\n";
    private static final String footer =
            "\n\n\n----------\n" +
            "Oscar is an automated email assistant system helping you remember and manage your GP appointment. " +
            "This email system can't provide you with medical advice and should not be used in case of an emergency. " +
            "Please DON'T disclose any personal information other than your availability.  " +
            "If you would like to talk to a human assistant, please find attached the following contact information:\n" +
            "\n" +
            "Surgery contact number: 01223 697600\n" +
            "Address: 48-49 Bateman St, Cambridge CB2 1LR\n" +
            "----------\n" ;

    /**
     * @param messagesToSend is a thread-safe queue from which message are taken to be sent
     * @throws FailedToInstantiateComponent if there is any instantiation error
     */
    private EmailSender( SegmentQueue<OutgoingEmailMessage> messagesToSend ) throws FailedToInstantiateComponent {
        if (null == applicationEmailAddress) {
            System.err.println(
                    "Sender: Can't send emails because Sender doesn't know application email address. Try checking system variables");
            throw new FailedToInstantiateComponent("EmailSender couldn't be instantiated because applicationEmailAddress is null");
        }
        System.out.println("Sender: EMAIL SENDER WILL SEND EMAILS FROM: " + applicationEmailAddress);
        this.messagesToSend = messagesToSend;
    }

    /**
     * This is an indempotent function
     * Only a singleton Sender will be created
     *
     * @param messagesToSend is a thread-safe queue from which message are taken to be sent
     * @return singleton instance of EmailSender
     * @throws FailedToInstantiateComponent if there is any instantiation error
     */
    public static EmailSender getEmailSender( SegmentQueue<OutgoingEmailMessage> messagesToSend ) throws FailedToInstantiateComponent {
        if (null == uniqueSender) {
            uniqueSender = new EmailSender(messagesToSend);

            Thread SenderThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        System.out.println("Sender: Need to send: " + messagesToSend.NumWaiting() + " emails.");
                        try {
                            TimeUnit.SECONDS.sleep(secondsToWaitBetweenCheckingQueue);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        if (0 == messagesToSend.NumWaiting()) continue;

                        OutgoingEmailMessage emailToSend = messagesToSend.take();
                        if (null == emailToSend) continue;

                        EmailMessageType type = emailToSend.getMessageType();
                        String patientEmailAddress = emailToSend.getPatientEmailAddress();
                        String patientName = emailToSend.getPatientName();
                        String doctorName = emailToSend.getDoctorName();
                        String appointmentDate = emailToSend.getAppointmentDate();
                        String appointmentTime = emailToSend.getAppointmentTime();
                        String appointmentID = emailToSend.getAppointmentID();

                        boolean successfullyDelivered = false;
                        try {
                            switch (type) {
                                case InitialReminderMessage:
                                    sendInitialReminderEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime,
                                            appointmentID
                                    );
                                    successfullyDelivered = true;
                                    break;
                                case CancellationMessage:
                                    sendCancelationEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime,
                                            appointmentID);
                                    successfullyDelivered = true;
                                    break;
                                case AskToPickAnotherTimeSlotMessage:
                                    sendEmailAskingToPickAnotherTimeSlots(
                                            patientEmailAddress,
                                            patientName,
                                            appointmentID);
                                    successfullyDelivered = true;
                                    break;
                                case NewAppointmentDetailsMessage:
                                    sendNewAppointmentDetailsEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime,
                                            appointmentID);
                                    successfullyDelivered = true;
                                    break;
                                case InvalidEmailMessage:
                                    sendUnexpectedSenderEmail(patientEmailAddress);
                                    successfullyDelivered = true;
                                    break;
                                case ConfirmationMessage:
                                    sendConfirmationEmail(
                                            patientEmailAddress,
                                            patientName,
                                            doctorName,
                                            appointmentDate,
                                            appointmentTime,
                                            appointmentID);
                                    successfullyDelivered = true;
                                    break;
                                default:
                                    System.err.println("Sender: Unimplemented email type");
                            }
                        } catch (FailedToSendEmail failedToSendEmail) {
                            System.err.println("Sender: We couldn't send email to " + patientEmailAddress);

                            successfullyDelivered = false;
                            failedToSendEmail.printStackTrace();
                        }

                        if(successfullyDelivered) Kernel.Confirm_Intro_Email_Sent(appointmentID);
                    }
                }
            };
            System.out.println("Sender started");
            SenderThread.setDaemon(true);
            SenderThread.start();
        }
        return uniqueSender;
    }

    /**
     *  DEPRECATED method because Sendgrid keeps blocking our application
     *
     *  Problem could be resolved if we were to remove API keys from GitHub
     */
    private static void sendEmailWithSendgrid (
            String senderEmailAddress,
            String receiverEmailAddress,
            String subject,
            String messageText ) throws FailedToSendEmail {
        Email sendersEmail = new Email(senderEmailAddress);
        Email receiversEmail = new Email(receiverEmailAddress);
        Content content = new Content("text/plain", messageText);

        System.out.println("Sender: SENDING EMAIL TO "+ receiverEmailAddress+": \n"+messageText);

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
            System.out.println("Sender: Sent email to: " + receiverEmailAddress);
        } catch (IOException ex) {
            throw new FailedToSendEmail(ex.getMessage());
        }
    }

    /**
     * @param senderEmailAddress
     * @param receiverEmailAddress 'nhs.appointment.reminder@gmail.com'
     * @param subject string of text at the top of email message
     * @param messageText string of text to be included as the message body
     * @throws FailedToSendEmail if Gmail API throws an exception
     */
    private static void sendEmailWithGmail(
            String senderEmailAddress,
            String receiverEmailAddress,
            String subject,
            String messageText ) throws FailedToSendEmail {
        try {
            GmailSender gmailSender = GmailSender.getGmailSender();
            gmailSender.sendMessage(receiverEmailAddress,
                    senderEmailAddress,
                    subject,
                    messageText);
        } catch (GeneralSecurityException | IOException | MessagingException e) {
            throw new FailedToSendEmail(e.getMessage());
        }
    }

    private static void sendEmail(
            String senderEmailAddress,
            String receiverEmailAddress,
            String subject,
            String messageText ) throws FailedToSendEmail {

        // Pick one of the implementations
        // 1. Gmail API
        sendEmailWithGmail(senderEmailAddress,
                receiverEmailAddress,
                subject,
                identificationHeader + messageText + footer);

        // 2. Sendgrid API
        //sendEmailWithSendgrid(senderEmailAddress,receiverEmailAddress,subject, messageText);

        try {
            TimeUnit.SECONDS.sleep(secondsToWaitBetweenEmails);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @throws FailedToSendEmail if sending API throws an exception
     */
    public static void sendInitialReminderEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime,
            String appointmentID ) throws FailedToSendEmail {
        sendEmail(applicationEmailAddress, patientEmailAddress, "[" + appointmentID + "] GP Appointment Reminder",
                "Dear " + patientName + ",\n" +
                        "\n" +
                        "You have an appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + ". \n" +
                        "\n" +
                        "Please reply to this email stating whether you would like to confirm, cancel or reschedule your appointment. \n" +
                        "\n" +
                        "In case you want a new appointment, " +
                        "please provide 3 time slots when you are available in the next few weeks in the following format: " +
                        "DD-MM-YYYY, from hh:mm AM/PM to hh:mm AM/PM.\n" +
                        "\n" +
                        "For example:\n" +
                        "22-02-2019 from 11:00 AM to 12:00 PM\n" +
                        "22-02-2019 from 03:00 PM to 05:30 PM\n" +
                        "25-02-2019 from 01:45 PM to 04:00 PM\n" +
                        "\n" +
                        "If you have any questions, use the contact details below to get in touch with my human supervisor.\n" +
                        "\n" +
                        "Thank you and have a nice day!\n" +
                        "Oscar\n");
    }

    /**
     * @throws FailedToSendEmail if sending API throws an exception
     */
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
                        + "You have kept the appointment ID number in the header of the email (we need it for our record-keeping!)\n"
                        + "\n"
                        + "Otherwise, for privacy and security reasons this email system cannot process your messages,\n"
                        + "and we advise you to contact the GP surgery with the contact information below. \n"
                        + "\n"
                        + "Many thanks,\n"
                        + "Oscar\n");
    }

    /**
     * @throws FailedToSendEmail if sending API throws an exception
     */
    public static void sendCancelationEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime,
            String appointmentID ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "[" + appointmentID + "]Your appointment was cancelled\n",
                "Dear "
                        + patientName
                        + ",\n"
                        + "Your appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + " has been cancelled. \n"
                        + "\n"
                        + "If you have any questions, use the contact details below to get in touch with my human supervisor.\n"
                        + "\n"
                        + "Thank you and have a nice day!\n"
                        + "Oscar\n");
    }

    /**
     * @throws FailedToSendEmail if sending API throws an exception
     */
    public static void sendConfirmationEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime,
            String appointmentID ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "[" + appointmentID + "] Confirmation email\n",
                "Dear " + patientName + ",\n" +
                        "\n" +
                        "You have confirmed your place for an appointment with " + doctorName + " on " + appointmentDate + " at " + appointmentTime + ". \n" +
                        "\n" +
                        "If you have any questions, use the contact details below to get in touch with my human supervisor.\n" +
                        "\n" +
                        "Thank you and have a nice day!\n" +
                        "Oscar\n");
    }

    /**
     * @throws FailedToSendEmail if sending API throws an exception
     */
    public static void sendEmailAskingToPickAnotherTimeSlots(
            String patientEmailAddress,
            String patientName,
            String appointmentID ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "[" + appointmentID + "] Pick another time for appointment",
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
                        + "If you have any questions, use the contact details below to get in touch with my human supervisor.\n"
                        + "\n"
                        + "Thank you and have a nice day!\n"
                        + "Oscar\n");
    }

    /**
     * @throws FailedToSendEmail if sending API throws an exception
     */
    public static void sendNewAppointmentDetailsEmail(
            String patientEmailAddress,
            String patientName,
            String doctorName,
            String appointmentDate,
            String appointmentTime,
            String appointmentID ) throws FailedToSendEmail {
        sendEmail(
                applicationEmailAddress,
                patientEmailAddress,
                "[" + appointmentID + "] Your appointment can be rescheduled\n",
                "Dear "
                        + patientName
                        + ",\n"
                        + "Your appointment can be rescheduled to "+ appointmentTime + " on "+ appointmentDate + ", with your previous Doctor, " + doctorName +"."
                        + " Would this be acceptable?"
                        + "\n"
                        + "If you have any questions, use the contact details below to get in touch with my human supervisor.\n"
                        + "\n"
                        + "Many thanks,\n"
                        + "Oscar \n");
    }

    /**
     * Example how to use EmailSender
     */
    public static void main( String[] args ) throws FailedToInstantiateComponent  {
        SegmentQueue OutQ = new SegmentQueue<>();
        EmailSender sender = EmailSender.getEmailSender(OutQ);
        OutgoingEmailMessage emailToSimon = new OutgoingEmailMessage(
                "mulevicius.simonas@gmail.com",
                "Mr. Justas",
                "Dr. Joanna Rimmer",
                "27-02-2021",
                "11:00 AM",
                InitialReminderMessage,
                "101123502");
        OutQ.put(emailToSimon);

        System.out.println("Main thread will sleep to allow sender to send email");
        try {
            TimeUnit.SECONDS.sleep(40);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}

