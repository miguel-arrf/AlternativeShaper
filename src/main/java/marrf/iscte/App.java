package marrf.iscte;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final int SCALE = 40;
    public static final int NUMBER_COLUMNS_AND_ROWS = 40;

    private final CustomRectangle beingDrawnCustomRectangle = new CustomRectangle(SCALE,SCALE);

    private final StackPane centerCustomRectangle = new StackPane();

    private Scene scene;

    private final GridCanvas gridCanvas = new GridCanvas();

    private Pane sceneStackPane = getGraphSection();

    private ArrayList<CustomRectangle> customRectangles = new ArrayList<>();
    private CustomRectangle selectedCustomRectangle;

    private boolean isCurrentSimple = true;
    private TextField currentName;

    private final BooleanProperty firstBasicShapeWasSaved = new SimpleBooleanProperty(false);

    private VBox mainPanel;


    private HBox widthSection;
    private HBox heightSection;
    private HBox translationYSection;
    private HBox translationXSection;
    private HBox scaleXSection;
    private HBox scaleYSection;


    private Slider heightSectionSlider;
    private Slider widthSectionSlider;
    private Slider translationXSlider;
    private Slider translationYSlider;
    private Slider scaleXSlider;
    private Slider scaleYSlider;

    private boolean resetingSliders = false;


    private ObservableList<CustomRectangle> sideBarThumbnails = FXCollections.observableList(new ArrayList<>());

    public CustomRectangle getDraggableCustomRectangle(){

        CustomRectangle customRectangle = new CustomRectangle(20,20);
        customRectangle.setFill(Color.rgb(0,0,255,1));

        customRectangles.add(customRectangle);

        customRectangle.setOnDragDetected(event -> {
            Dragboard db = customRectangle.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(customRectangles.indexOf(customRectangle)));
            db.setContent(content);

            event.consume();
        });

        System.out.println("AAAAAHHHHH width: " + customRectangle.getWidth());
        return customRectangle;
    }

    public CustomRectangle getCopyWithBindWidthAndHeightFrom(int position){
        CustomRectangle toCopyFrom = customRectangles.get(position);
        System.out.println("to copy from: width:" + toCopyFrom.getWidth() + ", height: " + toCopyFrom.getHeight());

        CustomRectangle customRectangle = new CustomRectangle();
        customRectangle.setFill(toCopyFrom.getFill());
        customRectangle.setWidth(toCopyFrom.getWidth());
        customRectangle.setHeight(toCopyFrom.getHeight());

        customRectangle.widthProperty().bind(toCopyFrom.widthProperty());
        customRectangle.heightProperty().bind(toCopyFrom.heightProperty());
        customRectangle.fillProperty().bind(toCopyFrom.fillProperty());

        return customRectangle;
    }

    private Pane getSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefHeight(8);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);

        return rectangle;
    }

    private Pane getBasicShape(){
        CustomRectangle customRectangle = new CustomRectangle(100,100);
        customRectangle.getRectangle();

        customRectangle.setFill(Color.rgb(0,255,255,1));
        customRectangles.add(customRectangle);

        return customRectangle.getThumbnail(() -> String.valueOf(customRectangles.indexOf(customRectangle)));

    }

    private ScrollPane getScrollPane(){
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: rgb(51,50,52); -fx-border-radius: 10");

        VBox content = new VBox(/*getBasicShape()*/);

        sideBarThumbnails.addListener((ListChangeListener<? super CustomRectangle>) change -> {
            while (change.next()){
                if(change.wasAdded()){
                    for (CustomRectangle customRectangleAdded : change.getAddedSubList()) {
                        Pane checkIfExists = customRectangleAdded.getThumbnail(() -> String.valueOf(customRectangles.indexOf(customRectangleAdded)));
                        content.getChildren().remove(checkIfExists);//This only removes if it exists
                        content.getChildren().add(checkIfExists);

                        checkIfExists.setOnMouseClicked(mouseEvent -> {
                            System.out.println("I've clicked on a thumbnail!");
                            gridCanvas.clearEverything(true);
                            //TODO aqui está a true, mas em algum momento não será...
                            isCurrentSimple = true;

                            mainPanel.getChildren().removeAll(scaleXSection, scaleYSection, translationXSection, translationYSection);
                            mainPanel.getChildren().removeAll(widthSection, heightSection);
                            mainPanel.getChildren().addAll(widthSection, heightSection);

                            resetSliders();

                            addShape(customRectangleAdded);

                        });

                    }
                }
            }

        });

        content.setAlignment(Pos.TOP_CENTER);
        content.setSpacing(10);
        content.setPadding(new Insets(10));

        scrollPane.setContent(content);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return scrollPane;
    }

    public HBox addBasicAndComplexButtons(){

        Image basicPlus = new Image(App.class.getResource("/icons/plus.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(12);

        Image complexPlus = new Image(App.class.getResource("/icons/plus.png").toExternalForm());
        ImageView complexPlusImageView = new ImageView(complexPlus);
        complexPlusImageView.setSmooth(true);
        complexPlusImageView.setPreserveRatio(true);
        complexPlusImageView.setFitWidth(12);


        Label basicShape = new Label("Basic Shape");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        basicShape.setTextFill(Color.web("#56CCF2"));

        HBox basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);

        VBox basicShapeVBox = new VBox(basicShapeHBox);
        basicShapeVBox.setAlignment(Pos.CENTER);
        basicShapeVBox.setStyle("-fx-background-color: #355765;-fx-background-radius: 20");
        HBox.setHgrow(basicShapeVBox, Priority.ALWAYS);

        basicShapeVBox.setOnMouseClicked(mouseEvent -> {
            System.out.println("I want to add a new basic shape!");
            gridCanvas.clearEverything(true);
            isCurrentSimple = true;
            currentName.setText("simpleDefault");

            mainPanel.getChildren().removeAll(scaleXSection, scaleYSection, translationXSection, translationYSection);
            mainPanel.getChildren().removeAll(widthSection, heightSection);
            mainPanel.getChildren().addAll(widthSection, heightSection);

            resetSliders();

            addShape(new CustomRectangle(SCALE, SCALE, true, Color.web("#55efc4")), true);

        });


        Label complexShape = new Label("Complex Shape");
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web("#F2C94C"));

        HBox complexShapeHBox = new HBox(complexPlusImageView, complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);

        VBox complexShapeVBox = new VBox(complexShapeHBox);
        complexShapeVBox.setAlignment(Pos.CENTER);
        complexShapeVBox.setStyle("-fx-background-color: #644832;-fx-background-radius: 20");
        HBox.setHgrow(complexShapeVBox, Priority.ALWAYS);

        complexShapeVBox.setOnMouseClicked(mouseEvent -> {
            System.out.println("I want to add a new complex shape!");
            gridCanvas.clearEverything(false);
            isCurrentSimple = false;
            currentName.setText("complexDefault");

            mainPanel.getChildren().removeAll(scaleXSection, scaleYSection, translationXSection, translationYSection);
            mainPanel.getChildren().removeAll(widthSection,heightSection);
            mainPanel.getChildren().addAll(scaleXSection, scaleYSection, translationXSection, translationYSection);
        });

        HBox saveHB = new HBox(basicShapeVBox, complexShapeVBox);
        saveHB.setSpacing(10);
        saveHB.setAlignment(Pos.CENTER);
        saveHB.setMaxHeight(50);
        saveHB.setPrefHeight(50);

        Pane toAdd = getSeparator();

        firstBasicShapeWasSaved.addListener((observableValue, aBoolean, t1) -> {
            if(t1){
                mainPanel.getChildren().addAll(toAdd, saveHB);
            }else{
                mainPanel.getChildren().remove(toAdd);
                mainPanel.getChildren().remove(saveHB);
            }
        });

        return saveHB;
    }

    private void addShape(CustomRectangle customRectangleToAdd){
        addShape(customRectangleToAdd, false);
    }

    private void addShape(CustomRectangle customRectangleToAdd, boolean selected){
        gridCanvas.addShape(customRectangleToAdd);
        if(!customRectangles.contains(customRectangleToAdd)){
            customRectangles.add(customRectangleToAdd);
        }
        if(selected){
            selectedCustomRectangle = customRectangleToAdd;
            selectedCustomRectangle.turnOnStroke();
            selectedCustomRectangle.toogleSelected();

            customRectangles.stream().filter(r -> r != customRectangleToAdd).forEach(CustomRectangle::turnOffStroke);

        }
    }




    public Pane getScenePanel(Scene scene){
        finishSetup();

        this.scene = scene;

        mainPanel = new VBox();
        mainPanel.setMaxWidth(400);
        mainPanel.setPrefSize(400, 700);
        mainPanel.setStyle("-fx-background-color: #262528;");
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setPadding(new Insets(10,20,10,20));
        mainPanel.setSpacing(15);


        //Slider slider = new Slider(30, 120, 90);
        //CustomRectangle draggableCustomRectangle = getDraggableCustomRectangle();
        //slider.valueProperty().addListener((observable, oldValue, newValue) -> draggableCustomRectangle.setWidth(newValue.doubleValue()));


        mainPanel.getChildren().addAll(/*draggableCustomRectangle.getRectangle(), slider, getScrollPane(), addBasicAndComplexButtons(), getSeparator(),*/getScrollPane(), getNameSection() /*, getTranslationXSection(), getTranslationYSection(), getScaleXSection(), getScaleYSection()*/,widthSection,heightSection,/*getButtonsSection(),*/ getSaveButtonSection());

        var scenePanel = new VBox(mainPanel);
        scenePanel.setStyle("-fx-background-color: black");
        scenePanel.setAlignment(Pos.CENTER);
        scenePanel.setPadding(new Insets(20));


        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #262528");
        borderPane.setPadding(new Insets(10));

        borderPane.setRight(mainPanel);
        borderPane.setCenter(sceneStackPane);

        addShape(new CustomRectangle(SCALE, SCALE, true, Color.web("#55efc4")));


        return borderPane;
    }

    private void finishSetup(){
        addBasicAndComplexButtons();
        setUpGetScaleXSection();
        setUpGetScaleYSection();
        setUpGetTranslationXSection();
        setUpGetTranslationYSection();

        setUpGetWidthSection();
        setUpGetHeightSection();

    }

    @Override
    public void start(Stage stage) {
        Pane borderPane = getScenePanel(scene);

        scene = new Scene(borderPane, 1920, 1080);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        scene.setFill(Color.BLACK);

        stage.setMinWidth(300);

        stage.setScene(scene);
        stage.show();


        for(int i = 0; i < Screen.getScreens().size(); i++){
            Screen screen = Screen.getScreens().get(i);
            Rectangle2D bounds = screen.getVisualBounds();


            if(i == 0){
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());

            }

        }

    }

    private Pane getGraphSection(){
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setMinHeight(300);
        pane.setMinWidth(300);

        VBox.setVgrow(pane, Priority.ALWAYS);
        pane.setStyle("-fx-background-color: #333234; -fx-background-radius: 20");

        Pane grid = gridCanvas.getGrid(pane);
        grid.setClip(getCustomRectangleClip(pane));
        pane.getChildren().add(grid);

        AnchorPane anchorPane = new AnchorPane();
        pane.getChildren().add(anchorPane);
        getAnchorPaneClip(anchorPane);

        pane.setOnMouseEntered(event -> {
            scene.setCursor(Cursor.CLOSED_HAND);
        });

        pane.setOnMouseExited(event -> {
            scene.setCursor(Cursor.DEFAULT);
        });

        pane.setOnDragEntered(event -> {
            if (event.getGestureSource() != pane &&
                    event.getDragboard().hasString()) {
                System.out.println("estou dragging: " + event.getDragboard().getString());
            }
            event.consume();
        });

        pane.setOnMouseClicked(event -> {
            customRectangles.forEach(rectangle -> {
                Point2D transformation = rectangle.localToScene(rectangle.getX(), rectangle.getY()).add(new Point2D(rectangle.getWidth(), 0));

                if(transformation.getX() - event.getSceneX() >= 0 && transformation.getX() - event.getSceneX() <= rectangle.getWidth()) {
                    if (transformation.getY() - event.getSceneY() >= 0 && transformation.getY() - event.getSceneY() <= rectangle.getHeight()) {
                        System.out.println("cliquei numa shape!");
                        selectedCustomRectangle = rectangle;
                        selectedCustomRectangle.turnOnStroke();
                        selectedCustomRectangle.toogleSelected();

                        customRectangles.stream().filter(r -> r != rectangle).forEach(CustomRectangle::turnOffStroke);
                        customRectangles.stream().filter(r -> r != rectangle).forEach(CustomRectangle::toogleOffSelection);

                        //selectedCustomRectangle.setStrokeWidth(2);
                        //selectedCustomRectangle.setStroke(Color.BLACK);

                        //CustomRectangles.stream().filter(r -> r != CustomRectangle).forEach(r -> r.setStrokeWidth(0));                    }
                    }
                }
            });
        });

        pane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes( TransferMode.ANY );
            }
            event.consume();
        });

        pane.setOnDragDropped(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            System.out.println("oi");

            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("this was dropped: " + db.getString());

                System.out.println("event x: " + event.getX() + ", y: " + event.getY());

                if(!isCurrentSimple){
                    CustomRectangle customRectangleToAdd = getCopyWithBindWidthAndHeightFrom(Integer.parseInt(db.getString()));
                    addShape(customRectangleToAdd);
                    customRectangleToAdd.toogleOffSelection();
                    //TODO Here we need to reset the sliders... that's why a offset is being created when we drop in a new shape...
                }else {
                    System.err.println("Não adicionei nada porque agora estamos numa current Simple!");
                }



                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });


        pane.setOnMouseMoved(event -> {
            customRectangles.forEach(rectangle -> {
                Point2D transformation = rectangle.localToScene(rectangle.getX(), rectangle.getY()).add(new Point2D(rectangle.getWidth(), 0));

                if(transformation.getX() - event.getSceneX() >= 0 && transformation.getX() - event.getSceneX() <= rectangle.getWidth()){
                    if(transformation.getY() - event.getSceneY() >= 0 && transformation.getY() - event.getSceneY() <= rectangle.getHeight()){
                        rectangle.turnOnStroke();
                    }else{
                        rectangle.turnOffStrokeIfNotSelected();
                    }
                }else{
                    rectangle.turnOffStrokeIfNotSelected();
                }
            });

        });

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


        Circle firstCircle = new Circle(10);
        firstCircle.setFill(Color.web("#4F4F4F"));
        Circle secondCircle = new Circle(5);
        secondCircle.setFill(Color.web("#333333"));

        centerCustomRectangle.getChildren().addAll(firstCircle, secondCircle);
        centerCustomRectangle.setAlignment(Pos.CENTER);
        centerCustomRectangle.setPrefSize(30,30);
        centerCustomRectangle.setStyle("-fx-background-color: #737373; -fx-background-radius: 10");

        AnchorPane.setBottomAnchor(centerCustomRectangle, 10.0);
        AnchorPane.setRightAnchor(centerCustomRectangle, 10.0);
        anchorPane.getChildren().add(centerCustomRectangle);

    }

    public Shape getCustomRectangleClip(Pane parent){
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

    private Pane getNameSection(){
        Label namePrompt = new Label("Name:");
        namePrompt.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        namePrompt.setTextFill(Color.web("#BDBDBD"));

        currentName = new TextField(" default ");
        currentName.setPromptText("default");
        currentName.setStyle("-fx-background-color: #333234; -fx-text-fill: #5D5C5E; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        currentName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));


        HBox nameHB = new HBox(namePrompt, currentName);
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

    private void setUpGetScaleXSection(){
        Label widthLabel = new Label("Scale X:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        scaleXSlider = new Slider();
        scaleXSlider.setMax(10);
        scaleXSlider.setMin(0.1);
        scaleXSlider.setValue(1);


        textField.setOnKeyPressed(keyEvent ->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){

                try{
                    if(Double.parseDouble(textField.getText()) < scaleXSlider.getMin()){
                        textField.setText(String.valueOf(scaleXSlider.getMin()));
                    }

                    scaleXSlider.setValue(Double.parseDouble(textField.getText()));

                }catch (NumberFormatException e){
                    textField.setText(String.valueOf(scaleXSlider.getMin()));
                    scaleXSlider.setValue(scaleXSlider.getMin());
                }

            }
        } );

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane


        scaleXSlider.setMajorTickUnit(0.1);
        scaleXSlider.setMinorTickCount(0);
        scaleXSlider.setSnapToTicks(true);

        scaleXSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            scaleXSlider.setValue(Double.parseDouble(df.format(t1.doubleValue())));
        }));

        scaleXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            if(!resetingSliders) {
                selectedCustomRectangle.setScaleX(newValue.doubleValue());
            }
        });

        scaleXSection = new HBox(widthLabel, horizontalGrower(), scaleXSlider, horizontalGrower(), textField);
        scaleXSection.setPadding(new Insets(10,10,10,15));
        scaleXSection.setAlignment(Pos.CENTER_LEFT);
        scaleXSection.setMinHeight(30);
        scaleXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private void setUpGetScaleYSection(){
        Label heightLabel = new Label("Scale Y:");
        heightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        heightLabel.setTextFill(Color.web("#BDBDBD"));

        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        scaleYSlider = new Slider();
        scaleYSlider.setMax(10);
        scaleYSlider.setMin(0.1);
        scaleYSlider.setValue(1);

        scaleYSlider.setMajorTickUnit(0.1);
        scaleYSlider.setMinorTickCount(0);
        scaleYSlider.setSnapToTicks(true);

        textField.setOnKeyPressed(keyEvent ->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){

                try{
                    if(Double.parseDouble(textField.getText()) < scaleYSlider.getMin()){
                        textField.setText(String.valueOf(scaleYSlider.getMin()));
                    }

                    scaleYSlider.setValue(Double.parseDouble(textField.getText()));

                }catch (NumberFormatException e){
                    textField.setText(String.valueOf(scaleYSlider.getMin()));
                    scaleYSlider.setValue(scaleYSlider.getMin());
                }

            }
        } );


        scaleYSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);

            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            if(!resetingSliders) {

                var oldHeight = selectedCustomRectangle.getHeight();
                selectedCustomRectangle.setScaleY(newValue.doubleValue());

                if (newValue.doubleValue() > oldValue.doubleValue()) {
                    selectedCustomRectangle.setTranslateY(selectedCustomRectangle.getTranslateY() - Math.abs(selectedCustomRectangle.getHeight() - oldHeight));
                    System.out.println("-> " + Math.abs(selectedCustomRectangle.getHeight() * oldValue.doubleValue() - selectedCustomRectangle.getHeight() * newValue.doubleValue()));
                    System.out.println("altura: " + selectedCustomRectangle.getHeight());
                }
            }


        });

        scaleYSection = new HBox(heightLabel, horizontalGrower(), scaleYSlider, horizontalGrower(), textField);
        scaleYSection.setPadding(new Insets(10,10,10,15));
        scaleYSection.setAlignment(Pos.CENTER_LEFT);
        scaleYSection.setMinHeight(30);
        scaleYSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }



    private void setUpGetTranslationXSection(){
        Label widthLabel = new Label("Translation X:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        translationXSlider = new Slider();
        translationXSlider.setMax(SCALE*NUMBER_COLUMNS_AND_ROWS/2.0);
        translationXSlider.setMin(-SCALE*NUMBER_COLUMNS_AND_ROWS/2.0);
        translationXSlider.setValue(1);


        textField.setOnKeyPressed(keyEvent ->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){

                try{
                    if(Double.parseDouble(textField.getText()) < translationXSlider.getMin()){
                        textField.setText(String.valueOf(translationXSlider.getMin()));
                    }

                    translationXSlider.setValue(Double.parseDouble(textField.getText()));

                }catch (NumberFormatException e){
                    textField.setText(String.valueOf(translationXSlider.getMin()));
                    translationXSlider.setValue(translationXSlider.getMin());
                }

            }
        } );

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane


        translationXSlider.setMajorTickUnit(0.1);
        translationXSlider.setMinorTickCount(0);
        translationXSlider.setSnapToTicks(true);

        translationXSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            translationXSlider.setValue(Double.parseDouble(df.format(t1.doubleValue())));
        }));

        translationXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            if(!resetingSliders) {
                selectedCustomRectangle.addTranslationX(newValue.doubleValue() - oldValue.doubleValue());
            }

        });

        translationXSection = new HBox(widthLabel, horizontalGrower(), translationXSlider, horizontalGrower(), textField);
        translationXSection.setPadding(new Insets(10,10,10,15));
        translationXSection.setAlignment(Pos.CENTER_LEFT);
        translationXSection.setMinHeight(30);
        translationXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private void setUpGetTranslationYSection(){
        Label heightLabel = new Label("Translation Y:");
        heightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        heightLabel.setTextFill(Color.web("#BDBDBD"));

        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        translationYSlider = new Slider();
        translationYSlider.setMax(SCALE*NUMBER_COLUMNS_AND_ROWS/2.0);
        translationYSlider.setMin(-SCALE*NUMBER_COLUMNS_AND_ROWS/2.0);
        translationYSlider.setValue(1);

        translationYSlider.setMajorTickUnit(0.1);
        translationYSlider.setMinorTickCount(0);
        translationYSlider.setSnapToTicks(true);

        textField.setOnKeyPressed(keyEvent ->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){

                try{
                    if(Double.parseDouble(textField.getText()) < translationYSlider.getMin()){
                        textField.setText(String.valueOf(translationYSlider.getMin()));
                    }

                    translationYSlider.setValue(Double.parseDouble(textField.getText()));

                }catch (NumberFormatException e){
                    textField.setText(String.valueOf(translationYSlider.getMin()));
                    translationYSlider.setValue(translationYSlider.getMin());
                }

            }
        } );


        translationYSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);

            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            var oldHeight = selectedCustomRectangle.getHeight();

            if(!resetingSliders) {

                selectedCustomRectangle.addTranslationY(newValue.doubleValue() - oldValue.doubleValue());
            }
        });

        translationYSection = new HBox(heightLabel, horizontalGrower(), translationYSlider, horizontalGrower(), textField);
        translationYSection.setPadding(new Insets(10,10,10,15));
        translationYSection.setAlignment(Pos.CENTER_LEFT);
        translationYSection.setMinHeight(30);
        translationYSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }



    private void setUpGetWidthSection(){
        Label widthLabel = new Label("Width:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);



        widthSectionSlider = new Slider();
        widthSectionSlider.setMax(10);
        widthSectionSlider.setMin(0.1);
        widthSectionSlider.setValue(1);


        textField.setOnKeyPressed(keyEvent ->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){

                try{
                    if(Double.parseDouble(textField.getText()) < widthSectionSlider.getMin()){
                        textField.setText(String.valueOf(widthSectionSlider.getMin()));
                    }

                    widthSectionSlider.setValue(Double.parseDouble(textField.getText()));

                }catch (NumberFormatException e){
                    textField.setText(String.valueOf(widthSectionSlider.getMin()));
                    widthSectionSlider.setValue(widthSectionSlider.getMin());
                }

            }
        } );

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane


        widthSectionSlider.setMajorTickUnit(0.1);
        widthSectionSlider.setMinorTickCount(0);
        widthSectionSlider.setSnapToTicks(true);

        widthSectionSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            widthSectionSlider.setValue(Double.parseDouble(df.format(t1.doubleValue())));
        }));

        widthSectionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            if(!resetingSliders) {

                selectedCustomRectangle.setWidth(newValue.doubleValue() * SCALE);
            }
        });

        widthSection = new HBox(widthLabel, horizontalGrower(), widthSectionSlider, horizontalGrower(), textField);
        widthSection.setPadding(new Insets(10,10,10,15));
        widthSection.setAlignment(Pos.CENTER_LEFT);
        widthSection.setMinHeight(30);

        widthSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private void  setUpGetHeightSection(){
        Label heightLabel = new Label("Height:");
        heightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        heightLabel.setTextFill(Color.web("#BDBDBD"));

        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        heightSectionSlider = new Slider();
        heightSectionSlider.setMax(10);
        heightSectionSlider.setMin(0.1);
        heightSectionSlider.setValue(1);

        heightSectionSlider.setMajorTickUnit(0.1);
        heightSectionSlider.setMinorTickCount(0);
        heightSectionSlider.setSnapToTicks(true);

        textField.setOnKeyPressed(keyEvent ->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){

                try{
                    if(Double.parseDouble(textField.getText()) < heightSectionSlider.getMin()){
                        textField.setText(String.valueOf(heightSectionSlider.getMin()));
                    }

                    heightSectionSlider.setValue(Double.parseDouble(textField.getText()));

                }catch (NumberFormatException e){
                    textField.setText(String.valueOf(heightSectionSlider.getMin()));
                    heightSectionSlider.setValue(heightSectionSlider.getMin());
                }

            }
        } );


        heightSectionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);

            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            if(!resetingSliders) {
                if (newValue.doubleValue() > oldValue.doubleValue()) {
                    selectedCustomRectangle.setTranslateY(selectedCustomRectangle.getTranslateY() - Math.abs(oldValue.doubleValue() - newValue.doubleValue()) * SCALE);
                } else {
                    selectedCustomRectangle.setTranslateY(selectedCustomRectangle.getTranslateY() + (oldValue.doubleValue() - newValue.doubleValue()) * SCALE);
                }

                selectedCustomRectangle.setHeight(newValue.doubleValue() * SCALE);

                heightSectionSlider.setValue(Double.parseDouble(df.format(newValue.doubleValue())));
            }
        });

        heightSection = new HBox(heightLabel, horizontalGrower(), heightSectionSlider, horizontalGrower(), textField);
        heightSection.setPadding(new Insets(10,10,10,15));
        heightSection.setAlignment(Pos.CENTER_LEFT);
        heightSection.setMinHeight(30);

        heightSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }


    private void resetSliders(){
        resetingSliders = true;
        heightSectionSlider.adjustValue(1);
        heightSectionSlider.setValue(1);
        widthSectionSlider.setValue(1);

        translationXSlider.setValue(1);
        translationYSlider.setValue(1);

        scaleXSlider.setValue(1);
        scaleYSlider.setValue(1);
        resetingSliders = false;
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
        var isPressed = new Object() {
            Boolean value = false;
        };

        Image image = new Image(this.getClass().getResource("/icons/checkmark.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(12);

        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);

        pane.setStyle("-fx-background-color: #4F4F4F;-fx-background-radius: 7");

        pane.setOnMouseClicked(event -> {
            if(isPressed.value){
                pane.getChildren().clear();
                pane.setStyle("-fx-background-color: #4F4F4F;-fx-background-radius: 7");
            }else{
                pane.getChildren().add(imageView);
                pane.setStyle("-fx-background-color: #6FCF97; -fx-background-radius: 7");
            }

            isPressed.value = !isPressed.value;
        });

        pane.setOnMouseEntered(event -> {
            pane.setStyle("-fx-background-color: #9B9B9B;-fx-background-radius: 7");
        });

        pane.setOnMouseExited(event -> {
            if(isPressed.value){
                pane.setStyle("-fx-background-color: #6FCF97; -fx-background-radius: 7");
            }else{
                pane.setStyle("-fx-background-color: #4F4F4F;-fx-background-radius: 7");
            }
        });

        pane.setPrefSize(20,20);

        pane.setMaxWidth(20);
        pane.setMaxHeight(20);

        return pane;
    }

    private Tooltip createToolTip() {
        Tooltip thisToolTip = new Tooltip();

        String htmlStr = "<body style=\"background-color:cornsilk; "
                + "border-style: none;\"> <u><b><font color=\"red\">Click Mouse's right button to see options</font></b></u><br><br>(3) Subha Jawahar of Chennai<br> now @ Chennai<br>Female <-> Married <-> Alive<br>Period : 1800 to 2099<br>D/o Dr. Subbiah [2] - <br> <b>Spouse :</b> Jawahar Rajamanickam [7] <br><br><b>Children :</b><br><br>Rudhra Jawahar [9]<br>Mithran Jawahar [10]<br><br></body>\n";
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.loadContent(htmlStr);

        thisToolTip.setStyle("\n"
                + "    -fx-border-color: black;\n"
                + "    -fx-border-width: 1px;\n"
                + "    -fx-font: normal bold 12pt \"Times New Roman\" ;\n"
                + "    -fx-background-color: cornsilk;\n"
                + "    -fx-text-fill: black;\n"
                + "    -fx-background-radius: 4;\n"
                + "    -fx-border-radius: 4;\n"
                + "    -fx-opacity: 1.0;");

        thisToolTip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        thisToolTip.setGraphic(browser);
        thisToolTip.setAutoHide(false);
        thisToolTip.setMaxWidth(300);
        thisToolTip.setGraphicTextGap(0.0);

        System.out.println("here!");

        return thisToolTip;
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
        Label saveLabel = new Label("Save");
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

        saveHB.setOnMouseClicked(mouseEvent -> {
            saveCurrentShape();
        });

        return saveHB;
    }

    private void saveCurrentShape(){
        if(isCurrentSimple){
            CustomRectangle currentRectangle = gridCanvas.getSimpleRectangle();
            currentRectangle.redrawThumbnail();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", currentRectangle.getUuid().toString());
            jsonObject.put("basic", "true");
            jsonObject.put("color", currentRectangle.rectangle.getFill().toString());
            jsonObject.put("width", currentRectangle.getWidth());
            jsonObject.put("height", currentRectangle.getHeight());
            jsonObject.put("name", currentName.getText());

            try{
                FileWriter fileWriter = new FileWriter("C:\\Users\\Miguel\\Downloads\\objetos\\test.json");
                System.out.println("vou escrever: " + jsonObject.toJSONString());
                fileWriter.append("\n");
                fileWriter.append(jsonObject.toJSONString());
                fileWriter.flush();
                fileWriter.close();

                firstBasicShapeWasSaved.setValue(true);
                sideBarThumbnails.add(currentRectangle);
                //sideBarThumbnails.add(customRectangles.get(0).getThumbnail(() -> String.valueOf(customRectangles.indexOf(customRectangles.get(0)))));
            }catch (IOException e){
                firstBasicShapeWasSaved.setValue(false);
                e.printStackTrace();
            }

        }else{
            System.err.println("Current is not simple!");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}