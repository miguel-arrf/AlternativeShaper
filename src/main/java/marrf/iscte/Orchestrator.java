package marrf.iscte;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import marrf.iscte.ShapeRules.ShapeRule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Orchestrator {

    private static final int SCALE = 40;
    private static final int NUMBER_COLUMNS_AND_ROWS = 40;

    //private String path = "C:\\Users\\mferr\\Downloads\\objetos\\test.json";
    private String path = "/Users/miguelferreira/Downloads/alternativeShaperSaves/test.json";

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();
    private final ArrayList<Process> processes = new ArrayList<>();
    private final ArrayList<ShapeRule> shapeRules = new ArrayList<>();

    public static int getSCALE() {
        return SCALE;
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

        return true;
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

    public void processesToString(){
        StringBuilder toReturn = new StringBuilder();

        processes.forEach(process -> {

            if(!Process.hasDependencies(process)){
                toReturn.append(process.getProcessCode()).append("\n");
            }else{
                toReturn.append(Process.solveDependency(processes, process)).append("\n");

            }

        });

        System.out.println(toReturn);
    }

    private void basicShapesToString(){
        StringBuilder toReturn = new StringBuilder();

        basicShapes.forEach(basicShape -> {

            toReturn.append("shape(").append(basicShape.getShapeName()).append(",COLOCAR_IMAGEM).").append("\n");
            toReturn.append("basicShapeDimention(").append(basicShape.getShapeName()).append(",").append(basicShape.getWidth()).append(",").append(basicShape.getHeight()).append(").");
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

        System.out.println(toReturn);
    }



    public void printDesignTXT(){
        System.out.println("--------- ### DESIGN.TXT ### ---------");
        System.out.println("scale_unit(40)");
        basicShapesToString();
        processesToString();


    }

    private void saveFile(){
        System.out.println("Orchestrator saveFile()");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("basicShapes", getBasicShapesJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());
        jsonObject.put("processes", getProcessesJSON());

        try{
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.flush();
            fileWriter.close();

            System.out.println("Guardei: " + jsonObject.toJSONString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
