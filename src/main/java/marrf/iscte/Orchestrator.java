package marrf.iscte;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import marrf.iscte.ShapeRules.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static marrf.iscte.ParametricCompositionShape.Information;


public class Orchestrator implements Serializable {

    private static final int SCALE = 40;
    private static final int NUMBER_COLUMNS_AND_ROWS = 40;

    //private String path = "C:\\Users\\mferr\\Downloads\\objetos\\test.json";
    public static String path = "/Users/miguelferreira/Downloads/alternativeShaperSaves/test.json";

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();
    private final ArrayList<Process> processes = new ArrayList<>();
    private final ArrayList<ShapeRule> shapeRules = new ArrayList<>();
    private final ArrayList<Variable> variables = new ArrayList<>();
    private final ArrayList<Power> powerShapes = new ArrayList<>();
    private final ArrayList<ParametricCompositionShape> parametricCompositionShapes = new ArrayList<>();

    public static String SHAPES_PATH = "/shapes/";
    public static String IMAGES_PATH = "/images/";

    public ArrayList<BasicShape> getBasicShapes() {
        return basicShapes;
    }

    public ArrayList<NewCompositionShape> getNewCompositionShapes() {
        return newCompositionShapes;
    }

    public ArrayList<Power> getPowerShapes() {
        return powerShapes;
    }

    public ArrayList<ParametricCompositionShape> getParametricCompositionShapes() {
        return parametricCompositionShapes;
    }

    public static int getSCALE() {
        return SCALE;
    }

    public String getBasicShapeNameFromID(String id){
        return  basicShapes.stream().filter(p -> p.getUUID().toString().equals(id)).findFirst().get().getShapeName();
    }

    public String getPowerShapeNameFromID(String id){
        return  powerShapes.stream().filter(p -> p.getUUID().toString().equals(id)).findFirst().get().getShapeName();
    }

    public Optional<BasicShape> existsBasicShapeWithID(String id) {
        return  basicShapes.stream().filter(p -> p.getUUID().toString().equals(id)).findFirst();
    }

    public BasicShape getBasicShapeFromPosition(int position){
        return basicShapes.get(position);
    }

    public boolean canAddVariable(Variable variable){
        return variables.stream().noneMatch(p -> p.getName().equals(variable.getName()));
    }

    public ArrayList<Variable> getVariables() {
        return variables;
    }

    public void removeVariable(Variable variable){
        variables.remove(variable);
    }

    public void addVariable(Variable variable){
        variables.add(variable);
    }

    public static int getNumberColumnsAndRows() {
        return NUMBER_COLUMNS_AND_ROWS;
    }

    public ArrayList<Process> getProcesses() {
        return processes;
    }

    public ArrayList<ShapeRule> getShapeRules() {
        return shapeRules;
    }

    public void addAllBasicShapes(ArrayList<BasicShape> basicShapesToAdd){
        basicShapes.clear();
        basicShapes.addAll(basicShapesToAdd);

        saveFile();
    }

    public BasicShape getCopyOfBasicShape(String id, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<Pane, Double> proceedWhenDeleting){
        BasicShape toReturn = null;

        Optional<BasicShape> shape = basicShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();

        if(shape.isPresent()){
            BasicShape toCopyFrom = shape.get();

            toReturn = new BasicShape(toCopyFrom.getWidth(), toCopyFrom.getHeight(), toCopyFrom.getFill(), toCopyFrom.getSelectedImage(), writeTranslateX, writeTranslateY, proceedWhenDeleting, toCopyFrom.getShapeName());
        }


        return toReturn;
    }

    public BasicShape getCopyOfParametricBasicShape(String id,Function<String, String> parametricXTranslation, Function<String, String> parametricYTranslation, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<Pane, Double> proceedWhenDeleting){
        BasicShape toReturn = null;

        Optional<BasicShape> shape = basicShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();

        if(shape.isPresent()){
            BasicShape toCopyFrom = shape.get();

            toReturn = new BasicShape(true,parametricXTranslation, parametricYTranslation, toCopyFrom.getWidth(), toCopyFrom.getHeight(), toCopyFrom.getFill(), toCopyFrom.getSelectedImage(), writeTranslateX, writeTranslateY, proceedWhenDeleting, toCopyFrom.getShapeName());
        }

        return toReturn;
    }

    public Power getPowerShapeWithGivenID(String id){
        Power toReturn = null;
        Optional<Power> shape = powerShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();
        System.out.println("EU ESTOU À PROCURA DA POWER SHAPE COM O ID: " + id);
        return shape.orElse(toReturn);
    }

    public Power getCopyOfParametricPowerShape(String id, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<Node, Double> proceedWhenDeleting){
        Power toReturn = null;

        Optional<Power> shape = powerShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();

        if(shape.isPresent()){
            Power toCopyFrom = shape.get();
            System.out.println("A VARIABEL A SER COPIADA É: " + toCopyFrom.getCenterVariable());
            toReturn = new Power(toCopyFrom.isFigureVariable(),toCopyFrom.getShapeName(), toCopyFrom.getUUID(), toCopyFrom.getHasLeft(), toCopyFrom.getHasRight(), toCopyFrom.getLeftHasVariable(), toCopyFrom.getRightHasVariable(), toCopyFrom.getCenterHasVariable(), toCopyFrom.getRightVariable(), toCopyFrom.getLeftVariable(), toCopyFrom.getCenterVariable(), toCopyFrom.getLeftValue(), toCopyFrom.getRightValue(), toCopyFrom.getRightTranslation(), toCopyFrom.getLeftTranslation(), writeTranslateX, writeTranslateY, proceedWhenDeleting, this);
        }

        return toReturn;
    }

    public Power getCopyOfPowerShape(String id, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<String, Double> proceedWhenDeleting){
        Power toReturn = null;

        Optional<Power> shape = powerShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();

        if(shape.isPresent()){
            Power toCopyFrom = shape.get();

            toReturn = new Power(toCopyFrom.isFigureVariable(), toCopyFrom.getShapeName(), toCopyFrom.getUUID(), toCopyFrom.getHasLeft(), toCopyFrom.getHasRight(), toCopyFrom.getLeftHasVariable(), toCopyFrom.getRightHasVariable(), toCopyFrom.getCenterHasVariable(), toCopyFrom.getRightVariable(), toCopyFrom.getLeftVariable(), toCopyFrom.getCenterVariable(), toCopyFrom.getLeftValue(), toCopyFrom.getRightValue(), toCopyFrom.getRightTranslation(), toCopyFrom.getLeftTranslation(), writeTranslateX, writeTranslateY, this, proceedWhenDeleting);
        }


        return toReturn;
    }

    public boolean canBasicShapeBeRemoved(String uuid){
        System.out.println("UUID HERE: " + uuid + ", newCompositionShapes: " + newCompositionShapes.size());
        for(NewCompositionShape newCompositionShape: newCompositionShapes){
            if(newCompositionShape.getBasicShapesUUIDList().stream().anyMatch(p -> p.equals(uuid))){
                return false;
            }
        }

        for(Power power: powerShapes){
            if(power.getCustomShape().getUUID().toString().equals(uuid))
                return false;
        }

        return true;
    }

    public boolean canCompositionShapeBeRemoved(String uuid){

        // Create an ArrayList of the List
        ArrayList<NewCompositionShape> arrayList = newCompositionShapes.stream().filter(p -> !p.getID().toString().equals(uuid)).collect(Collectors.toCollection(ArrayList::new));

        for(NewCompositionShape newCompositionShape : arrayList){
            if(newCompositionShape.getCompositionShapesUUIDList().stream().anyMatch(p -> p.equals(uuid))){
                return false;
            }
        }

        for(ShapeRule shapeRule : shapeRules) {
            if (shapeRule.getLeftShape().getCompositionShapesUUIDList().stream().anyMatch(p -> p.equals(uuid)))
                return false;
            if (shapeRule.getRightShape().getCompositionShapesUUIDList().stream().anyMatch(p -> p.equals(uuid)))
                return false;
        }

        for(Power power: powerShapes){
            if(power.getUUID().toString().equals(uuid))
                return false;
        }

        for(ParametricCompositionShape parametricCompositionShape : parametricCompositionShapes){
            if(parametricCompositionShape.getCompositionShapesUUIDList().stream().anyMatch( p-> p.equals(uuid))){
                return false;
                //TODO Verificar se ao removermos uma comp shape que esteja numa parametric se isto funciona!
            }
        }

        return true;
    }

    public void getVariablesFromFile(File file){
        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray variablesList = (JSONArray) jsonObject.get("variables");
            Iterator<JSONObject> iterator =  variablesList.iterator();

            while (iterator.hasNext()){
                JSONObject processJSON = iterator.next();

                String name = (String) processJSON.get("name");
                String value = (String) processJSON.get("value");

                Variable variableToAdd = new Variable(name, value);
                variables.add(variableToAdd);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Power> getPowerShapesFromFile(File file, Function<String, Double> proceedWhenDeletingFromThumbnail){
        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray variablesList = (JSONArray) jsonObject.get("powerShapes");
            Iterator<JSONObject> iterator =  variablesList.iterator();

            while (iterator.hasNext()){
                JSONObject powerJSON = iterator.next();

                String name = (String) powerJSON.get("name");
                UUID id = UUID.fromString((String) powerJSON.get("id"));

                Boolean hasLeft = (Boolean) powerJSON.get("hasLeft");
                Boolean hasRight = (Boolean) powerJSON.get("hasRight");

                Boolean leftHasVariable = (Boolean) powerJSON.get("leftHasVariable");
                Boolean rightHasVariable = (Boolean) powerJSON.get("rightHasVariable");
                Boolean centerHasVariable = (Boolean) powerJSON.get("centerHasVariable");

                JSONObject rightVariableText = (JSONObject) powerJSON.get("rightVariable");
                ParametricVariable rightVariable = new ParametricVariable(rightVariableText);

                JSONObject leftVariableText = (JSONObject) powerJSON.get("leftVariable");
                ParametricVariable leftVariable = new ParametricVariable(leftVariableText);

                JSONObject centerVariableText = (JSONObject) powerJSON.get("centerVariable");
                ParametricVariable centerVariable = new ParametricVariable(centerVariableText);


                String leftValue = (String) powerJSON.get("leftValue");
                String rightValue = (String) powerJSON.get("rightValue");

                String rightTranslation = (String) powerJSON.get("rightTranslation");
                String leftTranslation = (String) powerJSON.get("leftTranslation");

                boolean isFigureVariable = (boolean) powerJSON.get("isFigureVariable");


                UUID customShapeUUID = null;
                String customShapeID = (String) powerJSON.getOrDefault("customShapeID", null);


                Power powerToAdd = new Power(isFigureVariable, name, id,hasLeft, hasRight, leftHasVariable, rightHasVariable, centerHasVariable, rightVariable, leftVariable, centerVariable, leftValue, rightValue, rightTranslation, leftTranslation,
                        a -> 0.0, a -> 0.0, this, proceedWhenDeletingFromThumbnail);
                powerShapes.add(powerToAdd);

                if (customShapeID != null){
                    customShapeUUID = UUID.fromString(customShapeID);
                    UUID finalCustomShapeUUID = customShapeUUID;
                    basicShapes.forEach(basicShape -> {
                        if(basicShape.getUUID().equals(finalCustomShapeUUID)){
                            powerToAdd.addShape(basicShape);
                        }
                    });

                    newCompositionShapes.forEach(basicShape -> {
                        if(basicShape.getUUID().equals(finalCustomShapeUUID)){
                            powerToAdd.addShape(basicShape);
                        }
                    });
                }


            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return  powerShapes;
    }

    private ArrayList<Information<ArrayList<Pair<ParametricVariable, ParametricVariable>>>> loadParametricVariablePairs(JSONObject parametricCompositionShape){
        ArrayList<Information<ArrayList<Pair<ParametricVariable, ParametricVariable>>>> toReturn = new ArrayList<>();

        JSONArray outerArray = (JSONArray) parametricCompositionShape.get("variablePairsFinal");

        outerArray.forEach(outerArrayObject -> {
            JSONObject outterJsonObject = (JSONObject) outerArrayObject;
            String id = (String) outterJsonObject.get("id");
            System.out.println("O ID DO OUTER ARRAY É:" + id);
            ArrayList<Pair<ParametricVariable, ParametricVariable>> innerPairArray = new ArrayList<>();
            JSONArray innerArray = (JSONArray) outterJsonObject.get("innerArray");
            innerArray.forEach(innerArrayObject -> {
                JSONObject innerJsonObject = (JSONObject) innerArrayObject;

                System.out.println("value: " + innerJsonObject.get("value").toString());
                Pair<ParametricVariable, ParametricVariable> pair =
                        new Pair<>(new ParametricVariable((JSONObject) innerJsonObject.get("key") ),
                                new ParametricVariable((JSONObject) innerJsonObject.get("value") ));

                System.out.println("value do array: " + pair.getValue());
                innerPairArray.add(pair);
                System.out.println("inner pair: " + innerPairArray);
            });

            toReturn.add(
              new Information<>(id, innerPairArray)
            );

        });

        return toReturn;
    }

    private ParametricCompositionShape getParametricCompositionShapeFromJSONObject(JSONObject newCompositionShapeJSON, VBox transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        System.out.println(newCompositionShapeJSON);

        String name = (String) newCompositionShapeJSON.get("name");
        UUID id = UUID.fromString((String) newCompositionShapeJSON.get("id"));

        ArrayList<ParametricVariable> variables = new ArrayList<>();
        JSONArray variablesList = (JSONArray) newCompositionShapeJSON.get("variables");

        variablesList.forEach(variableObject -> {
            JSONObject variableJSON = (JSONObject) variableObject;

            ParametricVariable parametricVariable = new ParametricVariable(variableJSON);
            variables.add(parametricVariable);
        });


        //TODO There's no function being added to handle the thumbnail deletion nor other deletions...
        ParametricCompositionShape newCompositionShape = new ParametricCompositionShape(this, transformersBox,name, id, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting, loadParametricVariablePairs(newCompositionShapeJSON), variables);

        JSONArray powerShapesList = (JSONArray) newCompositionShapeJSON.get("powerShapes");

        /*powerShapesList.forEach(powerShapeObject -> {
            JSONObject powerShapeJSON = (JSONObject) powerShapeObject;

            String basicShapeID = (String) powerShapeJSON.get("id");
            System.out.println("id: " + id + ", com: " + powerShapeJSON.get("translationX"));
            double translationX = (double) powerShapeJSON.get("translationX");
            double translationY = (double) powerShapeJSON.get("translationY");

            String parametricX = (String) powerShapeJSON.get("parametricTranslationX");
            String parametricY = (String) powerShapeJSON.get("parametricTranslationY");

            newCompositionShape.addPowerShapeWithTranslation(basicShapeID, translationX, translationY, parametricX, parametricY);
        });*/
        powerShapesList.forEach(powerShapeObject -> {
            JSONObject powerShapeShapeInnerJSON = (JSONObject) powerShapeObject;

            String mapID = (String) powerShapeShapeInnerJSON.get("mapId");
            String parametricShapeInnerID = (String) powerShapeShapeInnerJSON.get("id");
            double translationX = (double) powerShapeShapeInnerJSON.get("translationX");
            double translationY = (double) powerShapeShapeInnerJSON.get("translationY");

            JSONObject parametricTranslationXJSON = (JSONObject) powerShapeShapeInnerJSON.get("parametricTranslationX");
            ParametricVariable parametricX = null;
            if(parametricTranslationXJSON != null)
                parametricX = new ParametricVariable(parametricTranslationXJSON);

            JSONObject parametricTranslationYJSON = (JSONObject) powerShapeShapeInnerJSON.get("parametricTranslationY");
            ParametricVariable parametricY = null;
            if(parametricTranslationYJSON != null)
                parametricY = new ParametricVariable(parametricTranslationYJSON);

            Power position = powerShapes.stream().filter(p -> p.getUUID().toString().equals(parametricShapeInnerID)).findFirst().get();

            //TODO: É aqui que temos que adicionar as conexões das variaveis!
            System.out.println("AQUIO A TRANSLACAO É: " + translationX + ", y: "+translationY);
            newCompositionShape.addPowerShapeWithTranslation(position.getUUID().toString(), translationX, translationY, mapID, parametricX, parametricY);

        });

        JSONArray newCompositionShapesInnerList = (JSONArray) newCompositionShapeJSON.get("compositionShapes");
        System.out.println("inner list:" + newCompositionShapesInnerList);
        System.out.println("------");

        newCompositionShapesInnerList.forEach(newCompositionShapeObject -> {
            JSONObject newCompositionShapeInnerJSON = (JSONObject) newCompositionShapeObject;

            String newCompositionShapeInnerID = (String) newCompositionShapeInnerJSON.get("id");

            double translationX = (double) newCompositionShapeInnerJSON.get("translationX");
            double translationY = (double) newCompositionShapeInnerJSON.get("translationY");

            JSONObject parametricTranslationXJSON = (JSONObject) newCompositionShapeInnerJSON.get("parametricTranslationX");
            ParametricVariable parametricX = null;
            if(parametricTranslationXJSON != null)
                parametricX = new ParametricVariable(parametricTranslationXJSON);

            JSONObject parametricTranslationYJSON = (JSONObject) newCompositionShapeInnerJSON.get("parametricTranslationY");
            ParametricVariable parametricY = null;
            if(parametricTranslationYJSON != null)
                parametricY = new ParametricVariable(parametricTranslationYJSON);


            NewCompositionShape position = newCompositionShapes.stream().filter(p -> p.getID().toString().equals(newCompositionShapeInnerID)).findFirst().get();

            //System.out.println("translationX: " + translationX + ", Y: " + translationY + ", id: " + id);

            //TODO: É aqui que temos que adicionar as conexões das variaveis!
            newCompositionShape.addNewCompositionShapeWithTranslation(position, translationX, translationY, UUID.randomUUID().toString(), parametricX, parametricY);
        });



        //TODO we are not adding parametric shapes inside other parametric shapes!
        JSONArray parametricShapesInnerList = (JSONArray) newCompositionShapeJSON.get("parametricShapes");
        parametricShapesInnerList.forEach(parametricShapeObject -> {
            JSONObject parametricShapeShapeInnerJSON = (JSONObject) parametricShapeObject;

            String mapID = (String) parametricShapeShapeInnerJSON.get("mapId");
            String parametricShapeInnerID = (String) parametricShapeShapeInnerJSON.get("id");
            System.out.println("O ID DA PARAMETRIC É: " + parametricShapeInnerID);
            double translationX = (double) parametricShapeShapeInnerJSON.get("translationX");
            double translationY = (double) parametricShapeShapeInnerJSON.get("translationY");

            System.out.println("----> translationX " + translationX + ", translationY: " + translationY);

            JSONObject parametricTranslationXJSON = (JSONObject) parametricShapeShapeInnerJSON.get("parametricTranslationX");
            ParametricVariable parametricX = null;
            if(parametricTranslationXJSON != null)
                parametricX = new ParametricVariable(parametricTranslationXJSON);

            JSONObject parametricTranslationYJSON = (JSONObject) parametricShapeShapeInnerJSON.get("parametricTranslationY");
            ParametricVariable parametricY = null;
            if(parametricTranslationYJSON != null)
                parametricY = new ParametricVariable(parametricTranslationYJSON);

            ParametricCompositionShape position = parametricCompositionShapes.stream().filter(p -> p.getID().toString().equals(parametricShapeInnerID)).findFirst().get();

            //TODO: É aqui que temos que adicionar as conexões das variaveis!
            newCompositionShape.addParametricCompositionShapeWithTranslation(position, translationX, translationY, mapID, parametricX, parametricY);
        });


        return newCompositionShape;
    }

    private NewCompositionShape getNewCompositionShapeFromJSONObject(JSONObject newCompositionShapeJSON, VBox transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        System.out.println(newCompositionShapeJSON);

        String name = (String) newCompositionShapeJSON.get("name");
        UUID id = UUID.fromString((String) newCompositionShapeJSON.get("id"));
        boolean isFigureVariable = (boolean) newCompositionShapeJSON.get("isFigureVariable");

        System.out.println("name: " + name);

        //TODO There's no function being added to handle the thumbnail deletion nor other deletions...
        NewCompositionShape newCompositionShape = new NewCompositionShape(isFigureVariable,this, transformersBox,name, id, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);

        JSONArray basicShapesList = (JSONArray) newCompositionShapeJSON.get("basicShapes");

        basicShapesList.forEach(basicShapeObject -> {
            JSONObject basicShapeJSON = (JSONObject) basicShapeObject;

            String basicShapeID = (String) basicShapeJSON.get("id");
            System.out.println("id: " + id + ", com: " + basicShapeJSON.get("translationX"));
            double translationX = (double) basicShapeJSON.get("translationX");
            double translationY = (double) basicShapeJSON.get("translationY");

            newCompositionShape.addBasicShapeWithTranslation(basicShapeID, translationX, translationY);
        });

        JSONArray newCompositionShapesInnerList = (JSONArray) newCompositionShapeJSON.get("compositionShapes");
        System.out.println("inner list:" + newCompositionShapesInnerList);
        System.out.println("------");

        newCompositionShapesInnerList.forEach(newCompositionShapeObject -> {
            JSONObject newCompositionShapeInnerJSON = (JSONObject) newCompositionShapeObject;

            String newCompositionShapeInnerID = (String) newCompositionShapeInnerJSON.get("id");
            double translationX = (double) newCompositionShapeInnerJSON.get("translationX");
            double translationY = (double) newCompositionShapeInnerJSON.get("translationY");

            NewCompositionShape position = newCompositionShapes.stream().filter(p -> p.getID().toString().equals(newCompositionShapeInnerID)).findFirst().get();

            //System.out.println("translationX: " + translationX + ", Y: " + translationY + ", id: " + id);

            newCompositionShape.addNewCompositionShapeWithTranslation(position, translationX, translationY, UUID.randomUUID().toString());
        });

        return newCompositionShape;
    }

    public ArrayList<ParametricCompositionShape> getParametricShapesFromFile(File file, VBox transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        ArrayList<ParametricCompositionShape> newParametricCompositionShapes = new ArrayList<>();

        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray newCompositionShapesList = (JSONArray) jsonObject.get("parametricCompositionShapes");
            Iterator<JSONObject> iterator =  newCompositionShapesList.iterator();

            while (iterator.hasNext()){
                JSONObject parametricShapeJSON = iterator.next();
                newParametricCompositionShapes.add(getParametricCompositionShapeFromJSONObject(parametricShapeJSON, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting));
                this.parametricCompositionShapes.add(newParametricCompositionShapes.get(newParametricCompositionShapes.size()-1));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return newParametricCompositionShapes;
    }

    public void getShapeRulesFromFile(File file){
        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray shapeRulesList = (JSONArray) jsonObject.get("shapeRules");
            Iterator<JSONObject> iterator =  shapeRulesList.iterator();

            while (iterator.hasNext()){
                JSONObject shapeRuleJSON = iterator.next();

                ShapeRule shapeRuleToAdd = null;
                String type = (String) shapeRuleJSON.get("type");

                UUID id = UUID.fromString((String) shapeRuleJSON.get("id"));
                String name = (String) shapeRuleJSON.get("name");
                boolean matched = (boolean) shapeRuleJSON.get("matched");

                JSONObject leftShape = (JSONObject) shapeRuleJSON.get("leftShape");
                JSONObject rightShape = (JSONObject) shapeRuleJSON.get("rightShape");


                if(type.equals("bool")){
                    String boolXML = (String)shapeRuleJSON.get("boolXML");
                    String boolCode = (String) shapeRuleJSON.get("boolCode");

                    shapeRuleToAdd = new BoolShapeShape(
                            getParametricCompositionShapeFromJSONObject(leftShape, null, a -> 0.0, a -> 0.0),
                            getParametricCompositionShapeFromJSONObject(rightShape, null, a -> 0.0, a -> 0.0),
                            id, name,
                            matched,
                            boolXML, boolCode,
                            a -> 0.0, a -> 0.0);

                }else if(type.equals("bool-proc")){
                    String boolXML = (String)shapeRuleJSON.get("boolXML");
                    String boolCode = (String) shapeRuleJSON.get("boolCode");

                    String processXML = (String)shapeRuleJSON.get("processXML");
                    String processCode = (String) shapeRuleJSON.get("processCode");

                    shapeRuleToAdd = new BoolShapeShapeProc(
                            getParametricCompositionShapeFromJSONObject(leftShape, null, a -> 0.0, a -> 0.0),
                            getParametricCompositionShapeFromJSONObject(rightShape, null, a -> 0.0, a -> 0.0),
                            id, name,
                            matched,
                            boolXML, boolCode,
                            a -> 0.0, a -> 0.0,
                            processXML, processCode);

                }else if(type.equals("proc")){
                    String processXML = (String)shapeRuleJSON.get("processXML");
                    String processCode = (String) shapeRuleJSON.get("processCode");

                    shapeRuleToAdd = new ShapeShapeProc(
                            getParametricCompositionShapeFromJSONObject(leftShape, null, a -> 0.0, a -> 0.0),
                            getParametricCompositionShapeFromJSONObject(rightShape, null, a -> 0.0, a -> 0.0),
                            id, name,
                            matched,
                            processXML, processCode,
                            a -> 0.0, a -> 0.0);

                }else if (type.equals("")){

                    shapeRuleToAdd = new ShapeShape(
                            getParametricCompositionShapeFromJSONObject(leftShape, null, a -> 0.0, a -> 0.0),
                            getParametricCompositionShapeFromJSONObject(rightShape, null, a -> 0.0, a -> 0.0),
                            id, name,
                            a -> 0.0, a -> 0.0);
                }

                System.out.println(shapeRuleJSON);


                shapeRules.add(shapeRuleToAdd);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getProcessesFromFile(File file){
        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray processesList = (JSONArray) jsonObject.get("processes");
            Iterator<JSONObject> iterator =  processesList.iterator();

            while (iterator.hasNext()){
                JSONObject processJSON = iterator.next();

                System.out.println(processJSON);

                String name = (String) processJSON.get("name");
                UUID id = UUID.fromString((String) processJSON.get("id"));
                String blocklyXML = (String)processJSON.get("blocklyXML");
                String processCode = (String) processJSON.get("processCode");

                Process processToAdd = new Process(id, name, blocklyXML, processCode);
                processes.add(processToAdd);

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public ArrayList<NewCompositionShape> getNewCompositionShapesFromFile(File file, VBox transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting){
        ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();

        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray newCompositionShapesList = (JSONArray) jsonObject.get("compositionShapes");
            Iterator<JSONObject> iterator =  newCompositionShapesList.iterator();

            while (iterator.hasNext()){
                JSONObject newCompositionShapeJSON = iterator.next();

                System.out.println(newCompositionShapeJSON);

                String name = (String) newCompositionShapeJSON.get("name");
                UUID id = UUID.fromString((String) newCompositionShapeJSON.get("id"));
                boolean isFigureVariable = (boolean) newCompositionShapeJSON.get("isFigureVariable");

                System.out.println("name: " + name);

                //TODO There's no function being added to handle the thumbnail deletion nor other deletions...
                NewCompositionShape newCompositionShape = new NewCompositionShape(isFigureVariable, this, transformersBox,name, id, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
                newCompositionShapes.add(newCompositionShape);

                JSONArray basicShapesList = (JSONArray) newCompositionShapeJSON.get("basicShapes");

                basicShapesList.forEach(basicShapeObject -> {
                    JSONObject basicShapeJSON = (JSONObject) basicShapeObject;

                    String basicShapeID = (String) basicShapeJSON.get("id");
                    System.out.println("id: " + id + ", com: " + basicShapeJSON.get("translationX"));
                    double translationX = (double) basicShapeJSON.get("translationX");
                    double translationY = (double) basicShapeJSON.get("translationY");

                    newCompositionShape.addBasicShapeWithTranslation(basicShapeID, translationX, translationY);
                });

                JSONArray newCompositionShapesInnerList = (JSONArray) newCompositionShapeJSON.get("compositionShapes");
                System.out.println("inner list:" + newCompositionShapesInnerList);
                System.out.println("------");
                newCompositionShapesInnerList.forEach(newCompositionShapeObject -> {
                    JSONObject newCompositionShapeInnerJSON = (JSONObject) newCompositionShapeObject;

                    String newCompositionShapeInnerID = (String) newCompositionShapeInnerJSON.get("id");
                    double translationX = (double) newCompositionShapeInnerJSON.get("translationX");
                    double translationY = (double) newCompositionShapeInnerJSON.get("translationY");

                    NewCompositionShape position = newCompositionShapes.stream().filter(p -> p.getID().toString().equals(newCompositionShapeInnerID)).findFirst().get();

                    //System.out.println("translationX: " + translationX + ", Y: " + translationY + ", id: " + id);

                    newCompositionShape.addNewCompositionShapeWithTranslation(position, translationX, translationY, UUID.randomUUID().toString());
                });


            }

        }catch (Exception e){
            e.printStackTrace();
        }

        this.newCompositionShapes.addAll(newCompositionShapes);
        return newCompositionShapes;
    }

    public ArrayList<BasicShape> getBasicShapesFromFile(File file){
        ArrayList<BasicShape> basicShapes = new ArrayList<>();

        JSONParser jsonParser = new JSONParser();
        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray basicShapesList = (JSONArray) jsonObject.get("basicShapes");

            Iterator<JSONObject> iterator = basicShapesList.iterator();

            while(iterator.hasNext()){
                JSONObject basicShapeJSON = iterator.next();

                String color = (String) basicShapeJSON.get("color");
                double width = (double) basicShapeJSON.get("width");
                double height = (double) basicShapeJSON.get("height");
                String name = (String) basicShapeJSON.get("name");
                UUID id = UUID.fromString((String) basicShapeJSON.get("id"));

                Image image = null;
                Color colorToSend = null;
                if(color.contains(".jpg") || color.contains(".png")){
                    //It is an image
                    File imageFile = new File(Orchestrator.path + IMAGES_PATH + color);
                    image = new Image(imageFile.toURI().toString());
                }else{
                    colorToSend = Color.web(color);
                }

                //TODO There's no function being added to handle the thumbnail deletion nor other deletions...
                //TODO
                BasicShape basicShape = new BasicShape(width, height, colorToSend, image, id, name);
                basicShapes.add(basicShape);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        this.basicShapes.addAll(basicShapes);

        return basicShapes;
    }

    public void addAllCompositionShapes(ArrayList<NewCompositionShape> newCompositionShapesToAdd){
        newCompositionShapes.clear();
        newCompositionShapes.addAll(newCompositionShapesToAdd);
        saveFile();
    }

    public void addAllPowerShapes(ArrayList<Power> powerShapesToAdd){
        powerShapes.clear();
        powerShapes.addAll(powerShapesToAdd);
        saveFile();
    }

    public void addAllParametricCompositionShapes(ArrayList<ParametricCompositionShape> parametricCompositionShapesToAdd){
        parametricCompositionShapes.clear();
        parametricCompositionShapes.addAll(parametricCompositionShapesToAdd);
        saveFile();
    }

    private JSONArray getBasicShapesJSON(){
        JSONArray list = new JSONArray();

        for (BasicShape basicShape : basicShapes) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", basicShape.getUUID().toString());
            jsonObject.put("basic", "true");
            jsonObject.put("color", basicShape.getFill() != null ? basicShape.getFill().toString() : basicShape.getImageName());
            jsonObject.put("width", basicShape.getWidth());
            jsonObject.put("height", basicShape.getHeight());
            jsonObject.put("name", basicShape.getShapeName());

            list.add(jsonObject);
        }

        return list;
    }

    private JSONArray getVariablesJSON(){
        JSONArray list = new JSONArray();

        for(Variable variable: variables){
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("name", variable.getName());
            jsonObject.put("value", variable.getValue());

            list.add(jsonObject);
        }

        return list;
    }

    private JSONArray getProcessesJSON(){
        JSONArray list = new JSONArray();

        for(Process process: processes){
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", process.getId().toString());
            jsonObject.put("name", process.getProcessName());
            jsonObject.put("blocklyXML", process.getBlocklyXML());
            jsonObject.put("processCode", process.getProcessCode());

            list.add(jsonObject);
        }

        return list;
    }

    private JSONArray getCompositionShapesJSON(){
        JSONArray list = new JSONArray();

        System.out.println("composition shape size: " + newCompositionShapes.size());

        for(NewCompositionShape newCompositionShape : newCompositionShapes){
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("isFigureVariable", newCompositionShape.isFigureVariable());
            jsonObject.put("id", newCompositionShape.getID().toString());
            jsonObject.put("name", newCompositionShape.getShapeName());
            jsonObject.put("basicShapes", newCompositionShape.getBasicShapesJSON());
            jsonObject.put("compositionShapes", newCompositionShape.getCompositionShapesJSON());

            list.add(jsonObject);
        }

        return list;
    }

    private JSONArray getParametricCompositionShapesJSON(){
        JSONArray list = new JSONArray();

        for(ParametricCompositionShape parametricCompositionShape : parametricCompositionShapes){
            //JSONObject jsonObject = new JSONObject();
            /*jsonObject.put("id", parametricCompositionShape.getID().toString());
            jsonObject.put("name", parametricCompositionShape.getShapeName());
            jsonObject.put("basicShapes", parametricCompositionShape.getBasicShapesJSON());
            jsonObject.put("compositionShapes", parametricCompositionShape.getCompositionShapesJSON());*/

            list.add(parametricCompositionShape.getJSONObject());
        }


        return list;
    }

    private JSONArray getShapeRulesJSON(){
        JSONArray list = new JSONArray();

        shapeRules.forEach(power -> list.add(power.getJSONObject()));

        return list;
    }

    private JSONArray getPowerShapesJSON(){
        JSONArray list = new JSONArray();

        powerShapes.forEach(power -> list.add(power.getJSONObject()));

        return list;
    }

    public StringBuilder processesToString(){
        StringBuilder toReturn = new StringBuilder();

        processes.forEach(process -> {

            if(!Process.hasDependencies(process)){
                toReturn.append(process.getProcessCode().replace("\n", "").replace(";","")).append("\n");
            }else{
                toReturn.append(solveDependencies(process.getProcessCode().replace("\n", "").replace(";",""))).append("\n");

            }

        });

        System.out.println(toReturn);
        return toReturn;
    }

    public StringBuilder variablesToString(){
        StringBuilder toReturn = new StringBuilder();

        toReturn.append("memory(global, [");

        for (int i = 0; i < variables.size(); i++) {
            Variable toAdd = variables.get(i);
            toReturn.append("var(").append(toAdd.getName()).append(",").append(toAdd.getValue()).append(")");
            if(i < variables.size() - 1){
                toReturn.append(",");
            }
        }

        toReturn.append("]).");

        return toReturn;
    }

    public StringBuilder powerToString(){
        StringBuilder toReturn = new StringBuilder();

        powerShapes.forEach(power -> toReturn.append(power.getPowerToString()).append("\n"));

        return toReturn;
    }

    public StringBuilder shapeRulesToString(){
        StringBuilder toReturn = new StringBuilder();

        shapeRules.forEach(shapeRule -> {

            if(!ShapeRule.hasDependencies(shapeRule)){
                toReturn.append(shapeRule.getCode().replace("\n", "").replace(";","")).append("\n");
            }else{
                toReturn.append(solveDependencies(shapeRule.getCode()).replace("\n", "").replace(";","")).append("\n");
            }

        });

        return toReturn;
    }

    private StringBuilder basicShapesToString(){
        StringBuilder toReturn = new StringBuilder();

        basicShapes.forEach(basicShape -> {

            toReturn.append("shape(").append(basicShape.getShapeName()).append("," + "'shapes/" + basicShape.getShapeName() + ".gif'" +  ").").append("\n");
            toReturn.append("basicShapeDimention(").append(basicShape.getShapeName()).append(",").append(basicShape.getWidth()/SCALE).append(",").append(basicShape.getHeight()/SCALE).append(").");
            toReturn.append("\n");
        });

        toReturn.append("shapebringtofront([");

        for(int i = 0; i < basicShapes.size() ; i++){
            toReturn.append(basicShapes.get(i).getShapeName());
            if(i < basicShapes.size() - 1){
                toReturn.append(",");
            }
        }

        toReturn.append("]).");

        basicShapes.forEach(basicShape -> {
            //Rectangle toExport = new Rectangle(basicShape.getWidth(), basicShape.getHeight());
            //toExport.setFill(basicShape.getFill());
            Pane toExport = basicShape.getRectangle();

            if (basicShape.isSelected()){
                basicShape.temporarilyTurnOffStroke();
            }

            WritableImage image = toExport.snapshot(new SnapshotParameters(), null);

            if (basicShape.isSelected()){
                basicShape.turnOnStroke();
            }

            try {
                File toSave = new File(path + SHAPES_PATH + basicShape.getShapeName() + ".gif");

                Path folderPath = Paths.get(path + SHAPES_PATH);
                if (!Files.exists(folderPath)){
                    Files.createDirectories(folderPath);
                }

                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "gif", toSave);
            }catch (IOException e){
                e.printStackTrace();
            }

        });

        System.out.println(toReturn);
        return toReturn;
    }

    private StringBuilder compositionShapesToString(){
        StringBuilder toReturn = new StringBuilder();

        newCompositionShapes.forEach(newCompositionShape -> {
            toReturn.append(newCompositionShape.getPrologRepresentation(true, false)).append("\n");
        });

        return toReturn;
    }

    private StringBuilder parametricCompositionShapesToString(){
        StringBuilder toReturn = new StringBuilder();

        parametricCompositionShapes.forEach(parametricCompositionShape -> {
            toReturn.append(parametricCompositionShape.getPrologRepresentation(true, false)).append("\n");
        });

        return toReturn;
    }

    public StringBuilder printDesignTXT(){
        StringBuilder toReturn = new StringBuilder();
        System.out.println("--------- ### DESIGN.TXT ### ---------");
        toReturn.append("scale_unit(40)").append("\n");

        toReturn.append("\n\n").append(basicShapesToString()).append("\n");

        toReturn.append("\n\n").append(variablesToString()).append("\n");

        toReturn.append("\n\n").append(powerToString()).append("\n");

        toReturn.append("\n\n").append(compositionShapesToString()).append("\n");

        toReturn.append("\n\n").append(parametricCompositionShapesToString().append("\n"));

        toReturn.append("\n\n").append(processesToString()).append("\n");

        toReturn.append("\n\n").append(shapeRulesToString());

        return toReturn;
    }

    private void saveFile(){
        System.out.println("Orchestrator saveFile()");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("basicShapes", getBasicShapesJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());
        jsonObject.put("parametricCompositionShapes", getParametricCompositionShapesJSON());
        jsonObject.put("processes", getProcessesJSON());
        jsonObject.put("variables", getVariablesJSON());
        jsonObject.put("powerShapes", getPowerShapesJSON());
        jsonObject.put("shapeRules", getShapeRulesJSON());

        try{
            FileWriter fileWriter = new FileWriter(path + "/toLoad.json");
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.flush();
            fileWriter.close();

            FileWriter fileWriterDesign = new FileWriter(path + "/design.txt");
            fileWriterDesign.write(printDesignTXT().toString());
            fileWriterDesign.flush();
            fileWriterDesign.close();

            System.out.println("Guardei: " + jsonObject.toJSONString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private String solveDependencies(String code){
        String toReturn = solveDependency(code, "existingproc");
        toReturn = solveDependency(toReturn, "existingproc0");
        toReturn = solveDependency(toReturn, "existingproc1");
        toReturn = solveDependency(toReturn, "existingbool");

        return toReturn;
    }

    private String solveDependency(String code, String existingProc){

        Pattern pattern = Pattern.compile(existingProc + "\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()){
            String match = matcher.group(1);
            String toReplace = existingProc + "(" + match + ")";

            if(existingProc.equals("existingproc")){

                Optional<Process> seeIfExists = processes.stream().filter(p -> p.getProcessName().equals(match)).findFirst();
                if(seeIfExists.isPresent()) {
                    Process toPut = seeIfExists.get();
                    if (Process.hasDependencies(toPut)) {
                        code = code.replaceAll(Pattern.quote(toReplace), solveDependency(toPut.getProcessCode(), existingProc));
                    } else {
                        code = code.replaceAll(Pattern.quote(toReplace), toPut.getProcessCode());
                    }
                }

            }else if(existingProc.equals("existingproc0")){
                Optional<ShapeRule> seeIfExists = shapeRules.stream().filter(p -> p.getShapeRuleName().equals(match)).findFirst();
                if(seeIfExists.isPresent()) {
                    ShapeRule toPut = seeIfExists.get();
                    if (ShapeRule.hasDependencies(toPut)) {
                        code = code.replaceAll(Pattern.quote(toReplace), solveDependency( toPut.getCode(), existingProc));
                    } else {
                        code = code.replaceAll(Pattern.quote(toReplace), toPut.getCode());
                    }
                }
            }else if(existingProc.equals("existingproc1")){
                Optional<ShapeRule> seeIfExists = shapeRules.stream().filter(p -> p.getShapeRuleName().equals(match)).findFirst();
                if(seeIfExists.isPresent()) {
                    ShapeRule toPut = seeIfExists.get();
                    if (ShapeRule.hasDependencies(toPut)) {
                        code = code.replaceAll(Pattern.quote(toReplace), solveDependency(toPut.getProcessCode(), existingProc));
                    } else {
                        code = code.replaceAll(Pattern.quote(toReplace), toPut.getProcessCode());
                    }
                }
            }else if(existingProc.equals("existingbool")){
                Optional<ShapeRule> seeIfExists = shapeRules.stream().filter(p -> p.getShapeRuleName().equals(match)).findFirst();
                if(seeIfExists.isPresent()) {
                    ShapeRule toPut = seeIfExists.get();
                    if (ShapeRule.hasDependencies(toPut)) {
                        code = code.replaceAll(Pattern.quote(toReplace), solveDependency( toPut.getBoolCode(), existingProc));
                    } else {
                        code = code.replaceAll(Pattern.quote(toReplace), toPut.getBoolCode());
                    }
                }
            }

        }

        return code;
    }

}
