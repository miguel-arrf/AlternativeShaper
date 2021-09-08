package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import static marrf.iscte.App.getAnchorPaneClip;
import static marrf.iscte.GridCanvas.NUMBER_COLUMNS_AND_ROWS;
import static marrf.iscte.GridCanvas.SCALE;
import static marrf.iscte.PopupWindow.startBlurAnimation;

public class ProcessesEditor {

    private Stage stage = new Stage();
    private Scene scene;

    private final VBox mainPanel = new VBox();
    private final HBox processPanel = new HBox();

    private final ScrollPane horizontalScrollPane = new ScrollPane();
    private final HBox processes = new HBox();
    private final HBox processGrid = new HBox();

    private final ArrayList<NewCompositionShape> newCompositionShapes;
    private final ArrayList<BasicShape> basicShapes;

    private SmallGridCanvas leftGrid;
    private SmallGridCanvas rightGrid;

    private Orchestrator orchestrator;

    private HBox translationXSection;
    private HBox translationYSection;

    private Arrow arrow = new Arrow();


    public ProcessesEditor(Scene scene, ArrayList<NewCompositionShape> newCompositionShapes, ArrayList<BasicShape> basicShapes, Orchestrator orchestrator){
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

    private ComboBox<String> getComboBox(){
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(getCompositionShapesStringArray());
        comboBox.getItems().addAll(getBasicShapesStringArray());

        comboBox.setMaxWidth(Double.MAX_VALUE);

        comboBox.setOnAction(event -> {
            leftGrid.clearEverything();
            rightGrid.clearEverything();
            resetSliders();
            String value = comboBox.getValue();



            if(getCompositionShapesStringArray().contains(value)){

                NewCompositionShape originalCompositionShape = newCompositionShapes.stream().filter( p -> p.getShapeName().equals(value)).findFirst().get();

                originalCompositionShape.getBasicShapes().forEach(p -> rightGrid.addShape(p));
                originalCompositionShape.getBasicShapes().forEach(p -> leftGrid.addShape(p));

                Pane toAdd = new Pane();
                originalCompositionShape.getTeste(toAdd, true, 0,0);

                rightGrid.addGroup(toAdd);

                Pane toAddLeft = new Pane();
                originalCompositionShape.getTeste(toAddLeft, true, 0,0);

                leftGrid.addGroup(toAddLeft);

            }else if(getBasicShapesStringArray().contains(value)){
                BasicShape originalBasicShape = basicShapes.stream().filter(p -> p.getShapeName().equals(value)).findFirst().get();

                BasicShape leftBasicShapeCopy = orchestrator.getCopyOfBasicShape(originalBasicShape.getUUID().toString(), a -> 0.0, a -> 0.0, a -> 0.0);
                BasicShape rightBasicShapeCopy = orchestrator.getCopyOfBasicShape(originalBasicShape.getUUID().toString(), a -> 0.0, a -> 0.0, a -> 0.0);

                leftGrid.addShape(leftBasicShapeCopy);

                rightGrid.addShape(rightBasicShapeCopy);
            }

            rightGrid.addArrow(arrow);


        });

        return comboBox;
    }

    private void translationXPanel(){
        Label translationLabel = new Label("Translation X:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField("0");
        textField.setPromptText(String.valueOf("0"));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Slider translationXSlider = new Slider();
        translationXSlider.setMax(SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationXSlider.setMin(- SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationXSlider.setValue(0);

        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < translationXSlider.getMin()) {
                        textField.setText(String.valueOf(translationXSlider.getMin()));
                    }

                    translationXSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(translationXSlider.getMin()));
                    translationXSlider.setValue(translationXSlider.getMin());
                }

            }
        });

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

            rightGrid.addTranslation( (newValue.doubleValue() - oldValue.doubleValue()), 0);
            arrow.setEndX(arrow.getEndX() + (newValue.doubleValue() - oldValue.doubleValue()) );
        });

        translationXSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationXSlider, Priority.ALWAYS);

        translationXSection.getChildren().addAll(translationLabel, translationXSlider, textField);
        translationXSection.setPadding(new Insets(10, 10, 10, 15));
        translationXSection.setAlignment(Pos.CENTER_LEFT);
        translationXSection.setMinHeight(30);
        translationXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        translationXSection.setSpacing(20);

    }

    private void translationYPanel(){
        Label translationLabel = new Label("Translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField("0");
        textField.setPromptText("0");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Slider translationYSlider = new Slider();
        translationYSlider.setMax(SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationYSlider.setMin(- SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationYSlider.setValue(0);

        translationYSlider.setMajorTickUnit(0.1);
        translationYSlider.setMinorTickCount(0);
        translationYSlider.setSnapToTicks(true);

        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < translationYSlider.getMin()) {
                        textField.setText(String.valueOf(translationYSlider.getMin()));
                    }

                    translationYSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(translationYSlider.getMin()));
                    translationYSlider.setValue(translationYSlider.getMin());
                }

            }
        });


        translationYSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);

            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            rightGrid.addTranslation(0, (newValue.doubleValue() - oldValue.doubleValue()));
            arrow.setEndY(arrow.getEndY() + (newValue.doubleValue() - oldValue.doubleValue()) );

        });

        translationYSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationYSlider, Priority.ALWAYS);


        translationYSection.getChildren().addAll(translationLabel, translationYSlider, textField);
        translationYSection.setPadding(new Insets(10, 10, 10, 15));
        translationYSection.setAlignment(Pos.CENTER_LEFT);
        translationYSection.setMinHeight(30);
        translationYSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        translationYSection.setSpacing(15);

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

    private Pane getLeftPane(){
        VBox vBox = new VBox();
        vBox.setSpacing(10);

        vBox.getChildren().addAll(getLeftGridPane(), getComboBox());

        return vBox;
    }

    private Pane getRightPane(){
        VBox vBox = new VBox();
        vBox.setSpacing(10);

        translationYPanel();
        translationXPanel();

        vBox.getChildren().addAll(getRightGridPane(), translationXSection, translationYSection);

        return vBox;
    }

    private void resetSliders(){
        translationXSection.getChildren().clear();
        translationYSection.getChildren().clear();
        translationXPanel();
        translationYPanel();
    }


    private Pane getButton(){

        Label complexShape = new Label("Bool");
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web("#EB5757"));

        HBox complexShapeHBox = new HBox(complexShape);
        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: #5E2323;-fx-background-radius: 20");
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        complexShapeHBox.setMaxHeight(50);
        complexShapeHBox.setPrefHeight(50);

        complexShapeHBox.setOnMouseClicked(event -> {
            System.out.println("yey");

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            File file = new File("/Users/miguelferreira/Downloads/blockly-samples-master/examples/getting-started-codelab/starter-code/novoHtml.html");
            webEngine.load(file.toURI().toString());

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

    private void setUpPanes(){
        translationYSection = new HBox();
        translationXSection = new HBox();

        processGrid.setSpacing(20);
        processGrid.getChildren().addAll(getLeftPane(), getButton(), getRightPane());
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

        mainPanel.getChildren().addAll(processPanel, horizontalScrollPane);

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

    public void openPopup(){
        scene.getRoot().setCache(true);
        scene.getRoot().setCacheHint(CacheHint.SPEED);
        startBlurAnimation(scene.getRoot(), 0.0, 30.0, Duration.millis(100), false);
        scene.getRoot().setCache(false);
        scene.getRoot().setCacheHint(CacheHint.DEFAULT);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(StartMenu.primaryStage);

        Scene dialogScene = new Scene(getEditor(), 1280, 720);
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
    }


}
