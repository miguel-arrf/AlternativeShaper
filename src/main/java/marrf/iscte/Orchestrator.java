package marrf.iscte;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class Orchestrator {

    private static final int SCALE = 40;
    private static final int NUMBER_COLUMNS_AND_ROWS = 40;

    //private String path = "C:\\Users\\mferr\\Downloads\\objetos\\test.json";
    private String path = "/Users/miguelferreira/Downloads/alternativeShaperSaves/test.json";

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private final ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();
    private final ArrayList<Process> processes = new ArrayList<>();

    public static int getSCALE() {
        return SCALE;
    }

    public static int getNumberColumnsAndRows() {
        return NUMBER_COLUMNS_AND_ROWS;
    }

    public ArrayList<Process> getProcesses() {
        return processes;
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

    /*public Pane getCopyOfCompositionShape(String id, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY){
        Pane toReturn = new Pane();

        Optional<NewCompositionShape> compositionShape = newCompositionShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();

        if(compositionShape.isPresent()){
            NewCompositionShape toCopyFrom = compositionShape.get();

            toCopyFrom.getBasicShapes().forEach(basicShape -> {
                basicShape.setTranslateX(basicShape.getInitialTranslation().getX());
                basicShape.setTranslateY(basicShape.getInitialTranslation().getY());

                toReturn.getChildren().add(basicShape.getRectangle());
            });



        }

        return toReturn;
    }*/

    public ArrayList<NewCompositionShape> getNewCompositionShapesFromFile(File file, VBox transformersBox){
        ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();

        JSONParser jsonParser = new JSONParser();

        try{
            Object object = jsonParser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;

            JSONArray newCompositionShapesList = (JSONArray) jsonObject.get("compositionShapes");
            Iterator<JSONObject> iterator = newCompositionShapesList.iterator();

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
                newCompositionShapesInnerList.forEach(newCompositionShapeObject -> {
                    JSONObject newCompositionShapeInnerJSON = (JSONObject) newCompositionShapeObject;

                    String newCompositionShapeInnerID = (String) newCompositionShapeInnerJSON.get("id");
                    double translationX = (double) newCompositionShapeInnerJSON.get("translationX");
                    double translationY = (double) newCompositionShapeInnerJSON.get("translationY");

                    NewCompositionShape position = newCompositionShapes.stream().filter(p -> p.getID().toString().equals(newCompositionShapeInnerID)).findFirst().get();

                    newCompositionShape.addNewCompositionShapeWithTranslation(position, translationX, translationY, newCompositionShapeInnerID);
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

    private void saveFile(){
        System.out.println("Orchestrator saveFile()");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("basicShapes", getBasicShapesJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());

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
