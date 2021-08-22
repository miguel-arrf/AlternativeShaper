package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorInput;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class NewCompositionShape implements CustomShape{

    private String name;
    private final UUID ID = UUID.randomUUID();

    private final Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();

    private final ArrayList<Information> compositionShapesXTranslation = new ArrayList<>();
    private final ArrayList<Information> compositionShapesYTranslation = new ArrayList<>();

    private final ArrayList<Information> basicShapesXTranslation = new ArrayList<>();
    private final ArrayList<Information> basicShapesYTranslation = new ArrayList<>();

    private final Orchestrator orchestrator;

    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;

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

        basicShapesXTranslation.forEach(information -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", information.getId());

            int position = basicShapesXTranslation.indexOf(information);

            jsonObject.put("translationX", basicShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", basicShapesYTranslation.get(position).getValue());

            array.add(jsonObject);
        });

        return array;
    }

    public JSONArray getCompositionShapesJSON(){
        JSONArray array = new JSONArray();

        compositionShapeMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Information information : compositionShapesXTranslation) {
                if(information.getId().equals(id))
                    position = compositionShapesXTranslation.indexOf(information);
            }

            jsonObject.put("id", compositionShape.getID());
            jsonObject.put("translationX",compositionShapesXTranslation.get(position));
            jsonObject.put("translationY", compositionShapesYTranslation.get(position));

            array.add(jsonObject);
        });

        return array;
    }

    public BasicShape addBasicShape(String basicShapesID){
        Information translationX = new Information(basicShapesID, 0.0);
        Information translationY = new Information(basicShapesID, 0.0);

        basicShapesXTranslation.add(translationX);
        basicShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfBasicShape(basicShapesID, translationX.getConsumer(), translationY.getConsumer());
    }

    public ArrayList<BasicShape> getBasicShapes(){
        ArrayList<BasicShape> basicShapes = new ArrayList<>();

        basicShapesXTranslation.forEach(information -> {
            int position = basicShapesXTranslation.indexOf(information);

            Information translationX = basicShapesXTranslation.get(position);
            Information translationY = basicShapesYTranslation.get(position);

            basicShapes.add(orchestrator.getCopyOfBasicShape(information.id, translationX.getConsumer(), translationY.getConsumer()));
        });


        return basicShapes;
    }

    public void getTeste(Pane toAdd){


        compositionShapeMap.forEach((newID, compositionShape) -> {
            Group addTo = new Group();



            int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Information translationX = compositionShapesXTranslation.get(position);
            Information translationY = compositionShapesYTranslation.get(position);

            //Adding basic shapes
            compositionShape.getBasicShapes().forEach(basicShape -> {
                double translateXBy = basicShape.getInitialTranslation().getX() * -1;
                double translateYBy = basicShape.getHeight() + basicShape.getInitialTranslation().getY() * -1;

                basicShape.setTranslateX(translationX.getValue() - translateXBy);
                basicShape.setTranslateY(translationY.getValue() - translateYBy);

                Pane rectangle = basicShape.getRectangle();
                /*rectangle.setOnMouseMoved(event -> {
                    System.out.println("fuck i'm here!! NO REC: " + rectangle);
                });

                rectangle.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
                    System.out.println("WOW, IS THIS WORKIIIING? RREEEC");
                });*/

                addTo.getChildren().add(rectangle);
            });
            toAdd.getChildren().add(addTo);

            compositionShape.getTeste(toAdd);

            Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth(), addTo.getLayoutBounds().getHeight());
            rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
            rectangle.setX(addTo.getLayoutBounds().getMinX());
            rectangle.setY(addTo.getLayoutBounds().getMinY());

            addTo.setOnMouseEntered(event -> {
                addTo.getChildren().add(rectangle);

            });

            addTo.setOnMouseExited(event -> {
                addTo.getChildren().remove(rectangle);
            });

            /*addTo.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
                System.out.println("WOW, IS THIS WORKIIIING?");
            });*/

        });

    }

    public Pane addNewCompositionShape(NewCompositionShape NewCompositionShape){
        String id = UUID.randomUUID().toString();

        compositionShapeMap.put(id, NewCompositionShape);

        compositionShapesXTranslation.add(new Information(id, 0.0));
        compositionShapesYTranslation.add(new Information(id, 0.0));

        Pane toAdd = new Pane();
        getTeste(toAdd);

        return toAdd;
    }

    private void setUpComponents(){
        setUpTranslationXBox();
        setUpTranslationYBox();
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

    public static class Information{
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
