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
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static marrf.iscte.PopupWindow.startBlurAnimation;
import static marrf.iscte.ShapeRuleEditor.getShapesJSON;

public class ProcessesEditor {

    private final Stage stage = new Stage();
    private final Scene scene;

    private final VBox mainPanel = new VBox();
    private final ScrollPane processesScrollPanel = new ScrollPane();
    private final HBox processesPanel = new HBox();
    private final HBox utilitiesButtons = new HBox();
    private final HBox addButton_ProcessesScrollPanel = new HBox();

    private final ArrayList<NewCompositionShape> newCompositionShapes;
    private final ArrayList<BasicShape> basicShapes;

    private final ArrayList<Process> processes;
    private Process currentProcess;
    private final ObservableList<Process> scrollBarThumbnails = FXCollections.observableList(new ArrayList<>());

    private final Orchestrator orchestrator;

    private final WebView webView = new WebView();

    private TextField nameTextfield;

    public ProcessesEditor(Scene scene, ArrayList<NewCompositionShape> newCompositionShapes, ArrayList<BasicShape> basicShapes, Orchestrator orchestrator){
        this.scene = scene;
        this.newCompositionShapes = newCompositionShapes;
        this.basicShapes = basicShapes;
        this.orchestrator = orchestrator;
        this.processes = orchestrator.getProcesses();
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


    private Node getWebView(){
        WebEngine webEngine = webView.getEngine();
        //webEngine.setUserAgent("AppleWebKit/537.44");

        File file = setUpFiles("proc");

        webEngine.load(file.toURI().toString());

        return webView;
    }

    private Function<UUID, Double> getProceedWhenDeleting(){
        return idToRemove -> {
            Process temp = processes.stream().filter( process -> process.getId().equals(idToRemove)).findFirst().get();
            scrollBarThumbnails.remove(temp);
            processes.remove(temp);

            if(processes.size() == 0){
                System.out.println("no processes left!");
                //There's no processes left
                webView.getEngine().executeScript("clear();");
                currentProcess = new Process();
                currentProcess.setProcessName("NO_NAME");


            }else{
                //Let's select the first one...
                selectTheFirstProcess();
            }

            return null;
        };
    }


    private void setUpScrollbarThumbnails(){
        scrollBarThumbnails.addListener((ListChangeListener<? super Process>) change -> {
            while (change.next()){
                if (change.wasRemoved()){
                    for(Process processRemoved : change.getRemoved()){
                        Node toRemove = processRemoved.getThumbnail();
                        processesPanel.getChildren().remove(toRemove);
                    }
                }

                if (change.wasAdded()){

                    for(Process processAdded : change.getAddedSubList()){
                        Node checkIfExists = processAdded.getThumbnail();
                        processesPanel.getChildren().remove(checkIfExists);
                        processesPanel.getChildren().add(checkIfExists);

                        checkIfExists.setOnMouseClicked(mouseEvent -> {
                            webView.getEngine().executeScript("clear();");
                            currentProcess = processAdded;
                            nameTextfield.setText(processAdded.getProcessName());
                            webView.getEngine().executeScript("novoTeste('"+ processAdded.getBlocklyXML() +"')");
                        });

                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem menuItem = new MenuItem("Delete Process");
                        menuItem.setStyle("-fx-text-fill: red");
                        contextMenu.getItems().add(menuItem);

                        menuItem.setOnAction(actionEvent -> {
                            getProceedWhenDeleting().apply(processAdded.getId());
                        });

                        checkIfExists.setOnContextMenuRequested(contextMenuEvent -> {
                            contextMenu.show(checkIfExists, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                        });

                    }

                }
            }

            if(scrollBarThumbnails.size() == 0 ){
                Node toRetain = addButton_ProcessesScrollPanel.getChildren().get(0);
                addButton_ProcessesScrollPanel.getChildren().clear();
                addButton_ProcessesScrollPanel.getChildren().add(toRetain);
            }else{
                if(addButton_ProcessesScrollPanel.getChildren().size() != 3){
                    addButton_ProcessesScrollPanel.getChildren().addAll(getSeparator(), processesScrollPanel);
                }
            }

        });
    }

    private void setUpNameTextfieldAndSaveButton(){
        utilitiesButtons.setSpacing(20);

        nameTextfield = new TextField("NO_NAME");
        nameTextfield.setMaxHeight(50);
        nameTextfield.setPrefHeight(50);
        nameTextfield.setStyle("-fx-background-color: #333234; -fx-text-fill: #5D5C5E; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97; -fx-background-radius: 10");
        nameTextfield.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));

        HBox.setHgrow(nameTextfield, Priority.ALWAYS);

        Label saveLabel = new Label("Save");
        saveLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        saveLabel.setTextFill(Color.web("#6FCF97"));

        HBox saveHB = new HBox(saveLabel);
        HBox.setHgrow(saveHB, Priority.ALWAYS);
        saveHB.setPadding(new Insets(10));
        saveHB.setAlignment(Pos.CENTER);
        saveHB.setMaxHeight(50);
        saveHB.setPrefHeight(50);

        saveHB.setStyle("-fx-background-color: #3C5849;-fx-background-radius: 10");


        saveHB.setOnMouseClicked(mouseEvent -> {

            int canSave = (int) webView.getEngine().executeScript("numberOfNonConnectedBlocks()");


            if(canSave == 1 || canSave == 0){

                String workspaceXML = (String) webView.getEngine().executeScript("teste()");
                String code = webView.getEngine().executeScript("getCode()").toString();

                currentProcess.setProcessCode(code);
                currentProcess.setBlocklyXML(workspaceXML);
                currentProcess.setProcessName(nameTextfield.getText());

                scrollBarThumbnails.add(currentProcess);

                Process.updateOrAdd(processes, currentProcess);

                orchestrator.processesToString();
            }



        });

        utilitiesButtons.getChildren().addAll(nameTextfield, saveHB);
        HBox.setHgrow(utilitiesButtons, Priority.ALWAYS);


    }

    private void setUpProcessesScrollPane(){
        processesScrollPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        processesScrollPanel.setFitToHeight(true);
        processesScrollPanel.setStyle("-fx-background-color: transparent; -fx-background: transparent");

        processesScrollPanel.setContent(processesPanel);

        processesPanel.setAlignment(Pos.CENTER_LEFT);
        processesPanel.setSpacing(20);
    }

    private Pane getSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefWidth(4);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);
        rectangle.setId("separator");

        return rectangle;
    }

    private Pane setUpAddAndProcessesScrolPane(){
        setUpScrollbarThumbnails();
        setUpProcessesScrollPane();

        Label saveLabel = new Label("New Process");
        saveLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        saveLabel.setTextFill(Color.web("#E69C59"));

        HBox addHBox = new HBox(saveLabel);
        HBox.setHgrow(addHBox, Priority.ALWAYS);
        VBox.setVgrow(addHBox, Priority.ALWAYS);
        addHBox.setPadding(new Insets(10));
        addHBox.setAlignment(Pos.CENTER);
        addHBox.setStyle("-fx-background-color: #7A532F;-fx-background-radius: 10");

        addHBox.setMinWidth(230);

        addHBox.setOnMouseClicked(mouseEvent -> {

            currentProcess = new Process();
            nameTextfield.setText("NO_NAME");
            webView.getEngine().executeScript("clear()");
        });

        addButton_ProcessesScrollPanel.setSpacing(20);

        addButton_ProcessesScrollPanel.getChildren().addAll(addHBox/*, getSeparator(), processesScrollPanel*/);

        return addButton_ProcessesScrollPanel;
    }

    private void setUpPanes(){
        mainPanel.setPadding(new Insets(20));
        mainPanel.setSpacing(20);
        mainPanel.setStyle("-fx-background-color: #262528;");

        mainPanel.getChildren().add(getWebView());

        setUpNameTextfieldAndSaveButton();
        mainPanel.getChildren().add(utilitiesButtons);

        mainPanel.getChildren().add(setUpAddAndProcessesScrolPane());

    }

    private Pane getEditor(){
        setUpPanes();

        return mainPanel;
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
        dialogScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        //Scene dialogScene = new Scene(getTemplate(), 230, 240);

        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
            if(t1 == Worker.State.SUCCEEDED){
                System.out.println("PAGE HAS LOADED!");
                loadProcesses();
                startBlurAnimation(dialogScene.getRoot(), 30.0, 0.0, Duration.millis(100), true);

            }
        });

        stage.setScene(dialogScene);
        stage.show();

        stage.sizeToScene();
        stage.setTitle("Processes Editor");

        stage.setResizable(true);

        stage.setOnCloseRequest(event -> {
            scene.getRoot().setCache(true);
            scene.getRoot().setCacheHint(CacheHint.SPEED);
            startBlurAnimation(scene.getRoot(), 30.0, 0.0, Duration.millis(100), true);
            scene.getRoot().setCache(false);
            scene.getRoot().setCacheHint(CacheHint.DEFAULT);
        });

        GaussianBlur blur = new GaussianBlur(30.0);
        dialogScene.getRoot().setEffect(blur);


    }

    private void loadProcesses(){
        if(processes.size() >= 1){
            scrollBarThumbnails.addAll(processes);
            selectTheFirstProcess();

            //addButton_ProcessesScrollPanel.getChildren().addAll(getSeparator(), processesScrollPanel);
        }else{
            currentProcess = new Process();
            currentProcess.setProcessName(nameTextfield.getText());
            //We have no processes.
            /*Node lastChild = mainPanel.getChildren().get(mainPanel.getChildren().size() - 1);
            mainPanel.getChildren().clear();
            mainPanel.getChildren().add(lastChild);*/
        }
    }

    private void selectTheFirstProcess(){
        currentProcess = processes.get(0);
        webView.getEngine().executeScript("clear();");
        nameTextfield.setText(currentProcess.getProcessName());
        webView.getEngine().executeScript("novoTeste('"+ currentProcess.getBlocklyXML() +"')");
    }




}
