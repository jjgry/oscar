package MailingServices;

//TODO delete this comment
public enum EmailMessageType {
    InitialReminderMessage,
    CancellationMessage,
    AskToPickAnotherTimeSlotMessage,
    ConfirmationMessage,
    NewAppointmentDetailsMessage,//"After deciding you want RESCHEDULE, this suggested time may be good! what do you think?"
    InvalidEmailMessage
}
