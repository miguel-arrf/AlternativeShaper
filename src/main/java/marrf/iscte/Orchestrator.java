package marrf.iscte;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.text.html.Option;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Orchestrator {

    //private String path = "C:\\Users\\mferr\\Downloads\\objetos\\test.json";
    private String path = "/Users/miguelferreira/Downloads/alternativeShaperSaves/test.txt";

    private ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private ArrayList<NewCompositionShape> newCompositionShapes = new ArrayList<>();

    public void addBasicShape(BasicShape basicShape){
        basicShapes.add(basicShape);
        saveFile();
    }

    public void addNewCompositionShape(NewCompositionShape newCompositionShape){
        newCompositionShapes.add(newCompositionShape);
        saveFile();
    }

    public void addAllBasicShapes(ArrayList<BasicShape> basicShapesToAdd){
        basicShapes.clear();
        basicShapes.addAll(basicShapesToAdd);

        saveFile();
    }

    public BasicShape getCopyOfBasicShape(String id, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<Double, Double> writeScaleX, Function<Double, Double> writeScaleY){
        BasicShape toReturn = null;

        Optional<BasicShape> shape = basicShapes.stream().filter(s -> s.getUUID().toString().equals(id)).findFirst();

        if(shape.isPresent()){
            BasicShape toCopyFrom = shape.get();

            toReturn = new BasicShape(toCopyFrom.getWidth(), toCopyFrom.getHeight(), toCopyFrom.getFill(), writeTranslateX, writeTranslateY, writeScaleX, writeScaleY);
        }


        return toReturn;
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
            jsonObject.put("name", newCompositionShape.getName());
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
