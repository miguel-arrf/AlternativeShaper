package marrf.iscte;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;


public class PopupWindow {

    private Stage stage = new Stage();

    public Stage getStage() {
        return stage;
    }

    public void createPopup(String windowTitle, Scene scene, @NamedArg("labelContent")String labelContent, Pane ... panes){

        scene.getRoot().setCache(true);
        scene.getRoot().setCacheHint(CacheHint.SPEED);
        startBlurAnimation(scene.getRoot(), 0.0, 30.0, Duration.millis(100), false);
        scene.getRoot().setCache(false);
        scene.getRoot().setCacheHint(CacheHint.DEFAULT);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(StartMenu.primaryStage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: #262528");

        Label label = new Label(labelContent);
        label.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        label.setTextFill(Color.web("#BDBDBD"));
        label.setWrapText(true);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setPadding(new Insets(10));
        label.setStyle("-fx-background-color: #333234; -fx-background-radius: 10");
        VBox.setVgrow(label, Priority.ALWAYS);

        dialogVbox.getChildren().add(label);


        VBox content = new VBox();
        dialogVbox.getChildren().addAll(content);

        content.setSpacing(20);
        content.setPadding(new Insets(0));
        content.getChildren().addAll(panes);

        dialogVbox.setMaxHeight(Double.MAX_VALUE);

        Scene dialogScene = new Scene(dialogVbox, 400, 300);
        stage.setScene(dialogScene);
        stage.show();

        stage.sizeToScene();
        stage.setTitle(windowTitle);

        //TODO if the screen resolution is to low, the stage may appear too big...
        stage.setResizable(false);

        stage.setOnCloseRequest(event -> {
            scene.getRoot().setCache(true);
            scene.getRoot().setCacheHint(CacheHint.SPEED);
            startBlurAnimation(scene.getRoot(), 30.0, 0.0, Duration.millis(100), true);
            scene.getRoot().setCache(false);
            scene.getRoot().setCacheHint(CacheHint.DEFAULT);
        });


    }

    private void startBlurAnimation(Parent on, Double from, Double to, Duration duration, Boolean removeOnEnd){

        // START BLUR ANIMATION
        GaussianBlur blur = new GaussianBlur(0);
        on.setEffect(blur);
        DoubleProperty value = new SimpleDoubleProperty(from);
        value.addListener((observable, oldV, newV)->
        {
            blur.setRadius(newV.doubleValue());
        });

        Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(value, to, Interpolator.EASE_BOTH);
        final KeyFrame kf = new KeyFrame(duration, kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        timeline.setOnFinished(event -> {
            if(removeOnEnd){
                on.setEffect(null);
            }
        });
        // END BLUR ANIMATION
    }

    public static Pane getButton(String text, String iconName, String backgroundColor, String textColor, EventHandler<? super MouseEvent> onMouseClicked){
        Label basicShape = new Label(text);
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
        basicShape.setTextFill(Color.web(textColor));
        basicShape.setMaxHeight(Double.MAX_VALUE);
        basicShape.setWrapText(true);

        HBox basicShapeHBox;

        if(!iconName.equals("null")){
            Image basicPlus = new Image(App.class.getResource("/icons/" + iconName).toExternalForm());
            ImageView basicPlusImageView = new ImageView(basicPlus);
            basicPlusImageView.setSmooth(true);
            basicPlusImageView.setPreserveRatio(true);
            basicPlusImageView.setFitWidth(18);

            basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        }else{
            basicShapeHBox = new HBox(basicShape);
        }

        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);
        basicShapeHBox.setStyle("-fx-background-color: " + backgroundColor + " ;-fx-background-radius: 10");

        basicShapeHBox.setPadding(new Insets(10));

        HBox.setHgrow(basicShapeHBox, Priority.ALWAYS);
        VBox.setVgrow(basicShapeHBox, Priority.ALWAYS);

        basicShapeHBox.setMinHeight(45);
        basicShapeHBox.maxHeight(Double.MAX_VALUE);

        basicShapeHBox.setOnMouseClicked(onMouseClicked);

        return basicShapeHBox;
    }

}
