package edu.wpi.N.database;

import edu.wpi.N.entities.DbNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CSVParserTest {

  @BeforeAll
  public static void initializeTest() throws SQLException, ClassNotFoundException, DBException {
    MapDB.initTestDB();
  }

  /** Tests that ParseCSV imports Nodes and Edges from csv files into Database successfully */
  @Test
  public void testParseCSVNodes() throws DBException {
    InputStream inputNodes = getClass().getResourceAsStream("../csv/TestNodes.csv");
    CSVParser.parseCSV(inputNodes);

    // Will check if first, middle and last were added to DB
    DbNode firstExpected =
        new DbNode("AAAAAAAAAA", 171, 851, 1, "MainBuil", "REST", "Arnold", "AA", 'E');
    DbNode middleExpected =
        new DbNode("H500000000", 316, 1132, 1, "MainBuil", "HALL", "HALOL", "LL", 'N');
    DbNode lastExpected =
        new DbNode("H130000000", 1341, 1114, 1, "MainBuil", "LABS", "HALOL", "TT", 'V');

    /*new DbNode("AAAAAAAAAA", 171, 851, 1, "MainBuil", "OFFI", "Arnold", "AA", 'E');
    DbNode middleExpected =
        new DbNode("H500000000", 316, 1132, 1, "MainBuil", "HALL", "HALOL", "LL", 'N');
    DbNode lastExpected =
        new DbNode("H130000000", 1341, 1114, 1, "MainBuil", "HALL", "HALOL", "TT", 'V');*/

    // Compare with first
    Assertions.assertEquals(firstExpected, MapDB.getNode("AAAAAAAAAA"));

    // Compare center
    Assertions.assertEquals(middleExpected, MapDB.getNode("H500000000"));
    // Compare last
    Assertions.assertEquals(lastExpected, MapDB.getNode("H130000000"));
    MapDB.clearNodes();
  }

  /**
   * Tests that parceCSVfromPath parces successfully data from Node CSV to Database given full path
   * to the file
   */
  @Test
  public void testParseCSVfromPathNodes() throws FileNotFoundException, DBException {
    File fNodes = new File("src/test/resources/edu/wpi/N/csv/TestNodes.csv");
    String path = fNodes.getAbsolutePath();
    CSVParser.parseCSVfromPath(path);

    // Will check if first, middle and last were added to DB
    DbNode firstExpected =
        new DbNode("AAAAAAAAAA", 171, 851, 1, "MainBuil", "REST", "Arnold", "AA", 'E');
    DbNode middleExpected =
        new DbNode("H500000000", 316, 1132, 1, "MainBuil", "HALL", "HALOL", "LL", 'N');
    DbNode lastExpected =
        new DbNode("H130000000", 1341, 1114, 1, "MainBuil", "LABS", "HALOL", "TT", 'V');

    /*new DbNode("AAAAAAAAAA", 171, 851, 1, "MainBuil", "OFFI", "Arnold", "AA", 'E');
    >>>>>>> dev:src/test/java/edu/wpi/N/database/CSVParserTest.java
        DbNode middleExpected =
            new DbNode("H500000000", 316, 1132, 1, "MainBuil", "HALL", "HALOL", "LL", 'N');
        DbNode lastExpected =
            new DbNode("H130000000", 1341, 1114, 1, "MainBuil", "HALL", "HALOL", "TT", 'V');*/

    // Compare with first
    Assertions.assertEquals(firstExpected, MapDB.getNode("AAAAAAAAAA"));
    // Compare center
    Assertions.assertEquals(middleExpected, MapDB.getNode("H500000000"));
    // Compare last
    Assertions.assertEquals(lastExpected, MapDB.getNode("H130000000"));
    MapDB.clearNodes();
  }

  /**
   * Tests that parceCSVfromPath parces successfully data from Edges CSV to Database given full path
   * to the file
   */
  @Test
  public void testParseCSVfromPathNodesAndEdges() throws FileNotFoundException, DBException {
    // Must parse Node CSV first, otherwise edges will not be created
    File fNodes = new File("src/test/resources/edu/wpi/N/csv/TestNodes.csv");
    File fEdges = new File("src/test/resources/edu/wpi/N/csv/TestEdges.csv");
    String pathToNodes = fNodes.getAbsolutePath();
    String pathToEdges = fEdges.getAbsolutePath();
    CSVParser.parseCSVfromPath(pathToNodes);
    CSVParser.parseCSVfromPath(pathToEdges);

    LinkedList<DbNode> expectedH9 = new LinkedList<DbNode>();
    expectedH9.add(new DbNode("CCCCCCCCCC", 776, 523, 1, "MainBuil", "LABS", "Candie", "CC", 'G'));
    expectedH9.add(new DbNode("H800000000", 596, 794, 1, "MainBuil", "HALL", "HALOL", "OO", 'Q'));
    expectedH9.add(new DbNode("H100000001", 999, 816, 1, "MainBuil", "HALL", "HALOL", "QQ", 'S'));
    expectedH9.add(new DbNode("H120000000", 1214, 715, 1, "MainBuil", "HALL", "HALOL", "SS", 'U'));

    LinkedList<DbNode> actualEdges = MapDB.getAdjacent("H900000000");

    Assertions.assertTrue(expectedH9.contains(actualEdges.get(0)));
    Assertions.assertTrue(expectedH9.contains(actualEdges.get(1)));
    Assertions.assertTrue(expectedH9.contains(actualEdges.get(2)));
    Assertions.assertTrue(expectedH9.contains(actualEdges.get(3)));

    MapDB.clearNodes();
  }

  /**
   * Tests that parceCSVfromPath throws file not found exception if incorrect path to file gets
   * inputted
   */
  @Test
  public void testParseCSVFileNotFound() throws DBException {
    File f = new File("src/test/resources/edu/wpi/N/csv/MapCoor.csv");
    String path = f.getAbsolutePath();

    Assertions.assertThrows(Exception.class, () -> CSVParser.parseCSVfromPath(path));
    MapDB.clearNodes();
  }

  /** Tests that parceCSVfromPath successfully parses Prototype Node file */
  @Test
  public void testParseCSVPrototypeNode() throws FileNotFoundException, DBException {
    File f = new File("src/test/resources/edu/wpi/N/csv/PrototypeNodes.csv");
    String path = f.getAbsolutePath();

    CSVParser.parseCSVfromPath(path);

    DbNode firstExpected =
        new DbNode(
            "BCONF00102",
            2150,
            1025,
            4,
            "45 Francis",
            "CONF",
            "Duncan Reid Conference Room",
            "Conf B0102",
            'Z');

    // Compare with first
    Assertions.assertEquals(firstExpected, MapDB.getNode("BCONF00102"));
    MapDB.clearNodes();
  }

  @AfterAll
  public static void clearDb() throws DBException {
    MapDB.clearNodes();
  }
}
