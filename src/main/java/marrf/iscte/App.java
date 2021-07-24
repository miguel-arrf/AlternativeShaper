package marrf.iscte;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;


/**
 * JavaFX App
 */
public class App extends Application {

    public static final int SCALE = 40;
    public static final int NUMBER_COLUMNS_AND_ROWS = 20;

    private final Rectangle beingDrawnRectangle = new Rectangle(SCALE,SCALE);
    private double initialRectangleHorizontalDrag =  0;
    private double initialRectangleVerticalDrag = 0;

    private double horizontalOffset = 0;
    private double verticalOffset = 0;

    private final ArrayList<Double> initialHorizontalDrag = new ArrayList<>();
    private final ArrayList<Double> initialVerticalDrag = new ArrayList<>();

    @Override
    public void start(Stage stage) {

        var mainPanel = new VBox();
        mainPanel.setMaxWidth(400);
        mainPanel.setMaxHeight(700);
        mainPanel.setPrefSize(400, 700);
        mainPanel.setStyle("-fx-background-color: #262528; -fx-background-radius: 20");
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setPadding(new Insets(10,20,10,20));
        mainPanel.setSpacing(15);

        mainPanel.getChildren().addAll(getNameSection(), getGraphSection(), getWidthSection(), getHeightSection(),getButtonsSection(), getSaveButtonSection());

        var scenePanel = new VBox(mainPanel);
        scenePanel.setStyle("-fx-background-color: black");
        scenePanel.setAlignment(Pos.CENTER);
        scenePanel.setPadding(new Insets(20));

        var scene = new Scene(scenePanel, 400, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        scene.setFill(Color.BLACK);

        stage.setMinWidth(350);

        stage.setScene(scene);
        stage.show();


        for(int i = 0; i < Screen.getScreens().size(); i++){
            Screen screen = Screen.getScreens().get(i);
            Rectangle2D bounds = screen.getVisualBounds();

            System.out.println("screen: " + screen);

            if(i == 1){
                stage.setX(bounds.getMinX() + 100);
                stage.setY(bounds.getMinY() + 100);

            }

        }

    }


    private Pane getGraphSection(){
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setMinHeight(300);
        pane.setMinWidth(300);
        pane.setPrefSize(300,300);

        VBox.setVgrow(pane, Priority.ALWAYS);
        pane.setStyle("-fx-background-color: #333234; -fx-background-radius: 20");

        Pane grid = getGrid(pane);
        grid.setClip(getRectangleClip(pane));
        pane.getChildren().add(grid);

        AnchorPane anchorPane = new AnchorPane();
        pane.getChildren().add(anchorPane);
        getAnchorPaneClip(anchorPane);

        //beingDrawnRectangle.setFill(Color.web("#6FCF97"));
        //beingDrawnRectangle.setClip(getRectangleClip(pane));
        //pane.getChildren().add(beingDrawnRectangle);

        //TODO: Add clipping mask to the beingDrawnRectangle.

        return pane;
    }

    public Shape getTopLeftArc(){
        Rectangle rectangle = new Rectangle(20,20);
        rectangle.setTranslateX(20);
        rectangle.setFill(Color.YELLOW);

        Arc arc = new Arc();

        arc.setCenterX(20);
        arc.setCenterY(20);
        arc.setRadiusX(20);
        arc.setRadiusY(20);
        arc.setStartAngle(360);
        arc.setLength(90);
        arc.setType(ArcType.ROUND);
        arc.setRotate(-90);
        arc.setFill(Color.BLUE);

        return Shape.subtract(rectangle, arc);
    }

    public void getAnchorPaneClip(AnchorPane anchorPane){
        Shape topLeft = getTopLeftArc();
        topLeft.setFill(Color.web("#262528"));
        topLeft.setTranslateX(-20);
        topLeft.setTranslateY(0);

        Shape bottomLeft = getTopLeftArc();
        bottomLeft.setRotate(-90);
        bottomLeft.setTranslateX(-20);
        bottomLeft.setFill(Color.web("#262528"));

        Shape topRight = getTopLeftArc();
        topRight.setRotate(90);
        topRight.setFill(Color.web("#262528"));

        Shape bottomRight = getTopLeftArc();
        bottomRight.setRotate(180);
        bottomRight.setFill(Color.web("#262528"));

        AnchorPane.setBottomAnchor(bottomRight, 0.0);
        AnchorPane.setRightAnchor(bottomRight, 0.0);

        anchorPane.getChildren().addAll(topRight, bottomRight);

        AnchorPane.setRightAnchor(topRight, 0.0);
        AnchorPane.setBottomAnchor(bottomLeft, 0.0);
        anchorPane.getChildren().add(bottomLeft);
        anchorPane.getChildren().add(topLeft);

    }

    public Shape getRectangleClip(Pane parent){
        Rectangle clip = new Rectangle(300,300);
        //clip.setStyle("-fx-background-color: #333234; -fx-background-radius: 20");

        clip.setTranslateX(SCALE);
        clip.setTranslateY(SCALE/2.0);

        parent.widthProperty().addListener((observable, oldValue, newValue) -> {
            double xTranslation = (newValue.doubleValue() - NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            clip.setWidth(newValue.doubleValue());
            clip.setTranslateX(-xTranslation);
        });

        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            double yTranslation = (newValue.doubleValue() - NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            clip.setHeight(newValue.doubleValue());
            clip.setTranslateY(-yTranslation);

        });


        return clip;
    }


    public Pane getGrid(Pane parent){

        int width = SCALE;

        Pane pane = new Pane();

        for (int i = 0; i < NUMBER_COLUMNS_AND_ROWS; i++) {
            for (int j = 0; j < NUMBER_COLUMNS_AND_ROWS; j++) {
                Rectangle rectangle = new Rectangle();
                rectangle.setX(width*j);
                rectangle.setY(width*i);
                rectangle.setWidth(width);
                rectangle.setHeight(width);
                rectangle.setFill(null);
                rectangle.setStroke(Color.web("#4F4F4F"));
                rectangle.setStrokeWidth(2);
                pane.getChildren().add(rectangle);
            }
        }

        Circle circle = new Circle(5);
        circle.setFill(Color.web("#6C696F"));
        circle.setCenterX(width*NUMBER_COLUMNS_AND_ROWS / 2.0);
        circle.setCenterY(width*NUMBER_COLUMNS_AND_ROWS / 2.0);

        beingDrawnRectangle.setFill(Color.web("#6FCF97"));
        beingDrawnRectangle.setTranslateX(NUMBER_COLUMNS_AND_ROWS*SCALE/2.0);
        beingDrawnRectangle.setTranslateY(NUMBER_COLUMNS_AND_ROWS*SCALE/2.0 - SCALE);


        pane.getChildren().add(circle);
        pane.getChildren().add(beingDrawnRectangle);


        parent.widthProperty().addListener((observable, oldValue, newValue) -> {
            double xTranslation = (newValue.doubleValue() - NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            pane.setTranslateX(xTranslation);
        });

        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
          double yTranslation = (newValue.doubleValue() - NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
          pane.setTranslateY(yTranslation);
        });


        parent.setOnMousePressed(event -> {
            horizontalOffset = event.getX();
            verticalOffset = event.getY();

            for (int i = 0; i < pane.getChildren().size(); i++) {
                initialHorizontalDrag.add(i, pane.getChildren().get(i).getTranslateX());
                initialVerticalDrag.add(i, pane.getChildren().get(i).getTranslateY());
            }

            initialRectangleHorizontalDrag = beingDrawnRectangle.getTranslateX();
            initialRectangleVerticalDrag = beingDrawnRectangle.getTranslateY();

        });


        parent.setOnMouseDragged(event -> {
            for (int i = 0; i < pane.getChildren().size(); i++) {
                pane.getChildren().get(i).setTranslateX(initialHorizontalDrag.get(i) + event.getX() - horizontalOffset);
                pane.getChildren().get(i).setTranslateY(initialVerticalDrag.get(i) + event.getY() - verticalOffset);
            }

            beingDrawnRectangle.setTranslateX(initialRectangleHorizontalDrag + event.getX() - horizontalOffset);
            beingDrawnRectangle.setTranslateY(initialRectangleVerticalDrag + event.getY() - verticalOffset);

        });

        parent.setOnMouseReleased(event ->  {
            horizontalOffset = verticalOffset = 0;
        });


        //pane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));

        return pane;
    }



    private Pane getNameSection(){
        Label namePrompt = new Label("Name:");
        namePrompt.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        namePrompt.setTextFill(Color.web("#BDBDBD"));

        Label defaultName = new Label("default");
        defaultName.setTextFill(Color.WHITE);
        defaultName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        defaultName.setTextFill(Color.web("#5D5C5E"));


        HBox nameHB = new HBox(namePrompt, defaultName);
        nameHB.setPadding(new Insets(10,10,10,15));
        nameHB.setAlignment(Pos.CENTER_LEFT);
        nameHB.setMaxHeight(30);
        nameHB.setSpacing(10);

        nameHB.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

        return nameHB;
    }

    private VBox verticalGrower(){
        var verticalGrower = new VBox();

        VBox.setVgrow(verticalGrower, Priority.ALWAYS);
        verticalGrower.setMaxHeight(Double.MAX_VALUE);

        return verticalGrower;
    }

    private HBox horizontalGrower(){
        var horizontalGrower = new HBox();

        HBox.setHgrow(horizontalGrower, Priority.ALWAYS);
        horizontalGrower.setMaxHeight(Double.MAX_VALUE);

        return horizontalGrower;
    }

    private HBox horizontalGrower(int maxSize){
        var horizontalGrower = new HBox();

        HBox.setHgrow(horizontalGrower, Priority.ALWAYS);
        horizontalGrower.setMaxHeight(maxSize);

        return horizontalGrower;
    }

    private Pane getWidthSection(){
        Label widthLabel = new Label("Width:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));

        Label valueLabel = new Label("0");
        valueLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        valueLabel.setTextFill(Color.web("#BDBDBD"));
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        valueLabel.setPrefWidth(50);

        Slider slider = new Slider();
        slider.setMax(10);
        slider.setMin(0.1);
        slider.setValue(1);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            valueLabel.setText(String.valueOf(truncatedDouble));

            beingDrawnRectangle.setWidth(newValue.doubleValue() * SCALE);

        });

        HBox widthHB = new HBox(widthLabel, horizontalGrower(), slider, horizontalGrower(), valueLabel);
        widthHB.setPadding(new Insets(10,10,10,15));
        widthHB.setAlignment(Pos.CENTER_LEFT);
        widthHB.setMaxHeight(30);

        widthHB.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");


        return widthHB;
    }

    private Pane getHeightSection(){
        Label heightLabel = new Label("Height:");
        heightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        heightLabel.setTextFill(Color.web("#BDBDBD"));

        Label valueLabel = new Label("0");
        valueLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        valueLabel.setTextFill(Color.web("#BDBDBD"));
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        valueLabel.setPrefWidth(50);

        Slider slider = new Slider();
        slider.setValue(1);
        slider.setMax(10);
        slider.setMin(0.1);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            valueLabel.setText(String.valueOf(truncatedDouble));

            beingDrawnRectangle.setTranslateY( beingDrawnRectangle.getTranslateY() - Math.abs(oldValue.doubleValue() - newValue.doubleValue() ) * SCALE );

            beingDrawnRectangle.setHeight(newValue.doubleValue() * SCALE);
        });

        HBox heightHB = new HBox(heightLabel, horizontalGrower(), slider, horizontalGrower(), valueLabel);
        heightHB.setPadding(new Insets(10,10,10,15));
        heightHB.setAlignment(Pos.CENTER_LEFT);
        heightHB.setMaxHeight(30);

        heightHB.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

        return heightHB;
    }

    private Pane getButtonsSection(){
        HBox hBox = new HBox(getToFrontPane(), getColorButton(), getImageButton());
        hBox.setSpacing(10);
        hBox.setPrefHeight(30);

        return hBox;
    }

    private Pane getToFrontPane(){
        Label widthLabel = new Label("To front:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));

        HBox heightHB = new HBox(widthLabel, horizontalGrower(), getCheckButton());
        heightHB.setPadding(new Insets(10,10,10,15));
        heightHB.setAlignment(Pos.CENTER_LEFT);
        heightHB.setMaxHeight(30);
        HBox.setHgrow(heightHB, Priority.ALWAYS);

        heightHB.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

        return heightHB;
    }

    private Pane getCheckButton(){
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: #4F4F4F;-fx-background-radius: 7");

        pane.setPrefSize(20,20);

        pane.setMaxWidth(20);
        pane.setMaxHeight(20);

        return pane;
    }

    private Pane getColorButton(){

        Label colorLabel = new Label("Color");
        colorLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        colorLabel.setTextFill(Color.web("#F2C94C"));

        HBox colorHB = new HBox(colorLabel);
        colorHB.setPadding(new Insets(10));
        colorHB.setAlignment(Pos.CENTER);
        colorHB.setMaxHeight(30);
        HBox.setHgrow(colorHB, Priority.ALWAYS);

        colorHB.setStyle("-fx-background-color: #644832;-fx-background-radius: 20");

        colorHB.setOnMouseEntered(event -> colorHB.setStyle("-fx-background-color: #C3834A;-fx-background-radius: 20"));

        colorHB.setOnMouseExited(event -> colorHB.setStyle("-fx-background-color: #644832;-fx-background-radius: 20"));

        return colorHB;
    }

    private Pane getImageButton(){
        Label imageLabel = new Label("Image");
        imageLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        imageLabel.setTextFill(Color.web("#56CCF2"));

        HBox imageHB = new HBox(imageLabel);
        imageHB.setPadding(new Insets(10));
        imageHB.setAlignment(Pos.CENTER);
        imageHB.setMaxHeight(30);
        HBox.setHgrow(imageHB, Priority.ALWAYS);

        imageHB.setStyle("-fx-background-color: #355765;-fx-background-radius: 20");

        imageHB.setOnMouseEntered(event -> {
            imageHB.setStyle("-fx-background-color: #4176B4;-fx-background-radius: 20");
        });

        imageHB.setOnMouseExited(event -> {
            imageHB.setStyle("-fx-background-color: #355765;-fx-background-radius: 20");
        });

        return imageHB;
    }

    private Pane getSaveButtonSection(){
        Label saveLabel = new Label("Save:");
        saveLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        saveLabel.setTextFill(Color.web("#6FCF97"));

        HBox saveHB = new HBox(saveLabel);
        saveHB.setPadding(new Insets(10));
        saveHB.setAlignment(Pos.CENTER);
        saveHB.setMaxHeight(50);
        saveHB.setPrefHeight(50);

        saveHB.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 20");

        saveHB.setOnMouseEntered(event -> saveHB.setStyle("-fx-background-color: #078D55;-fx-background-radius: 20"));

        saveHB.setOnMouseExited(event -> saveHB.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 20"));

        return saveHB;
    }

    public static void main(String[] args) {
        launch();
    }

}