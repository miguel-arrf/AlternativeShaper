package marrf.iscte;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class StartMenu extends Application {

    private App app = new App();

    private Scene scene;
    private Stage primaryStage;

    private Pane getBasicShape(){
        Image basicPlus = new Image(App.class.getResource("/icons/plus.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(12);

        Label basicShape = new Label("Basic Shape");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        basicShape.setTextFill(Color.web("#56CCF2"));

        HBox basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);
        basicShapeHBox.setStyle("-fx-background-color: #355765;-fx-background-radius: 20");
        basicShapeHBox.setMaxHeight(30);
        basicShapeHBox.setPrefHeight(30);

        basicShapeHBox.setOnMouseClicked(event -> {
            System.out.println("Add Basic Shape button clicked");

            scene.setRoot(app.getScenePanel(scene));
            primaryStage.setMinWidth(300);
            primaryStage.sizeToScene();
            primaryStage.setMaximized(true);
        });

        basicShapeHBox.setMinHeight(50);

        return basicShapeHBox;
    }

    private Pane getLoadFile(){
        Image basicPlus = new Image(App.class.getResource("/icons/jsonIcon.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(18);

        Label basicShape = new Label("Load JSON file");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        basicShape.setTextFill(Color.web("#56f28f"));

        HBox basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);
        basicShapeHBox.setStyle("-fx-background-color: #35654f;-fx-background-radius: 20");
        basicShapeHBox.setMaxHeight(30);
        basicShapeHBox.setPrefHeight(30);

        basicShapeHBox.setOnMouseClicked(event -> {
            System.out.println("Load JSON file button clicked");

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File selectedFile = fileChooser.showOpenDialog(basicShapeHBox.getScene().getWindow());

            System.out.println("Selected file: " + selectedFile);

            scene.setRoot(app.getScenePanelWithLoadedFile(scene, selectedFile));
            primaryStage.setMinWidth(300);
            primaryStage.sizeToScene();
            primaryStage.setMaximized(true);

        });

        basicShapeHBox.setMinHeight(50);

        return basicShapeHBox;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        var mainPanel = new VBox(getBasicShape(), getLoadFile());
        mainPanel.setPadding(new Insets(20));
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setStyle("-fx-background-color: #262528");
        mainPanel.setSpacing(20);

        scene = new Scene(mainPanel, 250, 160);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        scene.setFill(Color.BLACK);

        primaryStage.setTitle("Alternative Shaper");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.sizeToScene();

        for(int i = 0; i < Screen.getScreens().size(); i++){
            Screen screen = Screen.getScreens().get(i);
            Rectangle2D bounds = screen.getVisualBounds();

            System.out.println("screen: " + screen);

            if(i == 1){
                primaryStage.setX(bounds.getMinX());
                primaryStage.setY(bounds.getMinY());

            }

        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
