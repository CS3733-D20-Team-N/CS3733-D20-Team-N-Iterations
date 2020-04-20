package edu.wpi.N.views;

import edu.wpi.N.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminController implements Initializable {

  private App mainApp;

  @FXML Button btn_logout;
  @FXML Button btn_laundryReq;
  @FXML Button btn_transReq;

  @FXML TableView<MockData> tbMockData;

  @FXML TableColumn<MockData, String> tcData;

  @Override
  public void initialize(URL location, ResourceBundle resourceBundle) {
    // tbMockData.setItems(mockData);
  }

  /*
  private ObservableList<MockData> mockData =
      FXCollections.observableArrayList(new MockData("Hello"), new MockData("Goodbye"));

   */
}
