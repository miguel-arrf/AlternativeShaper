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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

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
    private boolean isCurrentSimple = true;
    private TextField currentName;

    private final BooleanProperty firstBasicShapeWasSaved = new SimpleBooleanProperty(false);
    private VBox mainPanel;
    private final VBox transformersBox = new VBox();
    private final ObservableList<CustomShape> sideBarThumbnails = FXCollections.observableList(new ArrayList<>());

    private CustomShape inDragCustomShape;

    private NewCompositionShape selectedCompositionShape;
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();

    private final Orchestrator orchestrator = new Orchestrator();

    private Pane getSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefHeight(8);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);

        return rectangle;
    }


    private String getStringToPutInDrag(CustomShape customShape){
        if(customShape instanceof BasicShape){
            return String.valueOf(basicShapes.indexOf(customShape));
        }else{

            return "positions.toString();";
        }
    }

    private Supplier<CustomShape> getConsumer(CustomShape customShape){
        return () -> {
            if(customShape instanceof NewCompositionShape){
                inDragCustomShape = customShape;
                System.out.println("ISTO É ENGRAÇAAAAADO. Agora sei o que estou dragging numa variável temporária!");
            }else{
                inDragCustomShape = null;
            }
            return null;
        };
    }

    private ScrollPane getScrollPane(){
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: rgb(51,50,52); -fx-border-radius: 10");

        VBox content = new VBox(/*getBasicShape()*/);

        sideBarThumbnails.addListener((ListChangeListener<? super CustomShape>) change -> {
            while (change.next()){
                if(change.wasAdded()){

                    for (CustomShape basicShapeAdded : change.getAddedSubList()) {

                        Pane checkIfExists = basicShapeAdded.getThumbnail(() -> getStringToPutInDrag(basicShapeAdded), getConsumer(basicShapeAdded));
                        content.getChildren().remove(checkIfExists);//This only removes if it exists
                        content.getChildren().add(checkIfExists);

                        if(basicShapeAdded instanceof NewCompositionShape){
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a composition shape thumbnail!");
                                selectedCompositionShape = (NewCompositionShape) basicShapeAdded;

                                gridCanvas.clearEverything(false);
                                //TODO aqui está a true, mas em algum momento não será...
                                isCurrentSimple = false;
                                currentName.setText(basicShapeAdded.getShapeName());

                                //addShape(basicShapeAdded, true);

                            });
                        }else{
                            //It is BasicShape
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a basic shape thumbnail!");
                                selectedBasicShape = (BasicShape) basicShapeAdded;

                                gridCanvas.clearEverything(true);
                                //TODO aqui está a true, mas em algum momento não será...
                                isCurrentSimple = true;
                                currentName.setText(basicShapeAdded.getShapeName());

                                //mainPanel.getChildren().removeAll(scaleXSection, scaleYSection, translationXSection, translationYSection);

                                addShape(basicShapeAdded);

                            });
                        }



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

    public void addBasicAndComplexButtons(){

        Image basicPlus = new Image(Objects.requireNonNull(App.class.getResource("/icons/plus.png")).toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(12);

        Image complexPlus = new Image(Objects.requireNonNull(App.class.getResource("/icons/plus.png")).toExternalForm());
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

            addShape(new BasicShape(SCALE, SCALE, Color.web("#55efc4")));

            selectedCompositionShape = null;
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

            selectedCompositionShape = new NewCompositionShape(orchestrator);
            newCompositionShapes.add(selectedCompositionShape);

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


    }



    private void addShape(CustomShape basicShapeToAdd){
            if(basicShapeToAdd instanceof BasicShape){
                BasicShape tempBasicShape = (BasicShape) basicShapeToAdd;

                gridCanvas.addShape(tempBasicShape);
                if(!basicShapes.contains(tempBasicShape)){
                        basicShapes.add(tempBasicShape);
                }
                selectedBasicShape = tempBasicShape;
                selectedBasicShape.turnOnStroke();
                selectedBasicShape.toogleSelected();

                basicShapes.stream().filter(r -> r != tempBasicShape).forEach(BasicShape::turnOffStroke);
                transformersBox.getChildren().clear();

                if(isCurrentSimple){
                    transformersBox.getChildren().addAll(tempBasicShape.getWidthSection(), tempBasicShape.getHeightSection());
                }
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

        addShape(new BasicShape(SCALE, SCALE, Color.web("#55efc4")));


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

            if(i == 0){
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

        pane.setOnMouseEntered(event -> scene.setCursor(Cursor.CLOSED_HAND));

        pane.setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT));

        pane.setOnDragEntered(event -> {
            if (event.getGestureSource() != pane &&
                    event.getDragboard().hasString()) {
                System.out.println("estou dragging: " + event.getDragboard().getString());
            }
            event.consume();
        });

        pane.setOnMouseClicked(event -> basicShapes.forEach(rectangle -> {
            Point2D transformation = rectangle.localToScene(rectangle.getX(), rectangle.getY()).add(new Point2D(rectangle.getWidth()    , 0));

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
        }));

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
                //System.out.println("event x: " + event.getX() + ", y: " + event.getY());

                if(!isCurrentSimple){

                    if(!db.getString().contains("[")){
                        System.out.println("I was dropped a simple shape");

                        addShape(selectedCompositionShape.addBasicShape(basicShapes.get(Integer.parseInt(db.getString())).getUUID().toString()));
                    }

                }

                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });


        pane.setOnMouseMoved(event -> basicShapes.forEach(rectangle -> {
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
        }));

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



    public static HBox horizontalGrower(){
        var horizontalGrower = new HBox();

        HBox.setHgrow(horizontalGrower, Priority.ALWAYS);
        horizontalGrower.setMaxHeight(Double.MAX_VALUE);

        return horizontalGrower;
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

        saveHB.setOnMouseClicked(mouseEvent -> saveCurrentShape());

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
                FileWriter fileWriter = new FileWriter("C:\\Users\\mferr\\Downloads\\objetos\\test.json");
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

            orchestrator.addAllBasicShapes(basicShapes);


        }
    }

    public static void main(String[] args) {
        launch();
    }
}