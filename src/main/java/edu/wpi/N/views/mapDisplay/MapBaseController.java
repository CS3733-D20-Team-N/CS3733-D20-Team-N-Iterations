package edu.wpi.N.views.mapDisplay;

import edu.wpi.N.App;
import edu.wpi.N.database.DBException;
import edu.wpi.N.entities.DbNode;
import edu.wpi.N.entities.Path;
import edu.wpi.N.entities.States.StateSingleton;
import edu.wpi.N.views.Controller;
import java.io.IOException;
import java.util.ArrayList;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class MapBaseController implements Controller {

  // ApplicationStates
  private App mainApp;
  private StateSingleton singleton;

  // Screen Constants
  private final double BAR_WIDTH = 300;
  private final float IMAGE_WIDTH = 2475;
  private final double IMAGE_HEIGHT = 1485;
  private final double SCREEN_WIDTH = 1920;
  private final double SCREEN_HEIGHT = 1080;
  private final double MAP_WIDTH = SCREEN_WIDTH - BAR_WIDTH;
  private final double MAP_HEIGHT = (MAP_WIDTH / IMAGE_WIDTH) * IMAGE_HEIGHT;
  private final double HORIZONTAL_OFFSET = 10;
  private final double VERTICAL_OFFSET = 5;
  private final double HORIZONTAL_SCALE = (MAP_WIDTH) / IMAGE_WIDTH;
  private final double VERTICAL_SCALE = (MAP_HEIGHT) / IMAGE_HEIGHT;

  // Zoom constants
  private final double MIN_MAP_SCALE = 1;
  private final double MAX_MAP_SCALE = 3;
  private final double ZOOM_STEP_SCROLL = 0.01;
  private final double ZOOM_STEP_BUTTON = 0.1;
  private double mapScaleAlpha;
  private double clickStartX, clickStartY;

  private final String FIRST_KIOSK = "NSERV00301";
  private final String THIRD_KIOSK = "NSERV00103";
  private final Color START_NODE_COLOR = Color.GREEN;
  private final Color END_NODE_COLOR = Color.RED;

  // Path GUI constants
  private final Color DEFAULT_PATH_COLOR = Color.DODGERBLUE; // Default color of path
  private final double DEFAULT_PATH_WIDTH = 4; // Default stroke width of path
  private final double LIGHT_OPACITY = 0.05; // Faded line
  private final double HEAVY_OPACITY = 0.8; // Bold line (default)
  private final ArrayList<Double> DASH_PATTERN =
      new ArrayList<>() {
        {
          add(20d);
          add(10d);
        }
      }; // Line-gap pattern
  private final double MAX_DASH_OFFSET =
      DASH_PATTERN.stream().reduce(0d, (a, b) -> (a + b)); // Pattern length
  private final int PATH_ANIM_LENGTH =
      2000; // How many milliseconds it takes for dashes to move up a spot
  private Timeline pathAnimTimeline = new Timeline(); // Timeline object to set line animation
  private KeyFrame keyStart, keyEnd; // Keyframes in path animation
  private ArrayList<KeyValue> keyStartVals, keyEndVals;
  private Label startLabel, endLabel;
  private final int NODE_LABEL_PADDING = 35;

  // FXML Item IDs
  @FXML StackPane pn_movableMap;
  @FXML Pane pn_path;
  @FXML ImageView img_map;
  @FXML Button btn_zoomIn, btn_zoomOut;

  /**
   * the constructor of MapBaseController
   *
   * @param singleton the algorithm singleton which determines the style of path finding
   */
  public MapBaseController(StateSingleton singleton) {
    this.singleton = singleton;
  }

  /**
   * allows reference back the main application class of this program
   *
   * @param mainApp the main application class of this program
   */
  @Override
  public void setMainApp(App mainApp) {
    this.mainApp = mainApp;
  }

  /**
   * initializes the MapBase Controller
   *
   * @throws DBException
   */
  public void initialize() throws DBException {
    startLabel = new Label();
    startLabel.setTextAlignment(TextAlignment.CENTER);
    startLabel.setAlignment(Pos.CENTER);
    startLabel.setMouseTransparent(true);
    startLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
    startLabel.setBorder(
        new Border(
            new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT)));
    endLabel = new Label();
    endLabel.setTextAlignment(TextAlignment.CENTER);
    endLabel.setAlignment(Pos.CENTER);
    endLabel.setMouseTransparent(true);
    endLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
    endLabel.setBorder(
        new Border(
            new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT)));
    initPathAnim();
  }

  /**
   * sets the current building of the map display
   *
   * @param building the name of the building to be displayed
   */
  public void setBuilding(String building, int floor, Path currentPath) throws DBException {
    setFloor(building, floor, currentPath);
  }

  /**
   * loads the new floor image loads the floor image and the draws the path if necessary
   *
   * @param building the current building of the map display
   * @param floor the new floor of the map display
   * @param currentPath the current path finding nodes
   * @throws DBException
   */
  public void setFloor(String building, int floor, Path currentPath) throws DBException {
    clearPath();
    img_map.setImage(singleton.mapImageLoader.getMap(building, floor));
    if (!(currentPath == null || currentPath.isEmpty())) {
      drawPath(currentPath, floor);
    }
  }

  private void initPathAnim() {
    // Set timeline to repeat
    pathAnimTimeline.setCycleCount(Timeline.INDEFINITE);

    // Init keyval lists
    keyStartVals = new ArrayList<>();
    keyEndVals = new ArrayList<>();

    // Setup initial (empty) frames
    setAnimFrames();
  }

  /**
   * Draws lines between each location specified by currentPath
   *
   * @param currentPath Path object containing the DbNodes picked by pathfinder algorithm
   * @param floor The current floor
   */
  public void drawPath(Path currentPath, int floor) {
    DbNode firstNode, secondNode;
    startLabel.setText("Start: ");
    endLabel.setText("Destination: ");
    for (int i = 0; i < currentPath.size() - 1; i++) {
      firstNode = currentPath.get(i);
      secondNode = currentPath.get(i + 1);
      if (firstNode.getFloor() == floor && secondNode.getFloor() == floor) {
        if (i == 0) {
          drawCircle(firstNode, START_NODE_COLOR, startLabel);
        } else if (i == currentPath.size() - 2) {
          drawCircle(secondNode, END_NODE_COLOR, endLabel);
        }
        Line line =
            new Line(
                scaleX(secondNode.getX()) + HORIZONTAL_OFFSET,
                scaleY(secondNode.getY()) + VERTICAL_OFFSET,
                scaleX(firstNode.getX()) + HORIZONTAL_OFFSET,
                scaleY(firstNode.getY()) + VERTICAL_OFFSET);
        styleLine(line);
        pn_path.getChildren().add(line);
      }
    }
    pn_path.getChildren().addAll(startLabel, endLabel); // To make sure they render over the path
    setAnimFrames();
  }

  /**
   * Applies animation, color and other style elements to given path line
   *
   * @param line The JavaFX Line object to modify
   */
  public void styleLine(Line line) {

    // Apply basic line stroke effects
    line.setStroke(DEFAULT_PATH_COLOR);
    line.setOpacity(HEAVY_OPACITY);
    line.setStrokeWidth(DEFAULT_PATH_WIDTH);
    line.setStrokeLineCap(StrokeLineCap.ROUND);
    line.getStrokeDashArray().setAll(DASH_PATTERN);

    // Add keyvalues for this line to keyvalue lists
    // Frame 0: no offset
    keyStartVals.add(new KeyValue(line.strokeDashOffsetProperty(), 0, Interpolator.LINEAR));
    // Frame 1: Offset by length of line-gap pattern (so it's continuous)
    keyEndVals.add(
        new KeyValue(line.strokeDashOffsetProperty(), MAX_DASH_OFFSET, Interpolator.LINEAR));
  }

  /**
   * Generate the new keyframes, apply them to the timeline, and play the timeline if there's
   * anything to animate
   */
  private void setAnimFrames() {

    // Set up keyframes
    keyStart = new KeyFrame(Duration.ZERO, "keyStart", null, keyStartVals);
    keyEnd = new KeyFrame(Duration.millis(PATH_ANIM_LENGTH), "keyEnd", null, keyEndVals);

    // Apply new frames
    pathAnimTimeline.getKeyFrames().addAll(keyStart, keyEnd);

    if (!(keyStartVals.isEmpty() || keyEndVals.isEmpty())) {
      // Play anim
      pathAnimTimeline.play();
    } else {
      pathAnimTimeline.stop();
    }
  }

  /**
   * draws either the start or end circle on the map, as well as a text label above it
   *
   * @param node the DbNode to be displayed on the map
   * @param c the color of the circle
   */
  public void drawCircle(DbNode node, Color c, Label label) {
    Circle circle = new Circle();
    circle.setRadius(5);
    circle.setCenterX(scaleX(node.getX()) + HORIZONTAL_OFFSET);
    circle.setCenterY(scaleY(node.getY()) + VERTICAL_OFFSET);
    circle.setFill(c);
    pn_path.getChildren().add(circle);
    if (label != null) {
      pn_path.getChildren().add(label);
      label.setText(label.getText() + node.getLongName());
      label.applyCss(); // To make sure prefWidth doesn't return 0, for whatever reason
      label.relocate(
          scaleX(node.getX()) + HORIZONTAL_OFFSET - label.prefWidth(-1) / 2,
          scaleY(node.getY()) + VERTICAL_OFFSET - NODE_LABEL_PADDING);
      pn_path.getChildren().remove(label); // Gets added back after all lines are drawn
    }
  }

  public double scaleX(double x) {
    return x * HORIZONTAL_SCALE;
  }

  public double scaleY(double y) {
    return y * VERTICAL_SCALE;
  }

  public void clearPath() {
    pathAnimTimeline.stop();
    pathAnimTimeline.getKeyFrames().clear();
    keyStart = null;
    keyEnd = null;
    keyStartVals.clear();
    keyEndVals.clear();
    pn_path.getChildren().clear();
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
