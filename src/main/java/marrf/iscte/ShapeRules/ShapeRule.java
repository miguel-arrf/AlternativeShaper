package marrf.iscte.ShapeRules;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import marrf.iscte.BasicShape;
import marrf.iscte.NewCompositionShape;

import java.util.ArrayList;
import java.util.UUID;

public abstract class ShapeRule {

    private final UUID id = UUID.randomUUID();
    private String shapeRuleName;

    private ArrayList<BasicShape> leftBasicShapes = new ArrayList<>();
    private ArrayList<NewCompositionShape> leftCompositionShapes = new ArrayList<>();

    private ArrayList<BasicShape> rightBasicShapes = new ArrayList<>();
    private ArrayList<NewCompositionShape> rightCompositionShapes = new ArrayList<>();

    private boolean hasBool;
    private boolean hasProc;

    private String boolCode;
    private String processCode;

    private String boolXML;
    private String processXML;

    private final Pane thumbnail = new VBox();

    public ShapeRule(boolean hasBool, boolean hasProc){
        this.hasBool = hasBool;
        this.hasProc = hasProc;
    }

    public String getProcessCode() {
        return processCode;
    }

    public String getBoolCode() {
        return boolCode;
    }

    public String getBoolXML() {
        return boolXML;
    }

    public String getProcessXML() {
        return processXML;
    }

    public UUID getId() {
        return id;
    }

    public void setBoolXML(String boolXML) {
        this.boolXML = boolXML;
    }

    public void setProcessXML(String processXML) {
        this.processXML = processXML;
    }

    public void setBoolCode(String boolCode) {
        this.boolCode = boolCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public boolean isHasBool() {
        return hasBool;
    }

    public boolean isHasProc() {
        return hasProc;
    }

    public void setShapeRuleName(String shapeRuleName) {
        this.shapeRuleName = shapeRuleName;
    }

    public String getShapeRuleName() {
        return shapeRuleName;
    }

    public ArrayList<BasicShape> getLeftBasicShapes() {
        return leftBasicShapes;
    }

    public ArrayList<NewCompositionShape> getLeftCompositionShapes() {
        return leftCompositionShapes;
    }

    public ArrayList<BasicShape> getRightBasicShapes() {
        return rightBasicShapes;
    }

    public ArrayList<NewCompositionShape> getRightCompositionShapes() {
        return rightCompositionShapes;
    }

    public void addBasicShapeToLeft(BasicShape basicShape){
        leftBasicShapes.add(basicShape);
    }

    public void addBasicShapeToRight(BasicShape basicShape){
        rightBasicShapes.add(basicShape);
    }

    public void addCompositionShapeToRight(NewCompositionShape newCompositionShape){
        rightCompositionShapes.add(newCompositionShape);
    }

    public void addCompositionShapeToLeft(NewCompositionShape newCompositionShape){
        leftCompositionShapes.add(newCompositionShape);
    }

    public void setLeftBasicShapes(ArrayList<BasicShape> basicShapes){
        this.leftBasicShapes = basicShapes;
    }

    public void setLeftCompositionShapes(ArrayList<NewCompositionShape> leftCompositionShapes) {
        this.leftCompositionShapes = leftCompositionShapes;
    }

    public void setRightBasicShapes(ArrayList<BasicShape> rightBasicShapes) {
        this.rightBasicShapes = rightBasicShapes;
    }

    public void setRightCompositionShapes(ArrayList<NewCompositionShape> rightCompositionShapes) {
        this.rightCompositionShapes = rightCompositionShapes;
    }

    public Pane getThumbnail(){
        thumbnail.setPadding(new Insets(5));
        ((VBox) thumbnail).setAlignment(Pos.CENTER);

        Label processNameLabel = new Label(getShapeRuleName());
        processNameLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        processNameLabel.setTextFill(Color.WHITE);

        thumbnail.getChildren().clear();
        thumbnail.getChildren().add(processNameLabel);
        thumbnail.setStyle("-fx-background-color: lightgreen; -fx-background-radius: 10");

        return thumbnail;
    }


}
