package marrf.iscte;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CompositionShape implements CustomShape{

    //Others
    private final int SCALE = 40;
    private final int NUMBER_COLUMNS_AND_ROWS = 40;

    //This Composition Shape
    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();
    private final Map<String, CompositionShape> compositionShapesMap = new HashMap<>();

    private final Map<DoubleProperty, String> translateXMapComposition = new HashMap<>();
    private final Map<DoubleProperty, String> translateYMapComposition = new HashMap<>();
    private final Map<DoubleProperty, String> scaleXMapComposition = new HashMap<>();
    private final Map<DoubleProperty, String> scaleYMapComposition = new HashMap<>();

    private final Map<DoubleProperty, BasicShape> translateXMapBasic = new HashMap<>();
    private final Map<DoubleProperty, BasicShape> translateYMapBasic = new HashMap<>();
    private final Map<DoubleProperty, BasicShape> scaleXMapBasic = new HashMap<>();
    private final Map<DoubleProperty, BasicShape> scaleYMapBasic = new HashMap<>();

    private String name;
    private final UUID uuid = UUID.randomUUID();

    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;
    private HBox scaleXBox;
    private HBox scaleYBox;

    //Composition Shape Thumbnail
    private final VBox thumbnail = new VBox();

    public void addBasicShape(BasicShape basicShapeToAdd){
        //BasicShape copy = getCopyWithBindWidthAndHeightFrom(basicShapeToAdd);
        basicShapes.add(basicShapeToAdd);

        translateXMapBasic.put(new SimpleDoubleProperty(0.0), basicShapeToAdd);
        translateYMapBasic.put(new SimpleDoubleProperty(0.0), basicShapeToAdd);
        scaleXMapBasic.put(new SimpleDoubleProperty(0.0), basicShapeToAdd);
        scaleYMapBasic.put(new SimpleDoubleProperty(0.0), basicShapeToAdd);
        System.out.println("Adicionei basic shape: " + basicShapeToAdd + ", a: " + this);
        /*BasicShape copy = getCopyWithBindWidthAndHeightFrom(basicShapeToAdd);
        basicShapes.add(copy);

        translateXMapBasic.put(new SimpleDoubleProperty(0.0), copy);
        translateYMapBasic.put(new SimpleDoubleProperty(0.0), copy);
        scaleXMapBasic.put(new SimpleDoubleProperty(0.0), copy);
        scaleYMapBasic.put(new SimpleDoubleProperty(0.0), copy);*/
    }

    public void addCompositionShape(CompositionShape compositionShape){
        String uuid = UUID.randomUUID().toString();
        compositionShapesMap.put(uuid, compositionShape);

        translateXMapComposition.put(new SimpleDoubleProperty(0.0), uuid);
        translateYMapComposition.put(new SimpleDoubleProperty(0.0), uuid);
        scaleXMapComposition.put(new SimpleDoubleProperty(0.0), uuid);
        scaleYMapComposition.put(new SimpleDoubleProperty(0.0), uuid);
    }

    private ArrayList<BasicShape> getFromParent(ArrayList<BasicShape> toAddTo, CompositionShape compositionShapeParent){
        //toAddTo.addAll(compositionShapeParent.basicShapes);
        toAddTo.addAll(compositionShapeParent.basicShapes.stream().map(this::getCopyWithBindWidthAndHeightFrom).collect(Collectors.toList()));

        compositionShapeParent.compositionShapesMap.forEach((id, compositionShape) -> {
            getFromParent(toAddTo, compositionShape);
            //toAddTo.addAll(compositionShape.getBasicShapes().stream().map(compositionShape::getCopyWithBindWidthAndHeightFrom).collect(Collectors.toList()));
            //getFromParent(toAddTo, compositionShape)
            //compositionShape.compositionShapesMap.forEach((id1, compositionShape1) -> toAddTo.addAll(getFromParent(toAddTo, compositionShape1)));
        });

       return toAddTo;
    }

    private ArrayList<BasicShape> getGraphicalRepresentation(){
        ArrayList<BasicShape> toReturn = new ArrayList<>();
        return getFromParent(toReturn, this);
    }

    public Pane getGraphicalRepresentationPane(){
        Pane toReturn = new Pane();

        //There are copies of the original shapes.
        getGraphicalRepresentation().forEach(basicShape -> toReturn.getChildren().add(basicShape.getRectangle()));

        return toReturn;
    }

    public Pane getGraphicalRepresentPaneOfCompositionShape(CompositionShape compositionShapeToGetGraphicalRepresentation){
        Pane toReturn = new Pane();

        ArrayList<BasicShape> arrayList = new ArrayList<>();
        getFromParent(arrayList, compositionShapeToGetGraphicalRepresentation);

        arrayList.forEach(basicShape -> toReturn.getChildren().add(basicShape.getRectangle()));

        return toReturn;
    }

    public Pane getGraphicalRepresentationPaneWithoutBasicShape(){
        Pane toReturn = new Pane();

        //There are copies of the original shapes.
        getGraphicalRepresentation().forEach(basicShape -> toReturn.getChildren().add(basicShape.getRectangle()));

        //The problem is:
        /*
        * Since we are getting copies, now we can't be removing copies... that's why he have duplicates.
        * We need to have the getGraphicalRepresentation where we 'save' the basic shapes from this composition that were copied from...
        * to them remove them xD
        * */
        basicShapes.forEach(basicShape -> toReturn.getChildren().remove(basicShape.getRectangle()));

        return toReturn;
    }


    public BasicShape getCopyWithBindWidthAndHeightFrom(BasicShape originalBasicShape){
        BasicShape basicShape = new BasicShape();
        basicShape.setFill(originalBasicShape.getFill());
        basicShape.setWidth(originalBasicShape.getWidth());
        basicShape.setHeight(originalBasicShape.getHeight());
        basicShape.widthProperty().bind(originalBasicShape.widthProperty().multiply(originalBasicShape.scaleXProperty()));
        basicShape.heightProperty().bind(originalBasicShape.heightProperty().multiply(originalBasicShape.scaleYProperty()));
        basicShape.fillProperty().bind(originalBasicShape.fillProperty());

        basicShape.setTranslateY(originalBasicShape.getTranslateY());
        basicShape.setTranslateX(originalBasicShape.getTranslateX());

        basicShape.setTranslateOffsetProperty(originalBasicShape.getXPropertyProperty(), originalBasicShape.getYPropertyProperty());

        return basicShape;
    }

    public ArrayList<BasicShape> getBasicShapes() {
        return basicShapes;
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
        return uuid;
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

    public void redrawThumbnail(){
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


}
