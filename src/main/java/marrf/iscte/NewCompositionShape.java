package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.horizontalGrower;

public class NewCompositionShape implements CustomShape{

    private String name;
    private final UUID ID = UUID.randomUUID();

    private final Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();

    private final ArrayList<Information> compositionShapesXTranslation = new ArrayList<>();
    private final ArrayList<Information> compositionShapesYTranslation = new ArrayList<>();

    private final ArrayList<Information> basicShapesXTranslation = new ArrayList<>();
    private final ArrayList<Information> basicShapesYTranslation = new ArrayList<>();

    private final Orchestrator orchestrator;

    private Node selected;
    private Information selectedTranslationX;
    private Information selectedTranslationY;

    private Pane transformersBox;

    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;

    //Composition Shape Thumbnail
    private final VBox thumbnail = new VBox();

    public NewCompositionShape(Orchestrator orchestrator, Pane transformersBox){
        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        //setUpComponents();
    }

    public UUID getID() {
        return ID;
    }

    public JSONArray getBasicShapesJSON(){
        JSONArray array = new JSONArray();

        basicShapesXTranslation.forEach(information -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", information.getId().toString());

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

            jsonObject.put("id", compositionShape.getID().toString());
            jsonObject.put("translationX",compositionShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", compositionShapesYTranslation.get(position).getValue());

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

    public void getTeste(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY){

        compositionShapeMap.forEach((newID, compositionShape) -> {
            Group addTo = new Group();

            int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Information translationX = compositionShapesXTranslation.get(position);
            Information translationY = compositionShapesYTranslation.get(position);

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

            if(toAdd instanceof Pane){
                Pane toAddTemp = (Pane) toAdd;
                toAddTemp.getChildren().add(addTo);
            }else if(toAdd instanceof Group){
                Group toAddTemp = (Group) toAdd;
                toAddTemp.getChildren().add(addTo);
            }
            //toAdd.getChildren().add(addTo);

            if(addHover)
                System.out.println("first translation X: " + translationX);

            compositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY);

            if(addHover){
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

                    setUpComponents();
                    System.out.println("I've clicked on here. It should now be true!");

                    transformersBox.getChildren().clear();
                    transformersBox.getChildren().addAll(getTransformers());
                });
            }

        });

    }

/*
    public void getTesteVelho(Node toAdd, boolean addHover){

        compositionShapeMap.forEach((newID, compositionShape) -> {
            Group addTo = new Group();

            int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Information translationX = compositionShapesXTranslation.get(position);
            Information translationY = compositionShapesYTranslation.get(position);

            //Adding basic shapes
            compositionShape.getBasicShapes().forEach(basicShape -> {
                double translateXBy = basicShape.getInitialTranslation().getX() * -1;
                double translateYBy = basicShape.getHeight() + basicShape.getInitialTranslation().getY() * -1;

                //Este é o translation X e Y final porque já não temos mais profundidade de composition shapes, visto que já é uma basic shape.
                basicShape.setTranslateX(translationX.getValue() - translateXBy);
                basicShape.setTranslateY(translationY.getValue() - translateYBy);

                Pane rectangle = basicShape.getRectangle();

                addTo.getChildren().add(rectangle);
            });

            if(toAdd instanceof Pane){
                Pane toAddTemp = (Pane) toAdd;
                toAddTemp.getChildren().add(addTo);
            }else if(toAdd instanceof Group){
                Group toAddTemp = (Group) toAdd;
                toAddTemp.getChildren().add(addTo);
            }
            //toAdd.getChildren().add(addTo);

            if(addHover)
                System.out.println("first translation X: " + translationX);

            compositionShape.getTeste(addTo, false);

            if(addHover){
                Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth(), addTo.getLayoutBounds().getHeight());
                rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
                rectangle.setX(addTo.getLayoutBounds().getMinX());
                rectangle.setY(addTo.getLayoutBounds().getMinY());

                addTo.setOnMouseEntered(event -> addTo.getChildren().add(rectangle));

                addTo.setOnMouseExited(event -> addTo.getChildren().remove(rectangle));

                addTo.setOnMouseClicked(event -> {

                    selected = addTo;
                    selectedTranslationX = translationX;
                    selectedTranslationY = translationY;

                    setUpComponents();
                    System.out.println("I've clicked on here. It should now be true!");

                    transformersBox.getChildren().clear();
                    transformersBox.getChildren().addAll(getTransformers());
                });
            }

        });

    }
 */

    public Pane addNewCompositionShape(NewCompositionShape NewCompositionShape){
        String id = UUID.randomUUID().toString();

        compositionShapeMap.put(id, NewCompositionShape);

        compositionShapesXTranslation.add(new Information(id, 0.0));
        compositionShapesYTranslation.add(new Information(id, 0.0));

        Pane toAdd = new Pane();
        getTeste(toAdd, true, 0,0);

        return toAdd;
    }

    private void setUpComponents(){
        setUpTranslationXBox();
        setUpTranslationYBox();
    }

    public ArrayList<Node> getTransformers(){
        ArrayList<Node> toReturn = new ArrayList<>();
        toReturn.add(translationXBox);
        toReturn.add(translationYBox);

        return toReturn;
    }

    private void setUpTranslationXBox(){
        Label translationLabel = new Label("Translation X:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));

        double labelSize = TextUtils.computeTextWidth(translationLabel.getFont(), translationLabel.getText(), 0.0D) + 10;
        translationLabel.setPrefWidth(labelSize);
        translationLabel.setMinWidth(labelSize);
        translationLabel.setMaxWidth(labelSize);

        String truncatedValue = Double.toString(BigDecimal.valueOf(selectedTranslationX.getValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());

        TextField textField = new TextField(truncatedValue);
        textField.setPromptText(truncatedValue);
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setMinWidth(90);
        textField.setPrefWidth(90);
        textField.setAlignment(Pos.CENTER);


        Slider slider = getSlider(selectedTranslationX, aDouble -> {
            selected.setTranslateX(selected.getTranslateX() + aDouble);
        }, aDouble -> {
            Double truncatedDouble = BigDecimal.valueOf(aDouble).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));
        });

        textField.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                try {
                    if (Double.parseDouble(textField.getText()) < slider.getMin()) {
                        textField.setText(String.valueOf(slider.getMin()));
                    }else if(Double.parseDouble(textField.getText()) > slider.getMax()){
                        textField.setText(String.valueOf(slider.getMax()));
                    }

                    slider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText("0");
                    slider.setValue(0);
                }
            }
        });

        translationXBox = new HBox(translationLabel, slider,horizontalGrower(), textField);
        translationXBox.setPadding(new Insets(10, 10, 10, 15));
        translationXBox.setAlignment(Pos.CENTER_LEFT);
        translationXBox.setSpacing(10);
        translationXBox.setMinHeight(30);
        translationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }
    private void setUpTranslationYBox(){
        Label translationLabel = new Label("Translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));

        double labelSize = TextUtils.computeTextWidth(translationLabel.getFont(), translationLabel.getText(), 0.0D) + 10;
        translationLabel.setPrefWidth(labelSize);
        translationLabel.setMinWidth(labelSize);
        translationLabel.setMaxWidth(labelSize);

        String truncatedValue = Double.toString(BigDecimal.valueOf(selectedTranslationY.getValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());

        TextField textField = new TextField(truncatedValue);
        textField.setPromptText(truncatedValue);
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setMinWidth(90);
        textField.setPrefWidth(90);
        textField.setAlignment(Pos.CENTER);

        Slider slider = getSlider(selectedTranslationY, aDouble -> {
            selected.setTranslateY(selected.getTranslateY() + aDouble);
        }, aDouble -> {
            Double truncatedDouble = BigDecimal.valueOf(aDouble).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));
        });

        textField.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                try {
                    if (Double.parseDouble(textField.getText()) < slider.getMin()) {
                        textField.setText(String.valueOf(slider.getMin()));
                    }else if(Double.parseDouble(textField.getText()) > slider.getMax()){
                        textField.setText(String.valueOf(slider.getMax()));
                    }

                    slider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText("0");
                    slider.setValue(0);
                }
            }
        });

        translationYBox = new HBox(translationLabel, slider,horizontalGrower(), textField);
        translationYBox.setPadding(new Insets(10, 10, 10, 15));
        translationYBox.setAlignment(Pos.CENTER_LEFT);
        translationYBox.setMinHeight(40);
        translationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    private Slider getSlider(Information informationToUpdate, Consumer<Double> consumer, Consumer<Double> textFieldConsumer){
        Slider slider = new Slider();
        slider.setMax(Orchestrator.getSCALE() * Orchestrator.getNumberColumnsAndRows() / 2.0);
        slider.setMin(-Orchestrator.getSCALE() *  Orchestrator.getNumberColumnsAndRows() / 2.0);
        slider.setValue(informationToUpdate.getValue());
        slider.setMajorTickUnit(0.1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);


        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            informationToUpdate.setValue(newValue.doubleValue());
            consumer.accept(newValue.doubleValue() - oldValue.doubleValue());
            textFieldConsumer.accept(newValue.doubleValue());
        });

        return slider;
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
            this.value = value;
        }
    }




}
