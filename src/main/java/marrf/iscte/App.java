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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
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
import marrf.iscte.JavaAndProlog.DesignToProlog;
import marrf.iscte.ShapeRules.ShapeRule;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
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

    public static Scene scene;
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
    private VBox sectionToKeep;
    private HBox topToolbar;
    private final VBox transformersBox = new VBox();
    private final ObservableList<CustomShape> sideBarThumbnails = FXCollections.observableList(new ArrayList<>());

    public static CustomShape inDragCustomShape;

    private NewCompositionShape selectedCompositionShape;
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();

    private ParametricCompositionShape selectedParametricCompositionShape;
    private final ArrayList<ParametricCompositionShape> newParametricCompositionShapes = new ArrayList<>();

    private final ArrayList<Power> powerArrayList = new ArrayList<>();
    private Power selectedPower;

    private final ArrayList<ShapeRule> shapeRuleArrayList = new ArrayList<>();

    private final Orchestrator orchestrator = new Orchestrator();

    public static Pane getSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefHeight(8);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);
        rectangle.setId("separator");

        rectangle.setMinHeight(6);

        return rectangle;
    }


    private String getStringToPutInDrag(CustomShape customShape){
        if(customShape instanceof BasicShape){
            inDragCustomShape = customShape;

            return String.valueOf(basicShapes.indexOf(customShape));
        }else if(customShape instanceof NewCompositionShape){
            inDragCustomShape = customShape;

            return "[" + customShape.getShapeName();
            //return "positions.toString();";
        }else if(customShape instanceof ParametricCompositionShape){
            inDragCustomShape = customShape;

            return "[" + customShape.getShapeName();
        }else if(customShape instanceof Power){
            inDragCustomShape = customShape;
            return String.valueOf(powerArrayList.indexOf(customShape));
        }else{
            inDragCustomShape = customShape;
            return "getStringToPutInDrag received something OTHER than BasicShape or NewCompositionShape";
        }
    }

    private Supplier<CustomShape> getConsumer(CustomShape customShape){
        return () -> {
            if(customShape instanceof NewCompositionShape){
                inDragCustomShape = customShape;
                System.out.println("Agora sei o que estou dragging numa variável temporária!");
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

            if(t1.equals("Basic Shapes")){
                addBasicShapesToSideBarIfTheyArent();

                sideBarThumbnails.removeIf(p -> {
                    if(p instanceof BasicShape){
                        return false;
                    }
                    return true;
                });
            }else if(t1.equals("Composition Shapes")){
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
                                sectionToKeep.getChildren().clear();
                                GridCanvas.clearEverything();
                                //TODO aqui está a true, mas em algum momento não será...
                                currentName.setText(basicShapeAdded.getShapeName());

                                isCurrentSimple = false;
                                selectedPower = null;
                                selectedParametricCompositionShape = null;

                                addCompositionShape(selectedCompositionShape, false);
                                //addShape(basicShapeAdded, true);


                            });

                        }else if (basicShapeAdded instanceof ParametricCompositionShape) {
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a parametric composition shape thumbnail!");
                                selectedParametricCompositionShape = (ParametricCompositionShape) basicShapeAdded;

                                transformersBox.getChildren().clear();
                                sectionToKeep.getChildren().clear();
                                sectionToKeep.getChildren().add(selectedParametricCompositionShape.variablesPane);
                                //transformersBox.getChildren().add(selectedParametricCompositionShape.variablesPane);

                                GridCanvas.clearEverything();
                                //TODO aqui está a true, mas em algum momento não será...
                                currentName.setText(basicShapeAdded.getShapeName());

                                isCurrentSimple = false;


                                addParametricCompositionShape(selectedParametricCompositionShape);
                                //addShape(basicShapeAdded, true);

                                selectedPower = null;
                                selectedCompositionShape = null;


                            });
                        } else if(basicShapeAdded instanceof BasicShape){
                            //It is BasicShape
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a basic shape thumbnail!");
                                selectedBasicShape = (BasicShape) basicShapeAdded;

                                transformersBox.getChildren().clear();
                                sectionToKeep.getChildren().clear();
                                GridCanvas.clearEverything();
                                //TODO aqui está a true, mas em algum momento não será...
                                isCurrentSimple = true;
                                currentName.setText(basicShapeAdded.getShapeName());

                                //mainPanel.getChildren().removeAll(scaleXSection, scaleYSection, translationXSection, translationYSection);

                                addShape(basicShapeAdded);

                                selectedPower = null;
                                selectedCompositionShape = null;
                                selectedParametricCompositionShape = null;

                            });
                        }else if(basicShapeAdded instanceof Power){
                            checkIfExists.setOnMouseClicked(mouseEvent -> {
                                System.out.println("I've clicked on a POWER shape thumbnail!");
                                selectedPower = (Power) basicShapeAdded;

                                transformersBox.getChildren().clear();
                                sectionToKeep.getChildren().clear();
                                GridCanvas.clearEverything();
                                //TODO aqui está a true, mas em algum momento não será...
                                isCurrentSimple = false;
                                currentName.setText(basicShapeAdded.getShapeName());

                                addPowerShape(selectedPower);

                                transformersBox.getChildren().add(basicShapeAdded.getTranslationXSection());

                                isCurrentSimple = false;
                                selectedCompositionShape = null;
                                selectedParametricCompositionShape = null;

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

    private void addParametricCompositionShape(ParametricCompositionShape compositionShape){

            //I've clicked on a thumbnail
        compositionShape.getBasicShapes().forEach(this::addShape);
        compositionShape.getPowerShapes().forEach(this::addPowerShape);

        Pane toAdd = new Pane();
            compositionShape.getTeste(toAdd, true, 0,0);

            GridCanvas.addGroup(toAdd);

    }

    private void addCompositionShape(NewCompositionShape compositionShape, boolean wasDragged){
        if(wasDragged){
            GridCanvas.addGroup(selectedCompositionShape.addNewCompositionShape(compositionShape));

        }else {
            //I've clicked on a thumbnail
            compositionShape.getBasicShapes().forEach(this::addShape);
            Pane toAdd = new Pane();
            compositionShape.getTeste(toAdd, true, 0,0);

            GridCanvas.addGroup(toAdd);
        }
    }


    private Pane getAddParametricCompositionShape(){


        HBox complexShapeVBox = getButtonWith_Label_Color_Image("Parametric Shape", "#644832", "#F2C94C", "icons8-plus-math-96.png", 10);


        complexShapeVBox.setOnMouseClicked(mouseEvent -> {
            System.out.println("I want to add a new Parametric composition shape!");
            GridCanvas.clearEverything();
            isCurrentSimple = false;
            currentName.setText("PARAMETRIC_COMPLEX_DEFAULT");

            transformersBox.getChildren().clear();

            selectedParametricCompositionShape = new ParametricCompositionShape(orchestrator, transformersBox, getProceedWhenDeletingParametricCompositionShape(), teste -> {
                System.err.println("oh, here I am!");
                newCompositionShapes.forEach(NewCompositionShape::redrawThumbnail);
                return null;
            });
            //transformersBox.getChildren().add(selectedParametricCompositionShape.variablesPane);
            sectionToKeep.getChildren().clear();
            sectionToKeep.getChildren().add(selectedParametricCompositionShape.variablesPane);

            selectedPower = null;
            selectedCompositionShape = null;

            newParametricCompositionShapes.add(selectedParametricCompositionShape);
            sideBarThumbnails.add(selectedParametricCompositionShape);
        });

        complexShapeVBox.setPrefWidth(180);
        complexShapeVBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(complexShapeVBox, Priority.ALWAYS);

        return  complexShapeVBox;
    }

    public void addBasicAndComplexButtons(){
        HBox basicShapeVBox = getButtonWith_Label_Color_Image("Basic Shape", "#355765", "#56CCF2", "icons8-plus-math-96.png");

        basicShapeVBox.setOnMouseClicked(mouseEvent -> {
            System.out.println("I want to add a new basic shape!");
            GridCanvas.clearEverything();
            isCurrentSimple = true;

            addNameSectionIfItIsNot();
            addSaveSectionIfItIsNot();

            currentName.setText("simpleDefault");

            transformersBox.getChildren().clear();

            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc5"), null, getProceedWhenDeleting());
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
            selectedCompositionShape = null;
            selectedPower = null;
        });



        HBox complexShapeVBox = getButtonWith_Label_Color_Image("Composition Shape", "#644832", "#F2C94C", "icons8-plus-math-96.png");

        complexShapeVBox.setOnMouseClicked(mouseEvent -> {
            System.out.println("I want to add a new composition shape!");
            GridCanvas.clearEverything();
            isCurrentSimple = false;
            selectedParametricCompositionShape = null;
            selectedPower = null;

            currentName.setText("complexDefault");

            transformersBox.getChildren().clear();


            selectedCompositionShape = new NewCompositionShape(orchestrator, transformersBox, getProceedWhenDeletingCompositionShape(), teste -> {
                System.err.println("oh, here I am!");
                newCompositionShapes.forEach(NewCompositionShape::redrawThumbnail);
                return null;
            });


            newCompositionShapes.add(selectedCompositionShape);
            sideBarThumbnails.add(selectedCompositionShape);
        });

        HBox basicShapeAndComplex = new HBox(basicShapeVBox, complexShapeVBox);
        basicShapeAndComplex.setSpacing(20);
        basicShapeAndComplex.setAlignment(Pos.CENTER);


        HBox parametricShapes = getButtonWith_Label_Color_Image("Parametric Shapes", "#472953", "#ff8ad8", "icons8-chevron-down-96.png");

        CustomMenuItem powerShapeMenuItem = new CustomMenuItem(getAddPowerButton());
        CustomMenuItem compositionShapeEditorMenuItem = new CustomMenuItem(getAddParametricCompositionShape());

        ContextMenu contextMenuParametric = new ContextMenu();
        contextMenuParametric.setId("testeCoiso");
        contextMenuParametric.getItems().addAll(compositionShapeEditorMenuItem, powerShapeMenuItem);

        parametricShapes.setOnMouseClicked(mouseEvent -> contextMenuParametric.show(parametricShapes, mouseEvent.getScreenX(), mouseEvent.getScreenY()));

        HBox parametricShapesMenu = new HBox(parametricShapes);
        parametricShapesMenu.setSpacing(20);






        HBox moreOptionsButton = getButtonWith_Label_Color_Image("More options", "#355765", "#56CCF2", "icons8-chevron-down-96.png", 10, -0.5);

        CustomMenuItem designViewerMenuItem = new CustomMenuItem(getDesignViewer());
        CustomMenuItem variableEditorMenuItem = new CustomMenuItem(getVariableEditor());
        CustomMenuItem shapeRuleEditorMenuItem = new CustomMenuItem(getShapeRuleButton());
        CustomMenuItem processEditorMenuItem = new CustomMenuItem(getProcessButton());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setId("testeCoiso");
        contextMenu.getItems().addAll(shapeRuleEditorMenuItem, processEditorMenuItem, designViewerMenuItem, variableEditorMenuItem);

        moreOptionsButton.setOnMouseClicked(mouseEvent -> contextMenu.show(moreOptionsButton, mouseEvent.getScreenX(), mouseEvent.getScreenY()));

        HBox designViewerAndVariablesEditor = new HBox(moreOptionsButton);
        designViewerAndVariablesEditor.setSpacing(20);

        HBox buttons = new HBox(basicShapeAndComplex, parametricShapesMenu,designViewerAndVariablesEditor);
        buttons.setSpacing(15);

        firstBasicShapeWasSaved.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                topToolbar.getChildren().add(buttons);
            }
        });

        /*
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

         */


    }

    public static HBox getButtonWith_Label_Color_Image(@NamedArg("label") String label, @NamedArg("background color") String backgroundColor,@NamedArg("label color") String labelColor, @NamedArg("image name") String imageLocation, @NamedArg("corner radius") int cornerRadius){
        Image complexPlus = new Image(Objects.requireNonNull(App.class.getResource("/icons/" + imageLocation)).toExternalForm());
        ImageView complexPlusImageView = new ImageView(complexPlus);
        complexPlusImageView.setSmooth(true);
        complexPlusImageView.setPreserveRatio(true);
        complexPlusImageView.setFitWidth(16);

        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox;
        if(label.isBlank()){
             complexShapeHBox = new HBox(complexPlusImageView);
        }else{
             complexShapeHBox = new HBox(complexPlusImageView, complexShape);
        }

        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);


        complexShapeHBox.setOnMouseEntered(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: " + cornerRadius);
        });

        complexShapeHBox.setOnMouseExited(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);
        });


        complexShape.setMinHeight(30);
        complexShapeHBox.setPadding(new Insets(0, 5, 0, 5));


        return complexShapeHBox;
    }


    public static HBox getButtonWith_Label_Color_Image(@NamedArg("label") String label, @NamedArg("background color") String backgroundColor,@NamedArg("label color") String labelColor, @NamedArg("image name") String imageLocation, @NamedArg("corner radius") int cornerRadius, double hue){
        Image complexPlus = new Image(Objects.requireNonNull(App.class.getResource("/icons/" + imageLocation)).toExternalForm());
        ImageView complexPlusImageView = new ImageView(complexPlus);
        complexPlusImageView.setSmooth(true);
        complexPlusImageView.setPreserveRatio(true);
        complexPlusImageView.setFitWidth(16);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(hue);
        complexPlusImageView.setEffect(colorAdjust);

        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexPlusImageView, complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);


        complexShapeHBox.setOnMouseEntered(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: " + cornerRadius);
        });

        complexShapeHBox.setOnMouseExited(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);
        });


        complexShape.setMinHeight(30);
        complexShapeHBox.setPadding(new Insets(0, 10, 0, 10));

        return complexShapeHBox;
    }







    public static HBox getButtonWith_Label_Color_Image(@NamedArg("label") String label, @NamedArg("background color") String backgroundColor,@NamedArg("label color") String labelColor, @NamedArg("image name") String imageLocation){
        Image complexPlus = new Image(Objects.requireNonNull(App.class.getResource("/icons/" + imageLocation)).toExternalForm());
        ImageView complexPlusImageView = new ImageView(complexPlus);
        complexPlusImageView.setSmooth(true);
        complexPlusImageView.setPreserveRatio(true);
        complexPlusImageView.setFitWidth(16);

        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexPlusImageView, complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: 10");
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        complexShapeHBox.setOnMouseEntered(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: 10");
        });

        complexShapeHBox.setOnMouseExited(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: 10");
        });

        complexShape.setMinHeight(30);
        complexShapeHBox.setPadding(new Insets(0, 10, 0, 10));

        return complexShapeHBox;
    }

    public static HBox getButtonWith_Label_Color(@NamedArg("label") String label, @NamedArg("background color") String backgroundColor,@NamedArg("label color") String labelColor){
        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: 10");
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }

    public static HBox getButtonWith_Label_Color(@NamedArg("label") String label, @NamedArg("background color") String backgroundColor,@NamedArg("label color") String labelColor, @NamedArg("corner radius") int cornerRadius){
        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }




    private Pane getProcessButton(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Processes editor", "#355C65", "#56CDF2", "process.png", 10);

        complexShapeHBox.setOnMouseClicked(event -> {
            ProcessesEditor processesEditor = new ProcessesEditor(scene, newCompositionShapes, basicShapesToSave, orchestrator);
            processesEditor.openPopup();
        });

        complexShapeHBox.setPrefWidth(180);
        complexShapeHBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }

    private Pane getShapeRuleButton(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Shape rules editor", "#472953", "#ff8ad8", "icons8-rules-96.png", 10);


        complexShapeHBox.setOnMouseClicked(event -> {
            /*ShapeRuleEditor shapeRuleEditor = new ShapeRuleEditor(scene, newCompositionShapes, basicShapesToSave, orchestrator);
            shapeRuleEditor.openPopup();*/

            NewShapeRuleEditor newShapeRuleEditor = new NewShapeRuleEditor(scene, newCompositionShapes, basicShapesToSave, newParametricCompositionShapes, powerArrayList, orchestrator);
            newShapeRuleEditor.openPopup();

        });

        complexShapeHBox.setPrefWidth(180);
        complexShapeHBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }

    private Pane getDesignViewer(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Design Viewer", "#2A2953", "#8194F4", "editorViewerIcon.png", 10);


        complexShapeHBox.setOnMouseClicked(event -> {
            DesignToProlog designToProlog = new DesignToProlog(scene);
            designToProlog.openPopup();

        });

        complexShapeHBox.setPrefWidth(180);
        complexShapeHBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }

    private Pane getAddPowerButton(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Power", "#2A2953", "#8194F4", "editorViewerIcon.png", 10);



        complexShapeHBox.setOnMouseClicked(event -> {
            System.out.println("I want to add a new power shape!");
            GridCanvas.clearEverything();
            isCurrentSimple = false;

            addNameSectionIfItIsNot();
            addSaveSectionIfItIsNot();

            currentName.setText("simplePower");

            transformersBox.getChildren().clear();

            Power power = new Power(getProceedWhenDeletingPowerShape(), orchestrator);
            addPowerShape(power);
            selectedPower = power;
            selectedCompositionShape = null;

            powerArrayList.add(power);
            sideBarThumbnails.add(power);

            transformersBox.getChildren().add(power.getTranslationXSection());

        });

        complexShapeHBox.setPrefWidth(180);
        complexShapeHBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }

    private Pane getVariableEditor(){
        HBox complexShapeHBox = getButtonWith_Label_Color_Image("Variables Editor", "#5B4E2D", "#C6AA63", "variableIcon.png", 10);


        complexShapeHBox.setOnMouseClicked(event -> {
            VariablesEditor variablesEditor = new VariablesEditor(scene, orchestrator);
            variablesEditor.openPopup();

        });

        complexShapeHBox.setPrefWidth(180);
        complexShapeHBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        return complexShapeHBox;
    }

    private void addShape(CustomShape basicShapeToAdd){
            if(basicShapeToAdd instanceof BasicShape){
                BasicShape tempBasicShape = (BasicShape) basicShapeToAdd;

                GridCanvas.addShape(tempBasicShape);
                if(!basicShapes.contains(tempBasicShape)){
                    //if(isCurrentSimple){
                    //TODO: Every basic shape is added to this vector...
                        basicShapes.add(tempBasicShape);
                    //}
                }
                selectedBasicShape = tempBasicShape;
                selectedBasicShape.turnOnStroke();
                selectedBasicShape.toogleSelected();

                basicShapes.stream().filter(r -> r != tempBasicShape).forEach(BasicShape::turnOffStroke);
                transformersBox.getChildren().clear();

                if(isCurrentSimple){
                    HBox colorAndImageSection = new HBox(tempBasicShape.getColorSection(), tempBasicShape.getImageSection());
                    colorAndImageSection.setSpacing(10);
                    HBox.setHgrow(colorAndImageSection, Priority.ALWAYS);

                    transformersBox.getChildren().addAll(tempBasicShape.getWidthSection(), tempBasicShape.getHeightSection(), colorAndImageSection);
                }else{
                    if(selectedParametricCompositionShape != null){
                        transformersBox.getChildren().addAll(tempBasicShape.getHorizontalParametricTranslationSection(), tempBasicShape.getVerticalParametricTranslationSection(), tempBasicShape.getTranslationXSection(), tempBasicShape.getTranslationYSection());
                    }else{
                        transformersBox.getChildren().addAll(tempBasicShape.getTranslationXSection(), tempBasicShape.getTranslationYSection());
                    }
                }
            }

    }

    private void addPowerShape(Power power){
        GridCanvas.addPowerShape(power);
        //GridCanvas.addNode(power.getEditorVisualization());

        if(selectedParametricCompositionShape != null){
            Group group = power.getCenterGroup();
            group.setOnMouseClicked(mouseEvent -> {
                transformersBox.getChildren().clear();
                if(selectedParametricCompositionShape != null){
                    transformersBox.getChildren().addAll(power.getParametricTranslationSection(), power.getRealTranslationSection());
                }else{
                    transformersBox.getChildren().add(power.getRealTranslationSection());
                }

            });

        }

    }

    private Pane setupScenePanel(Scene scene){
        finishSetup();

        this.scene = scene;

        topToolbar = new HBox();
        topToolbar.setStyle("-fx-background-color: #333234; -fx-background-radius: 10");
        topToolbar.setAlignment(Pos.CENTER_LEFT);
        topToolbar.setPadding(new Insets(10));
        topToolbar.setSpacing(15);
        topToolbar.setPrefHeight(50);

        BorderPane.setMargin(topToolbar, new Insets(0, 0, 25, 0));

        mainPanel = new VBox();
        mainPanel.setMaxWidth(500);
        mainPanel.setPrefSize(400, 700);
        mainPanel.setStyle("-fx-background-color: #262528;");
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setPadding(new Insets(0,0,0,20));
        mainPanel.setSpacing(15);

        sectionToKeep = new VBox();
        sectionToKeep.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
        sectionToKeep.setAlignment(Pos.TOP_CENTER);
        sectionToKeep.setSpacing(10);


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(transformersBox);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: rgb(38,37,40); -fx-background-radius: 10; -fx-background: transparent");

        mainPanel.getChildren().addAll(getNameSection(),sectionToKeep, scrollPane, getSaveButtonSection());

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

        ScrollPane shapesScrollPane = getScrollPane();
        shapesScrollPane.setPrefWidth(300);
        BorderPane.setMargin(shapesScrollPane, new Insets(0,25,0,0));
        borderPane.setLeft(shapesScrollPane);

        borderPane.setTop(topToolbar);

        return  borderPane;
    }

    public Pane getScenePanelWithLoadedFile(Scene scene){
       Pane borderPane = setupScenePanel(scene);
       sceneStackPane.layout();
        return borderPane;
    }

    public void loadVariables(File file){
        orchestrator.getVariablesFromFile(file);
    }

    public void loadParametricShapes(File file){
        ArrayList<ParametricCompositionShape> newCompositionShapeArrayList = orchestrator.getParametricShapesFromFile(file, transformersBox,getProceedWhenDeletingCompositionShape(), teste -> {
            newParametricCompositionShapes.forEach(ParametricCompositionShape::redrawThumbnail);
            return null;
        } );
        newParametricCompositionShapes.addAll(newCompositionShapeArrayList);
        sideBarThumbnails.addAll(newCompositionShapeArrayList);
    }

    public void loadPowerShapes(File file){
        ArrayList<Power> newPowerShapesArrayList = orchestrator.getPowerShapesFromFile(file, getProceedWhenDeletingPowerShape());
        powerArrayList.addAll(newPowerShapesArrayList);
        sideBarThumbnails.addAll(newPowerShapesArrayList);
        newPowerShapesArrayList.forEach(Power::redrawThumbnail);
    }

    public void loadProcesses(File file){
        orchestrator.getProcessesFromFile(file);
    }

    public void loadShapeRules(File file){
        orchestrator.getShapeRulesFromFile(file);
    }

    public void loadNewCompositionShapes(File file){
        ArrayList<NewCompositionShape> newCompositionShapeArrayList = orchestrator.getNewCompositionShapesFromFile(file, transformersBox,getProceedWhenDeletingCompositionShape(), teste -> {
            System.err.println("oh, here I am!");
            newCompositionShapes.forEach(NewCompositionShape::redrawThumbnail);
            return null;
        } );
        newCompositionShapes.addAll(newCompositionShapeArrayList);
        sideBarThumbnails.addAll(newCompositionShapeArrayList);
    }

    public void loadBasicShapes(File file){
        ArrayList<BasicShape> basicShapes = orchestrator.getBasicShapesFromFile(file);
        this.basicShapes.addAll(basicShapes);

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
            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc4"), null);
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
        }



    }

    public Pane getScenePanel(Scene scene){
        Pane borderPane = setupScenePanel(scene);

        BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#55efc4"), null, getProceedWhenDeleting());
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
                    mainPanel.getChildren().add(i+2, sectionToKeep);
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
        System.out.println("TAMANHO ERA: " + basicShapes.size());
        basicShapes.remove(temp);
        System.out.println("TAMANHO AGORA É: " + basicShapes.size());
        GridCanvas.clearEverything();
        transformersBox.getChildren().clear();
        sideBarThumbnails.remove(temp);

        if(basicShapesToSave.size() == 0){
            mainPanel.getChildren().remove(nameSection);
            mainPanel.getChildren().remove(sectionToKeep);
            mainPanel.getChildren().remove(saveSection);


        }else{
            currentName.setText(basicShapesToSave.get(0).getShapeName());
            addShape(basicShapesToSave.get(0));
        }

    }

    private void deleteParametricCompositionShape(String uuidToRemove){
        //We are cleaning everything, but if the user is on a composition shape, we shouldn't... this way is easier tho.
        ParametricCompositionShape temp = newParametricCompositionShapes.stream().filter(p -> p.getUUID().toString().equals(uuidToRemove)).findFirst().get();
        newParametricCompositionShapes.remove(temp);
        GridCanvas.clearEverything();
        transformersBox.getChildren().clear();
        sideBarThumbnails.remove(temp);

        System.err.println("basic shapes size: " + basicShapes.size());
        if(basicShapes.size() == 0){
            System.err.println("I've added a new basic shape since there was none");
            isCurrentSimple = true;
            inDragCustomShape = null;
            selectedPower = null;
            selectedParametricCompositionShape = null;
            selectedCompositionShape = null;
            //Ads a basic shape when we delete the last one!
            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#76d6ff"),null, getProceedWhenDeleting());
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
            currentName.setText("defaultName");
            sideBarThumbnails.add(toAdd);
            addSaveSectionIfItIsNot();


        }else{
            isCurrentSimple = true;
            inDragCustomShape = null;
            selectedPower = null;
            selectedCompositionShape =  null;
            selectedParametricCompositionShape = null;

            currentName.setText(basicShapesToSave.get(0).getShapeName());
            addShape(basicShapesToSave.get(0));


        }
    }

    private void deleteCompositionShape(String uuidToRemove){
        //We are cleaning everything, but if the user is on a composition shape, we shouldn't... this way is easier tho.
        NewCompositionShape temp = newCompositionShapes.stream().filter(p -> p.getUUID().toString().equals(uuidToRemove)).findFirst().get();
        newCompositionShapes.remove(temp);
        GridCanvas.clearEverything();
        transformersBox.getChildren().clear();
        sideBarThumbnails.remove(temp);

        //TODO Here it was basicShapes instead of basicShapesToSave, but this is how it makes sense!
        if(basicShapesToSave.size() == 0){
            //Ads a basic shape when we delete the last one!
            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#76d6ff"), null, getProceedWhenDeleting());
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
            currentName.setText("defaultName");
            sideBarThumbnails.add(toAdd);

            isCurrentSimple = true;
            inDragCustomShape = null;
            selectedPower = null;
            selectedParametricCompositionShape = null;
            selectedCompositionShape = null;
            addSaveSectionIfItIsNot();
        }else{
            System.out.println("here we have: " + basicShapes.size());
            currentName.setText(basicShapesToSave.get(0).getShapeName());
            addShape(basicShapesToSave.get(0));

            isCurrentSimple = true;
            inDragCustomShape = null;
            selectedPower = null;
            selectedCompositionShape =  null;
            selectedParametricCompositionShape = null;
        }
    }

    private void deleteCompositionShapeFromCompositionShape(String uuidToRemove){
        newCompositionShapes.forEach(newCompositionShape -> {
            newCompositionShape.deleteCompositionShape(uuidToRemove);
            //Maybe an uncessary redraw?
            newCompositionShape.redrawThumbnail();
        });
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

               Pane acceptButton = PopupWindow.getButton("Accept, and delete this shape from compositions shapes and shape rules that use it.", "null",  "#807229", "#E7CE4A", event -> {
                   deleteBasicShape(uuidToRemove);
                   deleteBasicShapeFromCompositionShape(uuidToRemove);
                   deleteBasicShapeFromShapeRule(uuidToRemove);

                   //saveCurrentShape(); //Needed in order to be able to then drag it!
                   tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
               });
               Pane refuseButton = PopupWindow.getButton("Keep this shape.", "null",  "#5E2323", "#EB5757", event -> {
                   tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
               });

                popupWindow.createPopup("Delete confirmation",scene,"Deleting this basic shape will also delete it from composition shapes and shape rules that use it. Do you want to proceed?", acceptButton, refuseButton);

            }else{

               deleteBasicShape(uuidToRemove);
                deleteBasicShapeFromShapeRule(uuidToRemove);
            }

            return null;
        };
    }

    private void deleteBasicShapeFromShapeRule(String uuidToRemove){
        orchestrator.getShapeRules().forEach(p -> {
            p.getLeftShape().deleteBasicShape(uuidToRemove);
            p.getRightShape().deleteBasicShape(uuidToRemove);
        });
    }

    private void deleteCompositionShapeFromShapeRule(String uuidToRemove){
        orchestrator.getShapeRules().forEach(p -> {
            p.getLeftShape().deleteCompositionShape(uuidToRemove);
            p.getRightShape().deleteCompositionShape(uuidToRemove);
        });
    }

    private void deletePowerShape(String uuidToRemove){
        Power powerToRemove = powerArrayList.stream().filter(p -> p.getUUID().toString().equals(uuidToRemove)).findFirst().get();
        powerArrayList.remove(powerToRemove);
        GridCanvas.clearEverything();
        transformersBox.getChildren().clear();
        sideBarThumbnails.remove(powerToRemove);

        if(basicShapes.size() == 0){
            isCurrentSimple = true;
            inDragCustomShape = null;
            selectedPower = null;
            selectedParametricCompositionShape = null;
            selectedCompositionShape = null;
            //Ads a basic shape when we delete the last one!
            BasicShape toAdd = new BasicShape(SCALE, SCALE, Color.web("#76d6ff"), null, getProceedWhenDeleting());
            addShape(toAdd);
            basicShapesToSave.add(toAdd);
            currentName.setText("defaultName");
            sideBarThumbnails.add(toAdd);
            addSaveSectionIfItIsNot();
        }else{
            currentName.setText(basicShapesToSave.get(0).getShapeName());
            addShape(basicShapesToSave.get(0));
        }
    }

    private Function<String, Double> getProceedWhenDeletingPowerShape(){
        return uuidToRemove -> {

            deletePowerShape(uuidToRemove);

            return null;
        };
    }

    private Function<String, Double> getProceedWhenDeletingCompositionShape(){
        return uuidToRemove -> {

            if(!orchestrator.canCompositionShapeBeRemoved(uuidToRemove)){
                System.err.println("Can't be removed without impacting other composition shapes!");

                PopupWindow popupWindow = new PopupWindow();

                Stage tempStage = popupWindow.getStage();

                Pane acceptButton = PopupWindow.getButton("Accept, and delete this shape from compositions shapes and shape rules that use it.", "null",  "#807229", "#E7CE4A", event -> {
                    deleteCompositionShape(uuidToRemove);
                    deleteCompositionShapeFromCompositionShape(uuidToRemove);
                    deleteCompositionShapeFromShapeRule(uuidToRemove);

                    tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
                });
                Pane refuseButton = PopupWindow.getButton("Keep this shape.", "null",  "#5E2323", "#EB5757", event -> {
                    tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
                });

                popupWindow.createPopup("Delete confirmation",scene,"Deleting this composition shape will also delete it from composition shapes and shape rules that use it. Do you want to proceed?", acceptButton, refuseButton);

            }else{

                deleteCompositionShape(uuidToRemove);
                deleteCompositionShapeFromShapeRule(uuidToRemove);

            }

            return null;
        };
    }


    private Function<String, Double> getProceedWhenDeletingParametricCompositionShape(){
        return uuidToRemove -> {

                deleteParametricCompositionShape(uuidToRemove);
                //TODO: deleteCompositionShapeFromShapeRule(uuidToRemove);

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
        pane.setStyle("-fx-background-color: #333234; -fx-background-radius: 10");

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
                        if(selectedParametricCompositionShape != null){
                            transformersBox.getChildren().addAll(rectangle.getHorizontalParametricTranslationSection(), rectangle.getVerticalParametricTranslationSection(), rectangle.getTranslationXSection(), rectangle.getTranslationYSection());
                        }else{
                            transformersBox.getChildren().addAll( rectangle.getTranslationXSection(), rectangle.getTranslationYSection());
                        }

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
            System.out.println("---------- dragDropped in APP");

            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                //System.out.println("this was dropped: " + db.getString());

                if(!isCurrentSimple){

                    if(!db.getString().contains("[")){
                        System.out.println("I was dropped a simple shape");
                        System.out.println("i was dropped: " + db.getString());
                        if(inDragCustomShape instanceof BasicShape && selectedCompositionShape != null)
                            addShape(selectedCompositionShape.addBasicShape(basicShapes.get(Integer.parseInt(db.getString())).getUUID().toString()));
                        else if(inDragCustomShape instanceof Power && selectedParametricCompositionShape != null){
                            addPowerShape(selectedParametricCompositionShape.addPowerShape(powerArrayList.get(Integer.parseInt(db.getString())).getUUID().toString()));
                        }else if(inDragCustomShape instanceof BasicShape && selectedParametricCompositionShape != null){
                            addShape(selectedParametricCompositionShape.addBasicShape(basicShapes.get(Integer.parseInt(db.getString())).getUUID().toString()));
                        }
                    }else{
                        if(inDragCustomShape instanceof NewCompositionShape){

                            if(selectedCompositionShape != null){
                                if(!inDragCustomShape.getUUID().equals(selectedCompositionShape.getUUID())) {
                                    System.out.println("I was dropped a composition shape");
                                        addCompositionShape((NewCompositionShape) inDragCustomShape, true);
                                }
                            }else if(selectedParametricCompositionShape != null){
                                if(!inDragCustomShape.getUUID().equals(selectedParametricCompositionShape.getUUID())) {
                                    GridCanvas.addGroup(selectedParametricCompositionShape.addNewCompositionShape((NewCompositionShape) inDragCustomShape));
                                }
                            }

                        }else if(inDragCustomShape instanceof ParametricCompositionShape){
                            if(selectedParametricCompositionShape != null){
                                GridCanvas.addGroup(selectedParametricCompositionShape.addParametricCompositionShape((ParametricCompositionShape) inDragCustomShape));
                            }
                        }


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
        Rectangle rectangle = new Rectangle(10,10);
        rectangle.setTranslateX(10);
        rectangle.setFill(Color.YELLOW);

        Arc arc = new Arc();

        arc.setCenterX(10);
        arc.setCenterY(10);
        arc.setRadiusX(10);
        arc.setRadiusY(10);
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
        topLeft.setTranslateX(-10);
        topLeft.setTranslateY(0);

        Shape bottomLeft = getTopLeftArc();
        bottomLeft.setRotate(-90);
        bottomLeft.setTranslateX(-10);
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

        currentName = new TextField("default");
        currentName.setPromptText("default");
        currentName.setStyle("-fx-background-color: #333234; -fx-text-fill: #5D5C5E; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        currentName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));


        HBox nameHB = new HBox(namePrompt, currentName);
        nameHB.setPadding(new Insets(10,10,10,15));
        nameHB.setAlignment(Pos.CENTER_LEFT);
        nameHB.setMaxHeight(30);
        nameHB.setSpacing(10);

        nameHB.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");

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
        HBox saveHB = getButtonWith_Label_Color_Image("Save", "#3C5849", "#6FCF97", "icons8-save-96.png", 10);

        saveHB.setPadding(new Insets(10));
        saveHB.setAlignment(Pos.CENTER);
        saveHB.setMaxHeight(50);
        saveHB.setPrefHeight(50);


        saveHB.setOnMouseClicked(mouseEvent -> saveCurrentShape());

        this.saveSection = saveHB;
        saveHB.setId("saveButton");

        return saveHB;
    }

    private void saveCurrentShape(){
        System.out.println("basic shapes aqui era: " + basicShapes.size());

        if(isCurrentSimple){
            System.out.println("basic shapes aqui era: " + basicShapes.size());

            BasicShape currentRectangle = gridCanvas.getSimpleRectangle();
            currentRectangle.setShapeName(currentName.getText());
            currentRectangle.redrawThumbnail();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", currentRectangle.getUUID().toString());
            jsonObject.put("basic", "true");
            jsonObject.put("color", currentRectangle.getFill() == null ? currentRectangle.getSelectedImage().getUrl() : currentRectangle.getColor().toString());
            jsonObject.put("width", currentRectangle.getWidth());
            jsonObject.put("height", currentRectangle.getHeight());
            jsonObject.put("name", currentName.getText());

            firstBasicShapeWasSaved.setValue(true);
            sideBarThumbnails.add(currentRectangle);

            orchestrator.addAllBasicShapes(basicShapesToSave);


            //TODO This was added. Is there any problem? If the comp shapes were added when the app was loaded, it shouldn't be needed.
            orchestrator.addAllCompositionShapes(newCompositionShapes);
            orchestrator.addAllPowerShapes(powerArrayList);
            orchestrator.addAllParametricCompositionShapes(newParametricCompositionShapes);
            System.out.println("basic shapes aqui é: " + basicShapes.size());
        }else if(selectedCompositionShape != null){
            selectedCompositionShape.setShapeName(currentName.getText());
            newCompositionShapes.forEach(NewCompositionShape::redrawThumbnail);
            newParametricCompositionShapes.forEach(ParametricCompositionShape::redrawThumbnail);

            orchestrator.addAllCompositionShapes(newCompositionShapes);
            orchestrator.addAllPowerShapes(powerArrayList);
            orchestrator.addAllParametricCompositionShapes(newParametricCompositionShapes);


        }else if(selectedPower != null){
            selectedPower.setShapeName(currentName.getText());
            orchestrator.addAllPowerShapes(powerArrayList);
            orchestrator.addAllCompositionShapes(newCompositionShapes);
            orchestrator.addAllParametricCompositionShapes(newParametricCompositionShapes);
            powerArrayList.forEach(Power::redrawThumbnail);

            newParametricCompositionShapes.forEach(ParametricCompositionShape::redrawThumbnail);

        }else if(selectedParametricCompositionShape != null){
            selectedParametricCompositionShape.setShapeName(currentName.getText());

            orchestrator.addAllParametricCompositionShapes(newParametricCompositionShapes);

            orchestrator.addAllPowerShapes(powerArrayList);
            orchestrator.addAllCompositionShapes(newCompositionShapes);

            newParametricCompositionShapes.forEach(ParametricCompositionShape::redrawThumbnail);
        }

        orchestrator.printDesignTXT();

    }

    public static void main(String[] args) {
        launch();
    }
}