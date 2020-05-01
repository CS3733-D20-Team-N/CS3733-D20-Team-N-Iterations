package edu.wpi.N.algorithms;

import edu.wpi.N.database.CSVParser;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.MapDB;
import edu.wpi.N.entities.DbNode;
import edu.wpi.N.entities.Path;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BFSTests {
  Algorithm myBFS = new Algorithm();

  public BFSTests() throws DBException {}

  @BeforeAll
  public static void initializeTest()
      throws SQLException, ClassNotFoundException, DBException, FileNotFoundException {
    MapDB.initTestDB();
    InputStream inputNodes = BFSTests.class.getResourceAsStream("../csv/TestNodes.csv");
    InputStream inputEdges = BFSTests.class.getResourceAsStream("../csv/TestEdges.csv");
    CSVParser.parseCSV(inputNodes);
    CSVParser.parseCSV(inputEdges);
  }

  /** Tests that findPath returns a Path object with the best route from H9 to EEE */
  @Test
  public void findPathNormalCaseBFSTest() throws DBException {
    myBFS.setPathFinder(new BFS());
    LinkedList<DbNode> actualPath = new LinkedList<DbNode>();
    actualPath.add(MapDB.getNode("H100000001"));
    actualPath.add(MapDB.getNode("H900000000"));
    actualPath.add(MapDB.getNode("H120000000"));
    actualPath.add(MapDB.getNode("H130000000"));
    actualPath.add(MapDB.getNode("EEEEEEEEEE"));

    Path testingPath =
        myBFS.findPath(MapDB.getNode("H100000001"), MapDB.getNode("EEEEEEEEEE"), false);

    Assertions.assertEquals(actualPath, testingPath.getPath());
  }

  /**
   * Tests that findPath method return a Path object with route consisting of 2 Nodes, since start
   * and end nodes are neighbors
   */
  @Test
  public void findPathStartIsNeighborWithEndNodeBFSTest() throws DBException {
    myBFS.setPathFinder(new BFS());
    LinkedList<DbNode> actualPath = new LinkedList<DbNode>();

    actualPath.add(MapDB.getNode("H120000000"));
    actualPath.add(MapDB.getNode("H130000000"));

    Path testingPath =
        myBFS.findPath(MapDB.getNode("H120000000"), MapDB.getNode("H130000000"), false);

    Assertions.assertEquals(actualPath, testingPath.getPath());
  }

  /**
   * Tests that findPath throws NullPointerException if the destination given is not connected to
   * any node
   */
  @Test
  public void findPathDestinationNotFoundBFSTest() throws DBException {
    myBFS.setPathFinder(new BFS());
    Assertions.assertNull(
        myBFS.findPath(MapDB.getNode("H120000000"), MapDB.getNode("NonExistentNode"), false));
  }

  /**
   * Tests that findPath method throws NullPointerException if start Node doesn't have a connection
   * to any node (including end node)
   */
  @Test
  public void findPathStartNodeHasNoEdgesBFSTest() throws DBException {
    myBFS.setPathFinder(new BFS());
    DbNode nonExistentNode = new DbNode();
    Assertions.assertNull(myBFS.findPath(nonExistentNode, MapDB.getNode("H120000000"), false));
  }

  /**
   * Tests that findPath method returns a Path object with only one node in its route since Start
   * Node = End Node
   */
  @Test
  public void findPathEndIsStartNodeBFSTest() throws DBException {
    myBFS.setPathFinder(new BFS());
    LinkedList<DbNode> actualPath = new LinkedList<DbNode>();

    actualPath.add(MapDB.getNode("H120000000"));
    Path testingPath =
        myBFS.findPath(MapDB.getNode("H120000000"), MapDB.getNode("H120000000"), false);

    Assertions.assertEquals(actualPath, testingPath.getPath());
  }

  @AfterAll
  public static void clear() throws DBException {
    MapDB.clearNodes();
  }
}
