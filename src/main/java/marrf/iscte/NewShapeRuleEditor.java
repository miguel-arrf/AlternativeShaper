package marrf.iscte;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
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
import marrf.iscte.ShapeRules.*;
import org.json.simple.JSONArray;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static marrf.iscte.App.getAnchorPaneClip;
import static marrf.iscte.PopupWindow.startBlurAnimation;
import static marrf.iscte.Power.styleLabel;

public class NewShapeRuleEditor {

    private final Stage stage = new Stage();
    private final Scene scene;

    private final ArrayList<NewCompositionShape> newCompositionShapes;
    private final ArrayList<BasicShape> basicShapes;
    private final ArrayList<Power> powerShapes;
    private final ArrayList<ParametricCompositionShape> parametricCompositionShapes;

    private final Orchestrator orchestrator;

    private final ArrayList<ShapeRule> shapeRules;
    private ShapeRule currentShapeRule;
    private final ObservableList<ShapeRule> scrollBarThumbnails = FXCollections.observableList(new ArrayList<>());

    //GUI
    private VBox mainPanel;

    private HBox editorPanel;
    private SmallGridCanvas leftGrid;
    private SmallGridCanvas rightGrid;

    private final VBox leftTranslationSection = new VBox();
    private final VBox rightTranslationSection = new VBox();

    private HBox nameAndSaveHBox;
    private TextField nameTextField;
    private HBox saveButton;

    private HBox newShapeRuleButton;

    private ScrollPane horizontalScrollPane;
    private HBox shapeRulesPanel;


    public NewShapeRuleEditor(Scene scene, ArrayList<NewCompositionShape> newCompositionShapes, ArrayList<BasicShape> basicShapes, ArrayList<ParametricCompositionShape> parametricCompositionShapes, ArrayList<Power> powerShapes, Orchestrator orchestrator){
        this.scene = scene;
        this.parametricCompositionShapes = parametricCompositionShapes;
        this.powerShapes = powerShapes;
        this.newCompositionShapes = newCompositionShapes;
        this.basicShapes = basicShapes;
        this.orchestrator = orchestrator;
        this.shapeRules = orchestrator.getShapeRules();
    }

    private void setUpHorizontalScrollPaneAndShapeRulesPanel(){
        shapeRulesPanel = new HBox();
        horizontalScrollPane = new ScrollPane();
        horizontalScrollPane.setFitToHeight(true);

        horizontalScrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

        horizontalScrollPane.setStyle("-fx-background-color: #232225; -fx-background-radius: 10; -fx-background: transparent");

        shapeRulesPanel.setPadding(new Insets(10));
        horizontalScrollPane.setContent(shapeRulesPanel);


        horizontalScrollPane.setMinHeight(60);
        shapeRulesPanel.setMinHeight(60);

        shapeRulesPanel.setAlignment(Pos.CENTER_LEFT);
        shapeRulesPanel.setSpacing(20);

        scrollBarThumbnails.addListener((ListChangeListener<? super ShapeRule>) change -> {
            while (change.next()){
                if (change.wasRemoved()){
                    for(ShapeRule removedShapeRule : change.getRemoved()){
                        Node toRemove = removedShapeRule.getThumbnail();
                        shapeRulesPanel.getChildren().remove(toRemove);
                    }
                }

                if (change.wasAdded()){

                    for(ShapeRule shapeAdded : change.getAddedSubList()){
                        Node checkIfExists = shapeAdded.getThumbnail();
                        shapeRulesPanel.getChildren().remove(checkIfExists);
                        shapeRulesPanel.getChildren().add(checkIfExists);

                        checkIfExists.setOnMouseClicked(mouseEvent -> {
                            currentShapeRule = shapeAdded;
                            nameTextField.setText(shapeAdded.getShapeRuleName());

                            if( currentShapeRule instanceof ShapeShape){
                                setUpShapeShape((ShapeShape) currentShapeRule);

                            }else if(currentShapeRule instanceof BoolShapeShape){
                                setUpBoolShapeShape((BoolShapeShape) currentShapeRule);

                            }else if(currentShapeRule instanceof ShapeShapeProc){
                                setUpShapeShapeProc((ShapeShapeProc) currentShapeRule);

                            }else if(currentShapeRule instanceof BoolShapeShapeProc){
                                setUpBoolShapeShapeProc((BoolShapeShapeProc) currentShapeRule);

                            }

                        });

                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem menuItem = new MenuItem("Delete Process");
                        menuItem.setStyle("-fx-text-fill: red");
                        contextMenu.getItems().add(menuItem);

                        menuItem.setOnAction(actionEvent -> {
                            getProceedWhenDeleting().apply(shapeAdded.getId());
                        });

                        checkIfExists.setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(checkIfExists, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));

                    }

                }
            }

            if(scrollBarThumbnails.size() == 0 ){
                System.out.println("THIS IS THE LAST ONE!");
                mainPanel.getChildren().remove(nameAndSaveHBox);
                mainPanel.getChildren().remove(editorPanel);
                mainPanel.getChildren().remove(horizontalScrollPane);

            }else{
                if(!mainPanel.getChildren().contains(horizontalScrollPane)){
                    mainPanel.getChildren().add(horizontalScrollPane);
                }
            }


        });
    }

    private Function<UUID, Double> getProceedWhenDeleting(){
        return idToRemove -> {

            ShapeRule temp = shapeRules.stream().filter(shapeRule -> shapeRule.getId().equals(idToRemove)).findFirst().get();
            scrollBarThumbnails.remove(temp);
            shapeRules.remove(temp);

            if(shapeRules.size() == 0){
                System.out.println("No shape rules left!");
                System.err.println("what to do now?");
                clearTranslationSections();
                editorPanel.getChildren().clear();
            }else{
                selectTheFirstProcess();
            }

            return null;
        };
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
        getAnchorPaneClip(anchorPane, "#232225");

        pane.widthProperty().addListener((observable, oldValue, newValue) -> anchorPane.setPrefWidth(newValue.doubleValue()));

        pane.heightProperty().addListener((observable, oldValue, newValue) -> anchorPane.setPrefHeight(newValue.doubleValue()));

        pane.setStyle("-fx-background-radius: 20; -fx-border-color: #4F4F4F; -fx-border-radius: 20;");


        return pane;
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


        getPowerShapesStringArray().forEach(p -> {
            Label basicShapeLabel = new Label(p);
            basicShapeLabel.setPadding(new Insets(5));
            basicShapeLabel.setStyle("-fx-background-color: #703636; -fx-background-radius: 10");
            basicShapeLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
            basicShapeLabel.setTextFill(Color.WHITE);

            basicShapeLabel.setOnMouseEntered(mouseEvent -> basicShapeLabel.setStyle("-fx-background-color: #F96767; -fx-background-radius: 10"));

            basicShapeLabel.setOnMouseExited(mouseEvent -> basicShapeLabel.setStyle("-fx-background-color: #703636; -fx-background-radius: 10"));

            basicShapeLabel.setOnMouseClicked(mouseEvent -> {
                Power originalBasicShape = powerShapes.stream().filter(a -> a.getShapeName().equals(p)).findFirst().get();

                //UUID toPut = UUID.randomUUID();

                Power shapeToAdd = smallGridCanvas.getCompositionShape().addPowerShape(originalBasicShape.getUUID().toString());
                shapeToAdd.setOnMouseClicked(mouseEvent1 -> {
                    toAddTo.getChildren().clear();
                    toAddTo.getChildren().addAll(shapeToAdd.getRealTranslationSection());
                });

                smallGridCanvas.addPowerShape(shapeToAdd);

            });

            horizontal.getChildren().add(basicShapeLabel);
            basicShapeLabel.setMinWidth(Region.USE_PREF_SIZE);

        });

        getCompositionShapesStringArray().forEach(p -> {
            Label compositionShapeLabel = new Label(p);
            compositionShapeLabel.setPadding(new Insets(5));
            compositionShapeLabel.setStyle("-fx-background-color: rgb(3,108,130); -fx-background-radius: 10");
            compositionShapeLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
            compositionShapeLabel.setTextFill(Color.WHITE);

            compositionShapeLabel.setOnMouseEntered(mouseEvent -> compositionShapeLabel.setStyle("-fx-background-color: rgb(0,149,181); -fx-background-radius: 10"));

            compositionShapeLabel.setOnMouseExited(mouseEvent -> compositionShapeLabel.setStyle("-fx-background-color: rgb(3,108,130); -fx-background-radius: 10"));

            compositionShapeLabel.setOnMouseClicked(mouseEvent -> {
                NewCompositionShape originalCompositionShape = newCompositionShapes.stream().filter( a -> a.getShapeName().equals(p)).findFirst().get();

                smallGridCanvas.addGroup(smallGridCanvas.getCompositionShape().addNewCompositionShape(originalCompositionShape), originalCompositionShape);

            });

            horizontal.getChildren().add(compositionShapeLabel);
            compositionShapeLabel.setMinWidth(Region.USE_PREF_SIZE);

        });


        getParametricShapesStringArray().forEach(p -> {
            Label compositionShapeLabel = new Label(p);
            compositionShapeLabel.setPadding(new Insets(5));
            compositionShapeLabel.setStyle("-fx-background-color: #5F3670; -fx-background-radius: 10");
            compositionShapeLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
            compositionShapeLabel.setTextFill(Color.WHITE);

            compositionShapeLabel.setOnMouseEntered(mouseEvent -> compositionShapeLabel.setStyle("-fx-background-color: #F967A8; -fx-background-radius: 10"));

            compositionShapeLabel.setOnMouseExited(mouseEvent -> compositionShapeLabel.setStyle("-fx-background-color: #5F3670; -fx-background-radius: 10"));

            compositionShapeLabel.setOnMouseClicked(mouseEvent -> {
                ParametricCompositionShape originalCompositionShape = parametricCompositionShapes.stream().filter( a -> a.getShapeName().equals(p)).findFirst().get();

                smallGridCanvas.addGroupParametric(smallGridCanvas.getCompositionShape().addParametricCompositionShape(originalCompositionShape), originalCompositionShape);

            });

            horizontal.getChildren().add(compositionShapeLabel);
            compositionShapeLabel.setMinWidth(Region.USE_PREF_SIZE);

        });


        scrollPane.setContent(horizontal);
        scrollPane.setMinHeight(50);

        return scrollPane;
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
        getAnchorPaneClip(anchorPane, "#232225");

        pane.widthProperty().addListener((observable, oldValue, newValue) -> anchorPane.setPrefWidth(newValue.doubleValue()));

        pane.heightProperty().addListener((observable, oldValue, newValue) -> anchorPane.setPrefHeight(newValue.doubleValue()));

        pane.setStyle("-fx-background-radius: 20; -fx-border-color: #4F4F4F; -fx-border-radius: 20;");


        return pane;
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

    private ArrayList<String> getPowerShapesStringArray(){
        ArrayList<String> toReturn = new ArrayList<>();
        powerShapes.forEach(p -> toReturn.add(p.getShapeName()));
        return toReturn;
    }

    private ArrayList<String> getParametricShapesStringArray(){
        ArrayList<String> toReturn = new ArrayList<>();
        parametricCompositionShapes.forEach(p -> toReturn.add(p.getShapeName()));
        return toReturn;
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

    public static String getShapesJSON(ArrayList<BasicShape> basicShapes, ArrayList<NewCompositionShape> newCompositionShapes, ArrayList<Power> powerShapes, ArrayList<ParametricCompositionShape> parametricCompositionShapes){
        StringBuilder toReturn = new StringBuilder();

        basicShapes.forEach(basicShape -> {
            toReturn.append("[\n");

            toReturn.append("\"").append(basicShape.getShapeName()).append("\",\n");
            toReturn.append("\"").append(basicShape.getShapeName()).append("\"\n");

            toReturn.append("],\n");

        });

        powerShapes.forEach(basicShape -> {
            toReturn.append("[\n");

            toReturn.append("\"").append(basicShape.getShapeName()).append("\",\n");
            toReturn.append("\"").append(basicShape.getShapeName()).append("\"\n");

            toReturn.append("],\n");

        });

        parametricCompositionShapes.forEach(basicShape -> {
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

    public  String getProcessAndRulesJSON(){
        StringBuilder toReturn = new StringBuilder();

        orchestrator.getProcesses().forEach(process -> {
            toReturn.append("[\n");

            toReturn.append("\"").append(process.getProcessName()).append("\",\n");
            toReturn.append("\"").append(process.getProcessName()).append("\"\n");

            toReturn.append("],\n");
        });

        orchestrator.getShapeRules().forEach(shapeRule -> {
            toReturn.append("[\n");

            toReturn.append("\"").append(shapeRule.getShapeRuleName()).append("\",\n");
            toReturn.append("\"").append(shapeRule.getShapeRuleName()).append("\"\n");

            toReturn.append("],\n");
        });

        return toReturn.toString();
    }
    public JSONArray getProcessAndRulesJSONToJSON(){
        JSONArray jsonArray = new JSONArray();

        orchestrator.getProcesses().forEach(process -> {
            JSONArray innerJSONArray = new JSONArray();

            innerJSONArray.add(process.getProcessName());
            innerJSONArray.add(process.getProcessName());


            jsonArray.add(innerJSONArray);
        });

        orchestrator.getShapeRules().forEach(shapeRule -> {
            JSONArray innerJSONArray = new JSONArray();

            innerJSONArray.add(shapeRule.getShapeRuleName());
            innerJSONArray.add(shapeRule.getShapeRuleName());


            jsonArray.add(innerJSONArray);
        });
        return jsonArray;
    }

    private void updateProcessesAndRulesNames(WebView webView){
        JSONArray processAndRulesToSend = getProcessAndRulesJSONToJSON();
        System.out.println("vou colocar: ");
        System.out.println(processAndRulesToSend.toJSONString());
        webView.getEngine().executeScript("updateProcessesAndRulesNames('" + processAndRulesToSend.toJSONString() +"')");
        System.out.println("updateProcessesAndRulesNamesToolBox: ");
        System.out.println( processAndRulesToSend.toJSONString());
        webView.getEngine().executeScript("updateProcessesAndRulesNamesToolBox('" + processAndRulesToSend.toJSONString() +"')");
        //webView.getEngine().executeScript("addUpdateProcessesAndRulesNamesSectionToToolBox()");
    }

    private File setUpFiles(String fileName){
        Path htmlOriginal = Paths.get(Orchestrator.path + "/" + fileName + ".html");

        File directory = new File(Orchestrator.path + "/");
        for (File childrenFile : directory.listFiles()){
            if(childrenFile.getName().contains("Copied_")){
                childrenFile.delete();
            }
        }

        int randomNum = ThreadLocalRandom.current().nextInt(0, 100 + 1);

        Path htmlCopied = Paths.get(Orchestrator.path + "/" + fileName + "Copied"+ "_" + randomNum + ".html");

        try{
            Files.copy(htmlOriginal, htmlCopied, StandardCopyOption.REPLACE_EXISTING);

            String fileContentJS = new String(Files.readAllBytes(htmlCopied));

            if(orchestrator.getProcesses().size() != 0 || orchestrator.getShapeRules().size() != 0){
                fileContentJS = fileContentJS.replace("<!--CHANGE_HERE_3-->", "<block type=\"availableProcessesAndRules\"></block>" );
            }


            fileContentJS = fileContentJS.replace("myBlocksCopied.js","myBlocksCopied.js?c=r_" + randomNum);
            Files.write(htmlCopied, fileContentJS.getBytes());


            Path original = Paths.get(Orchestrator.path + "/" + "scripts/myBlocks.js" );
            Path copied = Paths.get(Orchestrator.path + "/" + "scripts/myBlocksCopied.js" );

            Files.copy(original, copied, StandardCopyOption.REPLACE_EXISTING);


            String fileContent = new String(Files.readAllBytes(copied));
            fileContent = fileContent.replace("//CHANGE_HERE1", "{\n" +
                    "  \"type\": \"availableshapes\",\n" +
                    "  \"message0\": \"%1\",\n" +
                    "  \"args0\": [\n" +
                    "    {\n" +
                    "      \"type\": \"field_dropdown\",\n" +
                    "      \"name\": \"NAME\",\n" +
                    "      \"options\": [\n" +
                    "       " + getShapesJSON(orchestrator.getBasicShapes(), orchestrator.getNewCompositionShapes(), orchestrator.getPowerShapes(), orchestrator.getParametricCompositionShapes()) +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"output\": \"shape\",\n" +
                    "  \"colour\": 180,\n" +
                    "  \"tooltip\": \"\",\n" +
                    "  \"helpUrl\": \"\"\n" +
                    "},");

            fileContent = fileContent.replace("//CHANGE_HERE2", "{\n" +
                    "  \"type\": \"availableProcessesAndRules\",\n" +
                    "  \"message0\": \"%1\",\n" +
                    "  \"args0\": [\n" +
                    "    {\n" +
                    "      \"type\": \"field_dropdown\",\n" +
                    "      \"name\": \"NAME\",\n" +
                    "      \"options\": [\n" +
                    "       " + getProcessAndRulesJSON() +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"output\": null,\n" +
                    "  \"colour\": 20,\n" +
                    "  \"tooltip\": \"\",\n" +
                    "  \"helpUrl\": \"\"\n" +
                    "},");


            Files.write(copied, fileContent.getBytes());


        }catch (Exception e){

        }

        return htmlCopied.toFile();
    }

    private Pane getWebViewSaveButton(){
        Label saveLabel = new Label("Save");
        saveLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        saveLabel.setTextFill(Color.web("#6FCF97"));

        HBox saveButton = new HBox(saveLabel);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setPadding(new Insets(10));
        saveButton.setAlignment(Pos.CENTER);
        saveButton.setMaxHeight(50);
        saveButton.setPrefHeight(50);

        saveButton.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 10");

        saveButton.setOnMouseEntered(event -> saveButton.setStyle("-fx-background-color: #078D55;-fx-background-radius: 10"));
        saveButton.setOnMouseExited(event -> saveButton.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 10"));

        return saveButton;
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




        Label checkboxLabel = new Label("Matched: ");
        styleLabel(checkboxLabel);
        checkboxLabel.setMinWidth(Region.USE_PREF_SIZE);


        CheckBox checkboxToUse = new CheckBox();
        checkboxToUse.setSelected(true);

        HBox toPut = new HBox(checkboxLabel, checkboxToUse);
        checkboxToUse.setSelected(currentShapeRule.getMatched());
        toPut.setSpacing(20);

        complexShapeHBox.setOnMouseClicked(event -> {

            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(stage);

            VBox vBox = new VBox();
            vBox.setSpacing(20);
            vBox.setStyle("-fx-background-color: #191919");
            vBox.setPadding(new Insets(20));


            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            File boolFile = setUpFiles(isItBool ? "bool" : "proc1");

            webEngine.load(boolFile.toURI().toString());
            System.out.println("a carregar: " + boolFile.toURI().toString());
            webEngine.reload();


            checkboxToUse.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                currentShapeRule.setMatched(newValue);
                webEngine.executeScript("setMatched(" + newValue +")");
            });

            System.out.println("loading current saved");
            if(isItBool){
                if(currentShapeRule.getBoolXML() != null){
                    startBlurAnimation(vBox, 0.0, 30.0, Duration.millis(100), false);

                    System.out.println(currentShapeRule.getBoolXML());

                    webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
                        if(t1 == Worker.State.SUCCEEDED){
                            webView.getEngine().executeScript("novoTeste('"+ currentShapeRule.getBoolXML() +"')");
                            startBlurAnimation(vBox, 30.0, 0.0, Duration.millis(100), false);
                            updateProcessesAndRulesNames(webView);

                        }
                    });

                }

            }else{
                if(currentShapeRule.getProcessXML() != null){
                    startBlurAnimation(vBox, 0.0, 30.0, Duration.millis(100), false);

                    webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
                        if(t1 == Worker.State.SUCCEEDED){
                            webView.getEngine().executeScript("novoTeste('"+ currentShapeRule.getProcessXML() +"')");
                            startBlurAnimation(vBox, 30.0, 0.0, Duration.millis(100), false);
                            updateProcessesAndRulesNames(webView);

                        }
                    });

                }

            }
            System.out.println("ended current saved");



            Pane button = getWebViewSaveButton();
            System.out.println("cliquei!");
            button.setOnMouseClicked(mouseEvent -> {
                int canSave = (int) webView.getEngine().executeScript("numberOfNonConnectedBlocks()");
                if(canSave == 1 || canSave == 0){
                    String workspaceXML = (String) webView.getEngine().executeScript("teste()");
                    String code = webView.getEngine().executeScript("getCode()").toString();
                    System.out.println("saved code: " + code);
                    if(isItBool){
                        if(currentShapeRule.getMatched()){

                            String shapeNameToReplace = "";

                            System.out.println("compoisitonShapeSize: " + currentShapeRule.getLeftShapeCopy().getCompositionShapesUUIDList().size());

                            if(currentShapeRule.getLeftShapeCopy().getCompositionShapesUUIDList().size() == 1){
                                Map<String, NewCompositionShape> map = currentShapeRule.getLeftShapeCopy().getCompositionShapeMap();
                                for(var entry : map.entrySet()){
                                    shapeNameToReplace = entry.getValue().getShapeName();
                                    break;
                                }

                            }

                            System.err.println("we need to replace because of the matched: " + shapeNameToReplace);
                            code = code.replace("shape(" + shapeNameToReplace + ")","shape(matched(" + shapeNameToReplace + "))" );
                            System.out.println("new code: " + code);
                        }
                        currentShapeRule.setBoolCode(code);
                        currentShapeRule.setBoolXML(workspaceXML);
                    }else{
                        currentShapeRule.setProcessCode(code);
                        currentShapeRule.setProcessXML(workspaceXML);
                    }


                }

            });

            HBox hBox = new HBox(button, checkboxLabel, checkboxToUse);
            HBox.setHgrow(button, Priority.ALWAYS);
            HBox.setHgrow(hBox, Priority.ALWAYS);

            vBox.getChildren().addAll(webView, hBox);
            VBox.setVgrow(webView, Priority.ALWAYS);

            Scene dialogScene = new Scene(vBox, 1280, 720);
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

    private void clearTranslationSections(){
        leftTranslationSection.getChildren().clear();
        rightTranslationSection.getChildren().clear();
    }

    private void addRemovedPanes(){
        if(!mainPanel.getChildren().contains(editorPanel))
            mainPanel.getChildren().add(0, editorPanel);

        if(!mainPanel.getChildren().contains(nameAndSaveHBox))
            mainPanel.getChildren().add(1, nameAndSaveHBox);
    }

    private void setUpShapeShape(){
        clearTranslationSections();
        addRemovedPanes();
        editorPanel.getChildren().clear();
        editorPanel.getChildren().addAll(getLeftPane(),getArrowPane(), getRightPane());
    }

    private void setUpBoolShapeShape(){
        clearTranslationSections();
        addRemovedPanes();
        editorPanel.getChildren().clear();
        editorPanel.getChildren().addAll(getButton(true), getLeftPane(),getArrowPane(), getRightPane());
    }

    private void setUpShapeShapeProc(){
        clearTranslationSections();
        addRemovedPanes();
        editorPanel.getChildren().clear();
        editorPanel.getChildren().addAll(getLeftPane(),getArrowPane(), getRightPane(), getButton(false));
    }

    private void setUpBoolShapeShapeProc(){
        clearTranslationSections();
        addRemovedPanes();
        editorPanel.getChildren().clear();
        editorPanel.getChildren().addAll(getButton(true), getLeftPane(),getArrowPane(), getRightPane(), getButton(false));
    }

    private void setUpShapeShapeProc(ShapeShapeProc shapeShapeProc){
        setUpShapeShapeProc();
        addShapeRuleShapesToGrid(shapeShapeProc);
    }

    private void setUpBoolShapeShapeProc(BoolShapeShapeProc boolShapeShapeProc){
        setUpBoolShapeShapeProc();
        addShapeRuleShapesToGrid(boolShapeShapeProc);
    }

    private void setUpBoolShapeShape(BoolShapeShape boolShapeShape){
        setUpBoolShapeShape();
        addShapeRuleShapesToGrid(boolShapeShape);
    }

    private void addShapeRuleShapesToGrid(ShapeRule shapeRule){
        shapeRule.getRightShape().setTransformersBox(rightTranslationSection);
        shapeRule.getLeftShape().setTransformersBox(leftTranslationSection);

        shapeRule.setRightShapeCopy(shapeRule.getRightShape().getCopy());
        shapeRule.setLeftShapeCopy(shapeRule.getLeftShape().getCopy());

        leftGrid.setCompositionShape(shapeRule.getLeftShapeCopy());
        rightGrid.setCompositionShape(shapeRule.getRightShapeCopy());


        shapeRule.getLeftShapeCopy().getPowerShapes().forEach(shape -> {
            shape.setOnMouseClicked(mouseEvent1 -> {
                leftTranslationSection.getChildren().clear();
                leftTranslationSection.getChildren().addAll(shape.getRealTranslationSection());
            });

            leftGrid.addPowerShape(shape);
        });



        Pane toAdd = new Pane();
        shapeRule.getLeftShapeCopy().getTeste(toAdd, true, 0,0, null);
        leftGrid.addGroupParametric(toAdd, shapeRule.getLeftShapeCopy());



        shapeRule.getRightShapeCopy().getPowerShapes().forEach(shape -> {
            shape.setOnMouseClicked(mouseEvent1 -> {
                rightTranslationSection.getChildren().clear();
                rightTranslationSection.getChildren().addAll(shape.getRealTranslationSection());
            });

            rightGrid.addPowerShape(shape);
        });


        Pane toAddRight = new Pane();
        shapeRule.getRightShapeCopy().getTeste(toAddRight, true, 0,0, null);
        rightGrid.addGroupParametric(toAddRight, shapeRule.getRightShapeCopy());



    }

    private void setUpShapeShape(ShapeShape shape){
        setUpShapeShape();
        addShapeRuleShapesToGrid(shape);
    }

    private void setUpNewShapeRuleButton(){
        Label saveLabel = new Label("New Shape Rule");
        saveLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        saveLabel.setTextFill(Color.web("#E69C59"));

        newShapeRuleButton = new HBox(saveLabel);
        HBox.setHgrow(newShapeRuleButton, Priority.ALWAYS);
        VBox.setVgrow(newShapeRuleButton, Priority.ALWAYS);
        newShapeRuleButton.setPadding(new Insets(10));
        newShapeRuleButton.setAlignment(Pos.CENTER);
        newShapeRuleButton.setStyle("-fx-background-color: #7A532F;-fx-background-radius: 10");

        newShapeRuleButton.setMinWidth(230);

        ContextMenu contextMenu = new ContextMenu();

        contextMenu.setId("shapeRuleEditorContextMenu");

        CustomMenuItem menuItem_shape_shape = new CustomMenuItem(ProcTemplate.get_Shape_Shape(null));
        CustomMenuItem menuItem_shape_shape_proc = new CustomMenuItem(ProcTemplate.get_Shape_Shape_Proc(null));
        CustomMenuItem menuItem_bool_shape_shape = new CustomMenuItem(ProcTemplate.get_Bool_Shape_Shape(null));
        CustomMenuItem menuItem_bool_shape_shape_proc = new CustomMenuItem(ProcTemplate.get_Bool_Shape_Shape_Proc(null));

        menuItem_shape_shape.setOnAction(actionEvent -> {
            currentShapeRule = new ShapeShape(orchestrator, leftTranslationSection, rightTranslationSection, a -> 0.0, a -> 0.0);

            nameTextField.setText("NO_NAME");

            setUpShapeShape();

            leftGrid.setCompositionShape(currentShapeRule.getLeftShapeCopy());
            rightGrid.setCompositionShape(currentShapeRule.getRightShapeCopy());
        });

        menuItem_shape_shape_proc.setOnAction(actionEvent -> {
            currentShapeRule = new ShapeShapeProc(orchestrator, leftTranslationSection, rightTranslationSection, a -> 0.0, a -> 0.0);

            nameTextField.setText("NO_NAME");

            setUpShapeShapeProc();

            leftGrid.setCompositionShape(currentShapeRule.getLeftShapeCopy());
            rightGrid.setCompositionShape(currentShapeRule.getRightShapeCopy());
        });

        menuItem_bool_shape_shape.setOnAction(actionEvent -> {
            currentShapeRule = new BoolShapeShape(orchestrator, leftTranslationSection, rightTranslationSection, a -> 0.0, a -> 0.0);

            nameTextField.setText("NO_NAME");

            setUpBoolShapeShape();

            leftGrid.setCompositionShape(currentShapeRule.getLeftShapeCopy());
            rightGrid.setCompositionShape(currentShapeRule.getRightShapeCopy());
        });

        menuItem_bool_shape_shape_proc.setOnAction(actionEvent -> {
            currentShapeRule = new BoolShapeShapeProc(orchestrator, leftTranslationSection, rightTranslationSection, a -> 0.0, a -> 0.0);

            nameTextField.setText("NO_NAME");

            setUpBoolShapeShapeProc();

            leftGrid.setCompositionShape(currentShapeRule.getLeftShapeCopy());
            rightGrid.setCompositionShape(currentShapeRule.getRightShapeCopy());
        });

        contextMenu.getItems().addAll(menuItem_shape_shape, menuItem_shape_shape_proc, menuItem_bool_shape_shape, menuItem_bool_shape_shape_proc );

        newShapeRuleButton.setOnMouseClicked(mouseEvent -> contextMenu.show(newShapeRuleButton, mouseEvent.getScreenX(), mouseEvent.getScreenY()));

    }

    private void setUpNameTextField(){
        nameTextField = new TextField("NO_NAME");
        nameTextField.setMaxHeight(50);
        nameTextField.setPrefHeight(50);
        nameTextField.setStyle("-fx-background-color: #333234; -fx-text-fill: #5D5C5E; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97; -fx-background-radius: 10");
        nameTextField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));

        HBox.setHgrow(nameTextField, Priority.ALWAYS);
    }

    private void setUpSaveButton(){
        Label saveLabel = new Label("Save");
        saveLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        saveLabel.setTextFill(Color.web("#6FCF97"));

        saveButton = new HBox(saveLabel);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        saveButton.setPadding(new Insets(10));
        saveButton.setAlignment(Pos.CENTER);
        saveButton.setMaxHeight(50);
        saveButton.setPrefHeight(50);

        saveButton.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 10");

        saveButton.setOnMouseClicked(mouseEvent -> {
            currentShapeRule.setShapeRuleName(nameTextField.getText());
            scrollBarThumbnails.add(currentShapeRule);

            if(shapeRules.stream().noneMatch(p -> p.equals(currentShapeRule))){
                shapeRules.add(currentShapeRule);
            }

            currentShapeRule.setRightShape(currentShapeRule.getRightShapeCopy());
            currentShapeRule.setLeftShape(currentShapeRule.getLeftShapeCopy());

        });

        saveButton.setOnMouseEntered(event -> saveButton.setStyle("-fx-background-color: #078D55;-fx-background-radius: 10"));

        saveButton.setOnMouseExited(event -> saveButton.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 10"));
    }

    private void setUpNameAndSaveHBox(){
        nameAndSaveHBox = new HBox();
        nameAndSaveHBox.setSpacing(20);
        nameAndSaveHBox.getChildren().addAll(nameTextField, saveButton);
        HBox.setHgrow(nameAndSaveHBox, Priority.ALWAYS);
    }

    private void setUpEditorPanel(){
        editorPanel = new HBox();
        editorPanel.setAlignment(Pos.CENTER);
        editorPanel.setSpacing(20);
        editorPanel.setPadding(new Insets(20));
        editorPanel.setStyle("-fx-background-color: #232225; -fx-background-radius: 10");
    }

    private void setUpPanes(){
        leftTranslationSection.setSpacing(20);
        rightTranslationSection.setSpacing(20);

        setUpHorizontalScrollPaneAndShapeRulesPanel();
        setUpNewShapeRuleButton();
        setUpNameTextField();
        setUpSaveButton();
        setUpNameAndSaveHBox();
        setUpEditorPanel();

        mainPanel = new VBox();
        mainPanel.setSpacing(20);
        mainPanel.setPadding(new Insets(20));
        mainPanel.getChildren().addAll(newShapeRuleButton);
        mainPanel.setStyle("-fx-background-color: #262528;");
    }

    public void openPopup(){
        setUpPanes();

        scene.getRoot().setCache(true);
        scene.getRoot().setCacheHint(CacheHint.SPEED);

        startBlurAnimation(scene.getRoot(), 0.0, 30.0, Duration.millis(100), false);
        scene.getRoot().setCache(false);
        scene.getRoot().setCacheHint(CacheHint.DEFAULT);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(StartMenu.primaryStage);

        Scene dialogScene = new Scene(mainPanel, 1280, 720);
        dialogScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        loadShapeRules();
        startBlurAnimation(dialogScene.getRoot(), 30.0, 0.0, Duration.millis(100), true);

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

    private void loadShapeRules(){
        if(shapeRules.size() >= 1){
            scrollBarThumbnails.addAll(shapeRules);
            selectTheFirstProcess();
        }else{
            /*
            currentShapeRule = new ShapeShape();
            currentShapeRule.setShapeRuleName(nameTextField.getText());
             */
        }
    }

    private void selectTheFirstProcess(){
        currentShapeRule = shapeRules.get(0);
        nameTextField.setText(currentShapeRule.getShapeRuleName());

        loadShapeRule();
    }

    private void loadShapeRule(){

        if( currentShapeRule instanceof ShapeShape){
            setUpShapeShape((ShapeShape) currentShapeRule);

        }else if(currentShapeRule instanceof BoolShapeShape){
            setUpBoolShapeShape((BoolShapeShape) currentShapeRule);

        }else if(currentShapeRule instanceof ShapeShapeProc){
            setUpShapeShapeProc((ShapeShapeProc) currentShapeRule);

        }else if(currentShapeRule instanceof BoolShapeShapeProc){
            setUpBoolShapeShapeProc((BoolShapeShapeProc) currentShapeRule);

        }
    }

}
