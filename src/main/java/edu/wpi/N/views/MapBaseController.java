package edu.wpi.N.views;

import com.google.common.collect.HashBiMap;
import edu.wpi.N.App;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.MapDB;
import edu.wpi.N.entities.DbNode;
import edu.wpi.N.entities.UIDispEdge;
import edu.wpi.N.entities.UIDispNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class MapBaseController {

  private final int DEFAULT_FLOOR = 1;
  private final String DEFAULT_BUILDING = "Faulkner";
  int currentFloor;
  String currentBuilding;

  LinkedList<DbNode> allFloorNodes = new LinkedList<DbNode>(); // stores all the nodes on the floor
  LinkedList<UIDispNode> selectedNodes =
      new LinkedList<UIDispNode>(); // stores all the selected nodes on the map
  LinkedList<UIDispEdge> selectedEdges =
      new LinkedList<UIDispEdge>(); // stores all the selected edges on the map
  // LinkedList<String> longNamesList = new LinkedList<>(); // Stores Floor Node names

  public UIDispNode defaultNode;

  private HashBiMap<UIDispNode, DbNode>
      masterNodes; // Maps UINodes from pool to DbNodes from current floor
  private LinkedList<UIDispEdge> masterEdges =
      new LinkedList<>(); // Maps UINodes from pool to DbNodes from current floor

  // Screen constants
  private final float BAR_WIDTH = 300;
  private final float IMAGE_WIDTH = 2475;
  private final float IMAGE_HEIGHT = 1485;
  private final float SCREEN_WIDTH = 1920;
  private final float SCREEN_HEIGHT = 1080;
  private final float MAP_WIDTH = SCREEN_WIDTH - BAR_WIDTH;
  private final float MAP_HEIGHT = (MAP_WIDTH / IMAGE_WIDTH) * IMAGE_HEIGHT;
  private final float HORIZONTAL_OFFSET = 10;
  private final float VERTICAL_OFFSET = 8;
  private final float HORIZONTAL_SCALE = (MAP_WIDTH) / IMAGE_WIDTH;
  private final float VERTICAL_SCALE = (MAP_HEIGHT) / IMAGE_HEIGHT;

  // Map UI structure elements
  @FXML Pane pn_path, pn_routeNodes;
  @FXML StackPane pn_movableMap;
  @FXML AnchorPane pn_mapFrame;
  @FXML ImageView img_map;

  // Zoom/pan UI
  @FXML Button btn_zoomIn, btn_zoomOut;

  // Zoom/pan vars
  private double mapScaleAlpha; // Zoom ratio (0 = min, 1 = max)
  private double clickStartX, clickStartY; // Begin location of drag
  private boolean isStatic = false; // Zoom/pan controls disabled if true

  // Zoom/pan constants
  private final double MIN_MAP_SCALE = 1;
  private final double MAX_MAP_SCALE = 3;
  private final double ZOOM_STEP_SCROLL = 0.01;
  private final double ZOOM_STEP_BUTTON = 0.1;

  public MapBaseController() throws DBException {}

  public enum Mode {
    NO_STATE,
    PATH_STATE;
  }

  Mode mode;

  public void initialize() throws DBException {

    // this.currentFloor = DEFAULT_FLOOR;
    // this.currentBuilding = DEFAULT_BUILDING;

    masterNodes = HashBiMap.create();
    setMode(Mode.NO_STATE);
    changeBuilding(DEFAULT_BUILDING);

    try {
      defaultNode = getUiFromDb(allFloorNodes.getFirst());
    } catch (NoSuchElementException e) {
      Alert emptyMap = new Alert(Alert.AlertType.WARNING);
      emptyMap.setContentText("The map is empty!");
      emptyMap.show();
    }
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public void changeBuilding(String newBuilding) {
    // Change building, and show its default floor
    currentBuilding = newBuilding;
    changeFloor(DEFAULT_FLOOR, null);
  }

  public void changeFloor(int floorToDraw, LinkedList<DbNode> pathToDraw) {
    // Change floor in current building
    currentFloor = floorToDraw;
    App.mapData.refreshAllNodes();
    img_map.setImage(App.mapData.getMap(currentBuilding, floorToDraw));

    clearPath(); // Erase lines
    populateMap(); // Set up/recycle UIDispNodes

    // If there's a path to draw, draw it - otherwise just show the default kiosk
    if (mode == Mode.NO_STATE || pathToDraw == null || pathToDraw.isEmpty()) {
      defaultKioskNode();
    } else if (mode == Mode.PATH_STATE) {
      drawPath(pathToDraw);
    }
  }

  public LinkedList<String> defaultKioskNode() {

    LinkedList<String> kiosks = new LinkedList<>();

    if (currentFloor == 1) {
      try {
        DbNode floor1Kiosk = MapDB.getNode("NSERV00301"); // Should be specified elsewhere
        defaultNode = getUiFromDb(floor1Kiosk);
        kiosks.add(floor1Kiosk.getLongName());
      } catch (DBException e) {
        System.out.println("Couldn't find node NSERV00301 as 1st floor kiosk");
        e.printStackTrace();
      }

    } else if (currentFloor == 3) {
      try {
        DbNode floor3Kiosk = MapDB.getNode("NSERV00103"); // Should be specified elsewhere
        defaultNode = getUiFromDb(floor3Kiosk);
        kiosks.add(floor3Kiosk.getLongName());
      } catch (DBException e) {
        System.out.println("Couldn't find node NSERV00103 as 3rd floor kiosk");
        e.printStackTrace();
      }
    }
    return kiosks;
  }

  private UIDispNode makeUINode() {
    UIDispNode newNode = new UIDispNode(true);
    newNode.placeOnPane(pn_routeNodes);
    newNode.setBaseMap(this);
    return newNode;
  }

  private void addEdge(UIDispNode nodeA, UIDispNode nodeB) {
    UIDispEdge newEdge = nodeA.addEdgeTo(nodeB);
    if (newEdge != null) {
      masterEdges.add(newEdge);
      newEdge.placeOnPane(pn_path);
      newEdge.setBaseMap(this);
    }
  }

  private void breakEdge(UIDispNode nodeA, UIDispNode nodeB) {
    nodeA.breakEdgeTo(nodeB);
  }

  public DbNode getDbFromUi(UIDispNode uiNode) {
    return masterNodes.get(uiNode);
  }

  public UIDispNode getUiFromDb(DbNode dbNode) {
    return masterNodes.inverse().get(dbNode);
  }

  public UIDispNode getDefaultNode() {
    return defaultNode;
  }

  // Replace (or create) link between a UIDispNode and a DbNode
  private void setLink(UIDispNode uiNode, DbNode dbNode, boolean showKey) {
    if (uiNode != null && dbNode != null) {

      // if (dbNode.getNodeType().equals("HALL")) uiNode.setVisible(false);
      // else uiNode.setVisible(showKey);

      uiNode.setPos(
          dbNode.getX() * HORIZONTAL_SCALE + HORIZONTAL_OFFSET,
          dbNode.getY() * VERTICAL_SCALE + VERTICAL_OFFSET);
      if (masterNodes.replace(uiNode, dbNode) == null) { // If uiNode is a new map key
        masterNodes.put(uiNode, dbNode); // Add the new key
      }
      try { // Add any attached edges
        UIDispNode otherAsUI;
        for (DbNode other : MapDB.getAdjacent(dbNode.getNodeID())) {
          otherAsUI = getUiFromDb(other);
          if (otherAsUI != null) {
            addEdge(uiNode, otherAsUI);
          } else {
            // System.out.println("Couldn't find UI element for node " + other.getNodeID());
          }
        }
      } catch (DBException e) {
        System.out.println("No adjacent nodes to " + dbNode.getShortName());
      }
    }
  }

  // Called by the UIDispNode that was clicked
  public void onUINodeClicked(MouseEvent e, UIDispNode clickedNode) {

    if (clickedNode.getSelected()) {
      selectedNodes.add(clickedNode);
      // System.out.println("Selected node " + masterNodes.get(clickedNode).getLongName() + ".");
    } else {
      selectedNodes.remove(clickedNode);
      // System.out.println("Deselected node " + masterNodes.get(clickedNode).getLongName() + ".");
    }
  }

  // Called by the UIDispEdge that was clicked
  public void onUIEdgeClicked(MouseEvent e, UIDispEdge clickedEdge) {

    if (clickedEdge.getSelected()) {
      selectedEdges.add(clickedEdge);
      // System.out.println("Selected node " + masterNodes.get(clickedNode).getLongName() + ".");
    } else {
      selectedEdges.remove(clickedEdge);
      // System.out.println("Deselected node " + masterNodes.get(clickedNode).getLongName() + ".");
    }
  }

  public void populateMap() {
    try {
      Set<UIDispNode> keys = masterNodes.keySet();

      allFloorNodes = MapDB.floorNodes(currentFloor, currentBuilding); // Reference copy

      LinkedList<DbNode> thisFloor = new LinkedList<>();
      thisFloor.addAll(allFloorNodes); // Make a copy of allFloorNodes to use pop or pull on

      Iterator<UIDispNode> keysIterator = keys.iterator();
      Iterator<DbNode> dbIterator = thisFloor.iterator();
      while (dbIterator.hasNext()) {
        DbNode value = dbIterator.next();
        if (keysIterator.hasNext()) {
          UIDispNode key = keysIterator.next();
          setLink(key, value, true);
        } else {
          // If there are DbNodes without keys, add keys
          setLink(makeUINode(), value, true);
        }
      }

    } catch (DBException e) {
      System.out.print("Populating floor " + currentFloor + " in  " + currentBuilding + " failed.");
      e.printStackTrace();
    }
  }

  // Draw lines between each pair of nodes in given path
  public void drawPath(LinkedList<DbNode> pathNodes) {
    UIDispNode uiFirst, uiSecond;
    DbNode dbFirst, dbSecond;
    UIDispEdge edge;

    for (int i = 0; i < pathNodes.size() - 1; i++) {
      dbFirst = pathNodes.get(i);
      dbSecond = pathNodes.get(i + 1);

      uiFirst = getUiFromDb(dbFirst);
      uiSecond = getUiFromDb(dbSecond);

      if (i == 0) {
        uiFirst.setStart();
      } else if (i == pathNodes.size() - 2) {
        uiSecond.setEnd();
      }

      edge = uiFirst.edgeTo(uiSecond);
      if (edge != null) {
        edge.pointEdgeToward(uiSecond); // Set correct direction of line!
        edge.setHighlighted(true);
      } else {
        System.out.println(
            "Edge between "
                + dbFirst.getNodeID()
                + " and "
                + dbSecond.getNodeID()
                + " doesn't exist.");
      }
    }
  }

  // Resets highlighted edges to their default state
  public void clearPath() {
    for (UIDispEdge edge : masterEdges) {
      edge.setHighlighted(false);
    }
  }

  // Hide all edges
  public void hideEdges() {
    pn_path.setVisible(false);
  }

  /*
  // Hide all nodes
  public void hideNodes() {
    pn_routeNodes.setVisible(false);
  }
   */

  // Show all edges
  public void showEdges() {
    pn_path.setVisible(true);
  }

  // Show all nodes
  /*
  public void showNodes() {
    pn_routeNodes.setVisible(true);
  }
   */

  // Deselect nodes and remove lines
  public void deselectAll() {
    for (UIDispNode uiNode : masterNodes.keySet()) {
      uiNode.setSelected(false);
    }
    selectedNodes.clear();

    for (UIDispEdge edge : masterEdges) {
      edge.setSelected(false);
    }
  }

  public void forceSelect(UIDispNode uiNode, boolean selected) { // Don't really want
    uiNode.setSelected(selected);
  }

  //   == MAP ZOOM CONTROLS ==

  // Get zoom button input
  @FXML
  private void zoomToolHandler(MouseEvent event) throws IOException {

    if (event.getSource() == btn_zoomIn) {
      zoom(ZOOM_STEP_BUTTON);
    } else if (event.getSource() == btn_zoomOut) {
      zoom(-ZOOM_STEP_BUTTON);
    }
  }

  // When user scrolls mouse over map
  @FXML
  private void mapScrollHandler(ScrollEvent event) throws IOException {
    if (event.getSource() == pn_movableMap) {
      double deltaY = event.getDeltaY();
      zoom(deltaY * ZOOM_STEP_SCROLL);
    }
  }

  /**
   * zoom - Scale map pane up or down, clamping value between MIN_MAP_SCALE and MAX_MAP_SCALE
   *
   * @param percentDelta - Signed double representing how much to zoom in/out
   */
  private void zoom(double percentDelta) {

    // Scaling parameter (alpha) is clamped between 0 (min. scale) and 1 (max. scale)
    mapScaleAlpha = Math.max(0, Math.min(1, mapScaleAlpha + percentDelta));

    // Linearly interpolate (lerp) alpha to actual scale value
    double lerpedScale = MIN_MAP_SCALE + mapScaleAlpha * (MAX_MAP_SCALE - MIN_MAP_SCALE);

    // Apply new scale and correct panning
    pn_movableMap.setScaleX(lerpedScale);
    pn_movableMap.setScaleY(lerpedScale);
    clampPanning(0, 0);
  }

  // == MAP PANNING CONTROLS ==

  // User begins drag
  @FXML
  private void mapPressHandler(MouseEvent event) throws IOException {
    if (event.getSource() == pn_movableMap) {
      pn_movableMap.setCursor(Cursor.CLOSED_HAND);
      clickStartX = event.getSceneX();
      clickStartY = event.getSceneY();
    }
  }

  // User is currently dragging
  @FXML
  private void mapDragHandler(MouseEvent event) throws IOException {
    if (event.getSource() == pn_movableMap) {

      double dragDeltaX = event.getSceneX() - clickStartX;
      double dragDeltaY = event.getSceneY() - clickStartY;

      clampPanning(dragDeltaX, dragDeltaY);

      clickStartX = event.getSceneX();
      clickStartY = event.getSceneY();
    }
  }

  // User ends drag
  @FXML
  private void mapReleaseHandler(MouseEvent event) throws IOException {
    pn_movableMap.setCursor(Cursor.OPEN_HAND);
  }

  /**
   * clampPanning - Attempts to move map by deltaX and deltaY, clamping movement to stay in-bounds
   *
   * @param deltaX - How many screen pixels to move the map horizontally
   * @param deltaY - How many screen pixels to move the map vertically
   */
  private void clampPanning(double deltaX, double deltaY) {
    double xLimit = (pn_movableMap.getScaleX() - MIN_MAP_SCALE) * MAP_WIDTH / 2;
    double yLimit = (pn_movableMap.getScaleY() - MIN_MAP_SCALE) * MAP_HEIGHT / 2;

    double newTranslateX =
        Math.min(Math.max(pn_movableMap.getTranslateX() + deltaX, -xLimit), xLimit);
    double newTranslateY =
        Math.min(Math.max(pn_movableMap.getTranslateY() + deltaY, -yLimit), yLimit);

    pn_movableMap.setTranslateX(newTranslateX);
    pn_movableMap.setTranslateY(newTranslateY);
  }
}
