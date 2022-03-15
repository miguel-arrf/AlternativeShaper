package marrf.iscte;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.*;
import static marrf.iscte.BasicShape.colorToRGBString;
import static marrf.iscte.BasicShape.getRelativeLuminance;
import static marrf.iscte.Power.getDashStyle;

@SuppressWarnings("unchecked")
public class ParametricCompositionShape implements CustomShape, Serializable {

    public ParametricCompositionLogic parametricCompositionLogic = new ParametricCompositionLogic();

    private final Orchestrator orchestrator;

    //Composition Shape Thumbnail
    private final HBox thumbnail = new HBox();
    private final UUID ID;
    public VBox variablesPane = new VBox();
    public VBox variablesSelectionPane = new VBox();
    private String name = "PARAMETRIC_COMPLEX_DEFAULT";
    private Node selected;
    private Information<Double> selectedTranslationX;
    private Information<Double> selectedTranslationY;

    private Information<ParametricVariable> selectedParametricXTranslation;
    private Information<ParametricVariable> selectedParametricYTranslation;
    private Pane transformersBox;
    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;

    private VBox parametricTranslationXBox;
    private VBox parametricTranslationYBox;
    //Deletion handlers
    private Function<String, Double> proceedWhenDeletingFromThumbnail;
    private Function<String, Double> proceedToRedrawWhenDeleting;
    private Function<Pane, Double> proceedWhenDeleting;




    private boolean notParametric = false;

    public ParametricCompositionShape(Boolean notParametric, Orchestrator orchestrator, Pane transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        this.notParametric = notParametric;
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;

        ID = UUID.randomUUID();
        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        //setUpComponents();
    }

    public ParametricCompositionShape(Orchestrator orchestrator, Pane transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting, String name) {
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;
        this.name = name;

        ID = UUID.randomUUID();
        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        setUpVariablesPaneInit();

        //setUpComponents();
    }

    public void setUpVariablesPaneInit(){
        setUpVariablesPane();
        setUpVariablesSelectionPane();

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(15);
        flowPane.setVgap(15);

        parametricCompositionLogic.variables.addListener((ListChangeListener<? super ParametricVariable>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (ParametricVariable variableRemoved : change.getRemoved()) {
                        flowPane.getChildren().removeIf(p -> p.getId().equals(variableRemoved.getId()));
                    }
                }
                if (change.wasAdded()) {
                    for (ParametricVariable variableAdded : change.getAddedSubList()) {
                        if (flowPane.getChildren().filtered(p -> p.getId().equals(variableAdded.getId())).size() == 0) {
                            flowPane.getChildren().add(getPaneForVariable(variableAdded));
                        } else {
                            System.err.println("We are trying to add an already existing variable...!");
                        }
                    }
                }
            }
            setUpVariablesSelectionPane();
        });

        variablesPane.getChildren().add(flowPane);

    }

    public ParametricCompositionShape(Orchestrator orchestrator, Pane transformersBox, String name, UUID id, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting, ArrayList<Information<ArrayList<Pair<ParametricVariable, ParametricVariable>>>> variablePairs, ArrayList<ParametricVariable> variables) {
        this.name = name;
        this.ID = id;

        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        //setUpComponents();
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;

        this.parametricCompositionLogic.variablePairs = variablePairs;

        setUpVariablesPaneInit();
        this.parametricCompositionLogic.variables.addAll(variables);

    }

    public void removeCompositionShapeWithID(String uuidToRemove){
        parametricCompositionLogic.compositionShapeMap.forEach((key, value) -> {
            if (value.getID().toString().equals(uuidToRemove)) {
                parametricCompositionLogic.compositionShapesXTranslation.removeIf(b -> b.getId().equals(key));
                parametricCompositionLogic.compositionShapesYTranslation.removeIf(b -> b.getId().equals(key));

                parametricCompositionLogic.compositionShapesXParametricTranslation.removeIf(b -> b.getId().equals(key));
                parametricCompositionLogic.compositionShapesYParametricTranslation.removeIf(b -> b.getId().equals(key));

                parametricCompositionLogic.compositionShapeParametricVariableHover.remove(key);
                parametricCompositionLogic.variablePairs.removeIf(b -> b.getId().equals(key));
            }
        });

        parametricCompositionLogic.compositionShapeMap.entrySet().removeIf(p -> p.getValue().getID().toString().equals(uuidToRemove));

    }

    public void removePowerShapeWithID(String uuidToRemove){
        ArrayList<ArrayList<ParametricVariable>> variablesToRemove = new ArrayList<>();

        parametricCompositionLogic.powerShapesMap.forEach((key, value) -> {
            if (value.getUUID().toString().equals(uuidToRemove)) {
                parametricCompositionLogic.powerShapesXTranslation.removeIf(b -> b.getId().equals(key));
                parametricCompositionLogic.powerShapesYTranslation.removeIf(b -> b.getId().equals(key));

                parametricCompositionLogic.powerShapesXParametricTranslation.removeIf(b -> b.getId().equals(key));
                parametricCompositionLogic.powerShapesYParametricTranslation.removeIf(b -> b.getId().equals(key));

                parametricCompositionLogic.powerShapeParametricVariableHover.remove(key);
                parametricCompositionLogic.variablePairs.removeIf(b -> b.getId().equals(key));

                variablesToRemove.add(value.getOutputVariables());

            }
        });
        parametricCompositionLogic.powerShapesMap.entrySet().removeIf(p -> p.getValue().getUUID().toString().equals(uuidToRemove));

        variablesToRemove.forEach(variablesList -> {
            variablesList.forEach(variable -> {
                parametricCompositionLogic.variables.removeIf(v -> v.getId().equals(variable.getId()));

            });
        });

    }

    public void removeParametricCompositionShapeWithID(String uuidToRemove) {
        parametricCompositionLogic.parametricCompositionShapesMap.forEach((key, value) -> {
            if (value.getID().toString().equals(uuidToRemove)) {

                parametricCompositionLogic.parametricShapesXTranslation.removeIf(b -> b.getId().equals(key));
                parametricCompositionLogic.parametricShapesYTranslation.removeIf(b -> b.getId().equals(key));

                parametricCompositionLogic.parametricShapesXParametricTranslation.removeIf(b -> b.getId().equals(key));
                parametricCompositionLogic.parametricShapesYParametricTranslation.removeIf(b -> b.getId().equals(key));

                parametricCompositionLogic.parametricCompositionShapeParametricVariableHover.remove(key);
                parametricCompositionLogic.variablePairs.removeIf(b -> b.getId().equals(key));
            }
        });

        parametricCompositionLogic.parametricCompositionShapesMap.entrySet().removeIf(p -> p.getValue().getID().toString().equals(uuidToRemove));

    }

    private Pane getPaneForVariable(ParametricVariable variable) {
        return getPaneForVariable(variable, true);
    }

    private Pane getPaneForVariable(ParametricVariable variable, @NamedArg("With delete:") boolean withDelete) {
        return getPaneForVariable(variable, withDelete, 15, 30, 30, 6);
    }

    private Pane getPaneForVariable(ParametricVariable variable, boolean withDelete, int fontSize, int width, int height, int cornerRadius) {
        String backgroundColor = FxUtils.toRGBCode(Color.web(StartMenu.getRGBColor()).darker());

        Label label;
        if (variable.isNumeric()) {
            label = getLabel(variable.getVariableName() + " <- " + variable.getVariableValue(), fontSize);
        } else {
            label = getLabel(variable.getVariableName(), fontSize);
        }
        label.setWrapText(true);
        label.setTextFill(Color.web(backgroundColor).brighter().brighter());

        VBox vBox = new VBox(label);
        vBox.setPadding(new Insets(3));
        vBox.setAlignment(Pos.CENTER);
        vBox.setId(variable.getId());

        vBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);

        if (withDelete) {
            vBox.setOnMouseEntered(mouseEvent -> vBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: " + cornerRadius));

            vBox.setOnMouseExited(mouseEvent -> vBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius));

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setId("betterMenuItem");

            MenuItem menuItem = new MenuItem("Delete Parameter");
            menuItem.setStyle("-fx-text-fill: red");
            contextMenu.getItems().add(menuItem);

            menuItem.setOnAction(actionEvent -> {
                parametricCompositionLogic.variables.remove(variable);
                removeVariable(variable);
                setUpVariablesSelectionPane();
            });

            vBox.setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(vBox, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));

        }

        vBox.setMinWidth(width);
        vBox.setMinHeight(height);

        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setMaxHeight(Double.MAX_VALUE);


        return vBox;
    }

    private void removeVariable(ParametricVariable variable) {
        parametricCompositionLogic.compositionShapesXParametricTranslation.forEach(v -> {
            if (v.getValue() != null) {
                if (v.getValue().getId().equals(variable.getId())) {
                    v.setValue(null);
                }
            }

        });

        parametricCompositionLogic.compositionShapesYParametricTranslation.forEach(v -> {
            if (v.getValue() != null) {
                if (v.getValue().getId().equals(variable.getId())) {
                    v.setValue(null);
                }
            }
        });

        parametricCompositionLogic.parametricShapesXParametricTranslation.forEach(v -> {
            if (v.getValue() != null) {
                if (v.getValue().getId().equals(variable.getId())) {
                    v.setValue(null);
                }
            }
        });

        parametricCompositionLogic.parametricShapesYParametricTranslation.forEach(v -> {
            if (v.getValue() != null) {
                if (v.getValue().getId().equals(variable.getId())) {
                    v.setValue(null);
                }
            }
        });


        parametricCompositionLogic.variablePairs.forEach(information -> {
            ArrayList<Pair<ParametricVariable, ParametricVariable>> toRemove = new ArrayList<>();
            information.getValue().forEach(innerInformation -> {
                if (innerInformation.getValue() != null && innerInformation.getValue().getId().equals(variable.getId())) {
                    toRemove.add(innerInformation);
                }
            });
            information.getValue().removeAll(toRemove);
        });


    }

    public void setProceedToRedrawWhenDeleting(Function<String, Double> proceedToRedrawWhenDeleting) {
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;
    }

    public void setProceedWhenDeletingFromThumbnail(Function<String, Double> proceedWhenDeletingFromThumbnail) {
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
    }

    public void setProceedWhenDeleting(Function<Pane, Double> proceedWhenDeleting) {
        this.proceedWhenDeleting = proceedWhenDeleting;
    }

    public ParametricCompositionShape getCopy() {
        ParametricCompositionShape toReturn = new ParametricCompositionShape(orchestrator, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting, this.name);

        toReturn.notParametric = notParametric;
        toReturn.name = name;
        toReturn.parametricCompositionLogic.compositionShapeMap.putAll(parametricCompositionLogic.compositionShapeMap);
        toReturn.parametricCompositionLogic.parametricCompositionShapesMap.putAll(parametricCompositionLogic.parametricCompositionShapesMap);

        parametricCompositionLogic.compositionShapesXTranslation.forEach(information -> toReturn.parametricCompositionLogic.compositionShapesXTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.compositionShapesYTranslation.forEach(information -> toReturn.parametricCompositionLogic.compositionShapesYTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.compositionShapesXParametricTranslation.forEach(information -> toReturn.parametricCompositionLogic.compositionShapesXParametricTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.compositionShapesYParametricTranslation.forEach(information -> toReturn.parametricCompositionLogic.compositionShapesYParametricTranslation.add(new Information<>(information.getId(), information.getValue())));

        parametricCompositionLogic.parametricShapesXTranslation.forEach(information -> toReturn.parametricCompositionLogic.parametricShapesXTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.parametricShapesYTranslation.forEach(information -> toReturn.parametricCompositionLogic.parametricShapesYTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.parametricShapesXParametricTranslation.forEach(information -> toReturn.parametricCompositionLogic.parametricShapesXParametricTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.parametricShapesYParametricTranslation.forEach(information -> toReturn.parametricCompositionLogic.parametricShapesYParametricTranslation.add(new Information<>(information.getId(), information.getValue())));

        parametricCompositionLogic.powerShapesXTranslation.forEach(information -> toReturn.parametricCompositionLogic.powerShapesXTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.powerShapesYTranslation.forEach(information -> toReturn.parametricCompositionLogic.powerShapesYTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.powerShapesXParametricTranslation.forEach(information -> toReturn.parametricCompositionLogic.powerShapesXParametricTranslation.add(new Information<>(information.getId(), information.getValue())));
        parametricCompositionLogic.powerShapesYParametricTranslation.forEach(information -> toReturn.parametricCompositionLogic.powerShapesYParametricTranslation.add(new Information<>(information.getId(), information.getValue())));


        return toReturn;
    }

    public ArrayList<String> getCompositionShapesUUIDList() {
        ArrayList<String> toReturn = new ArrayList<>();

        parametricCompositionLogic.compositionShapeMap.forEach((a, b) -> toReturn.add(b.getID().toString()));

        return toReturn;
    }


    public void deleteCompositionShape(String uuidToRemove) {
        //TODO If we delete a basic shape from a composition shape that
        // only had it, and that composition shape is here, we should delete
        // the parameters that were defined.
        if (getCompositionShapesUUIDList().stream().anyMatch(p -> p.equals(uuidToRemove))) {

            ArrayList<String> toRemove = new ArrayList<>();

            parametricCompositionLogic.compositionShapeMap.forEach((id, compositionShape) -> {
                if (compositionShape.getID().toString().equals(uuidToRemove)) {
                    toRemove.add(id);
                }
            });

            toRemove.forEach(parametricCompositionLogic.compositionShapeMap::remove);

            parametricCompositionLogic.compositionShapesXTranslation.removeIf(p -> {
                return toRemove.stream().anyMatch(a -> a.equals(p.getId()));
            });

            parametricCompositionLogic.compositionShapesYTranslation.removeIf(p -> {
                return toRemove.stream().anyMatch(a -> a.equals(p.getId()));
            });
        }
    }

    public void setTransformersBox(Pane transformersBox) {
        this.transformersBox = transformersBox;
    }

    public Map<String, NewCompositionShape> getCompositionShapeMap() {
        return parametricCompositionLogic.compositionShapeMap;
    }

    public Map<String, ParametricCompositionShape> getParametricCompositionShapesMap() {
        return parametricCompositionLogic.parametricCompositionShapesMap;
    }

    public String getPrologRepresentation(boolean withFinalDot, boolean justInner) {
        StringBuilder toReturn = new StringBuilder();

        //remember that the parametric translation values are the values for us to use here to perform the translations
        //for example [1, 0,0,0,1,0, A, B]

        //the other variables, the ones that we've assigned from previous parametric composition shapes,
        //those are for us to call, for example previousFigure(1,d)[1,0,0,1,0,A,B]

        if (!justInner) {
            toReturn.append("shapeComposition(").append(getShapeName()).append("[");
        }


        parametricCompositionLogic.powerShapesXTranslation.forEach(information -> {
            Information<Double> translationY = parametricCompositionLogic.powerShapesYTranslation.stream().filter(inf -> inf.getId().equals(information.getId())).findFirst().get();

            Power correctID = parametricCompositionLogic.powerShapesMap.get(information.getId());

            toReturn.append("s(").append(orchestrator.getPowerShapeNameFromID(correctID.getUUID().toString()));
            toReturn.append(",[1,0,0,0,1,0,").append(information.getValue() / SCALE).append(",").append(translationY.getValue() / SCALE).append(",1]");

            toReturn.append("),");
        });

        AtomicInteger added = new AtomicInteger();

        parametricCompositionLogic.parametricCompositionShapesMap.forEach((randomID, newCompositionShape) -> {
            var translationX = parametricCompositionLogic.parametricShapesXTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();
            var translationY = parametricCompositionLogic.parametricShapesYTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();

            //First we need to check if there is variables for both X and Y translations...
            //And we set the string to contain the variables

            //After, if any of those variables have a connection in the variablePars array to another variable, then we
            //replace by it!

            if(newCompositionShape.getOutputVariables().size() != 0){
                toReturn.append("s(").append(newCompositionShape.getShapeName());
                toReturn.append("(");

                ArrayList<ParametricVariable> outputVariables = new ArrayList<>();
                ArrayList<Pair<ParametricVariable, ParametricVariable>> variableConnections = parametricCompositionLogic.variablePairs.stream().filter(p -> p.getId().equals(randomID)).findFirst().get().getValue();

                for(int i = 0; i < newCompositionShape.getOutputVariables().size(); i++){
                    boolean found = false;

                    for(Pair<ParametricVariable, ParametricVariable> variable : variableConnections){
                        if(variable.getKey().equals(newCompositionShape.getOutputVariables().get(i))){
                            if(variable.getValue() != null){
                                outputVariables.add(variable.getValue());
                                break;
                            }
                        }
                    }

                    if(found){
                        outputVariables.add(outputVariables.get(i));
                    }
                }

                toReturn.append(outputVariables);
                toReturn.append(")");
            }else{
                toReturn.append("s(").append(newCompositionShape.getShapeName());
            }

            String translationXValue;
            if(parametricCompositionLogic.parametricShapesXParametricTranslation.stream().anyMatch(translation -> translation.getId().equals(randomID))){
                if (parametricCompositionLogic.parametricShapesXParametricTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get().getValue() != null){
                    //TODO use toString?
                    translationXValue = parametricCompositionLogic.parametricShapesXParametricTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get().getValue().toString();
                }else{
                    translationXValue = String.valueOf(translationX.getValue());
                }
            }else{
                translationXValue = String.valueOf(translationX.getValue());
            }

            String translationYValue;
            if(parametricCompositionLogic.parametricShapesYParametricTranslation.stream().anyMatch(translation -> translation.getId().equals(randomID))){
                if (parametricCompositionLogic.parametricShapesYParametricTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get().getValue() != null){
                    //TODO use toString?
                    translationYValue = parametricCompositionLogic.parametricShapesYParametricTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get().getValue().toString();
                }else{
                    translationYValue = String.valueOf(translationY.getValue());
                }
            }else{
                translationYValue = String.valueOf(translationY.getValue());
            }

            toReturn.append(",[1,0,0,0,1,0,").append(translationXValue).append(",").append(translationYValue).append(",1]");
            toReturn.append("),");

            added.getAndIncrement();
        });

        parametricCompositionLogic.compositionShapeMap.forEach((randomID, newCompositionShape) -> {

            var translationX = parametricCompositionLogic.compositionShapesXTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();
            var translationY = parametricCompositionLogic.compositionShapesYTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();

            toReturn.append("s(").append(newCompositionShape.getShapeName());
            toReturn.append(",[1,0,0,0,1,0,").append(translationX.getValue()).append(",").append(translationY.getValue()).append(",1]");


            toReturn.append("),");

            added.getAndIncrement();


        });

        if (!justInner) {
            toReturn.append(withFinalDot ? "])." : "])");
        }


        String string = toReturn.toString();
        string = string.replaceAll("([,][,]+)", ",");
        string = string.replaceAll("([,][)])", ")");
        string = string.replaceAll("([,][]])", "]");


        return string;
    }

    public JSONArray getVariablesJSON(){
        JSONArray array = new JSONArray();
        parametricCompositionLogic.variables.forEach(variable -> array.add(variable.toJSON()));
        return array;
    }

    public JSONArray getVariablesPairsJSON(){
        JSONArray array = new JSONArray();
        Type customType = new TypeToken<Information<ArrayList<Pair<ParametricVariable, ParametricVariable>>>>(){}.getType();

        Gson gson = new Gson();

        parametricCompositionLogic.variablePairs.forEach(information -> {
            array.add(gson.toJson(information, customType));
        });
        return array;
    }

    public String getVariablesPairsTestJSON(){
        Type customType = new TypeToken<ArrayList<Information<ArrayList<Pair<ParametricVariable, ParametricVariable>>>>>(){}.getType();

        Gson gson = new Gson();
        return gson.toJson(parametricCompositionLogic.variablePairs, customType);
    }

    public JSONArray getVariablePairsJSON(){
        JSONArray outterArray = new JSONArray();

        parametricCompositionLogic.variablePairs.forEach(outterInformation -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", outterInformation.getId());

            JSONArray innerArray = new JSONArray();

            outterInformation.getValue().forEach(innerPair -> {
                JSONObject innerObject = new JSONObject();
                innerObject.put("key", innerPair.getKey().toJSON());
                if(innerPair.getValue() != null){
                    innerObject.put("value", innerPair.getValue().toJSON());
                    innerArray.add(innerObject);

                }

            });

            jsonObject.put("innerArray",  innerArray);

            outterArray.add(jsonObject);

        });

        return outterArray;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", getID().toString());
        jsonObject.put("name", getShapeName());
        jsonObject.put("variables", getVariablesJSON());
        //jsonObject.put("variablesPairs", getVariablesPairsJSON());
        //jsonObject.put("variablesPairsTest", getVariablesPairsTestJSON());
        jsonObject.put("variablePairsFinal", getVariablePairsJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());
        jsonObject.put("parametricShapes", getParametricShapesJSON());
        jsonObject.put("powerShapes", getPowerShapesJSON());

        return jsonObject;
    }

    public UUID getID() {
        return ID;
    }

    public JSONArray getCompositionShapesJSON() {
        JSONArray array = new JSONArray();

        parametricCompositionLogic.compositionShapeMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Information<Double> information : parametricCompositionLogic.compositionShapesXTranslation) {
                if (information.getId().equals(id))
                    position = parametricCompositionLogic.compositionShapesXTranslation.indexOf(information);
            }

            jsonObject.put("mapId", id);

            jsonObject.put("id", compositionShape.getID().toString());
            jsonObject.put("translationX", parametricCompositionLogic.compositionShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", parametricCompositionLogic.compositionShapesYTranslation.get(position).getValue());


            jsonObject.put("parametricTranslationX", parametricCompositionLogic.compositionShapesXParametricTranslation.get(position).getValue() == null ? parametricCompositionLogic.compositionShapesXParametricTranslation.get(position).getValue() : parametricCompositionLogic.compositionShapesXParametricTranslation.get(position).getValue().toJSON());
            jsonObject.put("parametricTranslationY", parametricCompositionLogic.compositionShapesYParametricTranslation.get(position).getValue() == null ? parametricCompositionLogic.compositionShapesYParametricTranslation.get(position).getValue() : parametricCompositionLogic.compositionShapesYParametricTranslation.get(position).getValue().toJSON());


            array.add(jsonObject);
        });

        return array;
    }

    public JSONArray getPowerShapesJSON() {
        JSONArray array = new JSONArray();

        parametricCompositionLogic.powerShapesMap.forEach((id, powerShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Information<Double> information : parametricCompositionLogic.powerShapesXTranslation) {
                if (information.getId().equals(id))
                    position = parametricCompositionLogic.powerShapesXTranslation.indexOf(information);
            }
            jsonObject.put("mapId", id);
            jsonObject.put("id", powerShape.getUUID().toString());
            jsonObject.put("translationX", parametricCompositionLogic.powerShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", parametricCompositionLogic.powerShapesYTranslation.get(position).getValue());

            jsonObject.put("parametricTranslationX", parametricCompositionLogic.powerShapesXParametricTranslation.get(position).getValue() == null ? parametricCompositionLogic.powerShapesXParametricTranslation.get(position).getValue() : parametricCompositionLogic.powerShapesXParametricTranslation.get(position).getValue().toJSON());
            jsonObject.put("parametricTranslationY", parametricCompositionLogic.powerShapesYParametricTranslation.get(position).getValue() == null ? parametricCompositionLogic.powerShapesYParametricTranslation.get(position).getValue() : parametricCompositionLogic.powerShapesYParametricTranslation.get(position).getValue().toJSON());

            array.add(jsonObject);
        });


        return array;

    }

    public JSONArray getParametricShapesJSON() {
        JSONArray array = new JSONArray();

        parametricCompositionLogic.parametricCompositionShapesMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Information<Double> information : parametricCompositionLogic.parametricShapesXTranslation) {
                if (information.getId().equals(id))
                    position = parametricCompositionLogic.parametricShapesXTranslation.indexOf(information);
            }
            jsonObject.put("mapId", id);
            jsonObject.put("id", compositionShape.getID().toString());
            jsonObject.put("translationX", parametricCompositionLogic.parametricShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", parametricCompositionLogic.parametricShapesYTranslation.get(position).getValue());

            jsonObject.put("parametricTranslationX", parametricCompositionLogic.parametricShapesXParametricTranslation.get(position).getValue() == null ? parametricCompositionLogic.parametricShapesXParametricTranslation.get(position).getValue() : parametricCompositionLogic.parametricShapesXParametricTranslation.get(position).getValue().toJSON());
            jsonObject.put("parametricTranslationY", parametricCompositionLogic.parametricShapesYParametricTranslation.get(position).getValue() == null ? parametricCompositionLogic.parametricShapesYParametricTranslation.get(position).getValue() : parametricCompositionLogic.parametricShapesYParametricTranslation.get(position).getValue().toJSON());


            array.add(jsonObject);
        });

        return array;
    }

    public Power addPowerShape(String powerShapeID) {
        Power power = orchestrator.getPowerShapeWithGivenID(powerShapeID);

        String id = UUID.randomUUID().toString();

        parametricCompositionLogic.addShape(id, power,parametricCompositionLogic.powerShapesMap,
                parametricCompositionLogic.powerShapesXTranslation,
                parametricCompositionLogic.powerShapesYTranslation,
                parametricCompositionLogic.powerShapesXParametricTranslation,
                parametricCompositionLogic.powerShapesYParametricTranslation,
                parametricCompositionLogic.powerShapeParametricVariableHover);

        System.out.println("ó maluco, here i am");
        //Information<Double> translationX = new Information<>(powerShapeID, 0.0);
        //Information<Double> translationY = new Information<>(powerShapeID, 0.0);
        //Information<String> parametricXTranslation = new Information<>(powerShapeID, "");
        //Information<String> parametricYTranslation = new Information<>(powerShapeID, "");
        //parametricCompositionLogic.powerShapesXParametricTranslation.add(parametricXTranslation);
        //parametricCompositionLogic.powerShapesYParametricTranslation.add(parametricYTranslation);
        //parametricCompositionLogic.powerShapesXTranslation.add(translationX);
        //parametricCompositionLogic.powerShapesYTranslation.add(translationY);
        Information<Double> translationX = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get();
        Information<Double> translationY = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get();

        addPowerShapesVariables();
        setUpVariablesSelectionPane();

        return orchestrator.getCopyOfParametricPowerShape(powerShapeID, translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(powerShapeID));

    }

    public Power addPowerShapeWithTranslation(String powerShapeID, double xTranslation, double yTranslation, String parametricX, String parametricY) {
        Power power = orchestrator.getPowerShapeWithGivenID(powerShapeID);
        if(power == null){
            System.err.println("HERE IS THE PROBLEM!: " + powerShapeID);
        }
        String id = UUID.randomUUID().toString();

        parametricCompositionLogic.addShape(id, power,parametricCompositionLogic.powerShapesMap,
                parametricCompositionLogic.powerShapesXTranslation,
                parametricCompositionLogic.powerShapesYTranslation,
                parametricCompositionLogic.powerShapesXParametricTranslation,
                parametricCompositionLogic.powerShapesYParametricTranslation,
                parametricCompositionLogic.powerShapeParametricVariableHover);
        //Information<Double> translationX = new Information<>(powerShapeID, xTranslation);
        //Information<Double> translationY = new Information<>(powerShapeID, yTranslation);

        //parametricCompositionLogic.powerShapesXTranslation.add(translationX);
        //parametricCompositionLogic.powerShapesYTranslation.add(translationY);
        Information<Double> translationX = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get();
        Information<Double> translationY = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get();

        translationX.setValue(xTranslation);
        translationY.setValue(yTranslation);

        setUpVariablesSelectionPane();


        return orchestrator.getCopyOfParametricPowerShape(powerShapeID, translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(powerShapeID));

    }

    public Power addPowerShapeWithTranslation(String powerShapeID, double xTranslation, double yTranslation, String id, ParametricVariable parametricX, ParametricVariable parametricY ) {
        Power power = orchestrator.getPowerShapeWithGivenID(powerShapeID);
        if(power == null){
            System.err.println("HERE IS THE PROBLEM!: " + powerShapeID);
        }

        parametricCompositionLogic.addShape(id, power,parametricCompositionLogic.powerShapesMap,
                parametricCompositionLogic.powerShapesXTranslation,
                parametricCompositionLogic.powerShapesYTranslation,
                parametricCompositionLogic.powerShapesXParametricTranslation,
                parametricCompositionLogic.powerShapesYParametricTranslation,
                parametricCompositionLogic.powerShapeParametricVariableHover);

        System.out.println("id é: " + id);
        parametricCompositionLogic.powerShapesXParametricTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(parametricX);
        parametricCompositionLogic.powerShapesYParametricTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(parametricY);

        parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(xTranslation);
        parametricCompositionLogic.powerShapesYTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(yTranslation);


        Information<Double> translationX = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get();
        Information<Double> translationY = parametricCompositionLogic.powerShapesYTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get();

        translationX.setValue(xTranslation);
        translationY.setValue(yTranslation);


        setUpVariablesSelectionPane();

        return orchestrator.getCopyOfParametricPowerShape(powerShapeID, translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(powerShapeID));

    }

    public ArrayList<Power> getPowerShapes() {
        ArrayList<Power> basicShapes = new ArrayList<>();

        parametricCompositionLogic.powerShapesXTranslation.forEach(information -> {
            int position = parametricCompositionLogic.powerShapesXTranslation.indexOf(information);

            Information<Double> translationX = parametricCompositionLogic.powerShapesXTranslation.get(position);
            Information<Double> translationY = parametricCompositionLogic.powerShapesYTranslation.get(position);

            Power correctID = parametricCompositionLogic.powerShapesMap.get(information.getId());

            basicShapes.add(orchestrator.getCopyOfParametricPowerShape(correctID.getUUID().toString(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(information.getId())));
        });


        return basicShapes;
    }


    private Function<String, Double> getProceedWhenDeletingPowerShape(String basicShapeID) {
        return a -> {
            Information<Double> xTranslationToRemove = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            Information<Double> yTranslationToRemove = parametricCompositionLogic.powerShapesYTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            parametricCompositionLogic.powerShapesXTranslation.remove(xTranslationToRemove);
            parametricCompositionLogic.powerShapesYTranslation.remove(yTranslationToRemove);
            System.err.println("VIM AQUI PARA APAGAR POWER SHAPE!");
            proceedToRedrawWhenDeleting.apply(null);
            setUpVariablesSelectionPane();
            return null;
        };
    }


    private Function<Node, Double> getProceedWhenDeletingPowerShape_Novo(String basicShapeID) {
        return a -> {
            Information<Double> xTranslationToRemove = parametricCompositionLogic.powerShapesXTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            Information<Double> yTranslationToRemove = parametricCompositionLogic.powerShapesYTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            parametricCompositionLogic.powerShapesXTranslation.remove(xTranslationToRemove);
            parametricCompositionLogic.powerShapesYTranslation.remove(yTranslationToRemove);
            System.err.println("VIM AQUI PARA APAGAR POWER SHAPE NOVO!");
            proceedToRedrawWhenDeleting.apply(null);
            setUpVariablesSelectionPane();
            return null;
        };
    }

    public void getTeste(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY, ParametricCompositionShape topLevel) {
        parametricCompositionLogic.parametricCompositionShapesMap.forEach((id, shape) -> {
            getTesteForSpecificParametric(toAdd, true, upperTranslationX, upperTranslationY, id, shape, topLevel);
            // Alterei aqui
        });

        parametricCompositionLogic.compositionShapeMap.forEach((newID, compositionShape) -> {
            Group addTo = new Group();

            int position = parametricCompositionLogic.compositionShapesXTranslation.indexOf(parametricCompositionLogic.compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Information<Double> translationX = parametricCompositionLogic.compositionShapesXTranslation.get(position);
            Information<Double> translationY = parametricCompositionLogic.compositionShapesYTranslation.get(position);

            Information<ParametricVariable> parametricTranslationX = parametricCompositionLogic.compositionShapesXParametricTranslation.get(position);
            Information<ParametricVariable> parametricTranslationY = parametricCompositionLogic.compositionShapesYParametricTranslation.get(position);

            //Adding basic shapes
            compositionShape.getBasicShapes().forEach(basicShape -> {
                double translateXBy = basicShape.getInitialTranslation().getX();
                double translateYBy = basicShape.getHeight() + basicShape.getInitialTranslation().getY() * -1;

                //Este é o translation X e Y final porque já não temos mais profundidade de composition shapes, visto que já é uma basic shape.
                basicShape.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
                basicShape.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

                Pane rectangle = basicShape.getRectangle();

                addTo.getChildren().add(rectangle);
            });

            if (toAdd instanceof Pane) {
                Pane toAddTemp = (Pane) toAdd;
                toAddTemp.getChildren().add(addTo);
            } else if (toAdd instanceof Group) {
                Group toAddTemp = (Group) toAdd;
                toAddTemp.getChildren().add(addTo);
            }

            compositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY);

            if ((topLevel != this) && (parametricTranslationX.getValue() != null || parametricTranslationY.getValue() != null)) {
                Arrow arrow = new Arrow();
                arrow.setEndX(/*compositionShape.getMinimumTranslationX() +*/ translationX.getValue());
                arrow.setEndY(/*compositionShape.getMinimumTranslationY()+*/ translationY.getValue());
                arrow.setTranslateX(upperTranslationX);
                arrow.setTranslateY(upperTranslationY);

                addTo.getChildren().add(arrow);
            }

            if (addHover) {
                Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth() + 20, addTo.getLayoutBounds().getHeight() + 20);
                rectangle.setArcWidth(10);
                rectangle.setArcHeight(10);
                rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
                rectangle.setX(addTo.getLayoutBounds().getMinX() - 10);
                rectangle.setY(addTo.getLayoutBounds().getMinY() - 10);

                String textLabel = compositionShape.getShapeName();
                Label shapeName = new Label(textLabel);

                shapeName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
                shapeName.setTextFill(Color.web("#BDBDBD"));
                shapeName.setPadding(new Insets(4));
                shapeName.setStyle("-fx-background-color:  rgba(255,255,255,0.3); -fx-background-radius: 15px;");

                if (parametricCompositionLogic.compositionShapeParametricVariableHover.get(newID) != null) {
                    parametricCompositionLogic.compositionShapeParametricVariableHover.get(newID).addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            addTo.getChildren().add(rectangle);
                        } else {
                            addTo.getChildren().remove(rectangle);
                        }
                    });
                }

                addTo.setOnMouseMoved(mouseEvent -> {
                    shapeName.setLayoutX(mouseEvent.getX() + 15);
                    shapeName.setLayoutY(mouseEvent.getY() + 15);
                });

                addTo.setOnMouseEntered(event -> {
                    addTo.getChildren().add(rectangle);
                    addTo.getChildren().add(shapeName);
                });


                addTo.setOnMouseExited(event -> {
                    addTo.getChildren().remove(rectangle);
                    addTo.getChildren().remove(shapeName);
                });

                addTo.setOnMouseClicked(event -> {
                    selected = addTo;
                    selectedTranslationX = translationX;
                    selectedTranslationY = translationY;

                    selectedParametricXTranslation = parametricTranslationX;
                    selectedParametricYTranslation = parametricTranslationY;

                    setUpComponents();
                    System.out.println("I've clicked on here. It should now be true!");

                    transformersBox.getChildren().clear();
                    transformersBox.getChildren().addAll(getTransformers());
                });

                ContextMenu contextMenu = new ContextMenu();
                contextMenu.setId("betterMenuItem");

                MenuItem menuItem = new MenuItem("Delete");
                menuItem.setStyle("-fx-text-fill: red");
                contextMenu.getItems().add(menuItem);

                menuItem.setOnAction(actionEvent -> {
                    ((Pane) addTo.getParent()).getChildren().remove(addTo);
                    parametricCompositionLogic.compositionShapeMap.remove(newID);
                    Information<Double> xTranslationToRemove = parametricCompositionLogic.compositionShapesXTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                    Information<Double> yTranslationToRemove = parametricCompositionLogic.compositionShapesYTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                    parametricCompositionLogic.compositionShapesXTranslation.remove(xTranslationToRemove);
                    parametricCompositionLogic.compositionShapesYTranslation.remove(yTranslationToRemove);
                    setUpVariablesSelectionPane();

                    //proceedWhenDeleting.apply(null);
                });

                addTo.setOnContextMenuRequested(contextMenuEvent -> {
                    contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                });

            }

        });

    }

    public void getTesteForSpecific(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY, String newID, NewCompositionShape compositionShape) {

        toAdd.setPickOnBounds(false);
        Group addTo = new Group();

        int position = parametricCompositionLogic.compositionShapesXTranslation.indexOf(parametricCompositionLogic.compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

        Information<Double> translationX = parametricCompositionLogic.compositionShapesXTranslation.get(position);
        Information<Double> translationY = parametricCompositionLogic.compositionShapesYTranslation.get(position);

        Information<ParametricVariable> parametricTranslationX = parametricCompositionLogic.compositionShapesXParametricTranslation.get(position);
        Information<ParametricVariable> parametricTranslationY = parametricCompositionLogic.compositionShapesYParametricTranslation.get(position);

        //Adding basic shapes
        compositionShape.getBasicShapes().forEach(basicShape -> {
            double translateXBy = basicShape.getInitialTranslation().getX();
            double translateYBy = basicShape.getHeight() + basicShape.getInitialTranslation().getY() * -1;

            //Este é o translation X e Y final porque já não temos mais profundidade de composition shapes, visto que já é uma basic shape.
            basicShape.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
            basicShape.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

            BasicShape copy = new BasicShape(basicShape.getWidth(), basicShape.getHeight(), basicShape.getFill(), basicShape.getSelectedImage(), basicShape.getWriteTranslateX(), basicShape.getWriteTranslateY(), basicShape.getProceedWhenDeleting());
            copy.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
            copy.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

            Pane rectangle = copy.getRectangle();

            addTo.getChildren().add(rectangle);
        });

        if (toAdd instanceof Pane) {
            Pane toAddTemp = (Pane) toAdd;
            toAddTemp.getChildren().add(addTo);
        } else if (toAdd instanceof Group) {
            Group toAddTemp = (Group) toAdd;
            toAddTemp.getChildren().add(addTo);
        }


        compositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY);

        if (addHover) {
            Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth() + 20, addTo.getLayoutBounds().getHeight() + 20);
            rectangle.setArcWidth(10);
            rectangle.setArcHeight(10);
            rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
            rectangle.setX(addTo.getLayoutBounds().getMinX() - 10);
            rectangle.setY(addTo.getLayoutBounds().getMinY() - 10);


            Label shapeName = new Label(compositionShape.getShapeName());
            shapeName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
            shapeName.setTextFill(Color.web("#BDBDBD"));
            shapeName.setPadding(new Insets(4));
            shapeName.setStyle("-fx-background-color:  rgba(255,255,255,0.3); -fx-background-radius: 15px;");

            if (parametricCompositionLogic.compositionShapeParametricVariableHover.get(newID) != null) {
                parametricCompositionLogic.compositionShapeParametricVariableHover.get(newID).addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        addTo.getChildren().add(rectangle);
                    } else {
                        addTo.getChildren().remove(rectangle);
                    }
                });
            }

            addTo.setOnMouseMoved(mouseEvent -> {
                shapeName.setLayoutX(mouseEvent.getX() + 15);
                shapeName.setLayoutY(mouseEvent.getY() + 15);
            });


            addTo.setOnMouseEntered(event -> {
                addTo.getChildren().add(rectangle);
                addTo.getChildren().add(shapeName);
            });


            addTo.setOnMouseExited(event -> {
                addTo.getChildren().remove(rectangle);
                addTo.getChildren().remove(shapeName);
            });

            addTo.setOnMouseClicked(event -> {
                selected = addTo;
                selectedTranslationX = translationX;
                selectedTranslationY = translationY;

                selectedParametricXTranslation = parametricTranslationX;
                selectedParametricYTranslation = parametricTranslationY;

                setUpComponents();
                System.out.println("I've clicked on here. It should now be true!");

                transformersBox.getChildren().clear();
                transformersBox.getChildren().addAll(getTransformers());
            });

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setId("betterMenuItem");

            MenuItem menuItem = new MenuItem("Delete");
            menuItem.setStyle("-fx-text-fill: red");
            contextMenu.getItems().add(menuItem);

            menuItem.setOnAction(actionEvent -> {
                ((Pane) addTo.getParent()).getChildren().remove(addTo);
                parametricCompositionLogic.compositionShapeMap.remove(newID);
                Information<Double> xTranslationToRemove = parametricCompositionLogic.compositionShapesXTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                Information<Double> yTranslationToRemove = parametricCompositionLogic.compositionShapesYTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                parametricCompositionLogic.compositionShapesXTranslation.remove(xTranslationToRemove);
                parametricCompositionLogic.compositionShapesYTranslation.remove(yTranslationToRemove);
                setUpVariablesSelectionPane();
                //proceedWhenDeleting.apply(null);
            });

            addTo.setOnContextMenuRequested(contextMenuEvent -> {
                contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

        }

    }

    private void addArrow(Double translationX, Double translationY, double upperTranslationX, double upperTranslationY, Group addTo) {
        Arrow arrow = new Arrow();
        arrow.setEndX(translationX);
        arrow.setEndY(translationY);
        arrow.setTranslateX(upperTranslationX);
        arrow.setTranslateY(upperTranslationY);

        addTo.getChildren().add(arrow);
    }

    public void getTesteForSpecificParametric(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY, String newID, ParametricCompositionShape parametricCompositionShape, @NamedArg("Top level") ParametricCompositionShape topLevel) {
        toAdd.setPickOnBounds(false);
        Group addTo = new Group();

        int position = parametricCompositionLogic.parametricShapesXTranslation.indexOf(parametricCompositionLogic.parametricShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

        Information<Double> translationX = parametricCompositionLogic.parametricShapesXTranslation.get(position);
        Information<Double> translationY = parametricCompositionLogic.parametricShapesYTranslation.get(position);

        Information<ParametricVariable> parametricTranslationX = parametricCompositionLogic.parametricShapesXParametricTranslation.get(position);
        Information<ParametricVariable> parametricTranslationY = parametricCompositionLogic.parametricShapesYParametricTranslation.get(position);

        parametricCompositionShape.getPowerShapes().forEach(power -> {
            double translateXBy = power.getInitialTranslation().getX();
            double translateYBy = power.getInitialTranslation().getY() * -1;

            power.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
            power.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

            Power copy = orchestrator.getCopyOfPowerShape(power.getUuid().toString(), power.getWriteTranslateX(), power.getWriteTranslateY(), getProceedWhenDeletingPowerShape(power.getUuid().toString()));

            Node editor = copy.getEditorVisualization();

            editor.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
            editor.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

            addTo.getChildren().add(editor);

        });

        if (this != topLevel) {
            if (parametricTranslationX.getValue() != null && !parametricTranslationX.getValue().isNumeric()) {
                addArrow(translationX.getValue(), translationY.getValue(), upperTranslationX, upperTranslationY, addTo);

            } else if (parametricTranslationX.getValue() == null || parametricTranslationY.getValue() == null) {
                addArrow(translationX.getValue(), translationY.getValue(), upperTranslationX, upperTranslationY, addTo);

            } else if (parametricTranslationY.getValue() != null && !parametricTranslationY.getValue().isNumeric()) {
                addArrow(translationX.getValue(), translationY.getValue(), upperTranslationX, upperTranslationY, addTo);
            }
        }

        if (toAdd instanceof Pane) {
            Pane toAddTemp = (Pane) toAdd;
            toAddTemp.getChildren().add(addTo);
        } else if (toAdd instanceof Group) {
            Group toAddTemp = (Group) toAdd;
            toAddTemp.getChildren().add(addTo);
        }

        parametricCompositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY, topLevel);

        if (addHover) {
            Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth() + 20, addTo.getLayoutBounds().getHeight() + 20);
            rectangle.setArcWidth(10);
            rectangle.setArcHeight(10);
            rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
            rectangle.setX(addTo.getLayoutBounds().getMinX() - 10);
            rectangle.setY(addTo.getLayoutBounds().getMinY() - 10);

            String textLabel = parametricCompositionShape.getShapeName();
            Label shapeName = new Label(textLabel);

            shapeName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
            shapeName.setTextFill(Color.web("#BDBDBD"));
            shapeName.setPadding(new Insets(4));
            shapeName.setStyle("-fx-background-color:  rgba(255,255,255,0.3); -fx-background-radius: 15px;");

            if (parametricCompositionLogic.parametricCompositionShapeParametricVariableHover.get(newID) != null) {
                parametricCompositionLogic.parametricCompositionShapeParametricVariableHover.get(newID).addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        addTo.getChildren().add(rectangle);
                    } else {
                        addTo.getChildren().remove(rectangle);
                    }
                });
            }

            addTo.setOnMouseMoved(mouseEvent -> {
                shapeName.setLayoutX(mouseEvent.getX() + 15);
                shapeName.setLayoutY(mouseEvent.getY() + 15);
            });


            addTo.setOnMouseEntered(event -> {
                addTo.getChildren().add(rectangle);
                addTo.getChildren().add(shapeName);
            });


            addTo.setOnMouseExited(event -> {
                addTo.getChildren().remove(rectangle);
                addTo.getChildren().remove(shapeName);
            });

            addTo.setOnMouseClicked(event -> {
                selected = addTo;
                selectedTranslationX = translationX;
                selectedTranslationY = translationY;

                selectedParametricXTranslation = parametricTranslationX;
                selectedParametricYTranslation = parametricTranslationY;

                setUpComponents();
                System.out.println("I've clicked on here. It should now be true!");

                transformersBox.getChildren().clear();
                transformersBox.getChildren().addAll(getTransformers());
            });

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setId("betterMenuItem");

            MenuItem menuItem = new MenuItem("Delete");
            menuItem.setStyle("-fx-text-fill: red");
            contextMenu.getItems().add(menuItem);

            menuItem.setOnAction(actionEvent -> {
                ((Pane) addTo.getParent()).getChildren().remove(addTo);
                parametricCompositionLogic.parametricCompositionShapesMap.remove(newID);
                Information<Double> xTranslationToRemove = parametricCompositionLogic.parametricShapesXTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                Information<Double> yTranslationToRemove = parametricCompositionLogic.parametricShapesYTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                parametricCompositionLogic.parametricShapesXTranslation.remove(xTranslationToRemove);
                parametricCompositionLogic.parametricShapesYTranslation.remove(yTranslationToRemove);

            });

            addTo.setOnContextMenuRequested(contextMenuEvent -> {
                contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

        }

    }

    public Pane addNewCompositionShape(NewCompositionShape newCompositionShape) {
        String id = UUID.randomUUID().toString();

        parametricCompositionLogic.addShape(id, newCompositionShape,
                parametricCompositionLogic.compositionShapeMap,
                parametricCompositionLogic.compositionShapesXTranslation,
                parametricCompositionLogic.compositionShapesYTranslation,
                parametricCompositionLogic.compositionShapesXParametricTranslation,
                parametricCompositionLogic.compositionShapesYParametricTranslation,
                parametricCompositionLogic.compositionShapeParametricVariableHover);

        Pane toAdd = new Pane();
        getTesteForSpecific(toAdd, true, 0, 0, id, newCompositionShape);

        setUpVariablesSelectionPane();

        return toAdd;
    }

    public Pane addNewCompositionShapeWithTranslation(NewCompositionShape newCompositionShape, double xTranslation, double yTranslation, String id, ParametricVariable parametricX, ParametricVariable parametricY) {
        parametricCompositionLogic.addShape(id, newCompositionShape,
                parametricCompositionLogic.compositionShapeMap,
                parametricCompositionLogic.compositionShapesXTranslation,
                parametricCompositionLogic.compositionShapesYTranslation,
                parametricCompositionLogic.compositionShapesXParametricTranslation,
                parametricCompositionLogic.compositionShapesYParametricTranslation,
                parametricCompositionLogic.compositionShapeParametricVariableHover);

        parametricCompositionLogic.compositionShapesXParametricTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(parametricX);
        parametricCompositionLogic.compositionShapesYParametricTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(parametricY);

        parametricCompositionLogic.compositionShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(xTranslation);
        parametricCompositionLogic.compositionShapesYTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(yTranslation);

        Pane toAdd = new Pane();
        getTesteForSpecific(toAdd, true, xTranslation, yTranslation, id, newCompositionShape);

        setUpVariablesSelectionPane();

        return toAdd;
    }

    public Pane addParametricCompositionShape(ParametricCompositionShape parametricCompositionShape) {
        String id = parametricCompositionLogic.addShape(parametricCompositionShape,
                parametricCompositionLogic.parametricCompositionShapesMap,
                parametricCompositionLogic.parametricShapesXTranslation,
                parametricCompositionLogic.parametricShapesYTranslation,
                parametricCompositionLogic.parametricShapesXParametricTranslation,
                parametricCompositionLogic.parametricShapesYParametricTranslation,
                parametricCompositionLogic.parametricCompositionShapeParametricVariableHover
        );

        Pane toAdd = new Pane();
        getTesteForSpecificParametric(toAdd, true, 0, 0, id, parametricCompositionShape, this);

        setUpVariablesSelectionPane();

        return toAdd;
    }

    public Pane addParametricCompositionShapeWithTranslation(ParametricCompositionShape parametricCompositionShape, double xTranslation, double yTranslation, String id, ParametricVariable parametricX, ParametricVariable parametricY){
        parametricCompositionLogic.addShape(id, parametricCompositionShape,
                parametricCompositionLogic.parametricCompositionShapesMap,
                parametricCompositionLogic.parametricShapesXTranslation,
                parametricCompositionLogic.parametricShapesYTranslation,
                parametricCompositionLogic.parametricShapesXParametricTranslation,
                parametricCompositionLogic.parametricShapesYParametricTranslation,
                parametricCompositionLogic.parametricCompositionShapeParametricVariableHover
        );
        System.out.println("id é: " + id);
        parametricCompositionLogic.parametricShapesXParametricTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(parametricX);
        parametricCompositionLogic.parametricShapesYParametricTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(parametricY);

        parametricCompositionLogic.parametricShapesXTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(xTranslation);
        parametricCompositionLogic.parametricShapesYTranslation.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(yTranslation);


        Pane toAdd = new Pane();
        getTesteForSpecificParametric(toAdd, true, xTranslation, yTranslation, id, parametricCompositionShape, this);

        setUpVariablesSelectionPane();

        return toAdd;
    }


    private void setUpComponents() {
        setUpTranslationXBox();
        setUpTranslationYBox();
        setUpParametricTranslationXBox();
        setUpParametricTranslationYBox();
    }

    public ArrayList<Node> getTransformers() {
        ArrayList<Node> toReturn = new ArrayList<>();
        if (!notParametric) {
            toReturn.add(parametricTranslationXBox);
            toReturn.add(parametricTranslationYBox);
        }
        toReturn.add(translationXBox);
        toReturn.add(translationYBox);

        return toReturn;
    }

    private void setUpParametricTranslationXBox() {
        Label translationLabel = new Label("Parametric translation X:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        VBox dropInBox = getDropBox("X");

        HBox.setHgrow(translationLabel, Priority.ALWAYS);
        HBox.setHgrow(dropInBox, Priority.ALWAYS);

        parametricTranslationXBox = new VBox(translationLabel, dropInBox);
        parametricTranslationXBox.setPadding(new Insets(10, 10, 10, 15));
        parametricTranslationXBox.setAlignment(Pos.CENTER_LEFT);
        parametricTranslationXBox.setSpacing(20);
        parametricTranslationXBox.setMinHeight(30);
        parametricTranslationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");

        parametricTranslationXBox = new VBox();
    }

    private void setUpLabelStyle(Label label) {
        label.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        label.setTextFill(Color.web("#BDBDBD"));
        label.setWrapText(false);
    }

    private VBox getDropBox(String xOrY) {
        Label label = new Label("Drop a variable for " + xOrY + " parametric translation");
        setUpLabelStyle(label);
        VBox dropInBox = new VBox(label);
        dropInBox.setAlignment(Pos.CENTER);
        dropInBox.setStyle(getDashStyle(10));

        dropInBox.setPrefHeight(50);

        System.out.println("HERE WE HAVE THE FOLLOWING VARIABLES: ");
        for (ParametricVariable parametricVariable : parametricCompositionLogic.variables) {
            System.out.println("Parameter: " + parametricVariable.getVariableName());
        }

        return dropInBox;
    }

    private void setUpParametricTranslationYBox() {
        Label translationLabel = new Label("Parametric translation Y:");
        setUpLabelStyle(translationLabel);

        VBox dropInBox = getDropBox("Y");

        HBox.setHgrow(translationLabel, Priority.ALWAYS);
        HBox.setHgrow(dropInBox, Priority.ALWAYS);

        parametricTranslationYBox = new VBox(translationLabel, dropInBox);
        parametricTranslationYBox.setPadding(new Insets(10, 10, 10, 15));
        parametricTranslationYBox.setAlignment(Pos.CENTER_LEFT);
        parametricTranslationYBox.setSpacing(20);
        parametricTranslationYBox.setMinHeight(30);
        parametricTranslationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");

        parametricTranslationYBox = new VBox();
    }

    private Pane getNumericLabel() {
        HBox numericLabel = getPopupButton("Numeric Parameter", "#703636", "#F96767");

        numericLabel.setPrefWidth(200);
        numericLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(numericLabel, Priority.ALWAYS);

        numericLabel.setOnMouseClicked(variableLabelMouseEvent -> {
            PopupWindow popupWindow = new PopupWindow();
            Stage tempStage = popupWindow.getStage();

            Pane closeButton = PopupWindow.getButton("Close", "null", "#5E2323", "#EB5757", event -> tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            popupWindow.createPopup("Create new Parameter", scene, 400, 250, getCreateNumericVariablePane(tempStage), closeButton);
        });

        return numericLabel;
    }

    private Pane getFigureVariableLabel() {
        HBox numericLabel = getPopupButton("Figure Parameter", "#36706D", "#67F0F9");

        numericLabel.setPrefWidth(200);
        numericLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(numericLabel, Priority.ALWAYS);

        numericLabel.setOnMouseClicked(variableLabelMouseEvent -> {
            PopupWindow popupWindow = new PopupWindow();
            Stage tempStage = popupWindow.getStage();

            Label hintLabel = getLabel("", 10);

            Pane closeButton = PopupWindow.getButton("Close", "null", "#5E2323", "#EB5757", event -> tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            popupWindow.createPopup("Create new Figure Parameter", scene, 400, 550, getCreateFigureVariablePane(tempStage, hintLabel), closeButton, hintLabel);
        });

        return numericLabel;
    }

    private boolean variableAlreadyExists(ParametricVariable variableName) {

        for (ParametricVariable variable : parametricCompositionLogic.variables) {
            if (variable.equals(variableName))
                return true;
        }
        return false;
    }


    private Pane getCreateVariablePane(Stage stage, Label hintLabel) {
        Label variableName = getLabel("Parameter Name:");

        TextField textField = new TextField();
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER_LEFT);

        Pane saveButton = PopupWindow.getButton("Save", "null", "#35654F", "#56F28F", event -> {
            if (textField.getText().isEmpty() || textField.getText().isBlank()) {
                textField.setPromptText("Parameter name empty");
                System.err.println("Parameter name is empty...!");
            } else if (!textField.getText().isEmpty() && Character.isUpperCase(textField.getText().charAt(0))) {
                textField.setText("");
                hintLabel.setText("Parameter name can't start with an upper case!");
                textField.setPromptText("Parameter name can't start with an upper case!");
                System.err.println("Parameter name starts with an upper case letter...!");
            } else {
                ParametricVariable parametricVariable = new ParametricVariable(textField.getText());
                if (!variableAlreadyExists(parametricVariable)) {
                    parametricCompositionLogic.variables.add(parametricVariable);
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                } else {
                    textField.setText("");
                    hintLabel.setText("Parameter name already in use");
                    textField.setPromptText("Parameter name already in use");
                    System.err.println("We are trying to add an already existing parameter... on the textfield!");
                }

            }

        });

        HBox hBox = new HBox(variableName, textField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        VBox toReturn = new VBox(hBox, saveButton);
        toReturn.setSpacing(20);


        return toReturn;
    }

    private VBox getShapesBox(String text) {
        Label label = getLabel(text);

        VBox box = new VBox(label);
        box.setSpacing(15);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        box.setStyle("-fx-background-radius: 10; -fx-background-color: #3D3B40");

        return box;
    }

    private Label createFigureVariableAndClosePopup(String shapeName, UUID id, Stage stage, Label hintLabel, CustomShape shape) {
        StringBuilder name = new StringBuilder(shapeName);

        boolean hasVariables =  false;
        ArrayList<ParametricVariable> variablesToSetUp = new ArrayList<>();

        if(shape instanceof ParametricCompositionShape){
            name.append("(");
            ArrayList<ParametricVariable> variableArrayList = ((ParametricCompositionShape) shape).getOutputVariables();
            for(int i = 0; i < variableArrayList.size(); i++){
                variablesToSetUp.add(variableArrayList.get(i));
                if(i == variableArrayList.size() -1){
                    name.append(variableArrayList.get(i).getVariableName()).append(", ");
                }else{
                    name.append(variableArrayList.get(i).getVariableName());
                }
            }

            if(variableArrayList.size() > 0)
                hasVariables = true;

            name.append(")");
        }else if(shape instanceof Power){
            name.append("(");
            ArrayList<ParametricVariable> variableArrayList = ((Power) shape).getOutputVariables();
            for(int i = 0; i < variableArrayList.size(); i++){
                variablesToSetUp.add(variableArrayList.get(i));
                if(i != variableArrayList.size() - 1){
                    name.append(variableArrayList.get(i).getVariableName()).append(", ");
                }else{
                    name.append(variableArrayList.get(i).getVariableName());
                }
            }

            if(variableArrayList.size() > 0)
                hasVariables = true;

            name.append(")");
        }

        Label label = getLabel(name.toString(), "#555259");

        if(!hasVariables){
            label.setOnMouseClicked(event -> {
                ParametricVariable parametricVariable = new ParametricVariable(shapeName, id);
                if (variableAlreadyExists(parametricVariable)) {
                    hintLabel.setText("Parameter name already in use");
                    System.err.println("We are trying to add an already existing parameter... on the textfield!");

                } else {
                    parametricCompositionLogic.variables.add(parametricVariable);
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                }

            });
        }else{
            label.setOnMouseClicked(event -> {
                PopupWindow popupWindow = new PopupWindow();
                Stage tempStage = popupWindow.getStage();

                Pane closeButton = PopupWindow.getButton("Close", "null", "#5E2323", "#EB5757", mouseEvent -> tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST)));
                popupWindow.createPopup(false,"Set new Figure Parameter", scene, 400, 300, getCreateSetFigureVariablesPane(shapeName, id, stage, tempStage, hintLabel, variablesToSetUp), closeButton, hintLabel);
            });
        }


        return label;
    }


    private Pane getCreateFigureVariablePane(Stage stage, Label hintLabel) {

        Label variableName = getLabel("Figure:");

        VBox basicShapes = getShapesBox("Basic Shapes:");
        VBox compositionShapes = getShapesBox("Composition Shapes:");
        VBox powerShapes = getShapesBox("Power Shapes:");
        VBox parametricShapes = getShapesBox("Parametric Composition Shapes:");

        orchestrator.getBasicShapes().forEach(b ->
                basicShapes.getChildren().add(createFigureVariableAndClosePopup(b.getShapeName(), b.getUUID(), stage, hintLabel, b)));

        orchestrator.getNewCompositionShapes().forEach(c ->
                compositionShapes.getChildren().add(createFigureVariableAndClosePopup(c.getShapeName(), c.getUUID(), stage, hintLabel, c)));

        orchestrator.getPowerShapes().forEach(p ->
                powerShapes.getChildren().add(createFigureVariableAndClosePopup(p.getShapeName(), p.getUUID(), stage, hintLabel, p)));

        orchestrator.getParametricCompositionShapes().forEach(p -> {
            if (p.getID() != getUUID()) {
                parametricShapes.getChildren().add(createFigureVariableAndClosePopup(p.getShapeName(), p.getUUID(), stage, hintLabel, p));
            }
        });

        VBox scrollPaneVBox = new VBox(basicShapes, compositionShapes, powerShapes, parametricShapes);
        scrollPaneVBox.setSpacing(15);

        ScrollPane scrollPane = new ScrollPane(scrollPaneVBox);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: rgb(38,37,40); -fx-background-radius: 10; -fx-background: transparent");


        VBox toReturn = new VBox(variableName, scrollPane);
        toReturn.setSpacing(20);

        return toReturn;
    }


    private Pane getCreateSetFigureVariablesPane(String shapeName,UUID id, Stage stage,Stage tempStage, Label hintLabel, ArrayList<ParametricVariable> variablesToSetUp) {

        ArrayList<TextField> textFields = new ArrayList<>();

        VBox variablesPane = getShapesBox(shapeName + ":");

        variablesToSetUp.forEach(variable -> {
            HBox hBox = new HBox();
            Label label = getLabel(variable.toString()+":", "#555259");
            TextField textField = new TextField(variable.getVariableName());
            textFields.add(textField);
            hBox.getChildren().addAll(label, textField);

            hBox.setPadding(new Insets(10));
            hBox.setSpacing(10);
            hBox.setAlignment(Pos.BASELINE_LEFT);

            variablesPane.getChildren().addAll(hBox);
        });

        Pane saveButton = PopupWindow.getButton("Save", "null", "#3C5849", "#6FCF97", mouseEvent -> {
            tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

            String variableName =  shapeName + "(";
            for(int i = 0; i < textFields.size(); i++){
                if(i != textFields.size() - 1){
                    variableName += (textFields.get(i).getText()) + (", ");
                }else{
                    variableName += (textFields.get(i).getText());
                }
            }
            variableName += ")";

            ParametricVariable parametricVariable = new ParametricVariable(variableName, id);
            parametricCompositionLogic.variables.add(parametricVariable);
            //TODO: Here we aren't checking if the name already exists...
        });


        VBox toReturn = new VBox(variablesPane, saveButton);
        toReturn.setSpacing(20);

        return toReturn;
    }


    private Pane getCreateNumericVariablePane(Stage stage) {

        Label variableName = getLabel("Parameter Name:");

        TextField textField = new TextField();
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER_LEFT);

        Label variableValue = getLabel("Parameter Value:");

        TextField textFieldValue = new TextField();
        textFieldValue.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textFieldValue.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textFieldValue.setAlignment(Pos.CENTER_LEFT);

        Pane saveButton = PopupWindow.getButton("Save", "null", "#35654F", "#56F28F", event -> {
            if (textField.getText().isEmpty() || textField.getText().isBlank() || textFieldValue.getText().isBlank() || textFieldValue.getText().isEmpty()) {
                System.err.println("Parameter name or variable value is empty...!");
            } else {
                try {
                    double value = Double.parseDouble(textFieldValue.getText());
                    ParametricVariable parametricVariable = new ParametricVariable(textField.getText(), value);
                    if (!variableAlreadyExists(parametricVariable)) {
                        parametricCompositionLogic.variables.add(parametricVariable);
                        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    } else {
                        textField.setPromptText("Parameter name empty");
                        System.err.println("We are trying to add an already existing variable... on the textfield!");
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.err.println("Parameter value is incorrect!");

                }

            }

        });

        HBox hBox = new HBox(variableName, textField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        HBox valueHBox = new HBox(variableValue, textFieldValue);
        valueHBox.setSpacing(10);
        valueHBox.setAlignment(Pos.CENTER_LEFT);

        VBox toReturn = new VBox(hBox, valueHBox, saveButton);
        toReturn.setSpacing(20);

        return toReturn;
    }

    private Pane getVariableLabel() {
        HBox variableLabel = getPopupButton("Parameter", "#5F3670", "#F967A8");

        variableLabel.setPrefWidth(200);
        variableLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(variableLabel, Priority.ALWAYS);

        variableLabel.setOnMouseClicked(variableLabelMouseEvent -> {
            PopupWindow popupWindow = new PopupWindow();
            Stage tempStage = popupWindow.getStage();

            Node hintLabel = getLabel("", 10);

            Pane closeButton = PopupWindow.getButton("Close", "null", "#5E2323", "#EB5757", event -> tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            popupWindow.createPopup("Create new Parameter", scene, 400, 230, getCreateVariablePane(tempStage, (Label) hintLabel), closeButton, hintLabel);
        });

        return variableLabel;
    }

    public ArrayList<ParametricVariable> getOutputVariables() {
        ArrayList<ParametricVariable> toReturn = new ArrayList<>();
        for (ParametricVariable variable : parametricCompositionLogic.variables) {
            if (!variable.isNumeric()) {
                toReturn.add(variable);
            }
        }
        return toReturn;
    }

    private HBox getPopupButton(String label, String backgroundColor, String labelColor) {
        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexShape);

        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + 10);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        complexShapeHBox.setOnMouseEntered(mouseEvent -> complexShapeHBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: " + 10));

        complexShapeHBox.setOnMouseExited(mouseEvent -> complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + 10));

        complexShape.setMinHeight(30);
        complexShapeHBox.setPadding(new Insets(0, 5, 0, 5));


        return complexShapeHBox;
    }

    private void addPowerShapesVariables(){
        parametricCompositionLogic.powerShapesMap.forEach((id, powerShape) -> {
            powerShape.getOutputVariables().forEach(powerVariable -> {
                if(parametricCompositionLogic.variables.stream().noneMatch(p -> p.getId().equals(powerVariable.getId()))){
                    parametricCompositionLogic.variables.add(powerVariable);
                }
            });
        });
    }

    private void removeNonNecessaryPowerVariables(){
        parametricCompositionLogic.powerShapesMap.forEach((id, powerShape) -> {
            ArrayList<ParametricVariable> inUse = powerShape.getOutputVariables();
            ArrayList<ParametricVariable> all = powerShape.getAllOutputVariables();

            all.forEach(powerVariable -> {
                if(inUse.stream().noneMatch(p -> p.getId().equals(powerVariable.getId()))){
                    if(parametricCompositionLogic.variables.stream().anyMatch(p -> p.getId().equals(powerVariable.getId()))){
                        //Let's remove unused power variable!
                        parametricCompositionLogic.variables.removeIf(a -> a.getId().equals(powerVariable.getId()));
                    }
                }
            });


        });
    }

    public void setUpVariablesSelectionPane() {
        removeNonNecessaryPowerVariables();

        variablesSelectionPane.getChildren().clear();

        CustomMenuItem numericVariable = new CustomMenuItem(getNumericLabel());
        CustomMenuItem figureVariable = new CustomMenuItem(getFigureVariableLabel());
        CustomMenuItem variable = new CustomMenuItem(getVariableLabel());

        ContextMenu contextMenuParametric = new ContextMenu();
        contextMenuParametric.setId("testeCoiso");
        contextMenuParametric.getItems().addAll(numericVariable, variable, figureVariable);

        VBox shapes = new VBox();
        shapes.setSpacing(10);
        parametricCompositionLogic.compositionShapeMap.forEach((id, compositionShape) -> {
            shapes.getChildren().add(getCompositionShapesParametricSelectionBoxes(id, compositionShape));
        });

        parametricCompositionLogic.parametricCompositionShapesMap.forEach((id, parametricShape) -> {
            shapes.getChildren().add(getParametricShapesParametricSelectionBoxes(id, parametricShape));
        });

        parametricCompositionLogic.powerShapesMap.forEach((id, powerShape) -> {
            shapes.getChildren().add(getPowerShapesParametricselectionBoxes(id, powerShape));
        });

        variablesSelectionPane.setPadding(new Insets(10));
        variablesSelectionPane.setPrefHeight(50);
        variablesSelectionPane.setSpacing(15);
        variablesSelectionPane.getChildren().add(shapes);
    }

    private Pane setUpParametricSelectionBox(String xOrY, String id, ArrayList<Information<ParametricVariable>> array) {
        return setUpParametricSelectionBox(xOrY, id, array, " parametric translation ->");
    }


    private Pane setUpParametricSelectionBox(String xOrY, String id, ArrayList<Information<ParametricVariable>> array, String text) {
        Label label = new Label(xOrY + text);
        setUpLabelStyle(label);
        VBox dropInBox = new VBox(label);
        dropInBox.setAlignment(Pos.CENTER);
        dropInBox.setStyle(getDashStyle(5));
        dropInBox.setPrefHeight(50);
        HBox.setHgrow(dropInBox, Priority.ALWAYS);

        HBox parametrixHBox = new HBox(dropInBox);
        parametrixHBox.setSpacing(20);
        HBox.setHgrow(parametrixHBox, Priority.ALWAYS);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setId("testeCoiso");

        for (ParametricVariable variable : parametricCompositionLogic.variables) {
            CustomMenuItem customMenuItem = new CustomMenuItem(getPaneForVariable(variable, false));
            customMenuItem.setOnAction(event -> {
                parametrixHBox.getChildren().removeIf(s -> s != dropInBox);

                Pane pane = getPaneForVariable(variable, false);
                ContextMenu contextMenuPane = new ContextMenu();
                contextMenuPane.setId("betterMenuItem");
                MenuItem menuItem = new MenuItem("Delete Parameter");
                menuItem.setStyle("-fx-text-fill: red");
                contextMenuPane.getItems().add(menuItem);
                menuItem.setOnAction(actionEvent -> {
                    parametrixHBox.getChildren().remove(pane);
                });
                pane.setOnContextMenuRequested(contextMenuPaneEvent -> contextMenuPane.show(pane, contextMenuPaneEvent.getScreenX(), contextMenuPaneEvent.getScreenY()));

                parametrixHBox.getChildren().add(pane);
                array.stream().filter(p -> p.getId().equals(id)).findFirst().get().setValue(variable);
            });
            contextMenu.getItems().add(customMenuItem);
        }

        Optional<Information<ParametricVariable>> currentVariable = array.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (currentVariable.isPresent() && currentVariable.get().getValue() != null) {
            Information<ParametricVariable> temp = currentVariable.get();
            parametrixHBox.getChildren().add(getPaneForVariable(temp.getValue(), false));
        }

        parametrixHBox.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                contextMenu.show(parametrixHBox, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });


        return parametrixHBox;
    }


    private Pane setUpParametricSelectionBoxOtherVariables(String xOrY, ArrayList<Pair<ParametricVariable, ParametricVariable>> array, String text, ParametricVariable keyVariable) {
        Label label = new Label(xOrY + text);
        setUpLabelStyle(label);
        VBox dropInBox = new VBox(label);
        dropInBox.setAlignment(Pos.CENTER);
        dropInBox.setStyle(getDashStyle(5));
        dropInBox.setPrefHeight(50);
        HBox.setHgrow(dropInBox, Priority.ALWAYS);

        HBox parametrixHBox = new HBox(dropInBox);
        parametrixHBox.setSpacing(20);
        HBox.setHgrow(parametrixHBox, Priority.ALWAYS);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setId("testeCoiso");

        for (ParametricVariable variable : parametricCompositionLogic.variables) {
            CustomMenuItem customMenuItem = new CustomMenuItem(getPaneForVariable(variable, false));
            customMenuItem.setOnAction(event -> {
                parametrixHBox.getChildren().removeIf(s -> s != dropInBox);

                Pane pane = getPaneForVariable(variable, false);
                ContextMenu contextMenuPane = new ContextMenu();
                contextMenuPane.setId("betterMenuItem");
                MenuItem menuItem = new MenuItem("Delete Parameter");
                menuItem.setStyle("-fx-text-fill: red");
                contextMenuPane.getItems().add(menuItem);
                menuItem.setOnAction(actionEvent -> {
                    parametrixHBox.getChildren().remove(pane);
                });
                pane.setOnContextMenuRequested(contextMenuPaneEvent -> contextMenuPane.show(pane, contextMenuPaneEvent.getScreenX(), contextMenuPaneEvent.getScreenY()));

                parametrixHBox.getChildren().add(pane);
                if (array.stream().anyMatch(p -> p.getKey().getId().equals(keyVariable.getId()))) {
                    array.remove(array.stream().filter(p -> p.getKey().getId().equals(keyVariable.getId())).findFirst().get());
                }

                array.add(new Pair<>(keyVariable, variable));
            });
            contextMenu.getItems().add(customMenuItem);
        }

        Optional<Pair<ParametricVariable, ParametricVariable>> currentPair = array.stream().filter(p -> p.getKey().equals(keyVariable)).findFirst();
        if (currentPair.isPresent() && currentPair.get().getValue() != null) {
            ParametricVariable temp = currentPair.get().getValue();
            parametrixHBox.getChildren().add(getPaneForVariable(temp, false));
        }
        /*Optional<Information<ParametricVariable>> currentVariable = array.stream().filter(p -> p.getId().equals(id)).findFirst();
        if(currentVariable.isPresent() && currentVariable.get().getValue() != null){
            Information<ParametricVariable> temp = currentVariable.get();
            parametrixHBox.getChildren().add(getPaneForVariable(temp.getValue(), false));
        }*/

        parametrixHBox.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                contextMenu.show(parametrixHBox, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });


        return parametrixHBox;
    }


    private Pane getCompositionShapesParametricSelectionBoxes(String id, NewCompositionShape compositionShape) {
        Label compositionShapeName = new Label(compositionShape.getShapeName());
        setUpLabelStyle(compositionShapeName);


        Label xLabel = new Label("X parametric translation ->");
        setUpLabelStyle(xLabel);
        VBox dropInBoxX = new VBox(xLabel);
        dropInBoxX.setAlignment(Pos.CENTER);
        dropInBoxX.setStyle(getDashStyle(5));
        dropInBoxX.setPrefHeight(50);
        HBox.setHgrow(dropInBoxX, Priority.ALWAYS);

        Pane xParametric = setUpParametricSelectionBox("X", id, parametricCompositionLogic.compositionShapesXParametricTranslation);
        Pane yParametric = setUpParametricSelectionBox("Y", id, parametricCompositionLogic.compositionShapesYParametricTranslation);

        VBox vBox = new VBox(compositionShapeName, xParametric, yParametric);

        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        vBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10");

        vBox.setOnMouseEntered(event -> parametricCompositionLogic.compositionShapeParametricVariableHover.get(id).setValue(true));

        vBox.setOnMouseExited(event -> parametricCompositionLogic.compositionShapeParametricVariableHover.get(id).setValue(false));

        return vBox;
    }

    private Pane getPowerShapesParametricselectionBoxes(String id, Power powerShape){
        Label compositionShapeName = new Label(powerShape.getShapeName());
        setUpLabelStyle(compositionShapeName);

        Label xLabel = new Label("X parametric translation ->");
        setUpLabelStyle(xLabel);
        VBox dropInBoxX = new VBox(xLabel);
        dropInBoxX.setAlignment(Pos.CENTER);
        dropInBoxX.setStyle(getDashStyle(5));
        dropInBoxX.setPrefHeight(50);
        HBox.setHgrow(dropInBoxX, Priority.ALWAYS);

        Pane xParametric = setUpParametricSelectionBox("X", id, parametricCompositionLogic.powerShapesXParametricTranslation);
        Pane yParametric = setUpParametricSelectionBox("Y", id, parametricCompositionLogic.powerShapesYParametricTranslation);

        VBox vBox = new VBox(compositionShapeName, xParametric, yParametric);

        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        vBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10");

        vBox.setOnMouseEntered(event -> parametricCompositionLogic.powerShapeParametricVariableHover.get(id).setValue(true));

        vBox.setOnMouseExited(event -> parametricCompositionLogic.powerShapeParametricVariableHover.get(id).setValue(false));


        ArrayList<Pair<ParametricVariable, ParametricVariable>> thisCompositionShape = parametricCompositionLogic.variablePairs.stream().filter(p -> p.getId().equals(id)).findFirst().get().getValue();

        if (powerShape.getOutputVariables().size() != 0) {

            for (ParametricVariable variable : powerShape.getOutputVariables()) {
                if (thisCompositionShape.stream().noneMatch(p -> p.getKey().getId().equals(variable.getId()))) {
                    //aqui tenho que adicionar apenas os pares que nao estavam ainda...
                    //Adiciono se nao houver ainda entrada para a variable certa...
                    thisCompositionShape.add(new Pair<>(variable, null));
                }
            }


            VBox variablesToFill = new VBox();
            variablesToFill.setSpacing(10);

            for (ParametricVariable variable : powerShape.getOutputVariables()) {
                HBox info = new HBox();
                info.setSpacing(10);

                Pane variablePane = getPaneForVariable(variable, false, 10, 20, 20, 3);

                info.getChildren().add(variablePane);
                Pane customPaneForVariable = setUpParametricSelectionBoxOtherVariables("-> ", thisCompositionShape, variable.getDescription() + "  ->", variable);

                info.getChildren().add(customPaneForVariable);

                variablesToFill.getChildren().addAll(info);
            }
            vBox.getChildren().add(variablesToFill);
        }

        return vBox;
    }


    private Pane getParametricShapesParametricSelectionBoxes(String id, ParametricCompositionShape compositionShape) {
        Label compositionShapeName = new Label(compositionShape.getShapeName());
        setUpLabelStyle(compositionShapeName);

        Label xLabel = new Label("X parametric translation ->");
        setUpLabelStyle(xLabel);
        VBox dropInBoxX = new VBox(xLabel);
        dropInBoxX.setAlignment(Pos.CENTER);
        dropInBoxX.setStyle(getDashStyle(5));
        dropInBoxX.setPrefHeight(50);
        HBox.setHgrow(dropInBoxX, Priority.ALWAYS);

        Pane xParametric = setUpParametricSelectionBox("X", id, parametricCompositionLogic.parametricShapesXParametricTranslation);
        Pane yParametric = setUpParametricSelectionBox("Y", id, parametricCompositionLogic.parametricShapesYParametricTranslation);

        VBox vBox = new VBox(compositionShapeName, xParametric, yParametric);

        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        vBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10");

        vBox.setOnMouseEntered(event -> parametricCompositionLogic.parametricCompositionShapeParametricVariableHover.get(id).setValue(true));

        vBox.setOnMouseExited(event -> parametricCompositionLogic.parametricCompositionShapeParametricVariableHover.get(id).setValue(false));


        ArrayList<Pair<ParametricVariable, ParametricVariable>> variablePars = parametricCompositionLogic.variablePairs.stream().filter(p -> p.getId().equals(id)).findFirst().get().getValue();

        if (compositionShape.getOutputVariables().size() != 0) {
            System.out.println("É DIFERENTE DE ZERO!");

            System.out.println("variables: " );
            compositionShape.getOutputVariables().forEach(variable -> {
                System.out.println("\tvariable:" + variable.getId() + ", name: " + variable.getVariableName());
            });

            System.out.println("arraylist pair:");
            variablePars.forEach(pair -> {
                System.out.println("\tpair key: " + pair.getKey().getId() + ", name: " + pair.getKey().getVariableName());
                if(pair.getValue() != null){
                    System.out.println("\t pair value: " + pair.getValue().getId() + ", name: " + pair.getValue().getVariableName());
                }else{
                    System.err.println("Without pair value!");
                }
                System.out.println("\t--");
            });

            for (ParametricVariable variable : compositionShape.getOutputVariables()) {
                System.out.println("estamos na varia´´el: " + variable.getVariableName() + ", " + variable.getId());
                if (variablePars.stream().noneMatch(p -> p.getKey().getId().equals(variable.getId()))) {
                    //aqui tenho que adicionar apenas os pares que nao estavam ainda...
                    //Adiciono se nao houver ainda entrada para a variable certa...
                    variablePars.add(new Pair<>(variable, null));
                }
            }


            VBox variablesToFill = new VBox();
            variablesToFill.setSpacing(10);

            for (ParametricVariable variable : compositionShape.getOutputVariables()) {
                HBox info = new HBox();
                info.setSpacing(10);

                Pane variablePane = getPaneForVariable(variable, false, 10, 20, 20, 3);

                info.getChildren().add(variablePane);
                //Pane customPaneForVariable = setUpParametricSelectionBox("-> ", id,parametricShapesParametricTranslation);
                Pane customPaneForVariable = setUpParametricSelectionBoxOtherVariables("-> ", variablePars, " parametric translation ->", variable);

                info.getChildren().add(customPaneForVariable);

                variablesToFill.getChildren().addAll(info);
            }
            vBox.getChildren().add(variablesToFill);
        }

        return vBox;
    }


    public void setUpVariablesPane() {
        HBox variablesLabel = getButtonWith_Label_Color("Parameters", "#717640", "#BBBBBB", 6);
        Pane addButton = getButtonWith_Label_Color_Image("", "#717640", "#BBBBBB", "icons8-plus-math-96.png", 6);

        variablesLabel.setMaxHeight(30);
        variablesLabel.setMinHeight(30);
        variablesLabel.setPrefHeight(30);

        addButton.setMaxHeight(30);
        addButton.setPrefHeight(30);
        addButton.setMinHeight(30);
        addButton.setPrefSize(30, 30);
        addButton.setMaxWidth(30);

        HBox topButtonsAndLabel = new HBox(variablesLabel, addButton);
        topButtonsAndLabel.setSpacing(10);

        CustomMenuItem numericVariable = new CustomMenuItem(getNumericLabel());
        CustomMenuItem figureVariable = new CustomMenuItem(getFigureVariableLabel());
        CustomMenuItem variable = new CustomMenuItem(getVariableLabel());

        ContextMenu contextMenuParametric = new ContextMenu();
        contextMenuParametric.setId("testeCoiso");
        contextMenuParametric.getItems().addAll(numericVariable, variable, figureVariable);

        addButton.setOnMouseClicked(mouseEvent -> contextMenuParametric.show(addButton, mouseEvent.getScreenX(), mouseEvent.getScreenY()));

        variablesPane.setPadding(new Insets(10));
        variablesPane.setPrefHeight(50);
        variablesPane.setSpacing(15);
        variablesPane.getChildren().add(topButtonsAndLabel);
    }


    private Label getLabel(String text) {
        Label translationLabel = new Label(text);
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);
        return translationLabel;
    }

    private Label getLabel(String text, String background) {
        Label translationLabel = new Label(text);
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        translationLabel.setPadding(new Insets(5));
        translationLabel.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 7;");
        return translationLabel;
    }

    private Label getLabel(String text, int size) {
        Label translationLabel = new Label(text);
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, size));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);
        return translationLabel;
    }

    private void setUpTranslationXBox() {
        Label translationLabel = getLabel(notParametric ? "Translation X:" : "Temporary translation X:");

        TextField textField = new TextField(String.valueOf(selectedTranslationX.getValue() / SCALE));
        textField.setPromptText(String.valueOf(selectedTranslationX.getValue() / SCALE));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Information<Double> tempTranslationX = selectedTranslationX;

        Slider translationXSlider = new Slider();
        translationXSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setMin(-NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setValue(tempTranslationX.getValue() / SCALE);

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


        translationXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            tempTranslationX.setValue(truncatedDouble * SCALE);
            selected.setTranslateX(selected.getTranslateX() + (newValue.doubleValue() - oldValue.doubleValue()) * SCALE);
        });

        translationXSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationXSlider, Priority.ALWAYS);


        translationXBox = new HBox(translationLabel, translationXSlider, textField);
        translationXBox.setPadding(new Insets(10, 10, 10, 15));
        translationXBox.setAlignment(Pos.CENTER_LEFT);
        translationXBox.setSpacing(20);
        translationXBox.setMinHeight(30);
        translationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
    }

    private void setUpTranslationYBox() {
        Label translationLabel = new Label(notParametric ? "Translation Y:" : "Temporary translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(String.valueOf(-selectedTranslationY.getValue() / SCALE));
        textField.setPromptText(String.valueOf(-selectedTranslationY.getValue() / SCALE));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Information<Double> tempTranslationY = selectedTranslationY;

        Slider translationYSlider = new Slider();
        translationYSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setMin(-NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setValue(-tempTranslationY.getValue() / SCALE);

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

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane

        translationYSlider.setMajorTickUnit(0.1);
        translationYSlider.setMinorTickCount(0);
        translationYSlider.setSnapToTicks(true);

        translationYSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            tempTranslationY.setValue(truncatedDouble * -SCALE);
            selected.setTranslateY(selected.getTranslateY() + (newValue.doubleValue() - oldValue.doubleValue()) * -SCALE);
        });

        translationYSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationYSlider, Priority.ALWAYS);


        translationYBox = new HBox(translationLabel, translationYSlider, textField);
        translationYBox.setPadding(new Insets(10, 10, 10, 15));
        translationYBox.setAlignment(Pos.CENTER_LEFT);
        translationYBox.setMinHeight(40);
        translationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
    }


    @Override
    public String getShapeName() {
        return name;
    }

    @Override
    public void setShapeName(String shapeName) {
        this.name = shapeName;
    }

    @Override
    public UUID getUUID() {
        return getID();
    }

    @Override
    public Pane getTranslationXSection() {
        return translationXBox;
    }

    @Override
    public Pane getTranslationYSection() {
        return translationYBox;
    }

    @Override
    public void redrawThumbnail() {
        thumbnail.getChildren().clear();

        addScreenshootAndTags();
    }

    private void addScreenshootAndTags() {
        thumbnail.getChildren().add(GridCanvas.takeScreenshootWithRoundedCornersAndLoadTemporarily(this));

        //label tags
        VBox nameAndTagVBox = new VBox();
        nameAndTagVBox.setSpacing(5);

        Label nameLabel = new Label(getShapeName());
        nameLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 12));
        nameLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)")));
        nameLabel.setWrapText(true);

        Label tagLabel = new Label("Parametric Comp. Shape");
        tagLabel.setWrapText(true);
        tagLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        tagLabel.setPadding(new Insets(3));
        tagLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        tagLabel.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");


        FlowPane detailsHB = new FlowPane(tagLabel);
        detailsHB.setVgap(10);
        detailsHB.setHgap(10);

        nameAndTagVBox.getChildren().addAll(nameLabel, detailsHB);

        FlowPane variablesFlowPane = new FlowPane();
        variablesFlowPane.setVgap(10);
        variablesFlowPane.setHgap(10);

        removeNonNecessaryPowerVariables();
        if (parametricCompositionLogic.variables.size() != 0) {
            //There are variables!
            nameAndTagVBox.getChildren().add(variablesFlowPane);
            for (ParametricVariable variable : getOutputVariables()) {
                Pane variablePane = getPaneForVariable(variable, false, 10, 20, 20, 3);
                variablesFlowPane.getChildren().add(variablePane);
            }
        }

        thumbnail.setAlignment(Pos.TOP_CENTER);
        thumbnail.getChildren().add(nameAndTagVBox);
    }

    @Override
    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier) {
        thumbnail.getChildren().clear();
        thumbnail.setMinWidth(0.0);
        thumbnail.setPadding(new Insets(10));
        thumbnail.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        thumbnail.setSpacing(10);
        thumbnail.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(thumbnail, Priority.NEVER);

        thumbnail.getChildren().clear();

        //label tags
        addScreenshootAndTags();
        //end label tags

        thumbnail.setOnDragDetected(event -> {
            Dragboard db = thumbnail.startDragAndDrop(TransferMode.ANY);
            supplier.get();
            ClipboardContent content = new ClipboardContent();
            content.putString(toPutIntoDragbord.get());
            db.setContent(content);

            event.consume();
        });

        setThumbnailDeleting();

        return thumbnail;
    }

    private void setThumbnailDeleting() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setId("betterMenuItem");

        MenuItem menuItem = new MenuItem("Delete Parametric Composition Shape");
        menuItem.setStyle("-fx-text-fill: red");
        contextMenu.getItems().add(menuItem);

        menuItem.setOnAction(actionEvent -> proceedWhenDeletingFromThumbnail.apply(getUUID().toString()));

        thumbnail.setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(thumbnail, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));
    }


    /*
     * Auxiliary methods
     * */
    public double getMinimumTranslationY_new() {
        ArrayList<Double> minimumValues = getMinimumTranslationY_inner();

        if (minimumValues.size() == 0) {
            return 0.0;
        } else {
            Collections.sort(minimumValues);
            return minimumValues.get(0);
        }
    }

    private ArrayList<Double> getMinimumTranslationY_inner() {
        if (parametricCompositionLogic.compositionShapesYTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMinimumTranslationY_inner_forCompositions(parametricCompositionLogic.compositionShapesYTranslation, 0));

        return maximumValues;
    }

    private double getMinimumTranslationY_inner_forCompositions(ArrayList<Information<Double>> compositionShapesY, double previousTranslation) {
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesY.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationY = information.getValue();
            NewCompositionShape shape = parametricCompositionLogic.compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMinimumBasicShapeYTranslation();
            tempValues.forEach(p -> {
                values.add(p + translationY + previousTranslation);
            });
            Collections.sort(values);
            toReturn.add(values.size() == 0 ? 0 : values.get(0));
            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMinimumTranslationY_inner_forCompositions(shape.compositionShapesYTranslation, translationY));

        });
        Collections.sort(toReturn);
        if (toReturn.size() == 0) {
            return 0;
        } else {
            return toReturn.get(0);
        }
    }

    public double getMaximumTranslationY() {
        ArrayList<Double> minimumValues = getMaximumTranslationY_inner();

        if (minimumValues.size() == 0) {
            return 0.0;
        } else {
            minimumValues.sort(Comparator.reverseOrder());
            return minimumValues.get(0);
        }
    }

    private ArrayList<Double> getMaximumTranslationY_inner() {
        if (parametricCompositionLogic.compositionShapesYTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMaximumTranslationY_inner_forCompositions(parametricCompositionLogic.compositionShapesYTranslation, 0));

        return maximumValues;
    }

    private double getMaximumTranslationY_inner_forCompositions(ArrayList<Information<Double>> compositionShapesY, double previousTranslation) {
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesY.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationY = information.getValue();
            NewCompositionShape shape = parametricCompositionLogic.compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMaximumBasicShapeYTranslation();
            tempValues.forEach(p -> values.add(p + translationY + previousTranslation));

            values.sort(Collections.reverseOrder());
            if (values.size() != 0) {
                toReturn.add(values.get(0));
            }
            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMaximumTranslationY_inner_forCompositions(shape.compositionShapesYTranslation, translationY));

        });
        toReturn.sort(Comparator.reverseOrder());
        //Collections.sort(toReturn);

        if (toReturn.size() == 0) {
            return 0;
        } else {
            return toReturn.get(0);
        }
    }


    public double getMinimumTranslationX() {
        ArrayList<Double> minimumValues = getMinimumTranslationX_inner();

        if (minimumValues.size() == 0) {
            return 0.0;
        } else {
            Collections.sort(minimumValues);
            return minimumValues.get(0);
        }

    }

    private ArrayList<Double> getMinimumTranslationX_inner() {
        if (parametricCompositionLogic.compositionShapesXTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMinimumTranslationX_inner_forCompositions(parametricCompositionLogic.compositionShapesXTranslation, 0));

        return maximumValues;
    }

    private double getMinimumTranslationX_inner_forCompositions(ArrayList<Information<Double>> compositionShapesX, double previousTranslation) {
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesX.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationX = information.getValue();
            NewCompositionShape shape = parametricCompositionLogic.compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMinimumBasicShapeXTranslation();
            tempValues.forEach(p -> {
                values.add(p + translationX + previousTranslation);
                System.out.println("---> TRANSLATIONX: " + translationX + ", p: " + p);

            });

            Collections.sort(values);
            toReturn.add(values.size() == 0 ? 0 : values.get(0));
            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMinimumTranslationX_inner_forCompositions(shape.compositionShapesXTranslation, translationX));

        });
        Collections.sort(toReturn);

        if (toReturn.size() == 0) {
            return 0;
        } else {
            return toReturn.get(0);
        }
    }

    private double getMaximumTranslationX_inner_forCompositions(ArrayList<Information<Double>> compositionShapesX, double previousTranslation) {
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesX.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationX = information.getValue();
            NewCompositionShape shape = parametricCompositionLogic.compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMaximumBasicShapeXTranslation();
            tempValues.forEach(p -> {
                values.add(p + translationX + previousTranslation);
                System.out.println("---> TRANSLATIONX: " + translationX + ", p: " + p);

            });

            values.sort(Collections.reverseOrder());
            toReturn.add(values.size() == 0 ? 0 : values.get(0));

            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMaximumTranslationX_inner_forCompositions(shape.compositionShapesXTranslation, translationX));

        });
        toReturn.sort(Collections.reverseOrder());

        if (toReturn.size() == 0) {
            return 0;
        } else {
            return toReturn.get(0);
        }
    }

    private ArrayList<Double> getMaximumTranslationX_inner() {
        if (parametricCompositionLogic.compositionShapesXTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMaximumTranslationX_inner_forCompositions(parametricCompositionLogic.compositionShapesXTranslation, 0));

        return maximumValues;
    }

    public double getMaximumTranslationX() {
        ArrayList<Double> maximumValues = getMaximumTranslationX_inner();
        if (maximumValues.size() == 0) {
            return 0.0;
        } else {
            maximumValues.sort(Collections.reverseOrder());
            return maximumValues.get(0);
        }
    }


    public static class Information<T> implements Serializable {
        private String id;
        public T value;

        public Information(String id, T value) {
            this.id = id;
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public Function<T, T> getConsumer() {
            return a -> {
                if (a == null) {
                    return getValue();
                } else {
                    setValue(a);
                    return a;
                }
            };
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
