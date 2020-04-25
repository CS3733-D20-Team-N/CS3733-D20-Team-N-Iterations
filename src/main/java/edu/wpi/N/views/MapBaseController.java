package edu.wpi.N.views;

import com.google.common.collect.HashBiMap;
import edu.wpi.N.App;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.MapDB;
import edu.wpi.N.database.ServiceDB;
import edu.wpi.N.entities.DbNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import edu.wpi.N.entities.UIEdge;
import edu.wpi.N.entities.UINode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class MapBaseController {

  // Node information
  int currentFloor;
  String currentBuilding = "Faulkner";

  LinkedList<DbNode> allFloorNodes; // stores all the nodes on the floor
  LinkedList<UINode> selectedNodes; // stores all the selected nodes on the map
  //LinkedList<String> longNamesList = new LinkedList<>(); // Stores Floor Node names

  public UINode defaultNode;

  private HashBiMap<UINode, DbNode> masterNodes; // Maps UINodes from pool to DbNodes from current floor
  private Set<UIEdge> masterEdges; // Maps UINodes from pool to DbNodes from current floor

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

  public void initialize() throws DBException {

    changeFloor(4);

    selectedNodes = new LinkedList<UINode>();
    allFloorNodes = MapDB.visNodes(currentFloor, currentBuilding);
    masterNodes = HashBiMap.create();

    populateMap();
    defaultNode = masterNodes.inverse().get(MapDB.getNode("NHALL00804"));

    try {
      if (defaultNode == null) {
        defaultNode = masterNodes.inverse().get(allFloorNodes.getFirst());
      }
    } catch (NoSuchElementException e) {
      Alert emptyMap = new Alert(Alert.AlertType.WARNING);
      emptyMap.setContentText("The map is empty!");
      emptyMap.show();
    }

  }

  public void changeFloor (int newFloor) {
    // Change floor
    currentFloor = newFloor;
    img_map.setImage(App.mapData.getMap(newFloor));
  }

  private UINode makeUINode() {
    UINode newNode = new UINode(false);
    newNode.placeOnPane(pn_routeNodes);
    newNode.setBaseMap(this);
    return newNode;
  }

  private void addEdge (UINode nodeA, UINode nodeB) {
    UIEdge newEdge = nodeA.addEdgeTo(nodeB);
    if (newEdge != null) { masterEdges.add(newEdge); }
  }

  private void breakEdge (UINode nodeA, UINode nodeB) {
    nodeA.breakEdgeTo(nodeB);
  }

  public DbNode getDbFromUi (UINode uiNode) {
    return masterNodes.get(uiNode);
  }

  public UINode getUiFromDb (DbNode dbNode) {
    return masterNodes.inverse().get(dbNode);
  }

  public UINode getDefaultNode () {
    return defaultNode;
  }

  // Replace (or create) link between a UINode and a DbNode
  private void setLink(UINode uiNode, DbNode dbNode, boolean showKey){
    if (uiNode != null && dbNode != null) {
      uiNode.setVisible(showKey);
      if (masterNodes.replace(uiNode, dbNode) == null) { // If uiNode is a new map key

        masterNodes.put(uiNode, dbNode);  // Add the new key

        try {  // Add any attached edges
          UINode otherAsUI;
          for (DbNode other : MapDB.getAdjacent(dbNode.getNodeID())) {
            otherAsUI = getUiFromDb(other);
            if (otherAsUI != null) { addEdge(uiNode, otherAsUI); }
          }
        } catch (DBException e) {

        }

      }
    }
  }

  // Called by the UINode that was clicked
  public void onUINodeClicked (MouseEvent e, UINode clickedNode) {

    if (clickedNode.getSelected()) {
      selectedNodes.add(clickedNode);
      System.out.println("Selected node " + masterNodes.get(clickedNode).getLongName() + ".");
    } else {
      selectedNodes.remove(clickedNode);
      System.out.println("Deselected node " + masterNodes.get(clickedNode).getLongName() + ".");
    }
  }

  // Called by the UINode that was clicked
  public void onUIEdgeClicked (MouseEvent e, UIEdge clickedEdge) {
    System.out.println("Edge (?) clicked!");
  }

  public void populateMap() {
    try {
      Set<UINode> keys = masterNodes.keySet();
      Stream<UINode> keyStream = keys.stream();
      LinkedList<DbNode> thisFloor = MapDB.floorNodes(currentFloor, currentBuilding);

      try {
        keyStream.forEach(key -> setLink(key, thisFloor.pop(), false));
      } catch (NoSuchElementException e) {
        // If there are more keys than nodes, hide extras from searcher functions
        System.out.println("More keys than DbNodes, as expected");
      }

      while (thisFloor.poll() != null) {
        // If there are DbNodes without keys, add keys
        setLink(makeUINode(), thisFloor.pop(), false);
      }


    } catch (DBException e) {
      System.out.print("Populating floor " + currentFloor + " in  " + currentBuilding + " failed.");
      e.printStackTrace();
    }

  }

  // Draw lines between each pair of nodes in given path
  public void drawPath(LinkedList<DbNode> pathNodes) {
    UINode uiFirst, uiSecond;
    DbNode dbFirst, dbSecond;
    UIEdge newEdge;

    for (int i = 0; i < pathNodes.size() - 1; i++) {
      dbFirst = pathNodes.get(i);
      dbSecond = pathNodes.get(i + 1);

      try {
        MapDB.getAdjacent(dbFirst.getNodeID());
      } catch (DBException e) {
        e.printStackTrace();
        return;
      }

      //float startX = (dbFirst.getX() * HORIZONTAL_SCALE) + HORIZONTAL_OFFSET;
      //float startY = (dbFirst.getY() * VERTICAL_SCALE) + VERTICAL_OFFSET;
      //float endX = (dbSecond.getX() * HORIZONTAL_SCALE) + HORIZONTAL_OFFSET;
      //float endY = (dbSecond.getY() * VERTICAL_SCALE) + VERTICAL_OFFSET;

      uiFirst = masterNodes.inverse().get(dbFirst);
      uiSecond = masterNodes.inverse().get(dbSecond);

      //Highlight edge
    }
  }

  // Deselect nodes and remove lines
  public void deselectAll() {
    for (UINode uiNode : masterNodes.keySet()) {
      uiNode.setSelected(false);
    }
    selectedNodes.clear();
  }

  public void forceSelect (UINode uiNode, boolean selected) {  // Don't really want
    uiNode.setSelected(selected);
  }


  // == MAP ZOOM CONTROLS ==

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
