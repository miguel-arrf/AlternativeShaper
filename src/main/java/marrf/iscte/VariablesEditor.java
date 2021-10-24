package marrf.iscte;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Objects;

import static marrf.iscte.PopupWindow.startBlurAnimation;

public class VariablesEditor {

    private final Stage stage = new Stage();
    private final Scene scene;
    private final Orchestrator orchestrator;

    private final HBox mainPanel = new HBox();

    private final ObservableList<Variable> scrollBarThumbnails = FXCollections.observableList(new ArrayList<>());
    private final VBox variablesPanel = new VBox();

    private HBox saveOrEditButton = App.getButtonWith_Label_Color("New Variable", "#35654F", "#56F28F");

    private final SimpleObjectProperty<Variable> selectedVariable =  new SimpleObjectProperty<>();

    private TextField nameTextField;
    private TextField valueTextField;

    private VBox leftEditor;

    public VariablesEditor(Scene scene, Orchestrator orchestrator){
        this.scene = scene;
        this.orchestrator = orchestrator;
    }

    private void removeEditButtonAndAddNewButton(){
        leftEditor.getChildren().remove(saveOrEditButton);
        saveOrEditButton =  App.getButtonWith_Label_Color("New Variable", "#35654F", "#56F28F");
        leftEditor.getChildren().add(saveOrEditButton);
        saveOrEditButton.setMaxHeight(50);
        saveOrEditButton.setPrefHeight(50);
        saveOrEditButton.setOnMouseClicked(saveButtonEventHandler(nameTextField, valueTextField));
    }

    private void resetTextFields(){
        nameTextField.setPromptText("Name");
        valueTextField.setPromptText("0.0");

        nameTextField.setText("");
        valueTextField.setText("");
    }

    private Pane getLeftEditor(){
        leftEditor = new VBox();
        leftEditor.setPadding(new Insets(20));
        leftEditor.setSpacing(20);
        leftEditor.setStyle("-fx-background-color: #333234; -fx-background-radius: 20px");

        leftEditor.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(leftEditor, Priority.ALWAYS);

        leftEditor.setAlignment(Pos.BOTTOM_CENTER);

        nameTextField = new TextField("empty");
        valueTextField = new TextField("0.0");

        leftEditor.getChildren().addAll(getTextFieldWithLabel("Name", nameTextField), App.getSeparator(), getTextFieldWithLabel("Value", valueTextField));


        selectedVariable.addListener((observableValue, variable, t1) -> {
            if(t1 != null){
                nameTextField.setText(t1.getName());
                valueTextField.setText(t1.getValue());
                leftEditor.getChildren().remove(saveOrEditButton);
                saveOrEditButton =  App.getButtonWith_Label_Color("Update Variable", "#5B6535", "#DCF256");
                leftEditor.getChildren().add(saveOrEditButton);

                saveOrEditButton.setMaxHeight(50);
                saveOrEditButton.setPrefHeight(50);


                saveOrEditButton.setOnMouseClicked(mouseEvent -> {

                    orchestrator.getVariables().forEach(Variable::setUnClicked);

                    t1.setName(nameTextField.getText());
                    t1.setValue(valueTextField.getText());
                    t1.getThumbnail();

                    selectedVariable.set(null);

                    removeEditButtonAndAddNewButton();
                    resetTextFields();
                });


            }
        });

        saveOrEditButton.setMaxHeight(50);
        saveOrEditButton.setPrefHeight(50);

        saveOrEditButton.setOnMouseClicked(saveButtonEventHandler(nameTextField, valueTextField));

        leftEditor.getChildren().add(saveOrEditButton);

        return leftEditor;
    }

    private javafx.event.EventHandler<? super javafx.scene.input.MouseEvent> saveButtonEventHandler(TextField nameTextField, TextField valueTextField){
        return mouseEvent -> {
            orchestrator.getVariables().forEach(Variable::setUnClicked);

            Variable toAdd = new Variable(nameTextField.getText(), valueTextField.getText());
            if(orchestrator.canAddVariable(toAdd)){
                orchestrator.addVariable(toAdd);
                resetTextFields();
                scrollBarThumbnails.add(toAdd);
            }
        };
    }

    private Pane getTextFieldWithLabel(String labelString, TextField textField){
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        Label label = new Label(labelString);
        label.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        label.setTextFill(Color.WHITE);

        textField.setStyle("-fx-background-color: #A6A6A6; -fx-text-fill: #5D5C5E; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97; -fx-background-radius: 10; -fx-prompt-text-fill: #918C8D;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));

        vBox.getChildren().addAll(label, textField);

        vBox.setStyle("-fx-background-color:  #4F4F4F; -fx-background-radius: 10px");

        vBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(vBox, Priority.ALWAYS);

        return vBox;
    }

    private Pane getRightEditor(){
        VBox rightEditor = new VBox();
        rightEditor.setPadding(new Insets(20));
        rightEditor.setSpacing(20);
        rightEditor.setStyle("-fx-background-color: #333234; -fx-background-radius: 20px");

        rightEditor.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(rightEditor, Priority.ALWAYS);

        scrollBarThumbnails.addListener((ListChangeListener<? super Variable>) change -> {
            while (change.next()){

                if(change.wasRemoved()){
                    for(Variable variable: change.getRemoved()){
                        variablesPanel.getChildren().remove(variable.getThumbnail());
                    }
                }

                if(change.wasAdded()){

                    for(Variable variable: change.getAddedSubList()){

                        Node checkIfExists = variable.getThumbnail();

                        if(!variablesPanel.getChildren().contains(checkIfExists)){
                            variablesPanel.getChildren().add(checkIfExists);
                        }

                        checkIfExists.setOnMouseClicked(mouseEvent -> {
                            orchestrator.getVariables().forEach(Variable::setUnClicked);
                            variable.setClicked();
                            selectedVariable.set(variable);
                        });

                        ContextMenu contextMenu = new ContextMenu();
                        contextMenu.setId("betterMenuItem");

                        MenuItem menuItem = new MenuItem("Delete");
                        menuItem.setStyle("-fx-text-fill: red");
                        contextMenu.getItems().add(menuItem);

                        menuItem.setOnAction(actionEvent -> {
                            scrollBarThumbnails.remove(variable);
                            orchestrator.removeVariable(variable);

                            if(selectedVariable.get() != null && selectedVariable.get().getName().equals(variable.getName())){
                                removeEditButtonAndAddNewButton();
                                resetTextFields();
                            }

                        });

                        checkIfExists.setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(checkIfExists, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));

                    }
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(variablesPanel);
        variablesPanel.setSpacing(20);
        variablesPanel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(variablesPanel, Priority.ALWAYS);

        scrollPane.setStyle("-fx-background: rgb(51,50,52); -fx-background-radius: 10");
        scrollPane.setStyle("-fx-background-color: rgb(51,50,52); -fx-background-radius: 10; -fx-background: transparent");


        scrollPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setFitToWidth(true);

        rightEditor.getChildren().add(scrollPane);

        rightEditor.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rightEditor, Priority.ALWAYS);

        return rightEditor;
    }

    private Pane getEditor(){
        mainPanel.setPadding(new Insets(20));
        mainPanel.setSpacing(20);
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setStyle("-fx-background-color: #262528 ");

        mainPanel.getChildren().addAll(getLeftEditor(), getRightEditor());

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

        Scene dialogScene = new Scene(getEditor(), 990, 790);
        dialogScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        loadVariables();


        stage.setScene(dialogScene);
        stage.show();

        stage.sizeToScene();
        stage.setTitle("Variables Editor");

        stage.setResizable(true);

        stage.setOnCloseRequest(event -> {
            scene.getRoot().setCache(true);
            scene.getRoot().setCacheHint(CacheHint.SPEED);
            startBlurAnimation(scene.getRoot(), 30.0, 0.0, Duration.millis(100), true);
            scene.getRoot().setCache(false);
            scene.getRoot().setCacheHint(CacheHint.DEFAULT);
        });


    }

    private void loadVariables(){
        if(orchestrator.getVariables().size() != 0){
            scrollBarThumbnails.addAll(orchestrator.getVariables());
        }
    }

}
