/*
package edu.wpi.N.views.outdated;

import edu.wpi.N.App;
import edu.wpi.N.algorithms.FuzzySearchAlgorithm;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.MapDB;
import edu.wpi.N.database.ServiceDB;
import edu.wpi.N.entities.DbNode;
import edu.wpi.N.entities.Path;
import edu.wpi.N.entities.States.StateSingleton;
import edu.wpi.N.entities.employees.Doctor;
import edu.wpi.N.qrcontrol.QRGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import edu.wpi.N.views.Controller;
import edu.wpi.N.views.mapDisplay.MapBaseController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MapDisplayControllerOUTDATED extends QRGenerator implements Controller {

  private StateSingleton singleton;

  @Override
  public void setSingleton(StateSingleton singleton) {
    this.singleton = singleton;
  }

  private App mainApp;
  final float BAR_WIDTH = 300;
  final float IMAGE_WIDTH = 2475;
  final float IMAGE_HEIGHT = 1485;
  final float SCREEN_WIDTH = 1920;
  final float SCREEN_HEIGHT = 1080;
  final float MAP_WIDTH = SCREEN_WIDTH - BAR_WIDTH;
  final float MAP_HEIGHT = (MAP_WIDTH / IMAGE_WIDTH) * IMAGE_HEIGHT;
  final float HORIZONTAL_OFFSET = 10;
  final float VERTICAL_OFFSET = 8;
  final float HORIZONTAL_SCALE = (MAP_WIDTH) / IMAGE_WIDTH;
  final float VERTICAL_SCALE = (MAP_HEIGHT) / IMAGE_HEIGHT;
  int currentFloor = 4;

  @FXML Button btn_find;
  @FXML Button btn_reset;
  @FXML AnchorPane pn_mapFrame;
  @FXML ImageView img_map;
  @FXML ComboBox<String> cb_languages;
  @FXML Button btn_Login;

  // HashBiMap<Circle, DbNode> masterNodes; // stores the map nodes and their respective database
  // nodes

  // Sidebar search by location initializations
  @FXML TextField txtf_searchlocation;
  @FXML ListView lst_locationsorted;
  @FXML Button btn_findlocationpath;

  // Sidebar search by doctor initializations
  @FXML TextField txtf_doctorname;
  @FXML ListView lst_doctornames;
  @FXML Button btn_searchdoc;
  @FXML ListView lst_doctorlocations;
  @FXML Button btn_findpathdoc;

  @FXML TextField txtf_translatorLocation;
  @FXML TextField txtf_laundryLocation;
  @FXML TextArea txtf_laundryNotes;
  @FXML TextArea txtf_translatorNotes;
  @FXML TextField txtf_language;
  @FXML ListView lst_laundryLocation;
  @FXML ListView lst_translatorSearchBox;

  // QR directions
  @FXML ImageView img_qrDirections;
  @FXML Pane pn_directionsBox;

  // Embedded map viewer
  @FXML private MapBaseController mapBaseController;

  LinkedList<String> longNamesList = new LinkedList<>(); // Stores Floor Node names

  private ObservableList<String> fuzzySearchTextList =
      FXCollections.observableArrayList(); // List that fills TextViews
  private LinkedList<DbNode> fuzzySearchNodeList =
      new LinkedList<>(); // List to store output of fuzzy search functions
  private ObservableList<String> fuzzySearchDoctorList =
      FXCollections.observableArrayList(); // List that fills TextViews

  private LinkedList<DbNode> fuzzySearchNodeListLaundry = new LinkedList<>();
  private LinkedList<DbNode> fuzzySearchNodeListTranslator = new LinkedList<>();
  private LinkedList<DbNode> getFuzzySearchNodeList;
  private LinkedList<Doctor> searchedDoc = new LinkedList<>();
  private LinkedList<DbNode> doctorNodes = new LinkedList<>();

  public MapDisplayControllerOUTDATED() throws DBException {}

  public void setMainApp(App mainApp) {
    this.mainApp = mainApp;
  }

  public void initialize() throws DBException {
    LinkedList<String> languages = ServiceDB.getLanguages();
    ObservableList<String> obvList = FXCollections.observableList(languages);
    cb_languages.setItems(obvList);
  }

  @FXML
  private void onBtnFindClicked(MouseEvent event) throws DBException {

    mapBaseController.clearPath();

    if (mapBaseController.selectedNodes.size() != 2) {
      return;
    }
    DbNode firstNode = mapBaseController.getDbFromUi(mapBaseController.selectedNodes.get(0));
    DbNode secondNode = mapBaseController.getDbFromUi(mapBaseController.selectedNodes.get(1));
    if (MapDB.getAdjacent(firstNode.getNodeID()).size() == 0
        || MapDB.getAdjacent(secondNode.getNodeID()).size() == 0) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText("Invalid input");
      errorAlert.setContentText("No existing paths to this node");
      errorAlert.showAndWait();
      return;
    }

    if (MapDB.getAdjacent(firstNode.getNodeID()).size() == 0
        || MapDB.getAdjacent(secondNode.getNodeID()).size() == 0) {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText("Invalid input");
      errorAlert.setContentText("No existing paths to this node");
      errorAlert.showAndWait();
      return;
    }

    Path path = singleton.savedAlgo.findPath(firstNode, secondNode, false);

    if (path != null) {
      LinkedList<DbNode> pathNodes = path.getPath();
      mapBaseController.drawPath(pathNodes);
      GenerateQRDirections(path);
    } else {
      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
      errorAlert.setHeaderText("Invalid input");
      errorAlert.setContentText("No existing paths to this node");
      errorAlert.showAndWait();
      return;
    }

    //    ArrayList<String> directions = path.getDirections();
    //    for (String s : directions) {
    //      System.out.println(s);
    //    }
    //    System.out.println(" ");

    // mapBase.deselectAll();
  }

  @FXML
  private void onResetClicked(MouseEvent event) throws Exception {
    pn_directionsBox.setVisible(false);
    // mapBaseController.showNodes();
    mapBaseController.showEdges();
    mapBaseController.deselectAll();
  }

  public void onReturnClicked() throws IOException {
    mainApp.switchScene("views/home.fxml", singleton);
  }

//  // mike
//  // Upon changing text in the search by location UI component this method
//  // is triggered
//  @FXML
//  private void searchByLocationTextFill(KeyEvent inputMethodEvent) throws DBException {
//    String currentText = txtf_searchlocation.getText();
//    fuzzySearchNodeList = FuzzySearchAlgorithm.suggestLocations(currentText);
//    LinkedList<String> fuzzySearchStringList = new LinkedList<>();
//    if (fuzzySearchNodeList != null) {
//
//      for (DbNode node : fuzzySearchNodeList) {
//        fuzzySearchStringList.add(node.getLongName());
//      }
//
//      fuzzySearchTextList = FXCollections.observableList(fuzzySearchStringList);
//    } else fuzzySearchTextList = FXCollections.observableList(longNamesList);
//    lst_locationsorted.setItems(fuzzySearchTextList);
//  }
//
//  // Upon clicking find path to location button call this method
//  @FXML
//  private void onLocationPathFindClicked(MouseEvent event) throws Exception {
//    mapBaseController.hideEdges();
//    int currentSelection = lst_locationsorted.getSelectionModel().getSelectedIndex();
//    DbNode destinationNode = fuzzySearchNodeList.get(currentSelection);
//    if (mapBaseController.selectedNodes.size() < 1)
//      mapBaseController.forceSelect(mapBaseController.defaultNode, true);
//    mapBaseController.forceSelect(mapBaseController.getUiFromDb(destinationNode), true);
//    onBtnFindClicked(event);
//    mapBaseController.deselectAll();
//  }
//
//  @FXML
//  private void onNearestBathroomClicked(MouseEvent event) throws Exception {
//    mapBaseController.clearPath();
//    DbNode startNode = mapBaseController.getDbFromUi(mapBaseController.defaultNode);
//    if (mapBaseController.selectedNodes.size() > 0)
//      startNode = mapBaseController.getDbFromUi(mapBaseController.selectedNodes.getFirst());
//    onResetClicked(event);
//
//    Path pathToBathroom = singleton.savedAlgo.findQuickAccess(startNode, "REST");
//    if (pathToBathroom != null) {
//      LinkedList<DbNode> pathNodes = pathToBathroom.getPath();
//      mapBaseController.drawPath(pathNodes);
//      GenerateQRDirections(pathToBathroom);
//    }
//  }
//
//  @FXML
//  private void searchByDoctorTextFill(KeyEvent keyEvent) throws DBException {
//    String currentText = txtf_doctorname.getText();
//    if (currentText.length() > 1) {
//      searchedDoc = FuzzySearchAlgorithm.suggestDoctors(currentText);
//      LinkedList<String> fuzzySearchStringList = new LinkedList<>();
//      for (Doctor doctors : searchedDoc) {
//        fuzzySearchStringList.add(doctors.getName());
//      }
//      fuzzySearchDoctorList = FXCollections.observableList(fuzzySearchStringList);
//      lst_doctornames.setItems(fuzzySearchDoctorList);
//    }
//  }
//
//  @FXML
//  private void onFindDoctorClicked(MouseEvent event) throws Exception {
//    int currentSelection = lst_doctornames.getSelectionModel().getSelectedIndex();
//    System.out.println(currentSelection);
//    Doctor selectedDoc = searchedDoc.get(currentSelection);
//    System.out.println(selectedDoc);
//    doctorNodes = selectedDoc.getLoc();
//    LinkedList<String> docNames = new LinkedList<>();
//    for (DbNode nodes : doctorNodes) {
//      docNames.add(nodes.getLongName());
//    }
//    ObservableList<String> doctorsLocations = FXCollections.observableList(docNames);
//    lst_doctorlocations.setItems(doctorsLocations);
//  }
//
//  // Upon clicking find path to location button call this method
//  @FXML
//  private void onDoctorPathFindClicked(MouseEvent event) throws Exception {
//    mapBaseController.clearPath();
//    int currentSelection = lst_doctorlocations.getSelectionModel().getSelectedIndex();
//    DbNode destinationNode = doctorNodes.get(currentSelection);
//    if (mapBaseController.selectedNodes.size() < 1)
//      mapBaseController.selectedNodes.add(mapBaseController.defaultNode);
//    mapBaseController.forceSelect(mapBaseController.getUiFromDb(destinationNode), true);
//    // if (selectedNodes.size() < 2) selectedNodes.add(defaultNode);
//    onBtnFindClicked(event);
//    mapBaseController.deselectAll();
//  }
//
//  public void fuzzySearchLaundryRequest(KeyEvent keyInput) throws DBException {
//    String currentText = txtf_laundryLocation.getText();
//    fuzzySearchNodeListLaundry = FuzzySearchAlgorithm.suggestLocations(currentText);
//    LinkedList<String> fuzzySearchStringList = new LinkedList<>();
//    if (fuzzySearchNodeListLaundry != null) {
//
//      for (DbNode node : fuzzySearchNodeListLaundry) {
//        fuzzySearchStringList.add(node.getLongName());
//      }
//
//      fuzzySearchTextList = FXCollections.observableList(fuzzySearchStringList);
//    } else fuzzySearchTextList = FXCollections.observableList(longNamesList);
//    lst_laundryLocation.setItems(fuzzySearchTextList);
//  }
//
//  @FXML
//  public void fuzzySearchTranslatorRequest(KeyEvent keyInput) throws DBException {
//    String currentText = txtf_translatorLocation.getText();
//    fuzzySearchNodeListTranslator = FuzzySearchAlgorithm.suggestLocations(currentText);
//    LinkedList<String> fuzzySearchStringList = new LinkedList<>();
//    if (fuzzySearchNodeListTranslator != null) {
//
//      for (DbNode node : fuzzySearchNodeListTranslator) {
//        fuzzySearchStringList.add(node.getLongName());
//      }
//
//      fuzzySearchTextList = FXCollections.observableList(fuzzySearchStringList);
//    } else fuzzySearchTextList = FXCollections.observableList(longNamesList);
//    lst_translatorSearchBox.setItems(fuzzySearchTextList);
//  }
//
//  @FXML
//  public void popupWindow(MouseEvent e) throws IOException {
//    Stage stage = new Stage();
//    Parent root;
//    root = FXMLLoader.load(getClass().getResource("adminRequestScreen.fxml"));
//    Scene scene = new Scene(root);
//    stage.setScene(scene);
//    stage.initModality(Modality.APPLICATION_MODAL);
//    stage.show();
//  }
//
//  @FXML
//  public void createNewLaundry() throws DBException {
//    int currentSelection = lst_laundryLocation.getSelectionModel().getSelectedIndex();
//    String nodeID;
//    try {
//      nodeID = fuzzySearchNodeListLaundry.get(currentSelection).getNodeID();
//    } catch (IndexOutOfBoundsException e) {
//      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//      errorAlert.setContentText("Please select a location for your service request!");
//      errorAlert.show();
//      return;
//    }
//    String notes = txtf_laundryNotes.getText();
//    int laundryRequest = ServiceDB.addLaundReq(notes, nodeID);
//
//    txtf_laundryLocation.clear();
//    txtf_laundryNotes.clear();
//    lst_laundryLocation.getItems().clear();
//
//    Alert confAlert = new Alert(Alert.AlertType.CONFIRMATION);
//    confAlert.setContentText("Request Recieved");
//    confAlert.show();
//  }
//
//  private void GenerateQRDirections(Path path) {
//    try {
//      ArrayList<String> directions = path.getDirections();
//      img_qrDirections.setImage(generateImage(directions, false));
//      pn_directionsBox.setVisible(true);
//    } catch (DBException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @FXML
//  public void createNewTranslator() throws DBException {
//    int currentSelection = lst_translatorSearchBox.getSelectionModel().getSelectedIndex();
//
//    String nodeID;
//    try {
//      nodeID = fuzzySearchNodeListTranslator.get(currentSelection).getNodeID();
//    } catch (IndexOutOfBoundsException e) {
//      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//      errorAlert.setContentText("Please select a location for your service request!");
//      errorAlert.show();
//      return;
//    }
//    String notes = txtf_translatorNotes.getText();
//    String language = cb_languages.getSelectionModel().getSelectedItem();
//    if (language == null) {
//      Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//      errorAlert.setContentText("Please select a language for your translation request!");
//      errorAlert.show();
//      return;
//    }
//    int transReq = ServiceDB.addTransReq(notes, nodeID, language);
//
//    txtf_translatorLocation.clear();
//    txtf_translatorNotes.clear();
//    cb_languages.cancelEdit();
//    lst_translatorSearchBox.getItems().clear();
//
//    Alert confAlert = new Alert(Alert.AlertType.CONFIRMATION);
//    confAlert.setContentText("Request Received");
//    confAlert.show();
//  }
//}
*/
