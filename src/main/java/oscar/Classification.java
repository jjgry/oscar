package oscar;

import java.io.IOException;

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
  private String[] dates;

  Classification(String emailText) throws IOException {
    String category = classifier.EmailClassifier.getCategory(emailText);
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

