import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

/**
 * Ethical surgery assistant for automated email replies. It uses different features of Apache OpenNLP
 * for understanding whether the patient is confirming, canceling or rescheduling the appointment or
 * whether the assistant is dealing with an automated email(or none of the situations described).
 */
public class SurgeryAssistant {

    private static Map<String, String> questionAnswer = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // Train categorizer model to the training data we created.
        DoccatModel model = trainCategorizerModel();

        // Take chat inputs from console (user) in a loop.
        Scanner scanner = new Scanner(System.in);
        while (true) {

            // Get chat input from user.
            System.out.println("##### You:");
            String emailText = scanner.nextLine();

            // Look for slots suggested by the patient (these are needed if the email's category is RESCHEDULE)
            String[] suggestedSlots = findSuggestedSlots(emailText);
            for(int i = 0; i < 6; i++)
                System.out.println(suggestedSlots[i]);

            // Find the category of the email.
            String category = assignCategoryToEmail(emailText, model);
        }
    }

    /**
     * Train categorizer model as per the category sample training data we created.
     */
    private static DoccatModel trainCategorizerModel() throws IOException {
        // training-data.txt is a custom training data with categories as per our chat requirements.
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File(
            "lib/training-data.txt"));
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

        TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 1000);
        params.put(TrainingParameters.CUTOFF_PARAM, 0);

        // Train a model with classifications from above file.
        DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, factory);
        return model;
    }

    /**
     * Assign to the text of an email one of the categories: CONFIRM, CANCEL, RESCHEDULE, AUTOMATED, OTHER.
     */
    private static String assignCategoryToEmail(String emailText, DoccatModel model) throws IOException {
        // Separate words from each sentence using tokenizer.
        String[] tokens = tokenizeSentence(emailText);

        // Tag separated words with POS tags to understand their grammatical structure.
        String[] posTags = detectPOSTags(tokens);

        // Lemmatize each word so that its easy to categorize.
        String[] lemmas = lemmatizeTokens(tokens, posTags);

        // Determine BEST category using lemmatized tokens used a mode that we trained at start.
        String category = detectCategory(model, lemmas);

        return category;
    }

    /**
     * Detect category using given token using the categorizer feature of Apache OpenNLP.
     */
    private static String detectCategory(DoccatModel model, String[] finalTokens) {

        // Initialize document categorizer tool
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);

        // Get best possible category.
        double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens);
        String category = myCategorizer.getBestCategory(probabilitiesOfOutcomes);
        System.out.println(myCategorizer.getAllResults(probabilitiesOfOutcomes));
        System.out.println("Category: " + category);

        return category;
    }

    /**
     * Break data into sentences using the sentence detection feature of Apache OpenNLP.
     */
    private static String[] breakSentences(String data) throws IOException {
        try (InputStream modelIn = new FileInputStream("model" + File.separator + "en-sent.bin")) {

            SentenceDetectorME myCategorizer = new SentenceDetectorME(new SentenceModel(modelIn));

            String[] sentences = myCategorizer.sentDetect(data);
            System.out.println("Sentence Detection: " + Arrays.stream(sentences).collect(Collectors
                    .joining(" | ")));

            return sentences;
        }
    }

    /**
     * Break sentence into words & punctuation marks using the tokenizer feature of Apache OpenNLP.
     */
    private static String[] tokenizeSentence(String sentence) throws IOException {
        try (InputStream modelIn = new FileInputStream("lib" + File.separator + "en-token.bin")) {

            // Initialize tokenizer tool
            TokenizerME myCategorizer = new TokenizerME(new TokenizerModel(modelIn));

            // Tokenize sentence.
            String[] tokens = myCategorizer.tokenize(sentence);
            System.out.println("Tokenizer : " + Arrays.stream(tokens).collect(Collectors.joining(" | ")));

            return tokens;
        }
    }

    /**
     * Find part-of-speech tags of all tokens using the POS tagger feature of Apache OpenNLP.
     */
    private static String[] detectPOSTags(String[] tokens) throws IOException {
        try (InputStream modelIn = new FileInputStream("lib" + File.separator + "en-pos-maxent.bin")) {

            // Initialize POS tagger tool
            POSTaggerME myCategorizer = new POSTaggerME(new POSModel(modelIn));

            // Tag sentence.
            String[] posTokens = myCategorizer.tag(tokens);
            System.out.println("POS Tags : " + Arrays.stream(posTokens).collect(Collectors.joining(" | ")));

            return posTokens;
        }
    }

    /**
     * Find lemma of tokens using the lemmatizer feature of Apache OpenNLP.
     */
    private static String[] lemmatizeTokens(String[] tokens, String[] posTags) throws IOException {
        try (InputStream modelIn = new FileInputStream("lib" + File.separator + "en-lemmatizer.bin")) {

            // Tag sentence.
            LemmatizerME myCategorizer = new LemmatizerME(new LemmatizerModel(modelIn));
            String[] lemmaTokens = myCategorizer.lemmatize(tokens, posTags);
            System.out.println("Lemmatizer : " + Arrays.stream(lemmaTokens).collect(Collectors.joining(" | ")));

            return lemmaTokens;
        }
    }

    /**
     * Look for slots suggested by the patient (these are needed if the email's category is RESCHEDULE).
     *
     * It returns an array of 6 strings, each having the format: YYYY/MM/DD hh:mm:ss
     * The 1st, 3rd and 5th strings represent the start times of the three proposed slots and
     * the 2nd, 4th and 6th strings represent the corresponding end times (note that
     * the 1st string and the 2nd one are associated with the first slot, and so on).
     *
     * If the patient suggests less than 3 slots, the corresponding strings are empty.
     */
    private static String[] findSuggestedSlots(String emailText) {
        String[] slots = new String[6];
        int dash, colon1, colon2, found;
        dash = emailText.indexOf("-");
        found = 0;
        String year, month, day, start, end;

        while(dash != -1 && found < 3) {
            int i = dash - 1;
            //Search for day.
            day = "";
            while(i >= 0 && emailText.charAt(i) == ' ') {
                i--;
            }
            while(i >= 0 && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
                day = emailText.charAt(i) + day;
                i--;
            }
            if(day.length() == 1) {
                day = "0" + day;
            }
            if(day.length() != 2) {
                dash = emailText.indexOf("-", dash + 1);
                continue;
            }

            // Search for month.
            month = "";
            i = dash + 1;
            while(i < emailText.length() && emailText.charAt(i) == ' ') {
                i++;
            }
            while(i < emailText.length() && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
                month = month + emailText.charAt(i);
                i++;
            }
            if(month.length() == 1) {
                month = "0" + month;
            }
            if(month.length() != 2) {
                dash = emailText.indexOf("-", dash + 1);
                continue;
            }

            // Search for year.
            year = "";
            while(i < emailText.length() && (emailText.charAt(i) == ' ' || emailText.charAt(i) == '-')) {
                i++;
            }
            while(i < emailText.length() && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
                year = year + emailText.charAt(i);
                i++;
            }
            if(year.length() != 4) {
                dash = emailText.indexOf("-", i);
                continue;
            }
            dash = emailText.indexOf("-", i);

            // Find times.
            colon1 = emailText.indexOf(":", i);
            colon2 = emailText.indexOf(":", colon1 + 1);
            int limit = (dash == -1) ? emailText.length() : dash;
            if(colon2 > limit || colon1 == -1 || colon2 == -1) {
                // no starting and ending time specified for the date that has been found
                continue;
            }

            // Find starting time.
            start = findHour(colon1, emailText, colon2);
            if(start.length() != 8) {
                continue;
            }

            // Find ending time.
            end = findHour(colon2, emailText, limit);
            if(end.length() != 8) {
                continue;
            }

            slots[2 * found] = year + "/" + month + "/" + day + " " + start;
            slots[2 * found + 1] = year + "/" + month + "/" + day + " " + end;
            ++found;
        }
        return slots;
    }

    /**
     * Finds the hour, given the email text, the position of the colon that the hour contains
     * and the index by which the hour should have been found in the text.
     * The hour is returned as a string in the format: HH:mm:ss
     */
    private static String findHour(int colon, String emailText, int limit) {
        String hour;
        hour = "";
        int i = colon - 1;
        while(i > 0 && emailText.charAt(i) == ' ') {
            i--;
        }
        while(i > 0 && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
            hour = emailText.charAt(i) + hour;
            i--;
        }
        if(hour.length() < 1 || hour.length() > 2) {
            return hour;
        }
        if(hour.length() == 1) {
            hour = "0" + hour;
        }
        hour += ":";
        i = colon + 1;
        while(i < emailText.length() && emailText.charAt(i) == ' ') {
            i++;
        }
        while(i < emailText.length() && emailText.charAt(i) >= '0' && emailText.charAt(i) <= '9') {
            hour  = hour + emailText.charAt(i);
            i++;
        }
        if(hour.length() != 5) {
            return hour;
        }

        if((emailText.indexOf("pm", i) < limit && emailText.indexOf("pm", i) > -1) ||
                (emailText.indexOf("Pm", i) < limit && emailText.indexOf("Pm", i) > -1) ||
                (emailText.indexOf("PM", i) < limit && emailText.indexOf("PM", i) > -1) ) {
            int nr = (hour.charAt(0) - '0') * 10 + (hour.charAt(1) - '0');
            if(nr < 12) {
                nr += 12;
            }
            hour = Integer.toString(nr) + hour.substring(2);
        }

        if((emailText.indexOf("am", i) < limit && emailText.indexOf("am", i) > -1) ||
                (emailText.indexOf("Am", i) < limit && emailText.indexOf("Am", i) > -1) ||
                (emailText.indexOf("AM", i) < limit && emailText.indexOf("AM", i) > -1) ) {
            int nr = (hour.charAt(0) - '0') * 10 + (hour.charAt(1) - '0');
            if(nr == 12) {
                hour = "00" + hour.substring(2);
            }
        }

        hour += ":00";
        return hour;
    }
}