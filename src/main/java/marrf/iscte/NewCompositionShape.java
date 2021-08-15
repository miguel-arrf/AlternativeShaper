package marrf.iscte;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewCompositionShape {

    private String name;
    private final UUID ID = UUID.randomUUID();

    private Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();

    private final Map<String, Double> compositionShapesXTranslation = new HashMap<>();
    private final Map<String, Double> compositionShapesYTranslation = new HashMap<>();
    private final Map<String, Double> compositionShapesScaleX = new HashMap<>();
    private final Map<String, Double> compositionShapesScaleY = new HashMap<>();

    private Map<String, Double> basicShapesXTranslation = new HashMap<>();
    private Map<String, Double> basicShapesYTranslation = new HashMap<>();
    private Map<String, Double> basicShapesScaleX = new HashMap<>();
    private Map<String, Double> basicShapesScaleY = new HashMap<>();

    private Orchestrator orchestrator;

    public NewCompositionShape(Orchestrator orchestrator){
        this.orchestrator = orchestrator;
    }

    public UUID getID() {
        return ID;
    }

    public String getName() {
        return name;
    }


    public JSONArray getBasicShapesJSON(){
        JSONArray array = new JSONArray();

        basicShapesScaleX.forEach((id, transformation) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);

            jsonObject.put("translationX", basicShapesXTranslation.get(id));
            jsonObject.put("translationY", basicShapesYTranslation.get(id));

            jsonObject.put("scaleX", transformation);
            jsonObject.put("scaleY", basicShapesScaleY.get(id));

            array.add(jsonObject);
        });

        return array;
    }

    public JSONArray getCompositionShapesJSON(){
        JSONArray array = new JSONArray();

        compositionShapeMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", compositionShape.getID());
            jsonObject.put("translationX",compositionShapesXTranslation.get(id));
            jsonObject.put("translationY", compositionShapesYTranslation.get(id));

            jsonObject.put("scaleX", compositionShapesScaleX.get(id));
            jsonObject.put("scaleY", compositionShapesScaleY.get(id));

            array.add(jsonObject);
        });

        return array;
    }

    public BasicShape addBasicShape(String basicShapesID){
        basicShapesXTranslation.put(basicShapesID, 0.0);
        basicShapesYTranslation.put(basicShapesID, 0.0);

        basicShapesScaleX.put(basicShapesID, 1.0);
        basicShapesScaleY.put(basicShapesID, 1.0);

        return orchestrator.getCopyOfBasicShape(basicShapesID);
    }

    public ArrayList<BasicShape> getBasicShapes(){
        ArrayList<BasicShape> basicShapes = new ArrayList<>();

        basicShapesScaleX.forEach((id, ignore) -> {
            basicShapes.add(orchestrator.getCopyOfBasicShape(id));
        });

        return basicShapes;
    }

    public void addNewCompositionShape(NewCompositionShape NewCompositionShape){
        String id = UUID.randomUUID().toString();

        compositionShapeMap.put(id, NewCompositionShape);

        compositionShapesXTranslation.put(id, 0.0);
        compositionShapesYTranslation.put(id, 0.0);

        compositionShapesScaleX.put(id, 1.0);
        compositionShapesScaleY.put(id, 1.0);
    }

}
