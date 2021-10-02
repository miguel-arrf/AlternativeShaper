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
import org.json.simple.JSONObject;

import java.util.UUID;
import java.util.function.Function;

public abstract class ShapeRule implements ShapeRuleInterface{

    private UUID id = UUID.randomUUID();
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

        /*if(processCode != null){
            if(processCode.contains("existingproc") ||
                    processCode.contains("existingproc0") ||
                    processCode.contains("existingproc1") ||
                    processCode.contains("existingbool")){
                //Can we simplify to just having "existing"...?
                return true;
            }
        }

        if(boolCode != null){
            if(boolCode.contains("existingproc") ||
                    boolCode.contains("existingproc0") ||
                    boolCode.contains("existingproc1") ||
                    boolCode.contains("existingbool")){
                //Can we simplify to just having "existing"...?
                return true;
            }
        }*/


        return false;
    }

  /*  public static String solveDependency(ArrayList<ShapeRule> shapeRules, ArrayList<Process> processes, String shapeRuleCode){
        String toReturn = solveDependency(shapeRules, processes,shapeRuleCode, "existingproc");
        toReturn = solveDependency(shapeRules,processes, toReturn, "existingproc0");
        toReturn = solveDependency(shapeRules, processes,toReturn, "existingproc1");
        toReturn = solveDependency(shapeRules, processes,toReturn, "existingbool");

        return toReturn;
    }

    public static String solveDependency(ArrayList<ShapeRule> shapeRules, ArrayList<Process> processes, String shapeRuleCode, String existingProc){

        Pattern pattern = Pattern.compile(existingProc + "\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(shapeRuleCode);

        if(shapeRules != null){
            while (matcher.find()){
                String match = matcher.group(1);
                String toReplace = existingProc + "(" + match + ")";


                    if(existingProc.equals("existingproc")){

                        Optional<Process> seeIfExists = processes.stream().filter(p -> p.getProcessName().equals(match)).findFirst();
                        if(seeIfExists.isPresent()) {
                            Process toPut = seeIfExists.get();
                            if (Process.hasDependencies(toPut)) {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), Process.solveDependency(processes, toPut));
                            } else {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), toPut.getProcessCode());
                            }
                        }

                    }else if(existingProc.equals("existingproc0")){
                        Optional<ShapeRule> seeIfExists = shapeRules.stream().filter(p -> p.getShapeRuleName().equals(match)).findFirst();
                        if(seeIfExists.isPresent()) {
                            ShapeRule toPut = seeIfExists.get();
                            if (ShapeRule.hasDependencies(toPut)) {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), solveDependency(shapeRules,processes, toPut.getCode()));
                            } else {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), toPut.getCode());
                            }
                        }
                    }else if(existingProc.equals("existingproc1")){
                        Optional<ShapeRule> seeIfExists = shapeRules.stream().filter(p -> p.getShapeRuleName().equals(match)).findFirst();
                        if(seeIfExists.isPresent()) {
                            ShapeRule toPut = seeIfExists.get();
                            if (ShapeRule.hasDependencies(toPut)) {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), solveDependency(shapeRules,processes, toPut.getProcessCode()));
                            } else {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), toPut.getProcessCode());
                            }
                        }
                    }else if(existingProc.equals("existingbool")){
                        Optional<ShapeRule> seeIfExists = shapeRules.stream().filter(p -> p.getShapeRuleName().equals(match)).findFirst();
                        if(seeIfExists.isPresent()) {
                            ShapeRule toPut = seeIfExists.get();
                            if (ShapeRule.hasDependencies(toPut)) {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), solveDependency(shapeRules,processes, toPut.getBoolCode()));
                            } else {
                                shapeRuleCode = shapeRuleCode.replaceAll(Pattern.quote(toReplace), toPut.getBoolCode());
                            }
                        }
                    }

                //}

            }
        }


        if(shapeRuleCode.chars().filter(ch -> ch == ';').count() >= 2){
            shapeRuleCode = shapeRuleCode.substring(0, shapeRuleCode.lastIndexOf(";")).replaceAll(";" , "").concat(shapeRuleCode.substring(shapeRuleCode.lastIndexOf(";")));
        }

        return shapeRuleCode;
    }

   */

    public ShapeRule(Orchestrator orchestrator, Pane transformersBox,Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        leftShape = new NewCompositionShape(orchestrator, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
        rightShape = new NewCompositionShape(orchestrator, right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);

        leftShapeCopy = leftShape.getCopy();
        rightShapeCopy = rightShape.getCopy();
    }

    public ShapeRule(Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting, NewCompositionShape leftShape, NewCompositionShape rightShape){
        leftShape.setProceedWhenDeletingFromThumbnail(proceedWhenDeletingFromThumbnail);
        leftShape.setProceedToRedrawWhenDeleting(proceedToRedrawWhenDeleting);

        rightShape.setProceedWhenDeletingFromThumbnail(proceedWhenDeletingFromThumbnail);
        rightShape.setProceedToRedrawWhenDeleting(proceedToRedrawWhenDeleting);

        this.leftShape = leftShape;
        this.rightShape = rightShape;


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
