package classifier;

import java.io.IOException;
import java.util.Scanner;

public class EmailDateExtractor {

  public static void main(String[] args) throws IOException {

    // Take chat inputs from console (user) in a loop.
    Scanner scanner = new Scanner(System.in);
    while (true) {

      // Get chat input from user.
      System.out.println("##### You:");
      String emailText = scanner.nextLine();

      // Look for slots suggested by the patient (these are needed if the email's category is RESCHEDULE)
      String[] suggestedSlots = findSuggestedSlots(emailText);
      for (int i = 0; i < 6; i++) {
        System.out.println(suggestedSlots[i]);
      }

    }
  }

  /**
   * Look for slots suggested by the patient (these are needed if the email's category is
   * RESCHEDULE).
   *
   * It returns an array of 6 strings, each having the format: YYYY/MM/DD hh:mm:ss The 1st, 3rd and
   * 5th strings represent the start times of the three proposed slots and the 2nd, 4th and 6th
   * strings represent the corresponding end times (note that the 1st string and the 2nd one are
   * associated with the first slot, and so on).
   *
   * If the patient suggests less than 3 slots, the corresponding strings are empty.
   */
  private static String[] findSuggestedSlots(String emailText) {
    String[] slots = new String[6];
    int dash, colon1, colon2, found;
    dash = emailText.indexOf("-");
    found = 0;
    String year, month, day, start, end;

    while (dash != -1 && found < 3) {
      int i = dash - 1;
      //Search for day.
      day = "";
      while (i >= 0 && emailText.charAt(i) == ' ') {
        i--;
      }
      while (i >= 0 && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
        day = emailText.charAt(i) + day;
        i--;
      }
      if (day.length() == 1) {
        day = "0" + day;
      }
      if (day.length() != 2) {
        dash = emailText.indexOf("-", dash + 1);
        continue;
      }

      // Search for month.
      month = "";
      i = dash + 1;
      while (i < emailText.length() && emailText.charAt(i) == ' ') {
        i++;
      }
      while (i < emailText.length() && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
        month = month + emailText.charAt(i);
        i++;
      }
      if (month.length() == 1) {
        month = "0" + month;
      }
      if (month.length() != 2) {
        dash = emailText.indexOf("-", dash + 1);
        continue;
      }

      // Search for year.
      year = "";
      while (i < emailText.length() && (emailText.charAt(i) == ' ' || emailText.charAt(i) == '-')) {
        i++;
      }
      while (i < emailText.length() && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
        year = year + emailText.charAt(i);
        i++;
      }
      if (year.length() != 4) {
        dash = emailText.indexOf("-", i);
        continue;
      }
      dash = emailText.indexOf("-", i);

      // Find times.
      colon1 = emailText.indexOf(":", i);
      colon2 = emailText.indexOf(":", colon1 + 1);
      int limit = (dash == -1) ? emailText.length() : dash;
      if (colon2 > limit || colon1 == -1 || colon2 == -1) {
        // no starting and ending time specified for the date that has been found
        continue;
      }

      // Find starting time.
      start = findHour(colon1, emailText, colon2);
      if (start.length() != 8) {
        continue;
      }

      // Find ending time.
      end = findHour(colon2, emailText, limit);
      if (end.length() != 8) {
        continue;
      }

      slots[2 * found] = year + "/" + month + "/" + day + " " + start;
      slots[2 * found + 1] = year + "/" + month + "/" + day + " " + end;
      ++found;
    }
    return slots;
  }

  /**
   * Finds the hour, given the email text, the position of the colon that the hour contains and the
   * index by which the hour should have been found in the text. The hour is returned as a string in
   * the format: HH:mm:ss
   */
  private static String findHour(int colon, String emailText, int limit) {
    String hour;
    hour = "";
    int i = colon - 1;
    while (i > 0 && emailText.charAt(i) == ' ') {
      i--;
    }
    while (i > 0 && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
      hour = emailText.charAt(i) + hour;
      i--;
    }
    if (hour.length() < 1 || hour.length() > 2) {
      return hour;
    }
    if (hour.length() == 1) {
      hour = "0" + hour;
    }
    hour += ":";
    i = colon + 1;
    while (i < emailText.length() && emailText.charAt(i) == ' ') {
      i++;
    }
    while (i < emailText.length() && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
      hour = hour + emailText.charAt(i);
      i++;
    }
    if (hour.length() != 5) {
      return hour;
    }

    if ((emailText.indexOf("pm", i) < limit && emailText.indexOf("pm", i) > -1) ||
        (emailText.indexOf("Pm", i) < limit && emailText.indexOf("Pm", i) > -1) ||
        (emailText.indexOf("PM", i) < limit && emailText.indexOf("PM", i) > -1)) {
      int nr = (hour.charAt(0) - '0') * 10 + (hour.charAt(1) - '0');
      if (nr < 12) {
        nr += 12;
      }
      hour = Integer.toString(nr) + hour.substring(2);
    }

    if ((emailText.indexOf("am", i) < limit && emailText.indexOf("am", i) > -1) ||
        (emailText.indexOf("Am", i) < limit && emailText.indexOf("Am", i) > -1) ||
        (emailText.indexOf("AM", i) < limit && emailText.indexOf("AM", i) > -1)) {
      int nr = (hour.charAt(0) - '0') * 10 + (hour.charAt(1) - '0');
      if (nr == 12) {
        hour = "00" + hour.substring(2);
      }
    }

    hour += ":00";
    return hour;
  }
}
