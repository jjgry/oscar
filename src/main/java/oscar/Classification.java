package oscar;

import java.io.IOException;

import MailingServices.InputSanitizer;
import opennlp.tools.doccat.DoccatModel;

public class Classification {

  public classes getDecision() {
    return decision;
  }

  public String[] getDates() {
    return dates;
  }

  enum classes {
    CONFIRM,
    CANCEL,
    RESCHEDULE,
    OTHER
  }

  /**
   Remove old message contents from new message
   */
  //TODO make private
  public static String removeContentsOfLastEmail(String unparsedEmail){
    int messageLength = unparsedEmail.length();

    int indexUnderScores = unparsedEmail.indexOf("_______");
    indexUnderScores = (-1 == indexUnderScores) ? messageLength : indexUnderScores;

    int indexDashes = unparsedEmail.indexOf("----------");
    indexDashes = (-1 == indexDashes) ? messageLength : indexDashes;

    int indexSmaller = unparsedEmail.indexOf("<");
    indexSmaller = (-1 == indexSmaller) ? messageLength : indexSmaller;

    int indexLarger = unparsedEmail.indexOf(">");
    indexLarger = (-1 == indexLarger) ? messageLength : indexLarger;

    int index = Math.min(Math.min(Math.min(indexUnderScores, indexDashes), indexLarger),indexSmaller);

    if (index != -1) {
      unparsedEmail = unparsedEmail.substring(0, index);
    }

    return InputSanitizer.removeOldEmailContents(unparsedEmail);
  }

  private static String removeNewLines(String unparsedEmail) {
    unparsedEmail = unparsedEmail.replace("\r", " ").replace("\n", " ");
    return unparsedEmail;
  }

  private classes decision;

  /**
   * an array of 6 strings, each having the format: YYYY/MM/DD hh:mm:ss The 1st, 3rd and
   * 5th strings represent the start times of the three proposed slots and the 2nd, 4th and 6th
   * strings represent the corresponding end times (note that the 1st string and the 2nd one are
   * associated with the first slot, and so on).
   * <p>
   * If the patient suggests less than 3 slots, the corresponding strings are empty.
   */
  private String[] dates;

  Classification(String emailText, DoccatModel model) throws IOException {
    //make email into correct format
    emailText = removeContentsOfLastEmail(emailText);
    emailText = removeNewLines(emailText);

    String category = classifier.EmailClassifier.getCategory(emailText, model);
    if (category == "Cancel") {
      decision = classes.CANCEL;
    } else if (category == "Confirm") {
      decision = classes.CONFIRM;
    } else if (category == "Other") {
      decision = classes.OTHER;
    } else if (category == "Reschedule") {
      decision = classes.RESCHEDULE;
      dates = classifier.EmailDateExtractor.getDates(emailText);
    }
  }
}

