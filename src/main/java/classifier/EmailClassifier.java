package classifier;

import opennlp.tools.doccat.*;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.*;
import opennlp.tools.util.model.ModelUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;


/**
 * Ethical GP surgery assistant for automated email replies. It uses different features of Apache
 * OpenNLP for understanding whether the patient is confirming, canceling or rescheduling the
 * appointment or whether the assistant is dealing with an automated email(or none of the situations
 * described).
 */
public class EmailClassifier {

  /**
   * Returns email category.
   */
  public static String getCategory(String emailText, DoccatModel model) throws IOException {
    return assignCategoryToEmail(emailText, model);
  }


  public static void main(String[] args) throws IOException {

    // Train categorizer model to the training data we created.
    DoccatModel model = trainCategorizerModel();

    // Take chat inputs from console (user) in a loop.
    Scanner scanner = new Scanner(System.in);
    while (true) {

      // Get chat input from user.
      System.out.println("##### You:");
      String emailText = scanner.nextLine();

      // Find the category of the email.
      String category = assignCategoryToEmail(emailText, model);
    }
  }

  /**
   * Train categorizer model as per the category sample training data we created.
   */
  public static DoccatModel trainCategorizerModel() throws IOException {
    // training-data.txt is a custom training data with categories as per our chat requirements.
    InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File(
        "lib/training-data.txt"));
    ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory,
        StandardCharsets.UTF_8);
    ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
    DoccatFactory factory = new DoccatFactory(
        new FeatureGenerator[]{new BagOfWordsFeatureGenerator()});

    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put(TrainingParameters.ITERATIONS_PARAM, 1000);
    params.put(TrainingParameters.CUTOFF_PARAM, 0);

    // Train a model with classifications from above file.
    DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, factory);
    return model;
  }

  /**
   * Assign to the text of an email one of the categories: CONFIRM, CANCEL, RESCHEDULE,
   * OTHER.
   */
  private static String assignCategoryToEmail(String emailText, DoccatModel model)
      throws IOException {

    // Separate words from each sentence using tokenizer.
    String[] tokens = tokenizeSentence(emailText);

    // Tag separated words with POS tags to understand their grammatical structure.
    String[] posTags = detectPOSTags(tokens);

    // Lemmatize each word so that its easy to categorize.
    String[] lemmas = lemmatizeTokens(tokens, posTags);

    // Determine BEST category using lemmatized tokens used a mode that we trained at start.
    String category = detectCategory(lemmas, model);

    return category;
  }

  /**
   * Detect category using given token using the categorizer feature of Apache OpenNLP.
   */
  private static String detectCategory(String[] finalTokens, DoccatModel model) {

    // Initialize document categorizer tool
    DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);

    // Get best possible category.
    double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens);
    String category = getBestCategory(probabilitiesOfOutcomes);
    System.out.println(myCategorizer.getAllResults(probabilitiesOfOutcomes));
    System.out.println("Category: " + category);

    return category;
  }


  private static String getBestCategory(double[] probabilitiesOfOutcomes) {
    String[] categories = {"Cancel", "Confirm", "Other", "Reschedule"};
    int bestIndex = 0;
    if ((probabilitiesOfOutcomes[0] == probabilitiesOfOutcomes[1]) && (probabilitiesOfOutcomes[2]
        == probabilitiesOfOutcomes[0])) {
      return "Other";
    }
    for (int i = 1; i < 4; i++) {
      if (probabilitiesOfOutcomes[i] > probabilitiesOfOutcomes[bestIndex]) {
        bestIndex = i;
      }
    }
    return categories[bestIndex];
  }

  //TODO:decide what to do for this one
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
      //drops response after
      int index = sentence.indexOf("-----------");
      sentence = sentence.substring(0, index);
      //Makes text into one line
      sentence = sentence.replace("\r", "").replace("\n", "");
      //Add space around dash and replace cannot with can't
      sentence = sentence.replace("-", " - ").replace("cannot", "ca n't");

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
      System.out
          .println("POS Tags : " + Arrays.stream(posTokens).collect(Collectors.joining(" | ")));

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
      System.out
          .println("Lemmatizer : " + Arrays.stream(lemmaTokens).collect(Collectors.joining(" | ")));

      return lemmaTokens;
    }
  }
}