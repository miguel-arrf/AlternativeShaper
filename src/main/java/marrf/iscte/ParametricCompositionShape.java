package marrf.iscte;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.*;
import static marrf.iscte.BasicShape.colorToRGBString;
import static marrf.iscte.BasicShape.getRelativeLuminance;

@SuppressWarnings("unchecked")
public class ParametricCompositionShape implements CustomShape {

    private final Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();
    private final ArrayList<Coiso<Double>> compositionShapesXTranslation = new ArrayList<>();
    private final ArrayList<Coiso<Double>> compositionShapesYTranslation = new ArrayList<>();

    private final ArrayList<Coiso<String>> compositionShapesXParametricTranslation = new ArrayList<>();
    private final ArrayList<Coiso<String>> compositionShapesYParametricTranslation = new ArrayList<>();

    private final Map<String, ParametricCompositionShape> parametricCompositionShapesMap = new HashMap<>();
    private final ArrayList<Coiso<Double>> parametricShapesXTranslation = new ArrayList<>();
    private final ArrayList<Coiso<Double>> parametricShapesYTranslation = new ArrayList<>();

    private final ArrayList<Coiso<String>> parametricShapesXParametricTranslation = new ArrayList<>();
    private final ArrayList<Coiso<String>> parametricShapesYParametricTranslation = new ArrayList<>();

    private final ArrayList<Coiso<Double>> basicShapesXTranslation = new ArrayList<>();
    private final ArrayList<Coiso<Double>> basicShapesYTranslation = new ArrayList<>();

    private final ArrayList<Coiso<String>> basicShapesXParametricTranslation = new ArrayList<>();
    private final ArrayList<Coiso<String>> basicShapesYParametricTranslation = new ArrayList<>();


    private final ArrayList<Coiso<Double>> powerShapesXTranslation = new ArrayList<>();
    private final ArrayList<Coiso<Double>> powerShapesYTranslation = new ArrayList<>();

    private final ArrayList<Coiso<String>> powerShapesXParametricTranslation = new ArrayList<>();
    private final ArrayList<Coiso<String>> powerShapesYParametricTranslation = new ArrayList<>();


    private final ObservableList<String> variables = FXCollections.observableList(new ArrayList<>());
    public VBox variablesPane = new VBox();

    private static final String SEPARATOR = "<-";
    private final Orchestrator orchestrator;
    //Composition Shape Thumbnail
    private final HBox thumbnail = new HBox();
    private String name = "PARAMETRIC_COMPLEX_DEFAULT";
    private final UUID ID;
    private Node selected;
    private Coiso<Double> selectedTranslationX;
    private Coiso<Double> selectedTranslationY;

    private Coiso<String> selectedParametricXTranslation;
    private Coiso<String> selectedParametricYTranslation;
    private Pane transformersBox;
    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;

    private HBox parametricTranslationXBox;
    private HBox parametricTranslationYBox;
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

    public ParametricCompositionShape(Orchestrator orchestrator, Pane transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;

        ID = UUID.randomUUID();
        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        setUpVariablesPane();

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(15);
        flowPane.setVgap(15);

        variables.addListener((ListChangeListener<? super String>) change -> {
            while(change.next()){
                if(change.wasRemoved()){
                    for(String variableRemoved : change.getRemoved()){
                        flowPane.getChildren().removeIf(p -> p.getId().equals(variableRemoved));
                    }
                }
                if(change.wasAdded()){
                    for(String variableAdded : change.getAddedSubList()){
                       if(flowPane.getChildren().filtered(p -> p.getId().equals(variableAdded)).size() == 0){
                           flowPane.getChildren().add(getPaneForVariable(variableAdded));
                       }else{
                           System.err.println("We are trying to add an already existing variable...!");
                       }
                    }
                }
            }
        });

        variablesPane.getChildren().add(flowPane);


        //setUpComponents();
    }

    private Pane getPaneForVariable(String variable){
        return getPaneForVariable(variable, true);
    }

    private Pane getPaneForVariable(String variable, boolean withDelete){
        return getPaneForVariable(variable, withDelete, 15, 30, 30, 6);
    }

    private Pane getPaneForVariable(String variable, boolean withDelete, int fontSize, int width, int height, int cornerRadius ){
        String backgroundColor = FxUtils.toRGBCode(Color.web(StartMenu.getRGBColor()).darker());

        Label label = getLabel(variable, fontSize);
        label.setWrapText(true);
        label.setTextFill(Color.web(backgroundColor).brighter().brighter());

        VBox vBox = new VBox(label);
        vBox.setPadding(new Insets(3));
        vBox.setAlignment(Pos.CENTER);
        vBox.setId(variable);

        vBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);

        if(withDelete){
            vBox.setOnMouseEntered(mouseEvent -> {
                vBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: " + cornerRadius);
            });

            vBox.setOnMouseExited(mouseEvent -> {
                vBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + cornerRadius);
            });

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setId("betterMenuItem");

            MenuItem menuItem = new MenuItem("Delete Variable");
            menuItem.setStyle("-fx-text-fill: red");
            contextMenu.getItems().add(menuItem);

            menuItem.setOnAction(actionEvent -> {
                variables.remove(variable);
            });

            vBox.setOnContextMenuRequested(contextMenuEvent -> {
                contextMenu.show(vBox, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

        }

        vBox.setMinWidth(width);
        vBox.setMinHeight(height);

        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setMaxHeight(Double.MAX_VALUE);


        return vBox;
    }

    public ParametricCompositionShape(Orchestrator orchestrator, Pane transformersBox, String name, UUID id, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        this.name = name;
        this.ID = id;

        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        //setUpComponents();
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;
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

    public ParametricCompositionShape getCopy(){
        ParametricCompositionShape toReturn = new ParametricCompositionShape(orchestrator, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);

        toReturn.notParametric = notParametric;
        toReturn.name = name;
        toReturn.compositionShapeMap.putAll(compositionShapeMap);
        toReturn.parametricCompositionShapesMap.putAll(parametricCompositionShapesMap);

        compositionShapesXTranslation.forEach(information -> toReturn.compositionShapesXTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        compositionShapesYTranslation.forEach(information -> toReturn.compositionShapesYTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        compositionShapesXParametricTranslation.forEach(information -> toReturn.compositionShapesXParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        compositionShapesYParametricTranslation.forEach(information -> toReturn.compositionShapesYParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));

        parametricShapesXTranslation.forEach(information -> toReturn.parametricShapesXTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        parametricShapesYTranslation.forEach(information -> toReturn.parametricShapesYTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        parametricShapesXParametricTranslation.forEach(information -> toReturn.parametricShapesXParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        parametricShapesYParametricTranslation.forEach(information -> toReturn.parametricShapesYParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));


        basicShapesXTranslation.forEach(information -> toReturn.basicShapesXTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        basicShapesYTranslation.forEach(information -> toReturn.basicShapesYTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        basicShapesXParametricTranslation.forEach(information -> toReturn.basicShapesXParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        basicShapesYParametricTranslation.forEach(information -> toReturn.basicShapesYParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));

        powerShapesXTranslation.forEach(information -> toReturn.powerShapesXTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        powerShapesYTranslation.forEach(information -> toReturn.powerShapesYTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        powerShapesXParametricTranslation.forEach(information -> toReturn.powerShapesXParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));
        powerShapesYParametricTranslation.forEach(information -> toReturn.powerShapesYParametricTranslation.add(new Coiso<>(information.getId(), information.getValue())));


        return toReturn;
    }

    public ArrayList<String> getCompositionShapesUUIDList() {
        ArrayList<String> toReturn = new ArrayList<>();

        compositionShapeMap.forEach((a,b) -> {
            toReturn.add(b.getID().toString());
        });

        return toReturn;
    }

    public ArrayList<String> getBasicShapesUUIDList() {
        ArrayList<String> toReturn = new ArrayList<>();

        basicShapesXTranslation.forEach(information -> {
            toReturn.add(information.getId());
        });

        return toReturn;
    }

    public void deleteBasicShape(String uuidToRemove) {
        if (getBasicShapesUUIDList().stream().anyMatch(p -> p.equals(uuidToRemove))) {
            Coiso<Double> basicShapeXTranslationToRemove = basicShapesXTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();
            Coiso<Double> basicShapeYTranslationToRemove = basicShapesYTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();

            basicShapesXTranslation.remove(basicShapeXTranslationToRemove);
            basicShapesYTranslation.remove(basicShapeYTranslationToRemove);
        }

    }

    public void deleteCompositionShape(String uuidToRemove){
        //TODO If we delete a basic shape from a composition shape that
        // only had it, and that composition shape is here, we should delete
        // the parameters that were defined.
        if (getCompositionShapesUUIDList().stream().anyMatch(p -> p.equals(uuidToRemove))) {

            ArrayList<String> toRemove = new ArrayList<>();

            compositionShapeMap.forEach((id, compositionShape) -> {
                if(compositionShape.getID().toString().equals(uuidToRemove)){
                    toRemove.add(id);
                }
            });

            toRemove.forEach(compositionShapeMap::remove);

            compositionShapesXTranslation.removeIf(p -> {
                if(toRemove.stream().anyMatch(a -> a.equals(p.getId()))){
                    return true;
                }
                return false;
            });

            compositionShapesYTranslation.removeIf(p -> {
                if(toRemove.stream().anyMatch(a -> a.equals(p.getId()))){
                    return true;
                }
                return false;
            });

            //Information compositionShapesXTranslationToRemove = compositionShapesXTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();
            //Information compositionShapesYTranslationToRemove = compositionShapesYTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();

            //compositionShapesXTranslation.remove(compositionShapesXTranslationToRemove);
            //compositionShapesYTranslation.remove(compositionShapesYTranslationToRemove);

        }
    }

    public void setTransformersBox(Pane transformersBox) {
        this.transformersBox = transformersBox;
    }

    public Map<String, NewCompositionShape> getCompositionShapeMap() {
        return compositionShapeMap;
    }

    public Map<String, ParametricCompositionShape> getParametricCompositionShapesMap() {
        return parametricCompositionShapesMap;
    }

    public String getPrologRepresentation(boolean withFinalDot, boolean justInner){
        StringBuilder toReturn = new StringBuilder();

        if(!justInner){
            toReturn.append("shapeComposition(").append(getShapeName()).append("[");
        }

        basicShapesXTranslation.forEach(information -> {
            Coiso<Double> translationY = basicShapesYTranslation.stream().filter(inf -> inf.getId().equals(information.getId())).findFirst().get();

            toReturn.append("s(").append(orchestrator.getBasicShapeNameFromID(information.getId()));
            toReturn.append(",[1,0,0,0,1,0,").append(information.getValue()/SCALE).append(",").append(translationY.getValue()/SCALE).append(",1]");

                toReturn.append("),");

        });

        powerShapesXTranslation.forEach(information -> {
            Coiso<Double> translationY = powerShapesYTranslation.stream().filter(inf -> inf.getId().equals(information.getId())).findFirst().get();

            toReturn.append("s(").append(orchestrator.getPowerShapeNameFromID(information.getId()));
            toReturn.append(",[1,0,0,0,1,0,").append(information.getValue()/SCALE).append(",").append(translationY.getValue()/SCALE).append(",1]");

                toReturn.append("),");

        });

        AtomicInteger added = new AtomicInteger();

        parametricCompositionShapesMap.forEach((randomID, newCompositionShape) -> {

            var translationX = parametricShapesXTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();
            var translationY = parametricShapesYTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();

            toReturn.append("s(").append(newCompositionShape.getShapeName());
            toReturn.append(",[1,0,0,0,1,0,").append(translationX.getValue()).append(",").append(translationY.getValue()).append(",1]");


                toReturn.append("),");

            added.getAndIncrement();


        });

        compositionShapeMap.forEach((randomID, newCompositionShape) -> {

            var translationX = compositionShapesXTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();
            var translationY = compositionShapesYTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();

            toReturn.append("s(").append(newCompositionShape.getShapeName());
            toReturn.append(",[1,0,0,0,1,0,").append(translationX.getValue()).append(",").append(translationY.getValue()).append(",1]");


                toReturn.append("),");

            added.getAndIncrement();


        });

        if(!justInner){
            toReturn.append(withFinalDot ? "])." : "])");
        }


        String string = toReturn.toString();
        string = string.replaceAll("([,][,]+)", ",");
        string = string.replaceAll("([,][)])", ")");
        string = string.replaceAll("([,][]])", "]");


        return string;
    }

    public JSONObject getJSONObject(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", getID().toString());
        jsonObject.put("name", getShapeName());
        jsonObject.put("basicShapes", getBasicShapesJSON());
        jsonObject.put("powerShapes", getPowerShapesJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());
        jsonObject.put("parametricShapes", getParametricShapesJSON());

        return jsonObject;
    }

    public UUID getID() {
        return ID;
    }

    public JSONArray getCompositionShapesJSON() {
        JSONArray array = new JSONArray();

        compositionShapeMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Coiso information : compositionShapesXTranslation) {
                if (information.getId().equals(id))
                    position = compositionShapesXTranslation.indexOf(information);
            }

            jsonObject.put("id", compositionShape.getID().toString());
            jsonObject.put("translationX", compositionShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", compositionShapesYTranslation.get(position).getValue());

            jsonObject.put("parametricTranslationX", compositionShapesXParametricTranslation.get(position).getValue());
            jsonObject.put("parametricTranslationY", compositionShapesYParametricTranslation.get(position).getValue());


            array.add(jsonObject);
        });

        return array;
    }

    public JSONArray getPowerShapesJSON(){
        JSONArray array = new JSONArray();

        powerShapesXTranslation.forEach(information -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", information.getId());

            int position = powerShapesXTranslation.indexOf(information);

            jsonObject.put("translationX", powerShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", powerShapesYTranslation.get(position).getValue());

            jsonObject.put("parametricTranslationX", powerShapesXParametricTranslation.get(position).getValue());
            jsonObject.put("parametricTranslationY", powerShapesYParametricTranslation.get(position).getValue());

            array.add(jsonObject);
        });

        return array;

    }

    public JSONArray getParametricShapesJSON(){
        JSONArray array = new JSONArray();

        parametricCompositionShapesMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Coiso information : parametricShapesXTranslation) {
                if (information.getId().equals(id))
                    position = parametricShapesXTranslation.indexOf(information);
            }

            jsonObject.put("id", compositionShape.getID().toString());
            jsonObject.put("translationX", parametricShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", parametricShapesYTranslation.get(position).getValue());

            jsonObject.put("parametricTranslationX", parametricShapesXParametricTranslation.get(position).getValue());
            jsonObject.put("parametricTranslationY", parametricShapesYParametricTranslation.get(position).getValue());


            array.add(jsonObject);
        });

        return array;
    }

    public JSONArray getBasicShapesJSON() {
        JSONArray array = new JSONArray();

        basicShapesXTranslation.forEach(information -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", information.getId());

            int position = basicShapesXTranslation.indexOf(information);

            jsonObject.put("translationX", basicShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", basicShapesYTranslation.get(position).getValue());

            jsonObject.put("parametricTranslationX", basicShapesXParametricTranslation.get(position).getValue());
            jsonObject.put("parametricTranslationY", basicShapesYParametricTranslation.get(position).getValue());


            array.add(jsonObject);
        });

        return array;
    }

    public BasicShape addBasicShape(String basicShapesID) {
        Coiso<Double> translationX = new Coiso<Double>(basicShapesID, 0.0);
        Coiso<Double> translationY = new Coiso<Double>(basicShapesID, 0.0);

        Coiso<String> parametricXTranslation = new Coiso<String>(basicShapesID, "");
        Coiso<String> parametricYTranslation = new Coiso<String>(basicShapesID, "");

        basicShapesXParametricTranslation.add(parametricXTranslation);
        basicShapesYParametricTranslation.add(parametricYTranslation);

        basicShapesXTranslation.add(translationX);
        basicShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfParametricBasicShape(basicShapesID,parametricXTranslation.getConsumer(), parametricYTranslation.getConsumer(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeleting(basicShapesID));
    }

    public BasicShape addBasicShapeWithTranslation(String basicShapesID, double xTranslation, double yTranslation, String parametricX, String parametricY) {
        Coiso<Double> translationX = new Coiso<Double>(basicShapesID, xTranslation);
        Coiso<Double> translationY = new Coiso<Double>(basicShapesID, yTranslation);

        Coiso<String> parametricXTranslation = new Coiso<String>(basicShapesID, parametricX);
        Coiso<String> parametricYTranslation = new Coiso<String>(basicShapesID, parametricY);

        basicShapesXParametricTranslation.add(parametricXTranslation);
        basicShapesYParametricTranslation.add(parametricYTranslation);

        basicShapesXTranslation.add(translationX);
        basicShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfParametricBasicShape(basicShapesID,parametricXTranslation.getConsumer(), parametricYTranslation.getConsumer(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeleting(basicShapesID));
    }

    public Power addPowerShape(String powerShapeID){
        System.out.println("ó maluco, here i am");
        Coiso<Double> translationX = new Coiso<>(powerShapeID, 0.0);
        Coiso<Double> translationY = new Coiso<>(powerShapeID, 0.0);

        Coiso<String> parametricXTranslation = new Coiso<>(powerShapeID, "");
        Coiso<String> parametricYTranslation = new Coiso<>(powerShapeID, "");

        powerShapesXParametricTranslation.add(parametricXTranslation);
        powerShapesYParametricTranslation.add(parametricYTranslation);

        powerShapesXTranslation.add(translationX);
        powerShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfParametricPowerShape(powerShapeID,parametricXTranslation.getConsumer(), parametricYTranslation.getConsumer(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(powerShapeID));
    }

    public Power addPowerShapeWithTranslation(String powerShapeID,double xTranslation, double yTranslation, String parametricX, String parametricY){
        Coiso<Double> translationX = new Coiso<>(powerShapeID, xTranslation);
        Coiso<Double> translationY = new Coiso<>(powerShapeID, yTranslation);

        Coiso<String> parametricXTranslation = new Coiso<>(powerShapeID, parametricX);
        Coiso<String> parametricYTranslation = new Coiso<>(powerShapeID, parametricY);

        powerShapesXParametricTranslation.add(parametricXTranslation);
        powerShapesYParametricTranslation.add(parametricYTranslation);

        powerShapesXTranslation.add(translationX);
        powerShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfParametricPowerShape(powerShapeID,parametricXTranslation.getConsumer(), parametricYTranslation.getConsumer(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(powerShapeID));

    }

    public ArrayList<Power> getPowerShapes() {
        ArrayList<Power> basicShapes = new ArrayList<>();

        powerShapesXTranslation.forEach(information -> {
            int position = powerShapesXTranslation.indexOf(information);

            Coiso<Double> translationX = powerShapesXTranslation.get(position);
            Coiso<Double> translationY = powerShapesYTranslation.get(position);

            Coiso<String> parametricXTranslation = powerShapesXParametricTranslation.get(position);
            Coiso<String> parametricYTranslation = powerShapesYParametricTranslation.get(position);

            basicShapes.add(orchestrator.getCopyOfParametricPowerShape(information.getId(),parametricXTranslation.getConsumer(), parametricYTranslation.getConsumer(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeletingPowerShape_Novo(information.getId())));
        });


        return basicShapes;
    }

    public ArrayList<BasicShape> getBasicShapes() {
        ArrayList<BasicShape> basicShapes = new ArrayList<>();

        basicShapesXTranslation.forEach(information -> {
            int position = basicShapesXTranslation.indexOf(information);

            Coiso<Double> translationX = basicShapesXTranslation.get(position);
            Coiso<Double> translationY = basicShapesYTranslation.get(position);

            Coiso<String> parametricXTranslation = basicShapesXParametricTranslation.get(position);
            Coiso<String> parametricYTranslation = basicShapesYParametricTranslation.get(position);
            //System.out.println("no get: " + "translationX: " + translationX.getValue() + ", translationY: " + translationY.getValue());

            basicShapes.add(orchestrator.getCopyOfParametricBasicShape(information.id,parametricXTranslation.getConsumer(), parametricYTranslation.getConsumer(),  translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeleting(translationY.getId())));
        });


        return basicShapes;
    }

    private Function<Pane, Double> getProceedWhenDeleting(String basicShapeID) {
        return a -> {
            Coiso<Double> xTranslationToRemove = basicShapesXTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            Coiso<Double> yTranslationToRemove = basicShapesYTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            basicShapesXTranslation.remove(xTranslationToRemove);
            basicShapesYTranslation.remove(yTranslationToRemove);
            System.err.println("VIM AQUI PARA APAGAR!");
            proceedToRedrawWhenDeleting.apply(null);
            return null;
        };
    }

    private Function<String, Double> getProceedWhenDeletingPowerShape(String basicShapeID) {
        return a -> {
            Coiso<Double> xTranslationToRemove = powerShapesXTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            Coiso<Double> yTranslationToRemove = powerShapesYTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            powerShapesXTranslation.remove(xTranslationToRemove);
            powerShapesYTranslation.remove(yTranslationToRemove);
            System.err.println("VIM AQUI PARA APAGAR!");
            proceedToRedrawWhenDeleting.apply(null);
            return null;
        };
    }


    private Function<Node, Double> getProceedWhenDeletingPowerShape_Novo(String basicShapeID) {
        return a -> {
            Coiso<Double> xTranslationToRemove = powerShapesXTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            Coiso<Double> yTranslationToRemove = powerShapesYTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            powerShapesXTranslation.remove(xTranslationToRemove);
            powerShapesYTranslation.remove(yTranslationToRemove);
            System.err.println("VIM AQUI PARA APAGAR!");
            proceedToRedrawWhenDeleting.apply(null);
            return null;
        };
    }

    public void getTeste(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY) {

        parametricCompositionShapesMap.forEach((id,shape) -> {
            getTesteForSpecificParametric(toAdd, true, 0,0, id, shape);
        });


        compositionShapeMap.forEach((newID, compositionShape) -> {
            Group addTo = new Group();

            int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Coiso<Double> translationX = compositionShapesXTranslation.get(position);
            Coiso<Double> translationY = compositionShapesYTranslation.get(position);

            Coiso<String> parametricTranslationX = compositionShapesXParametricTranslation.get(position);
            Coiso<String> parametricTranslationY = compositionShapesYParametricTranslation.get(position);

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
                    compositionShapeMap.remove(newID);
                    Coiso<Double> xTranslationToRemove = compositionShapesXTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                    Coiso<Double> yTranslationToRemove = compositionShapesYTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                    compositionShapesXTranslation.remove(xTranslationToRemove);
                    compositionShapesYTranslation.remove(yTranslationToRemove);

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

        int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

        Coiso<Double> translationX = compositionShapesXTranslation.get(position);
        Coiso<Double> translationY = compositionShapesYTranslation.get(position);

        Coiso<String> parametricTranslationX = compositionShapesXParametricTranslation.get(position);
        Coiso<String> parametricTranslationY = compositionShapesYParametricTranslation.get(position);

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
                compositionShapeMap.remove(newID);
                Coiso<Double> xTranslationToRemove = compositionShapesXTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                Coiso<Double> yTranslationToRemove = compositionShapesYTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                compositionShapesXTranslation.remove(xTranslationToRemove);
                compositionShapesYTranslation.remove(yTranslationToRemove);

                //proceedWhenDeleting.apply(null);
            });

            addTo.setOnContextMenuRequested(contextMenuEvent -> {
                contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

        }

    }

    public void getTesteForSpecificParametric(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY, String newID, ParametricCompositionShape parametricCompositionShape) {

        toAdd.setPickOnBounds(false);
        Group addTo = new Group();

        int position = parametricShapesXTranslation.indexOf(parametricShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

        Coiso<Double> translationX = parametricShapesXTranslation.get(position);
        Coiso<Double> translationY = parametricShapesYTranslation.get(position);

        Coiso<String> parametricTranslationX = parametricShapesXParametricTranslation.get(position);
        Coiso<String> parametricTranslationY = parametricShapesYParametricTranslation.get(position);


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

        //Adding basic shapes
        parametricCompositionShape.getBasicShapes().forEach(basicShape -> {
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


        parametricCompositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY);

        if (addHover) {
            Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth() + 20, addTo.getLayoutBounds().getHeight() + 20);
            rectangle.setArcWidth(10);
            rectangle.setArcHeight(10);
            rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
            rectangle.setX(addTo.getLayoutBounds().getMinX() - 10);
            rectangle.setY(addTo.getLayoutBounds().getMinY() - 10);


            Label shapeName = new Label(parametricCompositionShape.getShapeName());
            shapeName.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
            shapeName.setTextFill(Color.web("#BDBDBD"));
            shapeName.setPadding(new Insets(4));
            shapeName.setStyle("-fx-background-color:  rgba(255,255,255,0.3); -fx-background-radius: 15px;");

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
                parametricCompositionShapesMap.remove(newID);
                Coiso<Double> xTranslationToRemove = parametricShapesXTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                Coiso<Double> yTranslationToRemove = parametricShapesYTranslation.stream().filter(p -> p.id.equals(newID)).findFirst().get();
                parametricShapesXTranslation.remove(xTranslationToRemove);
                parametricShapesYTranslation.remove(yTranslationToRemove);

            });

            addTo.setOnContextMenuRequested(contextMenuEvent -> {
                contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

        }

    }

    public Pane addNewCompositionShape(NewCompositionShape newCompositionShape) {
        String id = UUID.randomUUID().toString();

        compositionShapeMap.put(id, newCompositionShape);

        compositionShapesXTranslation.add(new Coiso<>(id, 0.0));
        compositionShapesYTranslation.add(new Coiso<>(id, 0.0));

        compositionShapesXParametricTranslation.add(new Coiso<>(id, ""));
        compositionShapesYParametricTranslation.add(new Coiso<>(id, ""));

        Pane toAdd = new Pane();
        getTesteForSpecific(toAdd, true, 0, 0, id, newCompositionShape);

        return toAdd;
    }

    public Pane addNewCompositionShapeWithTranslation(NewCompositionShape newCompositionShape,double xTranslation, double yTranslation, String id, String parametricX, String parametricY) {
        compositionShapeMap.put(id, newCompositionShape);

        compositionShapesXTranslation.add(new Coiso<>(id, xTranslation));
        compositionShapesYTranslation.add(new Coiso<>(id, yTranslation));

        compositionShapesXParametricTranslation.add(new Coiso<>(id, parametricX));
        compositionShapesYParametricTranslation.add(new Coiso<>(id, parametricY));

        Pane toAdd = new Pane();
        getTesteForSpecific(toAdd, true, xTranslation, yTranslation, id, newCompositionShape);

        return toAdd;
    }

    public Pane addParametricCompositionShape(ParametricCompositionShape parametricCompositionShape){
        String id = UUID.randomUUID().toString();

        /*ArrayList<String> parametricShapeVariables = parametricCompositionShape.getOutputVariables();
        FlowPane flowPane = new FlowPane(new Label("Shape: " + parametricCompositionShape.getShapeName()));
        for(String variable: parametricShapeVariables){
            flowPane.getChildren().add(getPaneForVariable(variable, false, 10, 15, 15, 6));
        }
        variablesPane.getChildren().add(flowPane);*/

        parametricCompositionShapesMap.put(id, parametricCompositionShape);

        parametricShapesXTranslation.add(new Coiso<>(id, 0.0));
        parametricShapesYTranslation.add(new Coiso<>(id, 0.0));

        parametricShapesXParametricTranslation.add(new Coiso<>(id, ""));
        parametricShapesYParametricTranslation.add(new Coiso<>(id, ""));

        Pane toAdd = new Pane();
        getTesteForSpecificParametric(toAdd, true, 0, 0, id, parametricCompositionShape);

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
        if(!notParametric){
            toReturn.add(parametricTranslationXBox);
            toReturn.add(parametricTranslationYBox);
        }
        toReturn.add(translationXBox);
        toReturn.add(translationYBox);

        return toReturn;
    }

    private void setUpParametricTranslationXBox(){
        Label translationLabel = new Label("Parametric translation X:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(selectedParametricXTranslation.getValue());
        textField.setPromptText(selectedParametricXTranslation.getValue());
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER);

        textField.setOnKeyPressed(keyEvent -> {
            selectedParametricXTranslation.setValue(textField.getText());
        });

        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);


        parametricTranslationXBox = new HBox(translationLabel, textField);
        parametricTranslationXBox.setPadding(new Insets(10, 10, 10, 15));
        parametricTranslationXBox.setAlignment(Pos.CENTER_LEFT);
        parametricTranslationXBox.setSpacing(20);
        parametricTranslationXBox.setMinHeight(30);
        parametricTranslationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private void setUpParametricTranslationYBox(){
        Label translationLabel = new Label("Parametric translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(selectedParametricYTranslation.getValue());
        textField.setPromptText(selectedParametricYTranslation.getValue());
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER);

        textField.setOnKeyPressed(keyEvent -> {
                selectedParametricYTranslation.setValue(textField.getText());
        });

        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);


        parametricTranslationYBox = new HBox(translationLabel, textField);
        parametricTranslationYBox.setPadding(new Insets(10, 10, 10, 15));
        parametricTranslationYBox.setAlignment(Pos.CENTER_LEFT);
        parametricTranslationYBox.setSpacing(20);
        parametricTranslationYBox.setMinHeight(30);
        parametricTranslationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private Pane getNumericLabel(){
        HBox numericLabel = getPopupButton("Numeric Variable", "#703636", "#F96767");

        numericLabel.setPrefWidth(140);
        numericLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(numericLabel, Priority.ALWAYS);

        numericLabel.setOnMouseClicked(variableLabelMouseEvent -> {
            PopupWindow popupWindow = new PopupWindow();
            Stage tempStage = popupWindow.getStage();

            Pane closeButton = PopupWindow.getButton("Close", "null", "#5E2323", "#EB5757", event -> {
                tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            });
            popupWindow.createPopup("Create new Variable", scene, getCreateNumericVariablePane(tempStage), closeButton );
        });

        return numericLabel;
    }

    private boolean variableAlreadyExists(String variableName){
        String correctVariableName = variableName;
        if(variableName.contains(SEPARATOR)){
            String[] variableNameParts = variableName.split(SEPARATOR);
            correctVariableName = variableNameParts[0];
        }

        for(String variable : variables){
            if(variable.contains(SEPARATOR)){
                String[] parts = variable.split(SEPARATOR);
                if(parts[0].equals(correctVariableName))
                    return true;
            }
        }
        return false;
    }



    private Pane getCreateVariablePane(Stage stage){
        Label variableName = getLabel("Variable Name:");

        TextField textField = new TextField();
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER_LEFT);

        Pane saveButton = PopupWindow.getButton("Save", "null", "#35654F", "#56F28F", event -> {
            if(textField.getText().isEmpty() || textField.getText().isBlank()){
                textField.setPromptText("Variable name empty");
                System.err.println("Variable name is empty...!");
            }else{
                if(!variables.contains(textField.getText()) && !variableAlreadyExists(textField.getText())){
                    variables.add(textField.getText());
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                }else{
                    textField.setPromptText("Variable name empty");
                    System.err.println("We are trying to add an already existing variable... on the textfield!");
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

    private Pane getCreateNumericVariablePane(Stage stage){
        Label variableName = getLabel("Variable Name:");

        TextField textField = new TextField();
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER_LEFT);

        Label variableValue = getLabel("Variable Value:");

        TextField textFieldValue = new TextField();
        textFieldValue.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textFieldValue.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textFieldValue.setAlignment(Pos.CENTER_LEFT);

        Pane saveButton = PopupWindow.getButton("Save", "null", "#35654F", "#56F28F", event -> {
            if(textField.getText().isEmpty() || textField.getText().isBlank() || textFieldValue.getText().isBlank() || textFieldValue.getText().isEmpty()){
                System.err.println("Variable name or variable value is empty...!");
            }else{
                try{
                    double value = Double.parseDouble(textFieldValue.getText());
                    if(!variables.contains(textField.getText())  && !textField.getText().contains(SEPARATOR) && !variableAlreadyExists(textField.getText())){
                        variables.add(textField.getText() + SEPARATOR + value );
                        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    }else{
                        textField.setPromptText("Variable name empty");
                        System.err.println("We are trying to add an already existing variable... on the textfield!");
                    }
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    System.err.println("Variable value is incorrect!");

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

    private Pane getVariableLabel(){
        HBox variableLabel = getPopupButton("Variable", "#5F3670", "#F967A8");

        variableLabel.setPrefWidth(140);
        variableLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(variableLabel, Priority.ALWAYS);

        variableLabel.setOnMouseClicked(variableLabelMouseEvent -> {
            PopupWindow popupWindow = new PopupWindow();
            Stage tempStage = popupWindow.getStage();

            Pane closeButton = PopupWindow.getButton("Close", "null", "#5E2323", "#EB5757", event -> {
                tempStage.fireEvent(new WindowEvent(tempStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            });
            popupWindow.createPopup("Create new Variable", scene, getCreateVariablePane(tempStage), closeButton );
        });

        return variableLabel;
    }

    public ArrayList<String> getOutputVariables(){
        ArrayList<String> toReturn = new ArrayList<>();
        for(String variable: variables){
            if(!variable.contains(SEPARATOR)){
                toReturn.add(variable);
            }
        }
        return toReturn;
    }

    private HBox getPopupButton(String label, String backgroundColor, String labelColor){
        Label complexShape = new Label(label);
        complexShape.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        complexShape.setTextFill(Color.web(labelColor));

        HBox complexShapeHBox = new HBox(complexShape);

        complexShapeHBox.setAlignment(Pos.CENTER);
        complexShapeHBox.setSpacing(5);
        complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + 10);
        HBox.setHgrow(complexShapeHBox, Priority.ALWAYS);

        complexShapeHBox.setOnMouseEntered(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + FxUtils.toRGBCode(Color.web(backgroundColor).darker()) + ";-fx-background-radius: " + 10);
        });

        complexShapeHBox.setOnMouseExited(mouseEvent -> {
            complexShapeHBox.setStyle("-fx-background-color: " + backgroundColor + ";-fx-background-radius: " + 10);
        });

        complexShape.setMinHeight(30);
        complexShapeHBox.setPadding(new Insets(0, 5, 0, 5));


        return complexShapeHBox;
    }

    public void setUpVariablesPane(){
        HBox variablesLabel = getButtonWith_Label_Color("Variables", "#717640", "#BBBBBB", 6);
        Pane addButton = getButtonWith_Label_Color_Image("", "#717640", "#BBBBBB", "icons8-plus-math-96.png", 6);

        variablesLabel.setMaxHeight(30);
        variablesLabel.setMinHeight(30);
        variablesLabel.setPrefHeight(30);

        addButton.setMaxHeight(30);
        addButton.setPrefHeight(30);
        addButton.setMinHeight(30);
        addButton.setPrefSize(30,30);
        addButton.setMaxWidth(30);

        HBox topButtonsAndLabel = new HBox(variablesLabel, addButton);
        topButtonsAndLabel.setSpacing(10);

        CustomMenuItem numericVariable = new CustomMenuItem(getNumericLabel());
        CustomMenuItem variable = new CustomMenuItem(getVariableLabel());

        ContextMenu contextMenuParametric = new ContextMenu();
        contextMenuParametric.setId("testeCoiso");
        contextMenuParametric.getItems().addAll(numericVariable, variable);

        addButton.setOnMouseClicked(mouseEvent -> contextMenuParametric.show(addButton, mouseEvent.getScreenX(), mouseEvent.getScreenY()));

        variablesPane.setPadding(new Insets(10));
        variablesPane.setPrefHeight(50);
        variablesPane.setSpacing(15);
        variablesPane.getChildren().add(topButtonsAndLabel);
    }



    private Label getLabel(String text){
        Label translationLabel = new Label(text);
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);
        return translationLabel;
    }

    private Label getLabel(String text, int size){
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

        Coiso<Double> tempTranslationX = selectedTranslationX;

        Slider translationXSlider = new Slider();
        translationXSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setMin(- NUMBER_COLUMNS_AND_ROWS);
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
            selected.setTranslateX(selected.getTranslateX() + (newValue.doubleValue() -oldValue.doubleValue()) * SCALE) ;
        });

        translationXSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationXSlider, Priority.ALWAYS);



        translationXBox = new HBox(translationLabel, translationXSlider, textField);
        translationXBox.setPadding(new Insets(10, 10, 10, 15));
        translationXBox.setAlignment(Pos.CENTER_LEFT);
        translationXBox.setSpacing(20);
        translationXBox.setMinHeight(30);
        translationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private void setUpTranslationYBox() {
        Label translationLabel = new Label(notParametric? "Translation Y:" : "Temporary translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(String.valueOf(-selectedTranslationY.getValue() / SCALE));
        textField.setPromptText(String.valueOf(-selectedTranslationY.getValue() / SCALE));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Coiso<Double> tempTranslationY = selectedTranslationY;

        Slider translationYSlider = new Slider();
        translationYSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setMin(- NUMBER_COLUMNS_AND_ROWS);
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
            selected.setTranslateY(selected.getTranslateY() + (newValue.doubleValue() - oldValue.doubleValue()) * -SCALE); ;
        });

        translationYSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationYSlider, Priority.ALWAYS);


        translationYBox = new HBox(translationLabel, translationYSlider, textField);
        translationYBox.setPadding(new Insets(10, 10, 10, 15));
        translationYBox.setAlignment(Pos.CENTER_LEFT);
        translationYBox.setMinHeight(40);
        translationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
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

        Label tagLabel = new Label("Composition Shape");
        tagLabel.setWrapText(true);
        tagLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        tagLabel.setPadding(new Insets(3));
        tagLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        tagLabel.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");

        /*
        Label numberOfBasicShapes = new Label(basicShapesXTranslation.size() + "x");
        numberOfBasicShapes.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        numberOfBasicShapes.setPadding(new Insets(3));
        numberOfBasicShapes.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        numberOfBasicShapes.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");

        Label numberOfCompositionShapes = new Label(compositionShapeMap.size() + "x");
        numberOfCompositionShapes.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        numberOfCompositionShapes.setPadding(new Insets(3));
        numberOfCompositionShapes.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        numberOfCompositionShapes.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");
         */
        FlowPane detailsHB = new FlowPane(tagLabel);
        detailsHB.setVgap(10);
        detailsHB.setHgap(10);

        nameAndTagVBox.getChildren().addAll(nameLabel, detailsHB);

        FlowPane variablesFlowPane = new FlowPane();
        variablesFlowPane.setVgap(10);
        variablesFlowPane.setHgap(10);

        if(variables.size() != 0){
            //There are variables!
            nameAndTagVBox.getChildren().add(variablesFlowPane);
            for(String variable: getOutputVariables()){
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

        menuItem.setOnAction(actionEvent -> {
            proceedWhenDeletingFromThumbnail.apply(getUUID().toString());
        });

        thumbnail.setOnContextMenuRequested(contextMenuEvent -> {
            contextMenu.show(thumbnail, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }


    public static class Coiso<T> {
        private String id;
        private T value;

        public Coiso(String id, T value) {
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
