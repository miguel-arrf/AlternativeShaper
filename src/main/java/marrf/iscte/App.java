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
import java.util.ArrayList;
import java.util.Objects;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final int SCALE = 40;
    public static final int NUMBER_COLUMNS_AND_ROWS = 40;

    private final StackPane centerCustomRectangle = new StackPane();

    private Scene scene;

    private final GridCanvas gridCanvas = new GridCanvas();

    private final Pane sceneStackPane = getGraphSection();

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private BasicShape selectedBasicShape;
    private CompositionShape selectedCompositionShape;

    private boolean isCurrentSimple = true;
    private TextField currentName;

    private final BooleanProperty firstBasicShapeWasSaved = new SimpleBooleanProperty(false);
    private VBox mainPanel;
    private final VBox transformersBox = new VBox();
    private final ObservableList<CustomShape> sideBarThumbnails = FXCollections.observableList(new ArrayList<>());


    public BasicShape getDraggableCustomRectangle(){

        BasicShape basicShape = new BasicShape(20,20);
        basicShape.setFill(Color.rgb(0,0,255,1));

        basicShapes.add(basicShape);

        basicShape.setOnDragDetected(event -> {
            Dragboard db = basicShape.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(basicShapes.indexOf(basicShape)));
            db.setContent(content);

            event.consume();
        });

        System.out.println("AAAAAHHHHH width: " + basicShape.getWidth());
        return basicShape;
    }

    public BasicShape getCopyWithBindWidthAndHeightFrom(int position){
        BasicShape toCopyFrom = basicShapes.get(position);
        System.out.println("to copy from: width:" + toCopyFrom.getWidth() + ", height: " + toCopyFrom.getHeight());

        BasicShape basicShape = new BasicShape();
        basicShape.setFill(toCopyFrom.getFill());
        basicShape.setWidth(toCopyFrom.getWidth());
        basicShape.setHeight(toCopyFrom.getHeight());
        basicShape.widthProperty().bind(toCopyFrom.widthProperty().multiply(toCopyFrom.scaleXProperty()));
        basicShape.heightProperty().bind(toCopyFrom.heightProperty().multiply(toCopyFrom.scaleYProperty()));
        basicShape.fillProperty().bind(toCopyFrom.fillProperty());

        return basicShape;
    }

    private Pane getSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefHeight(8);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);

        return rectangle;
    }

    private Pane getBasicShape(){
        BasicShape basicShape = new BasicShape(100,100);
        basicShape.getRectangle();

        basicShape.setFill(Color.rgb(0,255,255,1));
        basicShapes.add(basicShape);

        return basicShape.getThumbnail(() -> String.valueOf(basicShapes.indexOf(basicShape)));

    }

    private ScrollPane getScrollPane(){
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: rgb(51,50,52); -fx-border-radius: 10");

        VBox content = new VBox(/*getBasicShape()*/);



        sideBarThumbnails.addListener((ListChangeListener<? super CustomShape>) change -> {
            while (change.next()){
                if(change.wasAdded()){

                    for (CustomShape basicShapeAdded : change.getAddedSubList()) {
                        Pane checkIfExists = basicShapeAdded.getThumbnail(() -> String.valueOf(basicShapes.indexOf(basicShapeAdded)));
                        content.getChildren().remove(checkIfExists);//This only removes if it exists
                        content.getChildren().add(checkIfExists);

                        checkIfExists.setOnMouseClicked(mouseEvent -> {
                            System.out.println("I've clicked on a thumbnail!");
                            gridCanvas.clearEverything(true);
                            //TODO aqui está a true, mas em algum momento não será...
                            isCurrentSimple = true;

                            //mainPanel.getChildren().removeAll(scaleXSection, scaleYSection, translationXSection, translationYSection);

                            addShape(basicShapeAdded, true);

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

            transformersBox.getChildren().clear();

            addShape(new BasicShape(SCALE, SCALE, true, Color.web("#55efc4")), true);

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

            transformersBox.getChildren().clear();
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

    private void addShape(CustomShape basicShapeToAdd){
        addShape(basicShapeToAdd, false);
    }

    private void addShape(CustomShape basicShapeToAdd, boolean selected){
            BasicShape tempBasicShape = (BasicShape) basicShapeToAdd;

            gridCanvas.addShape(tempBasicShape);
            if(!basicShapes.contains(tempBasicShape)){
                basicShapes.add(tempBasicShape);
            }
            if(selected){
                selectedBasicShape = tempBasicShape;
                selectedBasicShape.turnOnStroke();
                selectedBasicShape.toogleSelected();

                basicShapes.stream().filter(r -> r != tempBasicShape).forEach(BasicShape::turnOffStroke);

            }
            transformersBox.getChildren().clear();

            if(isCurrentSimple){
                transformersBox.getChildren().addAll(tempBasicShape.getWidthSection(), tempBasicShape.getHeightSection());
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

        mainPanel.getChildren().addAll(getScrollPane(), getNameSection(),transformersBox, getSaveButtonSection());

        transformersBox.setSpacing(10);

        var scenePanel = new VBox(mainPanel);
        scenePanel.setStyle("-fx-background-color: black");
        scenePanel.setAlignment(Pos.CENTER);
        scenePanel.setPadding(new Insets(20));


        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #262528");
        borderPane.setPadding(new Insets(10));

        borderPane.setRight(mainPanel);
        borderPane.setCenter(sceneStackPane);

        addShape(new BasicShape(SCALE, SCALE, true, Color.web("#55efc4")), true);


        return borderPane;
    }

    private void finishSetup(){
        addBasicAndComplexButtons();

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
            basicShapes.forEach(rectangle -> {
                Point2D transformation = rectangle.localToScene(rectangle.getX(), rectangle.getY()).add(new Point2D(rectangle.getWidth(), 0));

                if(transformation.getX() - event.getSceneX() >= 0 && transformation.getX() - event.getSceneX() <= rectangle.getWidth()) {
                    if (transformation.getY() - event.getSceneY() >= 0 && transformation.getY() - event.getSceneY() <= rectangle.getHeight()) {
                        System.out.println("cliquei numa shape!");
                        selectedBasicShape = rectangle;
                        selectedBasicShape.turnOnStroke();
                        selectedBasicShape.toogleSelected();

                        basicShapes.stream().filter(r -> r != rectangle).forEach(BasicShape::turnOffStroke);
                        basicShapes.stream().filter(r -> r != rectangle).forEach(BasicShape::toogleOffSelection);

                        if(!isCurrentSimple){
                            transformersBox.getChildren().clear();
                            transformersBox.getChildren().addAll(rectangle.getScaleXSection(), rectangle.getScaleYSection(), rectangle.getTranslationXSection(), rectangle.getTranslationYSection());
                        }

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
                    BasicShape basicShapeToAdd = getCopyWithBindWidthAndHeightFrom(Integer.parseInt(db.getString()));
                    addShape(basicShapeToAdd);
                    basicShapeToAdd.toogleOffSelection();
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
            basicShapes.forEach(rectangle -> {
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

    public static HBox horizontalGrower(){
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
            BasicShape currentRectangle = gridCanvas.getSimpleRectangle();
            currentRectangle.setShapeName(currentName.getText());
            currentRectangle.redrawThumbnail();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", currentRectangle.getUUID().toString());
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
            }catch (IOException e){
                firstBasicShapeWasSaved.setValue(false);
                e.printStackTrace();
            }

        }else{
            System.err.println("Current is not simple!");
            ArrayList<BasicShape> basicShapes = gridCanvas.getSimpleRectangles();

            CompositionShape compositionShape = new CompositionShape(basicShapes, currentName.getText());
            compositionShape.redrawThumbnail();


        }
    }

    public static void main(String[] args) {
        launch();
    }
}