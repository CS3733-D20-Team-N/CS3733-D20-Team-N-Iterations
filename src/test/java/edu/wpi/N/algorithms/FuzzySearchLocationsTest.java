package edu.wpi.N.algorithms;

import edu.wpi.N.database.CSVParser;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.DbController;
import edu.wpi.N.entities.DbNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FuzzySearchLocationsTest {

  @BeforeAll
  public static void init()
      throws SQLException, ClassNotFoundException, FileNotFoundException, DBException {
    DbController.initDB();
    File fNodes = new File("src/test/resources/edu/wpi/N/csv/PrototypeNodes.csv");
    String path = fNodes.getAbsolutePath();
    CSVParser.parseCSVfromPath(path);
    fNodes = null;
    path = null;
  }

  /**
   * Tests that string with a len of 1
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithCorrecitonInputIsOneLetter() throws DBException {
    String userInput = "c";

    LinkedList<DbNode> expected = null;

    Assertions.assertEquals(
        expected, FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput));
    Assertions.assertEquals(expected, FuzzySearchAlgorithm.suggestLocationsWithCorrection(""));
    Assertions.assertEquals(expected, FuzzySearchAlgorithm.suggestLocationsWithCorrection(" "));
  }

  /**
   * Tests that the function outputs proper DbNode corresponding to user's input "Je"
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithCorrectionInputIsTwoLetters() throws DBException {
    String userInput = "Je";
    // expected output: Psych/Addiction Care, Psychiatric Inpatient Care,
    LinkedList<DbNode> expected = new LinkedList<DbNode>();
    expected.add(DbController.getNode("BDEPT00402"));

    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);

    Assertions.assertTrue(actual.contains(expected.get(0)));
  }

  /**
   * Tests that functions returns list of nodes containing Center in their LongName
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithCorrectionInputIsSixLetters() throws DBException {
    String userInput = "Center";
    LinkedList<DbNode> expected = new LinkedList<DbNode>();
    expected.add(DbController.getNode("BDEPT00402"));
    expected.add(DbController.getNode("BDEPT00302"));
    expected.add(DbController.getNode("BDEPT00902"));
    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);
    Assertions.assertTrue(actual.size() == 3);
    Assertions.assertTrue(actual.contains(expected.get(0)));
    Assertions.assertTrue(actual.contains(expected.get(1)));
    Assertions.assertTrue(actual.contains(expected.get(2)));
  }

  /**
   * Tests that function outputs list of nodes Containing Center in their longName given incorrectly
   * spelled input
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithCorrectionInputSixLettersWrongSpelling() throws DBException {
    String userInput = "Centar";

    LinkedList<DbNode> expected = new LinkedList<DbNode>();
    expected.add(DbController.getNode("BDEPT00402"));
    expected.add(DbController.getNode("BDEPT00302"));
    expected.add(DbController.getNode("BDEPT00902"));

    long startTime = System.nanoTime();

    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);

    long endTime = System.nanoTime();

    long timeElapsed = endTime - startTime;
    System.out.println("Elapsed time for FuzzySearch in milliseconds:" + timeElapsed / 1000000);

    // LinkedList<String> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);

    Assertions.assertTrue(actual.size() == 3);
    Assertions.assertTrue(actual.contains(expected.get(0)));
    Assertions.assertTrue(actual.contains(expected.get(1)));
    Assertions.assertTrue(actual.contains(expected.get(2)));
  }

  /**
   * Tests that function returns correct DbNode given 3 words as input, One of the words was spelled
   * incorrectly
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithTwoWordIncorrectInput() throws DBException {
    String userInput = "Noose and ear";

    LinkedList<DbNode> expected = new LinkedList<DbNode>();
    expected.add(DbController.getNode("BDEPT00502"));

    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);
    Assertions.assertTrue(actual.contains(expected.get(0)));
    Assertions.assertTrue(actual.size() == 1);
  }

  /**
   * Tests that function returns correct DbNode given two words as input Correctly spelled
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithTwoWordsCorrectInput() throws DBException {
    String userInput = "Ear Nose";

    LinkedList<DbNode> expected = new LinkedList<DbNode>();
    expected.add(DbController.getNode("BDEPT00502"));
    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);
    Assertions.assertTrue(actual.contains(expected.get(0)));
    Assertions.assertTrue(actual.size() == 1);
  }

  /**
   * Tests that function return empy list given dummy input
   *
   * @throws DBException
   */
  @Test
  public void testSearchWithNonExistentIncorrectInput() throws DBException {
    String userInput = "Alaskan Airlines";

    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);
    Assertions.assertTrue(actual.isEmpty());
  }

  /**
   * Tests that function returns correct DbNode given only part of the word as user input
   *
   * @throws DBException
   */
  @Test
  public void testSearchCommonInputCorrectSpellingShortWordForm() throws DBException {
    String userInput = "Info";

    LinkedList<DbNode> expected = new LinkedList<DbNode>();
    expected.add(DbController.getNode("BINFO00102"));

    LinkedList<DbNode> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);

    Assertions.assertTrue(actual.contains(expected.get(0)));
  }

  //  /**
  //   * Tests performance time given .csv of All Nodes Make sure to uncomment the test if need to
  // measure performance
  //   *
  //   * @throws DBException
  //   * @throws FileNotFoundException
  //   */
  //  @Test
  //  public void testSearchMeasureTimeOnAllNode() throws DBException, FileNotFoundException {
  //    DbController.clearNodes();
  //
  //    File fNodes = new File("src/test/resources/edu/wpi/N/csv/MapNAllnodes.csv");
  //    String path = fNodes.getAbsolutePath();
  //    CSVParser.parseCSVfromPath(path);
  //
  //    String userInput = "restrom";
  //
  //    long startTime = System.nanoTime();
  //
  //    LinkedList<String> actual = FuzzySearchAlgorithm.suggestLocationsWithCorrection(userInput);
  //
  //    long endTime = System.nanoTime();
  //
  //    System.out.println("Size of DB:" + DbController.allNodes().size());
  //
  //    long timeElapsed = endTime - startTime;
  //    System.out.println("Elapsed time for FuzzySearch in milliseconds:" + timeElapsed / 1000000);
  //  }

  @AfterAll
  public static void clearDB() throws DBException {
    DbController.clearNodes();
  }
}
