package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NewCompositionShape implements CustomShape{

    private String name;
    private final UUID ID = UUID.randomUUID();

    private final Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();

    private final Map<String, Double> compositionShapesXTranslation = new HashMap<>();
    private final Map<String, Double> compositionShapesYTranslation = new HashMap<>();
    private final Map<String, Double> compositionShapesScaleX = new HashMap<>();
    private final Map<String, Double> compositionShapesScaleY = new HashMap<>();

    private final ArrayList<Information> basicShapesXTranslation = new ArrayList<>();
    private final ArrayList<Information> basicShapesYTranslation = new ArrayList<>();
    private final ArrayList<Information> basicShapesScaleX = new ArrayList<>();
    private final ArrayList<Information> basicShapesScaleY = new ArrayList<>();

    private final Orchestrator orchestrator;

    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;
    private HBox scaleXBox;
    private HBox scaleYBox;

    //Composition Shape Thumbnail
    private final VBox thumbnail = new VBox();

    public NewCompositionShape(Orchestrator orchestrator){
        this.orchestrator = orchestrator;
        setUpComponents();
    }

    public UUID getID() {
        return ID;
    }

    public String getName() {
        return name;
    }


    public JSONArray getBasicShapesJSON(){
        JSONArray array = new JSONArray();

        basicShapesScaleX.forEach(information -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", information.getId());

            int position = basicShapesScaleX.indexOf(information);

            jsonObject.put("translationX", basicShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", basicShapesYTranslation.get(position).getValue());

            jsonObject.put("scaleX", information.getValue());
            jsonObject.put("scaleY", basicShapesScaleY.get(position).getValue());

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
        Information translationX = new Information(basicShapesID, 0.0);
        Information translationY = new Information(basicShapesID, 0.0);

        Information scaleX = new Information(basicShapesID, 1.0);
        Information scaleY = new Information(basicShapesID, 1.0);

        basicShapesXTranslation.add(translationX);
        basicShapesYTranslation.add(translationY);

        basicShapesScaleX.add(scaleX);
        basicShapesScaleY.add(scaleY);

        System.out.println("TAMANHOOOOOO: " + basicShapesXTranslation.size());

        return orchestrator.getCopyOfBasicShape(basicShapesID, translationX.getConsumer(), translationY.getConsumer(), scaleX.getConsumer(), scaleY.getConsumer());
    }

    public ArrayList<BasicShape> getBasicShapes(){
        ArrayList<BasicShape> basicShapes = new ArrayList<>();

        basicShapesScaleX.forEach(information -> {
            int position = basicShapesScaleX.indexOf(information);

            Information translationX = basicShapesXTranslation.get(position);
            Information translationY = basicShapesYTranslation.get(position);

            Information scaleY = basicShapesScaleY.get(position);

            basicShapes.add(orchestrator.getCopyOfBasicShape(information.id, translationX.getConsumer(), translationY.getConsumer(), information.getConsumer(), scaleY.getConsumer()));
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

    private void setUpComponents(){
        setUpTranslationXBox();
        setUpTranslationYBox();

        setUpScaleXBox();
        setUpScaleYBox();
    }

    private void setUpTranslationXBox(){
        translationXBox = new HBox(new Label("translation x"));
        translationXBox.setPadding(new Insets(10, 10, 10, 15));
        translationXBox.setAlignment(Pos.CENTER_LEFT);
        translationXBox.setMinHeight(30);
        translationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }
    private void setUpTranslationYBox(){
        translationYBox = new HBox(new Label("translation y"));
        translationYBox.setPadding(new Insets(10, 10, 10, 15));
        translationYBox.setAlignment(Pos.CENTER_LEFT);
        translationYBox.setMinHeight(30);
        translationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }
    private void setUpScaleXBox(){
        scaleXBox = new HBox(new Label("scale y"));
        scaleXBox.setPadding(new Insets(10, 10, 10, 15));
        scaleXBox.setAlignment(Pos.CENTER_LEFT);
        scaleXBox.setMinHeight(30);
        scaleXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }
    private void setUpScaleYBox(){
        scaleYBox = new HBox(new Label("scale y"));
        scaleYBox.setPadding(new Insets(10, 10, 10, 15));
        scaleYBox.setAlignment(Pos.CENTER_LEFT);
        scaleYBox.setMinHeight(30);
        scaleYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    @Override
    public void setShapeName(String shapeName) {
        this.name = shapeName;
    }

    @Override
    public String getShapeName() {
        return name;
    }

    @Override
    public UUID getUUID() {
        return getID();
    }

    @Override
    public Pane getScaleXSection() {
        return scaleXBox;
    }

    @Override
    public Pane getScaleYSection() {
        return scaleYBox;
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
        thumbnail.getChildren().add(new Label(getShapeName()));
    }

    @Override
    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier) {
        thumbnail.getChildren().clear();
        thumbnail.setMinWidth(0.0);
        thumbnail.setPadding(new Insets(10));
        thumbnail.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");

        HBox.setHgrow(thumbnail, Priority.NEVER);
        thumbnail.getChildren().add(new Label(getShapeName()));

        System.out.println("recebi toPutIntoDragBoard: " + toPutIntoDragbord.get());

        thumbnail.setOnDragDetected(event -> {
            Dragboard db = thumbnail.startDragAndDrop(TransferMode.ANY);
            supplier.get();
            ClipboardContent content = new ClipboardContent();
            content.putString(toPutIntoDragbord.get());
            db.setContent(content);

            event.consume();
        });

        return thumbnail;
    }

    public class Information{
        private String id;
        private Double value;

        public Information(String id, Double value){
            this.id = id;
            this.value = value;
        }

        public Double getValue() {
            return value;
        }

        public Function<Double, Double> getConsumer(){
            return a -> {
                if(a == null){
                    return getValue();
                }else{
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

        public void setValue(Double value) {
            System.out.println("SET VALUE PARA ISTO");
            this.value = value;
        }
    }

}
