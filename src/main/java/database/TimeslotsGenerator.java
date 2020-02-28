package database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TimeslotsGenerator {
    public static void main(String args[]) {
        try {
            FileWriter fw = new FileWriter("Timeslots.csv");
            BufferedWriter bw = new BufferedWriter(fw);
            String minutes[] = {"00", "10", "20", "30", "40"};
            int id = 1;
            List<String> locations = new ArrayList<>();
            locations.add("Addenbrookes");
            locations.add("Newnham Walk Surgery");
            locations.add("Town Centre Surgery");
            for (int day = 1; day <= 5; day++) {
                for (int doc = 1; doc <= 5; doc++) {
                    for (int hour = 10; hour <= 15; hour++) {
                        for (String minute : minutes) {
                            Random randomGenerator = new Random();
                            bw.append(String.join(",", "" + (id++), "2020-03-0" + day + " " + hour + ":" + minute + ":00", "1", "" + doc, locations.get(randomGenerator.nextInt(locations.size()))));
                            bw.append("\n");
                        }
                    }
                }
            }
            bw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
