package marrf.iscte;

import javafx.application.Application;
import javafx.beans.NamedArg;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
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
import javafx.stage.WindowEvent;
import marrf.iscte.ShapeRules.ShapeRule;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final int SCALE = 40;
    public static final int NUMBER_COLUMNS_AND_ROWS = 40;

    private Scene scene;
    private final GridCanvas gridCanvas = new GridCanvas();
    private final Pane sceneStackPane = getGraphSection();

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private final ArrayList<BasicShape> basicShapesToSave = new ArrayList<>();

    private BasicShape selectedBasicShape;
    private boolean isCurrentSimple = true;
    private TextField currentName;
    private Pane nameSection;
    private Pane saveSection;

    private final BooleanProperty firstBasicShapeWasSaved = new SimpleBooleanProperty(false);
    private VBox mainPanel;
    private final VBox transformersBox = new VBox();
    private final ObservableList<CustomShape> sideBarThumbnails = FXCollections.observableList(new ArrayList<>());

    private CustomShape inDragCustomShape;

    private NewCompositionShape selectedCompositionShape;
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();

    private ArrayList<ShapeRule> shapeRuleArrayList = new ArrayList<>();

    private final Orchestrator orchestrator = new Orchestrator();

    private Pane getSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefHeight(8);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);
        rectangle.setId("separator");

        return rectangle;
    }


    private String getStringToPutInDrag(CustomShape customShape){
        if(customShape instanceof BasicShape){
            return String.valueOf(basicShapes.indexOf(customShape));
        }else{
            return "[";
            //return "positions.toString();";
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
        scrollPane.setStyle("-fx-background: rgb(51,50,52); -fx-background-radius: 10");
        scrollPane.setStyle("-fx-background-color: rgb(51,50,52); -fx-background-radius: 10; -fx-background: transparent");

        VBox content = new VBox(/*getBasicShape()*/);

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Basic Shapes",
                        "Composition Shapes",
                        "All Shapes"
                );
        final ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.setPrefHeight(40);
        comboBox.setFocusTraversable(false);

        comboBox.setMaxWidth(Double.MAX_VALUE);

        comboBox.valueProperty().addListener((observableValue, o, t1) -> {

            if(t1.toString().equals("Basic Shapes")){
                addBasicShapesToSideBarIfTheyArent();

                sideBarThumbnails.removeIf(p -> {
                    if(p instanceof BasicShape){
                        return false;
                    }
                    return true;
                });
            }else if(t1.toString().equals("Composition Shapes")){
                addCompositionShapesToSideBarIfTheyArent();

                sideBarThumbnails.removeIf(p -> {
                    if(p instanceof NewCompositionShape){
                        return false;
                    }
                    return true;
                });
            }else{
                System.out.println("No basic or composition");
                addBasicShapesToSideBarIfTheyArent();
                addCompositionShapesToSideBarIfTheyArent();
            }
        });

        comboBox.getSelectionModel().selectLast();

        content.getChildren().add(comboBox);

        scrollPane.setFitToWidth(true);

        sideBarThumbnails.addListener((ListChangeListener<? super CustomShape>) change -> {
            while (change.next()){

                if(change.wasRemoved()){
                    for(CustomShape basicShapeRemoved: change.getRemoved()){
                        Pane checkIfExists = basicShapeRemoved.getThumbnail(() -> getStringToPutInDrag(basicShapeRemoved), getConsumer(basicShapeRemoved));
                        content.getChildren().remove(checkIfExists);//This only removes if it exists
                    }
                }

                if(change.wasAdded()){
                    for (CustomShape basicShapeAdded : change.getAddedSubList()) {
                        Pane checkIfExists = basicShapeAdded.getThumbnail(() -> getStringToPutInDrag(basicShapeAdded), getConsumer(basicShapeAdded));
                        content.getChildren().remove(checkIfExists);//This only removes if it exists
                        content.getChildren().add(checkIfExists);

                        if(basicShapeAdded instanceof NewCompositionShape){
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a composition shape thumbnail!");
                                selectedCompositionShape = (NewCompositionShape) basicShapeAdded;

                                transformersBox.getChildren().clear();
                                GridCanvas.clearEverything();
                                //TODO aqui está a true, mas em algum momento não será...
                                isCurrentSimple = false;
                                currentName.setText(basicShapeAdded.getShapeName());

                                addCompositionShape(selectedCompositionShape, false);
                                //addShape(basicShapeAdded, true);
                            });
                        }else{
                            //It is BasicShape
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a basic shape thumbnail!");
                                selectedBasicShape = (BasicShape) basicShapeAdded;

                                transformersBox.getChildren().clear();
                                GridCanvas.clearEverything();
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

    private void addBasicShapesToSideBarIfTheyArent(){
        basicShapesToSave.forEach(p -> {
            if(sideBarThumbnails.stream().noneMatch(s -> s.getUUID().equals(p.getUUID()))){
                sideBarThumbnails.add(p);
            }
        });
    }


    private void addCompositionShapesToSideBarIfTheyArent(){
        newCompositionShapes.forEach(p -> {
            if(sideBarThumbnails.stream().noneMatch(s -> s.getUUID().equals(p.getUUID()))){
                sideBarThumbnails.add(p);
            }
        });
    }


    private void addCompositionShape(NewCompositionShape compositionShape, boolean wasDragged){
        if(wasDragged){
            gridCanvas.addGroup(selectedCompositionShape.addNewCompositionShape(compositionShape));

        }else {
            //I've clicked on a thumbnail
            compositionShape.getBasicShapes().forEach(this::addShape);
            Pane toAdd = new Pane();
            compositionShape.getTeste(toAdd, true, 0,0);
            System.out.println("tamanho de basic shapes: " + compositionShape.getBasicShapes().size());

            gridCanvas.addGroup(toAdd);
        }
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


        Label basicShape = new Label("Add Basic Shape");
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
            GridCanvas.clearEverything();
            isCurrentSimple = true;

            addNameSectionIfItIsNot();
            addSaveSectionIfItIsNot();

            currentName.setText("simpleDefault");

            transformersBox.getChildren().clear();

            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc5"), getProceedWhenDeleting());
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
            selectedCompositionShape = null;
        });


        Label complexShape = new Label("Add Complex Shape");
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
            gridCanvas.clearEverything();
            isCurrentSimple = false;
            currentName.setText("complexDefault");

            transformersBox.getChildren().clear();
            selectedCompositionShape = new NewCompositionShape(orchestrator, transformersBox, teste1 -> {
                return null;
            }, teste -> {
                System.err.println("oh, here I am!");
                newCompositionShapes.forEach(NewCompositionShape::redrawThumbnail);
                return null;
            });
            newCompositionShapes.add(selectedCompositionShape);
        });

        HBox saveHB = new HBox(basicShapeVBox, complexShapeVBox);
        saveHB.setSpacing(20);
        saveHB.setAlignment(Pos.CENTER);
        saveHB.setMaxHeight(50);
        saveHB.setPrefHeight(50);

        HBox shapeAndProcessBox = new HBox(getShapeRuleButton(), getProcessButton());
        shapeAndProcessBox.setSpacing(20);

        VBox buttons = new VBox(saveHB, shapeAndProcessBox);
        buttons.setSpacing(15);
        buttons.setAlignment(Pos.CENTER);

        Pane toAdd = getSeparator();

        firstBasicShapeWasSaved.addListener((observableValue, aBoolean, t1) -> {
            if(t1){
                for(int i = 0; i < mainPanel.getChildren().size(); i++){
                    Node node = mainPanel.getChildren().get(i);
                    if(node.getId() != null && node.getId().equals("saveButton")){
                        mainPanel.getChildren().add(i, toAdd);
                        mainPanel.getChildren().add(i+1, buttons);
                        break;
                    }
                }
            }else{
                mainPanel.getChildren().remove(toAdd);
                mainPanel.getChildren().remove(saveHB);
            }
        });


    }

    private HBox getButtonWith_Label_Color_Image(@NamedArg("label") String label, @NamedArg("background color") String backgroundColor,@NamedArg("label color") String labelColor, @NamedArg("image name") String imageLocation){
        Image complexPlus = new Image(Objects.requireNonNull(App.class.getResource("/icons/" + imageLocation)).toExternalForm());
        ImageView complexPlusImageView = new ImageView(complexPlus);
        complexPlusImageView.setSmooth(true);
        complexPlusImageView.setPreserveRatio(true);
        complexPlusImageView.setFitWidth(12);

        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexPlusImageView, complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: 20");
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }


    private Pane getProcessButton(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Processes editor", "#355C65", "#56CDF2", "process.png");

        complexShapeHBox.setMaxHeight(50);
        complexShapeHBox.setPrefHeight(50);

        complexShapeHBox.setOnMouseClicked(event -> {
            ProcessesEditor processesEditor = new ProcessesEditor(scene, newCompositionShapes, basicShapesToSave, orchestrator);
            processesEditor.openPopup();
        });

        return complexShapeHBox;
    }

    private Pane getShapeRuleButton(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Shape rules editor", "#472953", "#ff8ad8", "icons8-rules-96.png");

        complexShapeHBox.setMaxHeight(50);
        complexShapeHBox.setPrefHeight(50);

        complexShapeHBox.setOnMouseClicked(event -> {
            /*ShapeRuleEditor shapeRuleEditor = new ShapeRuleEditor(scene, newCompositionShapes, basicShapesToSave, orchestrator);
            shapeRuleEditor.openPopup();*/

            NewShapeRuleEditor newShapeRuleEditor = new NewShapeRuleEditor(scene, newCompositionShapes, basicShapesToSave, orchestrator, shapeRuleArrayList);
            newShapeRuleEditor.openPopup();

        });

        return complexShapeHBox;
    }

    private void addShape(CustomShape basicShapeToAdd){
            if(basicShapeToAdd instanceof BasicShape){
                BasicShape tempBasicShape = (BasicShape) basicShapeToAdd;

                GridCanvas.addShape(tempBasicShape);
                if(!basicShapes.contains(tempBasicShape)){
                        basicShapes.add(tempBasicShape);
                }
                selectedBasicShape = tempBasicShape;
                selectedBasicShape.turnOnStroke();
                selectedBasicShape.toogleSelected();

                basicShapes.stream().filter(r -> r != tempBasicShape).forEach(BasicShape::turnOffStroke);
                transformersBox.getChildren().clear();

                if(isCurrentSimple){
                    transformersBox.getChildren().addAll(tempBasicShape.getWidthSection(), tempBasicShape.getHeightSection(), tempBasicShape.getColorSection());
                }else{
                    transformersBox.getChildren().addAll(tempBasicShape.getTranslationXSection(), tempBasicShape.getTranslationYSection());
                }
            }

    }

    public Pane getScenePanelWithLoadedFile(Scene scene, File file){
        finishSetup();

        this.scene = scene;

        mainPanel = new VBox();
        mainPanel.setMaxWidth(400);
        mainPanel.setPrefSize(400, 700);
        mainPanel.setStyle("-fx-background-color: #262528;");
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setPadding(new Insets(0,0,0,20));
        mainPanel.setSpacing(15);

        mainPanel.getChildren().addAll(getScrollPane(), getNameSection(),transformersBox, getSaveButtonSection());

        transformersBox.setSpacing(20);

        var scenePanel = new VBox(mainPanel);
        scenePanel.setStyle("-fx-background-color: black");
        scenePanel.setAlignment(Pos.CENTER);
        scenePanel.setPadding(new Insets(20));


        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #262528");
        borderPane.setPadding(new Insets(20));

        borderPane.setRight(mainPanel);
        borderPane.setCenter(sceneStackPane);



        sceneStackPane.layout();


        return borderPane;
    }

    public void loadNewCompositionShapes(File file){
        ArrayList<NewCompositionShape> newCompositionShapeArrayList = orchestrator.getNewCompositionShapesFromFile(file, transformersBox);
        newCompositionShapes.addAll(newCompositionShapeArrayList);
        sideBarThumbnails.addAll(newCompositionShapeArrayList);
    }

    public void loadBasicShapes(File file){
        ArrayList<BasicShape> basicShapes = orchestrator.getBasicShapesFromFile(file);

        basicShapesToSave.addAll(basicShapes);

        //If the file is empty, or with a bad format, then we won't have a zero positioned element.
        //In that case we want to add a new basic shape...
        //TODO Fix this.

        if(basicShapes.size() > 0){
            currentName.setText(basicShapes.get(0).getShapeName());
            addShape(basicShapes.get(0));
            sideBarThumbnails.addAll(basicShapes);
            firstBasicShapeWasSaved.setValue(true);
        }else{
            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc4"));
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
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
        mainPanel.setPadding(new Insets(0,0,0,20));
        mainPanel.setSpacing(15);

        mainPanel.getChildren().addAll(getScrollPane(), getNameSection(),transformersBox, getSaveButtonSection());

        transformersBox.setSpacing(20);

        var scenePanel = new VBox(mainPanel);
        scenePanel.setStyle("-fx-background-color: black");
        scenePanel.setAlignment(Pos.CENTER);
        scenePanel.setPadding(new Insets(20));


        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #262528");
        borderPane.setPadding(new Insets(20));

        borderPane.setRight(mainPanel);
        borderPane.setCenter(sceneStackPane);


        BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc4"), getProceedWhenDeleting());
        addShape(toAdd);
        basicShapesToSave.add(toAdd);

        return borderPane;
    }

    private void addNameSectionIfItIsNot(){
        if (!mainPanel.getChildren().contains(nameSection)) {
            for(int i = 0;i < mainPanel.getChildren().size(); i++ ){
                Node node = mainPanel.getChildren().get(i);
                if(node instanceof ScrollPane){
                    mainPanel.getChildren().add(i+1,nameSection);
                    break;
                }
            }
        }
    }

    private void addSaveSectionIfItIsNot(){
        if (!mainPanel.getChildren().contains(saveSection)) {
            mainPanel.getChildren().add(mainPanel.getChildren().size(), saveSection);

        }
    }

    private void deleteBasicShape(String uuidToRemove){
        //We are cleaning everything, but if the user is on a composition shape, we shouldn't... this way is easier tho.
        BasicShape temp = basicShapesToSave.stream().filter(p -> p.getUUID().toString().equals(uuidToRemove)).findFirst().get();
        basicShapesToSave.remove(temp);
        basicShapes.remove(temp);
        GridCanvas.clearEverything();
        transformersBox.getChildren().clear();
        sideBarThumbnails.remove(temp);

        if(basicShapesToSave.size() == 0){
            //mainPanel.getChildren().remove(nameSection);
            //mainPanel.getChildren().remove(saveSection);
            //Ads a basic shape when we delete the last one!
            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc4"), getProceedWhenDeleting());
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
            currentName.setText("defaultName");
            sideBarThumbnails.add(toAdd);
        }else{
            currentName.setText(basicShapesToSave.get(0).getShapeName());
            addShape(basicShapesToSave.get(0));
        }

    }

    private void deleteBasicShapeFromCompositionShape(String uuidToRemove){
        newCompositionShapes.forEach(newCompositionShape -> {
            newCompositionShape.deleteBasicShape(uuidToRemove);
            //Maybe an uncessary redraw?
            newCompositionShape.redrawThumbnail();
        });
    }

    private Function<String, Double> getProceedWhenDeleting(){
        return uuidToRemove -> {

            if(!orchestrator.canBasicShapeBeRemoved(uuidToRemove)){
                System.err.println("Can't be removed without impacting other composition shapes!");

               PopupWindow popupWindow = new PopupWindow();

               Stage tempStage = popupWindow.getStage();

               Pane acceptButton = PopupWindow.getButton("Accept, and delete this shape from compositions shapes that use this one.", "null",  "#807229", "#E7CE4A", event -> {
                   deleteBasicShape(uuidToRemove);
                   deleteBasicShapeFromCompositionShape(uuidToRemove);
                   tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
               });
               Pane refuseButton = PopupWindow.getButton("Keep this shape.", "null",  "#5E2323", "#EB5757", event -> {
                   tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
               });

                popupWindow.createPopup("Delete confirmation",scene,"Deleting this basic shape will also delete compositionShapes that use it. Do you want to proceed?", acceptButton, refuseButton);

            }else{

               deleteBasicShape(uuidToRemove);

            }

            return null;
        };
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
        pane.setPickOnBounds(false);

        pane.setAlignment(Pos.CENTER);
        pane.setMinHeight(300);
        pane.setMinWidth(300);

        VBox.setVgrow(pane, Priority.ALWAYS);
        pane.setStyle("-fx-background-color: #333234; -fx-background-radius: 20");

        Pane grid = gridCanvas.getGrid(pane);
        grid.setClip(getCustomRectangleClip(pane));
        pane.getChildren().add(grid);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPickOnBounds(false);

        pane.getChildren().add(anchorPane);
        getAnchorPaneClip(anchorPane, "#262528");

        pane.setOnMouseEntered(event -> scene.setCursor(Cursor.CLOSED_HAND));

        pane.setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT));

        pane.setOnDragEntered(event -> {
            if (event.getGestureSource() != pane &&
                    event.getDragboard().hasString()) {
                System.out.println("estou dragging: " + event.getDragboard().getString());
            }
        });

        pane.setOnMouseClicked(event -> basicShapes.forEach(rectangle -> {
            //System.out.println("basicShapes : " + basicShapes.size());

            Point2D transformation = rectangle.localToScene(rectangle.getX(), rectangle.getY()).add(new Point2D(rectangle.getWidth() , 0));


            if(transformation.getX() - event.getSceneX() >= 0 && transformation.getX() - event.getSceneX() <= rectangle.getWidth() ) {
                if(transformation.getY() - event.getSceneY() >= 0 && transformation.getY() - event.getSceneY() <= rectangle.getHeight() ){
                    System.out.println("cliquei numa shape!");
                    selectedBasicShape = rectangle;
                    selectedBasicShape.turnOnStroke();
                    selectedBasicShape.toogleSelected();

                    basicShapes.stream().filter(r -> r != rectangle).forEach(BasicShape::turnOffStroke);
                    basicShapes.stream().filter(r -> r != rectangle).forEach(BasicShape::toogleOffSelection);

                    if(!isCurrentSimple){
                        transformersBox.getChildren().clear();
                        transformersBox.getChildren().addAll( rectangle.getTranslationXSection(), rectangle.getTranslationYSection());
                    }

                }
            }
        }));

        pane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes( TransferMode.ANY );
            }
        });

        pane.setOnDragDropped(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            System.out.println("oi");

            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                //System.out.println("this was dropped: " + db.getString());

                if(!isCurrentSimple){

                    if(!db.getString().contains("[")){
                        System.out.println("I was dropped a simple shape");
                        System.out.println("i was dropped: " + db.getString());
                        addShape(selectedCompositionShape.addBasicShape(basicShapes.get(Integer.parseInt(db.getString())).getUUID().toString()));
                    }else{
                        System.out.println("I was dropped a composition shape");
                        addCompositionShape((NewCompositionShape) inDragCustomShape, true);
                    }

                }

                success = true;
            }
            event.setDropCompleted(success);

        });


        pane.setOnMouseMoved(event -> basicShapes.forEach(rectangle -> {
            Point2D transformation = rectangle.localToScene(rectangle.getX(), rectangle.getY()).add(new Point2D(rectangle.getWidth() , 0));


            if(transformation.getX() - event.getSceneX() >= 0 && transformation.getX() - event.getSceneX() <= rectangle.getWidth() ) {
                if(transformation.getY() - event.getSceneY() >= 0 && transformation.getY() - event.getSceneY() <= rectangle.getHeight() ){
                    rectangle.turnOnStroke();
                }else{
                    rectangle.turnOffStrokeIfNotSelected();
                    if(!rectangle.isSelected()){
                        if(transformersBox.getChildren().contains(rectangle.getTranslationXSection())){
                            transformersBox.getChildren().clear();
                        }
                    }

                }
            }else{
                rectangle.turnOffStrokeIfNotSelected();
                if(!rectangle.isSelected()){
                    if(transformersBox.getChildren().contains(rectangle.getTranslationXSection())){
                        transformersBox.getChildren().clear();
                    }
                }
            }

        }));

        return pane;
    }

    public static Shape getTopLeftArc(){
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

    public static void getAnchorPaneClip(AnchorPane anchorPane, String color){
        Shape topLeft = getTopLeftArc();
        topLeft.setFill(Color.web(color));
        topLeft.setTranslateX(-20);
        topLeft.setTranslateY(0);

        Shape bottomLeft = getTopLeftArc();
        bottomLeft.setRotate(-90);
        bottomLeft.setTranslateX(-20);
        bottomLeft.setFill(Color.web(color));

        Shape topRight = getTopLeftArc();
        topRight.setRotate(90);
        topRight.setFill(Color.web(color));

        Shape bottomRight = getTopLeftArc();
        bottomRight.setRotate(180);
        bottomRight.setFill(Color.web(color));

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

    }

    public static Shape getCustomRectangleClip(Pane parent){
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

        this.nameSection = nameHB;

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

        this.saveSection = saveHB;
        saveHB.setId("saveButton");

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
            jsonObject.put("color", currentRectangle.getFill().toString());
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

            orchestrator.addAllBasicShapes(basicShapesToSave);

        }else{
            selectedCompositionShape.setShapeName(currentName.getText());
            sideBarThumbnails.add(selectedCompositionShape);
            orchestrator.addAllCompositionShapes(newCompositionShapes);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}