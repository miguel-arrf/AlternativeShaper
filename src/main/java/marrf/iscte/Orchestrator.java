package marrf.iscte;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import marrf.iscte.ShapeRules.BoolShapeShape;
import marrf.iscte.ShapeRules.ShapeRule;
import marrf.iscte.ShapeRules.ShapeShape;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Orchestrator {

    private static final int SCALE = 40;
    private static final int NUMBER_COLUMNS_AND_ROWS = 40;

    //private String path = "C:\\Users\\mferr\\Downloads\\objetos\\test.json";
    public static String path = "/Users/miguelferreira/Downloads/alternativeShaperSaves/test.json";

    public static String htmlFolder = "/Users/miguelferreira/Downloads/AlternativeShaperFiles/";

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();
    private final ArrayList<Process> processes = new ArrayList<>();
    private final ArrayList<ShapeRule> shapeRules = new ArrayList<>();
    private final ArrayList<Variable> variables = new ArrayList<>();


    public static int getSCALE() {
        return SCALE;
    }

    public String getBasicShapeNameFromID(String id){
        return  basicShapes.stream().filter(p -> p.getUUID().toString().equals(id)).findFirst().get().getShapeName();
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

            toReturn = new BasicShape(toCopyFrom.getWidth(), toCopyFrom.getHeight(), toCopyFrom.getFill(), writeTranslateX, writeTranslateY, proceedWhenDeleting);
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

        for(ShapeRule shapeRule : shapeRules) {
            if (shapeRule.getLeftShape().getBasicShapesUUIDList().stream().anyMatch(p -> p.equals(uuid)))
                return false;
            if (shapeRule.getRightShape().getBasicShapesUUIDList().stream().anyMatch(p -> p.equals(uuid)))
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
                Double value = (Double) processJSON.get("value");

                Variable variableToAdd = new Variable(name, value);
                variables.add(variableToAdd);

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

    public ArrayList<NewCompositionShape> getNewCompositionShapesFromFile(File file, VBox transformersBox){
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

                System.out.println("name: " + name);

                //TODO There's no function being added to handle the thumbnail deletion nor other deletions...
                NewCompositionShape newCompositionShape = new NewCompositionShape(this, transformersBox,name, id);
                newCompositionShapes.add(newCompositionShape);

                JSONArray basicShapesList = (JSONArray) newCompositionShapeJSON.get("basicShapes");

                basicShapesList.forEach(basicShapeObject -> {
                    JSONObject basicShapeJSON = (JSONObject) basicShapeObject;

                    String basicShapeID = (String) basicShapeJSON.get("id");
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

                    System.out.println("translationx: " + translationX + ", Y: " + translationY + ", id: " + id);

                    newCompositionShape.addNewCompositionShapeWithTranslation(position, translationX, translationY, UUID.randomUUID().toString());
                });


            }

        }catch (Exception e){
            e.printStackTrace();
        }

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

                //TODO There's no function being added to handle the thumbnail deletion nor other deletions...
                BasicShape basicShape = new BasicShape(width, height, Color.web(color), id, name);
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

    private JSONArray getBasicShapesJSON(){
        JSONArray list = new JSONArray();

        for (BasicShape basicShape : basicShapes) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", basicShape.getUUID().toString());
            jsonObject.put("basic", "true");
            jsonObject.put("color", basicShape.getFill().toString());
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

            jsonObject.put("id", newCompositionShape.getID().toString());
            jsonObject.put("name", newCompositionShape.getShapeName());
            jsonObject.put("basicShapes", newCompositionShape.getBasicShapesJSON());
            jsonObject.put("compositionShapes", newCompositionShape.getCompositionShapesJSON());

            list.add(jsonObject);
        }

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
            Rectangle toExport = new Rectangle(basicShape.getWidth(), basicShape.getHeight());
            toExport.setFill(basicShape.getFill());

            WritableImage image = toExport.snapshot(new SnapshotParameters(), null);
            try {
                File toSave = new File(path + "/shapes/" + basicShape.getShapeName() + ".gif");

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

    public StringBuilder printDesignTXT(){
        StringBuilder toReturn = new StringBuilder();
        System.out.println("--------- ### DESIGN.TXT ### ---------");
        toReturn.append("scale_unit(40)").append("\n");

        toReturn.append("\n\n").append(basicShapesToString()).append("\n");

        toReturn.append("\n\n").append(variablesToString()).append("\n");

        toReturn.append("\n\n").append(compositionShapesToString()).append("\n");

        toReturn.append("\n\n").append(processesToString()).append("\n");

        toReturn.append("\n\n").append(shapeRulesToString());

        return toReturn;
    }

    private void saveFile(){
        System.out.println("Orchestrator saveFile()");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("basicShapes", getBasicShapesJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());
        jsonObject.put("processes", getProcessesJSON());
        jsonObject.put("variables", getVariablesJSON());

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
