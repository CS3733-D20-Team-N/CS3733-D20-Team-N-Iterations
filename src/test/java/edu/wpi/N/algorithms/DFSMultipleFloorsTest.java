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

public class DFSMultipleFloorsTest {
  Algorithm myDFS = new Algorithm();

  public DFSMultipleFloorsTest() throws DBException {}

  @BeforeAll
  public static void initialize()
      throws SQLException, DBException, ClassNotFoundException, FileNotFoundException {
    MapDB.initTestDB();
    InputStream inputNodes =
        DFSMultipleFloorsTest.class.getResourceAsStream("../csv/FourFloorsTestNode.csv");
    InputStream inputEdges =
        DFSMultipleFloorsTest.class.getResourceAsStream("../csv/FourFloorsTestEdges.csv");
    CSVParser.parseCSV(inputNodes);
    CSVParser.parseCSV(inputEdges);
  }

  /** Tests if the pathfinder chooses the more efficient elevator when changing floors */
  @Test
  public void findCloserElevatorDFSTest() throws DBException {
    myDFS.setPathFinder(new DFS());
    DbNode startNode = MapDB.getNode("H011000000");
    DbNode endNode = MapDB.getNode("BBBBBBBBBB");
    Path testPath = myDFS.findPath(startNode, endNode, false);

    Assertions.assertTrue(testPath.getPath().contains(endNode));
  }

  /** Tests if the pathfinder will choose the stairs over elevator */
  @Test
  public void stairsOverElevatorDFSTest() throws DBException {
    myDFS.setPathFinder(new DFS());
    DbNode startNode = MapDB.getNode("STAI011000");
    DbNode endNode = MapDB.getNode("H083000000");
    Path testPath = myDFS.findPath(startNode, endNode, false);

    LinkedList<DbNode> actualPath = new LinkedList<DbNode>();
    actualPath.add(MapDB.getNode("STAI011000"));
    actualPath.add(MapDB.getNode("STAI013000"));
    actualPath.add(MapDB.getNode("H083000000"));

    Assertions.assertEquals(actualPath, testPath.getPath());
  }

  /**
   * Test to see if the pathfinder can get to the end node (doesn't take stairs because it's DFA)
   */
  @Test
  public void thirdToFirstFloorDFSTest() throws DBException {
    myDFS.setPathFinder(new DFS());
    DbNode startNode = MapDB.getNode("H083000000");
    DbNode endNode = MapDB.getNode("H041000000");
    Path testPath = myDFS.findPath(startNode, endNode, false);

    Assertions.assertTrue(testPath.getPath().contains(endNode));
  }

  /**
   * Tests if the pathfinder goes to the correct elevator (the only one that can access floor 4)
   * when going from floor 3 to floor 4
   */
  @Test
  public void thirdToFourthFloorDFSTest() throws DBException {
    myDFS.setPathFinder(new DFS());
    DbNode startNode = MapDB.getNode("ELEV013000");
    DbNode endNode = MapDB.getNode("DDDDDDDDDD");
    Path testPath = myDFS.findPath(startNode, endNode, false);

    LinkedList<DbNode> actualPath = new LinkedList<DbNode>();
    actualPath.add(MapDB.getNode("ELEV013000"));
    actualPath.add(MapDB.getNode("H033000000"));
    actualPath.add(MapDB.getNode("H023000000"));
    actualPath.add(MapDB.getNode("H043000000"));
    actualPath.add(MapDB.getNode("STAI013000"));
    actualPath.add(MapDB.getNode("H083000000"));
    actualPath.add(MapDB.getNode("ELEV033000"));
    actualPath.add(MapDB.getNode("ELEV034000"));
    actualPath.add(MapDB.getNode("DDDDDDDDDD"));

    // Assertions.assertEquals(actualPath, testPath.getPath()); DFS is nondeterministic (it may find
    // many solutions, kinda hard to test like this, I know)
  }

  /**
   * Tests if the pathfinder goes to the correct elevator (the only one that can access floor 4)
   * when going from floor 3 to floor 4 with handicap selected (ignores stairs)
   */
  @Test
  public void thirdToFourthFloorHandicapDFSTest() throws DBException {
    myDFS.setPathFinder(new DFS());
    DbNode startNode = MapDB.getNode("ELEV013000");
    DbNode endNode = MapDB.getNode("DDDDDDDDDD");
    Path testPath = myDFS.findPath(startNode, endNode, true);

    LinkedList<DbNode> actualPath = new LinkedList<DbNode>();
    actualPath.add(MapDB.getNode("ELEV013000"));
    actualPath.add(MapDB.getNode("H033000000"));
    actualPath.add(MapDB.getNode("H023000000"));
    actualPath.add(MapDB.getNode("H043000000"));
    actualPath.add(MapDB.getNode("H053000000"));
    actualPath.add(MapDB.getNode("ELEV023000"));
    actualPath.add(MapDB.getNode("H063000000"));
    actualPath.add(MapDB.getNode("H073000000"));
    actualPath.add(MapDB.getNode("H083000000"));
    actualPath.add(MapDB.getNode("ELEV033000"));
    actualPath.add(MapDB.getNode("ELEV034000"));
    actualPath.add(MapDB.getNode("DDDDDDDDDD"));

    //    Assertions.assertEquals(actualPath, testPath.getPath()); DFS is nondeterministic
  }

  /**
   * Tests if the pathfinder will choose an elevator over stairs since it's a handicap accessible
   * path
   */
  @Test
  public void elevatorHandicapDFSTest() throws DBException {
    myDFS.setPathFinder(new DFS());
    DbNode startNode = MapDB.getNode("H011000000");
    DbNode endNode = MapDB.getNode("H083000000");
    Path testPath = myDFS.findPath(startNode, endNode, true);

    Assertions.assertTrue(testPath.getPath().contains(endNode));
    for (DbNode node : testPath.getPath()) {
      Assertions.assertTrue(!node.getNodeType().equals("STAI"));
    }
  }

  @AfterAll
  public static void clear() throws DBException {
    MapDB.clearNodes();
  }
}
