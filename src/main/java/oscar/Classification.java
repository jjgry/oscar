package oscar;

import java.io.IOException;
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

