package marrf.iscte.ShapeRules;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import marrf.iscte.NewCompositionShape;
import marrf.iscte.Orchestrator;

import java.util.UUID;
import java.util.function.Function;

public abstract class ShapeRule {

    private final UUID id = UUID.randomUUID();
    private String shapeRuleName;

    private NewCompositionShape leftShape;
    private NewCompositionShape rightShape;

    private NewCompositionShape leftShapeCopy;
    private NewCompositionShape rightShapeCopy;

    private String boolCode;
    private String processCode;

    private String boolXML;
    private String processXML;

    private final Pane thumbnail = new VBox();

    public ShapeRule(Orchestrator orchestrator, Pane transformersBox,Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        leftShape = new NewCompositionShape(orchestrator, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
        rightShape = new NewCompositionShape(orchestrator, right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);

        leftShapeCopy = leftShape.getCopy();
        rightShapeCopy = rightShape.getCopy();
    }

    public NewCompositionShape getLeftShapeCopy() {
        return leftShapeCopy;
    }

    public NewCompositionShape getRightShapeCopy() {
        return rightShapeCopy;
    }

    public void setLeftShapeCopy(NewCompositionShape leftShapeCopy) {
        this.leftShapeCopy = leftShapeCopy;
    }

    public void setRightShapeCopy(NewCompositionShape rightShapeCopy) {
        this.rightShapeCopy = rightShapeCopy;
    }

    public void setLeftShape(NewCompositionShape leftShape) {
        this.leftShape = leftShape;
    }

    public void setRightShape(NewCompositionShape rightShape) {
        this.rightShape = rightShape;
    }

    public NewCompositionShape getLeftShape() {
        return leftShape;
    }

    public NewCompositionShape getRightShape() {
        return rightShape;
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

    public void setShapeRuleName(String shapeRuleName) {
        this.shapeRuleName = shapeRuleName;
    }

    public String getShapeRuleName() {
        return shapeRuleName;
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
