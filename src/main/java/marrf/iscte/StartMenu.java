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
import javafx.scene.layout.Priority;
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
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
        basicShape.setTextFill(Color.web("#56CCF2"));

        HBox basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);
        basicShapeHBox.setStyle("-fx-background-color: #355765;-fx-background-radius: 20");
        basicShapeHBox.setPrefHeight(30);

        basicShapeHBox.setOnMouseClicked(event -> {
            System.out.println("Add Basic Shape button clicked");

            scene.setRoot(app.getScenePanel(scene));
            primaryStage.setMinWidth(300);
            primaryStage.sizeToScene();
            primaryStage.setMaximized(true);
        });

        HBox.setHgrow(basicShapeHBox, Priority.ALWAYS);
        basicShapeHBox.setMinHeight(45);

        return basicShapeHBox;
    }

    private Pane getLoadFile(){
        Image basicPlus = new Image(App.class.getResource("/icons/jsonIcon.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(18);

        Label basicShape = new Label("Load JSON file");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
        basicShape.setTextFill(Color.web("#56f28f"));

        HBox basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);
        basicShapeHBox.setStyle("-fx-background-color: #35654f;-fx-background-radius: 20");
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

        HBox.setHgrow(basicShapeHBox, Priority.ALWAYS);
        basicShapeHBox.setMinHeight(45);

        return basicShapeHBox;
    }

    private Pane getBottomPane(){
        Label title = new Label("ALTERNATIVE SHAPER");
        title.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        title.setTextFill(Color.WHITE);

        Label subTitle = new Label("Create amazing designs with a simple system!");
        subTitle.setFont(Font.font("SF Pro Rounded", FontWeight.LIGHT, 15));
        subTitle.setTextFill(Color.WHITE);

        var horizontalPanel = new HBox(getBasicShape(), getLoadFile());
        horizontalPanel.setSpacing(20);

        var verticalPanel = new VBox(title, subTitle,verticalGrower(), horizontalPanel);
        verticalPanel.setSpacing(20);
        verticalPanel.setPadding(new Insets(30,20,5,20));

        verticalPanel.setMaxHeight(239);
        verticalPanel.setMinHeight(239);

        return verticalPanel;
    }


    public static VBox verticalGrower(){
        var verticalGrower = new VBox();

        VBox.setVgrow(verticalGrower, Priority.ALWAYS);
        verticalGrower.setMaxHeight(Double.MAX_VALUE);

        return verticalGrower;
    }

    private Pane getTopPane(){

        Image iconImage = new Image(App.class.getResource("/icons/bigIcon.png").toExternalForm());
        ImageView iconImageView = new ImageView(iconImage);
        iconImageView.setSmooth(true);
        iconImageView.setPreserveRatio(true);
        iconImageView.setFitWidth(208);

        var verticalPanel = new VBox(iconImageView);
        verticalPanel.setMaxHeight(314);
        verticalPanel.setMinHeight(314);
        verticalPanel.setStyle("-fx-background-color: #333234");
        verticalPanel.setAlignment(Pos.CENTER);

        return verticalPanel;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(App.class.getResource("/icons/appIcon.png").toExternalForm()));


        var mainPanel = new VBox(getTopPane(), getBottomPane());
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setStyle("-fx-background-color: #262528");

        scene = new Scene(mainPanel, 377, 553);
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
