package marrf.iscte.ShapeRules;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import marrf.iscte.Orchestrator;
import marrf.iscte.ParametricCompositionShape;
import org.json.simple.JSONObject;

import java.util.UUID;
import java.util.function.Function;

public abstract class ShapeRule implements ShapeRuleInterface{

    private UUID id = UUID.randomUUID();
    private String shapeRuleName;

    private ParametricCompositionShape leftShape;
    private ParametricCompositionShape rightShape;

    private ParametricCompositionShape leftShapeCopy;
    private ParametricCompositionShape rightShapeCopy;

    private String boolCode;
    private String processCode;

    private String boolXML;
    private String processXML;

    private final Pane thumbnail = new VBox();

    private boolean matched = false;

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean getMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", this.getId().toString());
        jsonObject.put("name", this.getShapeRuleName());


        jsonObject.put("leftShape", this.getLeftShape().getJSONObject());
        jsonObject.put("rightShape", this.getRightShape().getJSONObject());

        jsonObject.put("boolCode", this.getBoolCode());
        jsonObject.put("processCode", this.getProcessCode());


        jsonObject.put("boolXML", this.getBoolXML());
        jsonObject.put("processXML", this.getProcessXML());

        jsonObject.put("matched", this.getMatched());

        if(this instanceof BoolShapeShapeProc){
            jsonObject.put("type", "bool-proc");
        }else if(this instanceof BoolShapeShape){
            jsonObject.put("type", "bool");
        }else if(this instanceof ShapeShape){
            jsonObject.put("type", "");
        }else if(this instanceof ShapeShapeProc){
            jsonObject.put("type","proc");
        }


        return jsonObject;
    }

    public static boolean hasDependencies(ShapeRule shapeRule){

        String processCode = shapeRule.getProcessCode();
        String boolCode = shapeRule.getBoolCode();

        String code = shapeRule.getCode();

        if(code != null){
            if(code.contains("existingproc") ||
                    code.contains("existingproc0") ||
                    code.contains("existingproc1") ||
                    code.contains("existingbool")){
                //Can we simplify to just having "existing"...?
                return true;
            }
        }


        return false;
    }



    public ShapeRule(Orchestrator orchestrator, Pane transformersBox,Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        leftShape = new ParametricCompositionShape(true, orchestrator, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
        rightShape = new ParametricCompositionShape(true, orchestrator, right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);

        leftShapeCopy = leftShape.getCopy();
        rightShapeCopy = rightShape.getCopy();
    }

    public ShapeRule(Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting, ParametricCompositionShape leftShape, ParametricCompositionShape rightShape){
        leftShape.setProceedWhenDeletingFromThumbnail(proceedWhenDeletingFromThumbnail);
        leftShape.setProceedToRedrawWhenDeleting(proceedToRedrawWhenDeleting);

        rightShape.setProceedWhenDeletingFromThumbnail(proceedWhenDeletingFromThumbnail);
        rightShape.setProceedToRedrawWhenDeleting(proceedToRedrawWhenDeleting);

        this.leftShape = leftShape;
        this.rightShape = rightShape;


        leftShapeCopy = leftShape.getCopy();
        rightShapeCopy = rightShape.getCopy();
    }

    public ParametricCompositionShape getLeftShapeCopy() {
        return leftShapeCopy;
    }

    public ParametricCompositionShape getRightShapeCopy() {
        return rightShapeCopy;
    }

    public void setLeftShapeCopy(ParametricCompositionShape leftShapeCopy) {
        this.leftShapeCopy = leftShapeCopy;
    }

    public void setRightShapeCopy(ParametricCompositionShape rightShapeCopy) {
        this.rightShapeCopy = rightShapeCopy;
    }

    public void setLeftShape(ParametricCompositionShape leftShape) {
        this.leftShape = leftShape;
    }

    public void setRightShape(ParametricCompositionShape rightShape) {
        this.rightShape = rightShape;
    }

    public ParametricCompositionShape getLeftShape() {
        return leftShape;
    }

    public ParametricCompositionShape getRightShape() {
        return rightShape;
    }

    public String getBoolCode() {
        return boolCode;
    }

    public String getProcessCode() {
        return processCode;
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
