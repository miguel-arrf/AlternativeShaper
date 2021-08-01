package marrf.iscte;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.stage.Stage;

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
            System.out.println("+ Basic Shape button was clicked");

            scene.setRoot(app.getScenePanel(scene));
            primaryStage.setMinWidth(300);
            primaryStage.setMaximized(true);
        });

        return basicShapeHBox;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        var mainPanel = new VBox(getBasicShape());
        mainPanel.setPadding(new Insets(10));
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setStyle("-fx-background-color: #262528");

        scene = new Scene(mainPanel, 300, 50);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        scene.setFill(Color.BLACK);

        primaryStage.setTitle("Alternative Shaper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
