package edu.wpi.N.views.mapDisplay;

import edu.wpi.N.AppClass;
import edu.wpi.N.MainClass;
import edu.wpi.N.entities.States.StateSingleton;
import edu.wpi.N.views.Controller;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

public class GoogleMapController implements Controller, Initializable {

  private StateSingleton singleton;
  private AppClass mainApp;
  private String pathToHTML;

  @FXML protected WebView webView;

  // String pathToHTML
  public GoogleMapController(String pathToHTML) {
    this.pathToHTML = pathToHTML;
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    webView.getEngine().load(MainClass.class.getResource(pathToHTML).toString());
  }

  public void setSingleton(StateSingleton singleton) {
    this.singleton = singleton;
  }

  @Override
  public void setMainApp(AppClass mainApp) {
    this.mainApp = mainApp;
  }
}
