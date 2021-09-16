package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static marrf.iscte.App.getAnchorPaneClip;
import static marrf.iscte.PopupWindow.startBlurAnimation;

public class ShapeRuleEditor {

    private final Stage stage = new Stage();
    private final Scene scene;

    private final VBox mainPanel = new VBox();
    private final HBox processPanel = new HBox();

    private final ScrollPane horizontalScrollPane = new ScrollPane();
    private final HBox processes = new HBox();
    private final HBox processGrid = new HBox();

    private final ArrayList<NewCompositionShape> newCompositionShapes;
    private final ArrayList<BasicShape> basicShapes;

    private SmallGridCanvas leftGrid;
    private SmallGridCanvas rightGrid;

    private final VBox leftTranslationSection = new VBox();
    private final VBox rightTranslationSection = new VBox();

    private final Orchestrator orchestrator;


    public ShapeRuleEditor(Scene scene, ArrayList<NewCompositionShape> newCompositionShapes, ArrayList<BasicShape> basicShapes, Orchestrator orchestrator){
        this.scene = scene;
        this.newCompositionShapes = newCompositionShapes;
        this.basicShapes = basicShapes;
        this.orchestrator = orchestrator;
    }

    private ArrayList<String> getCompositionShapesStringArray(){
        ArrayList<String> toReturn = new ArrayList<>();
        newCompositionShapes.forEach( p -> toReturn.add(p.getShapeName()));

        return toReturn;
    }

    private ArrayList<String> getBasicShapesStringArray(){
        ArrayList<String> toReturn = new ArrayList<>();
        basicShapes.forEach( p -> toReturn.add(p.getShapeName()));

        return toReturn;
    }

    private Pane getLeftGridPane(){
        leftGrid = new SmallGridCanvas();
        Pane pane = new Pane();
        Pane grid = leftGrid.getGrid(pane);
        grid.setClip(App.getCustomRectangleClip(pane));

        pane.getChildren().add(grid);
        VBox.setVgrow(pane, Priority.ALWAYS);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPickOnBounds(false);
        pane.getChildren().add(anchorPane);
        getAnchorPaneClip(anchorPane, "#333234");

        pane.widthProperty().addListener((observable, oldValue, newValue) -> {
            anchorPane.setPrefWidth(newValue.doubleValue());
        });

        pane.heightProperty().addListener((observable, oldValue, newValue) -> {
            anchorPane.setPrefHeight(newValue.doubleValue());
        });

        pane.setStyle("-fx-background-radius: 20; -fx-border-color: #4F4F4F; -fx-border-radius: 20;");


        return pane;
    }

    private Pane getRightGridPane(){
        rightGrid = new SmallGridCanvas();
        Pane pane = new Pane();
        Pane grid = rightGrid.getGrid(pane);
        grid.setClip(App.getCustomRectangleClip(pane));

        pane.getChildren().add(grid);
        VBox.setVgrow(pane, Priority.ALWAYS);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPickOnBounds(false);
        pane.getChildren().add(anchorPane);
        getAnchorPaneClip(anchorPane, "#333234");

        pane.widthProperty().addListener((observable, oldValue, newValue) -> {
            anchorPane.setPrefWidth(newValue.doubleValue());
        });

        pane.heightProperty().addListener((observable, oldValue, newValue) -> {
            anchorPane.setPrefHeight(newValue.doubleValue());
        });

        pane.setStyle("-fx-background-radius: 20; -fx-border-color: #4F4F4F; -fx-border-radius: 20;");


        return pane;
    }

    private Pane getArrowPane(){
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        Arrow arrow = new Arrow();

        arrow.setStartY(0);
        arrow.setStartX(0);

        arrow.setEndY(0);
        arrow.setEndX(100);

        vBox.getChildren().add(arrow);

        VBox.setVgrow(vBox, Priority.ALWAYS);

        return vBox;
    }

    private Node getScrollShapes(SmallGridCanvas smallGridCanvas, Pane toAddTo){
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent");

        HBox horizontal = new HBox();
        horizontal.setAlignment(Pos.CENTER_LEFT);
        horizontal.setSpacing(20);

        getBasicShapesStringArray().forEach(p -> {
            Label basicShapeLabel = new Label(p);
            basicShapeLabel.setPadding(new Insets(5));
            basicShapeLabel.setStyle("-fx-background-color: rgb(130,114,17); -fx-background-radius: 10");
            basicShapeLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
            basicShapeLabel.setTextFill(Color.WHITE);

            basicShapeLabel.setOnMouseEntered(mouseEvent -> {
                basicShapeLabel.setStyle("-fx-background-color: rgb(176,154,20); -fx-background-radius: 10");

            });

            basicShapeLabel.setOnMouseExited(mouseEvent -> {
                basicShapeLabel.setStyle("-fx-background-color: rgb(130,114,17); -fx-background-radius: 10");
            });

            basicShapeLabel.setOnMouseClicked(mouseEvent -> {
                BasicShape originalBasicShape = basicShapes.stream().filter(a -> a.getShapeName().equals(p)).findFirst().get();

                UUID toPut = UUID.randomUUID();

                BasicShape leftBasicShapeCopy = orchestrator.getCopyOfBasicShape(originalBasicShape.getUUID().toString(), a -> 0.0, a -> 0.0, a -> {
                    return 0.0;
                });

                leftBasicShapeCopy.setOnMouseClicked(mouseEvent1 -> {
                    toAddTo.getChildren().clear();
                    toAddTo.getChildren().addAll(leftBasicShapeCopy.getTranslationXSection(), leftBasicShapeCopy.getTranslationYSection());
                });

                leftBasicShapeCopy.setProceedWhenDeleting(a -> {
                    if(toAddTo.getChildren().contains(leftBasicShapeCopy.getTranslationXSection())){
                        toAddTo.getChildren().removeAll(leftBasicShapeCopy.getTranslationXSection(), leftBasicShapeCopy.getTranslationYSection());
                    }
                    return 0.0;
                });

                smallGridCanvas.addShape(leftBasicShapeCopy, toPut);

            });

            horizontal.getChildren().add(basicShapeLabel);
        });

        getCompositionShapesStringArray().forEach(p -> {
            Label compositionShapeLabel = new Label(p);
            compositionShapeLabel.setPadding(new Insets(5));
            compositionShapeLabel.setStyle("-fx-background-color: rgb(3,108,130); -fx-background-radius: 10");
            compositionShapeLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
            compositionShapeLabel.setTextFill(Color.WHITE);

            compositionShapeLabel.setOnMouseEntered(mouseEvent -> {
                compositionShapeLabel.setStyle("-fx-background-color: rgb(0,149,181); -fx-background-radius: 10");

            });

            compositionShapeLabel.setOnMouseExited(mouseEvent -> {
                compositionShapeLabel.setStyle("-fx-background-color: rgb(3,108,130); -fx-background-radius: 10");
            });

            compositionShapeLabel.setOnMouseClicked(mouseEvent -> {
                NewCompositionShape originalCompositionShape = newCompositionShapes.stream().filter( a -> a.getShapeName().equals(p)).findFirst().get();

                Pane toAddLeft = new Pane();
                NewCompositionShape toPut = originalCompositionShape.getPaneWithBasicAndCompositionShapes(toAddLeft, true, 0,0, toAddTo);

                toPut.setProceedWhenDeleting(a -> {
                    if(toAddTo.getChildren().contains(toPut.getTranslationXSection())){
                        toAddTo.getChildren().removeAll(toPut.getTranslationXSection(), toPut.getTranslationYSection());
                    }
                    return 0.0;}
                );

                smallGridCanvas.addGroup(toAddLeft, toPut);

            });

            horizontal.getChildren().add(compositionShapeLabel);

        });

        scrollPane.setContent(horizontal);

        return scrollPane;
    }

    private Pane getLeftPane(){
        VBox vBox = new VBox();
        vBox.setSpacing(10);

        vBox.getChildren().addAll(getLeftGridPane(), getScrollShapes(leftGrid, leftTranslationSection), leftTranslationSection);

        return vBox;
    }

    private Pane getRightPane(){
        VBox vBox = new VBox();
        vBox.setSpacing(10);

        vBox.getChildren().addAll(getRightGridPane(),getScrollShapes(rightGrid, rightTranslationSection),rightTranslationSection);

        return vBox;
    }

    public static String getShapesJSON(ArrayList<BasicShape> basicShapes, ArrayList<NewCompositionShape> newCompositionShapes){
        StringBuilder toReturn = new StringBuilder();

        basicShapes.forEach(basicShape -> {
            toReturn.append("[\n");

            toReturn.append("\"").append(basicShape.getShapeName()).append("\",\n");
            toReturn.append("\"").append(basicShape.getShapeName()).append("\"\n");

            toReturn.append("],\n");

        });

        newCompositionShapes.forEach(newCompositionShape -> {
            toReturn.append("[\n");

            toReturn.append("\"").append(newCompositionShape.getShapeName()).append("\",\n");
            toReturn.append("\"").append(newCompositionShape.getShapeName()).append("\"\n");

            toReturn.append("],\n");
        });



        return toReturn.toString();
    }

    private File setUpFiles(String fileName){
        Path htmlOriginal = Paths.get("/Users/miguelferreira/Downloads/blockly-samples-master/examples/getting-started-codelab/starter-code/" + fileName + ".html");

        File directory = new File("/Users/miguelferreira/Downloads/blockly-samples-master/examples/getting-started-codelab/starter-code/");
        for (File childrenFile : directory.listFiles()){
            if(childrenFile.getName().contains("Copied_")){
                childrenFile.delete();
            }
        }

        int randomNum = ThreadLocalRandom.current().nextInt(0, 100 + 1);

        Path htmlCopied = Paths.get("/Users/miguelferreira/Downloads/blockly-samples-master/examples/getting-started-codelab/starter-code/" + fileName + "Copied"+ "_" + randomNum + ".html");

        try{
            Files.copy(htmlOriginal, htmlCopied, StandardCopyOption.REPLACE_EXISTING);

            String fileContentJS = new String(Files.readAllBytes(htmlCopied));
            fileContentJS = fileContentJS.replace("myBlocksCopied.js","myBlocksCopied.js?c=r_" + randomNum);
            Files.write(htmlCopied, fileContentJS.getBytes());


            Path original = Paths.get("/Users/miguelferreira/Downloads/blockly-samples-master/examples/getting-started-codelab/starter-code/scripts/myBlocks.js" );
            Path copied = Paths.get("/Users/miguelferreira/Downloads/blockly-samples-master/examples/getting-started-codelab/starter-code/scripts/myBlocksCopied.js" );

            Files.copy(original, copied, StandardCopyOption.REPLACE_EXISTING);


            String fileContent = new String(Files.readAllBytes(copied));
            fileContent = fileContent.replace("//CHANGE_HERE", "{\n" +
                    "  \"type\": \"availableshapes\",\n" +
                    "  \"message0\": \"%1\",\n" +
                    "  \"args0\": [\n" +
                    "    {\n" +
                    "      \"type\": \"field_dropdown\",\n" +
                    "      \"name\": \"NAME\",\n" +
                    "      \"options\": [\n" +
                    "       " + getShapesJSON(basicShapes, newCompositionShapes) +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"output\": \"shape\",\n" +
                    "  \"colour\": 180,\n" +
                    "  \"tooltip\": \"\",\n" +
                    "  \"helpUrl\": \"\"\n" +
                    "},");
            Files.write(copied, fileContent.getBytes());


        }catch (Exception e){

        }

        return htmlCopied.toFile();
    }


    private Pane getButton(boolean isItBool){

        Label complexShape = new Label(isItBool ? "Bool" : "Proc1");

        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill( isItBool ? Color.web("#EB5757") : Color.web("#5778EB"));

        HBox complexShapeHBox = new HBox(complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + (isItBool ? "#5E2323" : "#233B5E") +";-fx-background-radius: 10");

        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        complexShapeHBox.setMaxHeight(50);
        complexShapeHBox.setPrefHeight(20);

        complexShapeHBox.setOnMouseClicked(event -> {
            System.out.println("yey");

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            File boolFile = setUpFiles(isItBool ? "bool" : "proc1");

            webEngine.load(boolFile.toURI().toString());
            System.out.println("a carregar: " + boolFile.toURI().toString());
            webEngine.reload();


            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(stage);

            Scene dialogScene = new Scene(webView, 1280, 720);
            dialogScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

            newStage.setScene(dialogScene);
            newStage.show();


            newStage.sizeToScene();
            newStage.setTitle("Web View");

            newStage.setResizable(true);

        });

        complexShapeHBox.setMinWidth(60);

        return complexShapeHBox;
    }

    private void setUpProcessPanelWith_Shape_Shape(){
        processGrid.getChildren().clear();
        processGrid.getChildren().addAll(getLeftPane(),getArrowPane(), getRightPane());
    }

    private void setUpProcessPanelWith_Shape_Shape_Proc(){
        processGrid.getChildren().clear();
        processGrid.getChildren().addAll(getLeftPane(),getArrowPane(), getRightPane(), getButton(false));
    }

    private void setUpProcessPanelWith_Bool_Shape_Shape(){
        processGrid.getChildren().clear();
        processGrid.getChildren().addAll(getButton(true), getLeftPane(),getArrowPane(), getRightPane());
    }

    private void setUpProcessPanelWith_Bool_Shape_Shape_Proc(){
        processGrid.getChildren().clear();
        processGrid.getChildren().addAll(getButton(true), getLeftPane(),getArrowPane(), getRightPane(), getButton(false));
    }

    private void setUpPanes(){
        leftTranslationSection.setSpacing(20);
        rightTranslationSection.setSpacing(20);

        processGrid.setSpacing(20);
        processGrid.getChildren().addAll(getLeftPane(), getButton(true), getRightPane());
        processGrid.setAlignment(Pos.CENTER);
        processGrid.setPadding(new Insets(20));
        processGrid.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(processGrid, Priority.ALWAYS);

        mainPanel.setPadding(new Insets(20));
        mainPanel.setSpacing(20);
        mainPanel.setStyle("-fx-background-color: #262528;");

        horizontalScrollPane.setPrefHeight(250);
        horizontalScrollPane.setStyle("-fx-background-color: #333234");
        horizontalScrollPane.setContent(processes);

        //mainPanel.getChildren().addAll(processPanel, horizontalScrollPane);
        mainPanel.getChildren().addAll(getTemplate());


        processPanel.setMaxHeight(Double.MAX_VALUE);
        processPanel.setStyle("-fx-background-color: #333234; -fx-background-radius: 20");
        VBox.setVgrow(processPanel, Priority.ALWAYS);
        processPanel.getChildren().add(processGrid);

        HBox.setHgrow(processPanel, Priority.ALWAYS);
        processPanel.setMaxWidth(Double.MAX_VALUE);

    }

    public Pane getEditor(){
        setUpPanes();

        return mainPanel;
    }

    public Pane getTemplate(){
        HBox toReturn = new HBox();

        toReturn.getChildren().addAll(ProcTemplate.get_Shape_Shape(event -> {
            setUpProcessPanelWith_Shape_Shape();

            if(mainPanel.getChildren().size() == 1){
               mainPanel.getChildren().add(0,processPanel);
           }else if(mainPanel.getChildren().size() == 2){
               mainPanel.getChildren().remove(0);
               mainPanel.getChildren().add(0, processPanel);
           }
        }) , ProcTemplate.get_Shape_Shape_Proc(event -> {
            setUpProcessPanelWith_Shape_Shape_Proc();

            if(mainPanel.getChildren().size() == 1){
                mainPanel.getChildren().add(0,processPanel);
            }else if(mainPanel.getChildren().size() == 2){
                mainPanel.getChildren().remove(0);
                mainPanel.getChildren().add(0, processPanel);
            }
        }), ProcTemplate.get_Bool_Shape_Shape(event -> {
            setUpProcessPanelWith_Bool_Shape_Shape();

            if(mainPanel.getChildren().size() == 1){
                mainPanel.getChildren().add(0,processPanel);
            }else if(mainPanel.getChildren().size() == 2){
                mainPanel.getChildren().remove(0);
                mainPanel.getChildren().add(0, processPanel);
            }
        }), ProcTemplate.get_Bool_Shape_Shape_Proc(event -> {
            setUpProcessPanelWith_Bool_Shape_Shape_Proc();

            if(mainPanel.getChildren().size() == 1){
                mainPanel.getChildren().add(0,processPanel);
            }else if(mainPanel.getChildren().size() == 2){
                mainPanel.getChildren().remove(0);
                mainPanel.getChildren().add(0, processPanel);
            }
        }));


        toReturn.setSpacing(20);
        toReturn.setAlignment(Pos.CENTER);

        return toReturn;
    }

    public void openPopup(){
        scene.getRoot().setCache(true);
        scene.getRoot().setCacheHint(CacheHint.SPEED);
        startBlurAnimation(scene.getRoot(), 0.0, 30.0, Duration.millis(100), false);
        scene.getRoot().setCache(false);
        scene.getRoot().setCacheHint(CacheHint.DEFAULT);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(StartMenu.primaryStage);

        Scene dialogScene = new Scene(getEditor(), 1280, 720);
        //Scene dialogScene = new Scene(getTemplate(), 230, 240);

        stage.setScene(dialogScene);
        stage.show();

        stage.sizeToScene();
        stage.setTitle("Shape Rules Editor");

        stage.setResizable(true);

        stage.setOnCloseRequest(event -> {
            scene.getRoot().setCache(true);
            scene.getRoot().setCacheHint(CacheHint.SPEED);
            startBlurAnimation(scene.getRoot(), 30.0, 0.0, Duration.millis(100), true);
            scene.getRoot().setCache(false);
            scene.getRoot().setCacheHint(CacheHint.DEFAULT);
        });


    }

}
