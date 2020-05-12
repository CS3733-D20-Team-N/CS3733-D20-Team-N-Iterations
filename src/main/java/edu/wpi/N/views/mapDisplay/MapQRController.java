package edu.wpi.N.views.mapDisplay;

import com.jfoenix.controls.JFXTabPane;
import edu.wpi.N.App;
import edu.wpi.N.views.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class MapQRController implements Controller {

  @FXML JFXTabPane tbpn_directions;
  @FXML Tab tb_faulkner_directions;
  @FXML Tab tb_main_directions;
  @FXML Tab tb_drive_directions;
  @FXML TextArea txt_faulkner_directions;
  @FXML TextArea txt_main_directions;
  @FXML TextArea txt_drive_directions;
  @FXML ImageView img_faulkner;
  @FXML ImageView img_main;
  @FXML ImageView img_drive;

  @Override
  public void setMainApp(App mainApp) {}

  public void initialize() {}

  public void setDirectionTab(String location) {
    if (location.equals("Faulkner")) {
      tbpn_directions.getSelectionModel().select(0);
    } else if (location.equals("edu.wpi.N.Main")) {
      tbpn_directions.getSelectionModel().select(1);
    } else if (location.equals("Street View")) {
      tbpn_directions.getSelectionModel().select(2);
    }
  }

  public TextArea getTextFaulkner() {
    return this.txt_faulkner_directions;
  }

  public TextArea getTextMain() {
    return this.txt_main_directions;
  }

  public TextArea getTextDrive() {
    return this.txt_drive_directions;
  }

  public ImageView getImageFaulkner() {
    return this.img_faulkner;
  }

  public ImageView getImageMain() {
    return this.img_main;
  }

  public ImageView getImageDrive() {
    return this.img_drive;
  }
}
