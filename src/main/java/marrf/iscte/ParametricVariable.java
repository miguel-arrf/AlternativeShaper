package marrf.iscte;

import javafx.beans.NamedArg;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class ParametricVariable implements Serializable {
    
    private UUID id = UUID.randomUUID();
    private String variableName;
    private Double variableValue;
    private boolean isNumeric;
    private boolean isFigure;
    private String description;

    private UUID figureID;

    @Override
    public String toString() {
        if(isNumeric){
            return Double.toString(variableValue);
        }
        return variableName;
    }

    public ParametricVariable(){

    }

    public ParametricVariable(@NamedArg("Variable Name") String variableName, @NamedArg("Variable value") double variableValue){
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.isNumeric = true;
        isFigure = false;
    }

    public ParametricVariable(@NamedArg("Variable Name") String variableName){
        this.variableName = variableName;
        this.variableValue = null;
        this.isNumeric = false;
        isFigure = false;
    }

    public ParametricVariable(JSONObject jsonObject){
        this.id = UUID.fromString((String) jsonObject.get("id"));
        this.variableName = (String) jsonObject.getOrDefault("variableName", null);
        this.variableValue = (Double) jsonObject.getOrDefault("variableValue", null);
        this.isNumeric = (boolean) jsonObject.get("isNumeric");
        this.isFigure = (boolean) jsonObject.get("isFigure");
        String figureIDTemp = (String) jsonObject.getOrDefault("figureID", null);
        if(isFigure && figureIDTemp == null)
            System.err.println("Parametric variable is a figure but its figure ID is null.");
        if(figureIDTemp == null){
            this.figureID = null;
        }else{
            this.figureID = UUID.fromString(figureIDTemp);
        }
        this.description = (String) jsonObject.getOrDefault("description", null);


    }

    public ParametricVariable(@NamedArg("Variable Name") String variableName, String description){
        this.variableName = variableName;
        this.variableValue = null;
        this.isNumeric = false;
        isFigure = false;

        this.description = description;
    }

    public ParametricVariable(@NamedArg("Figure Name") String figureName, @NamedArg("Figure ID") UUID id){
        this.variableName = figureName;
        this.figureID = id;
        this.isNumeric = false;
        this.isFigure = true;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id.toString();
    }

    public double getVariableValue() {
        return variableValue;
    }

    public String getVariableName() {
        return variableName;
    }
    
    public boolean isNumeric() {
        return isNumeric;
    }

    public boolean isFigure() {
        return isFigure;
    }

    public void setVariableValue(double variableValue) {
        this.variableValue = variableValue;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setNumeric(boolean numeric) {
        isNumeric = numeric;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParametricVariable that = (ParametricVariable) o;

        if(Objects.equals(id, that.id)){
            return true;
        }

        if(getVariableName().equals(that.getVariableName())){
            return true;
        }

        return false;
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        System.out.println("parametric variable figureId: " + figureID + ", for: " + variableName + " que, " + isFigure);

        jsonObject.put("id", this.getId());
        jsonObject.put("variableName", variableName.toString());
        jsonObject.put("variableValue", variableValue);
        jsonObject.put("isNumeric", isNumeric);
        jsonObject.put("isFigure", isFigure);
        if(isFigure){
            jsonObject.put("figureID", figureID.toString());
        }
        jsonObject.put("description", description);

        return jsonObject;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
