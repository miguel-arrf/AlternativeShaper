package marrf.iscte;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.UUID;

public class SmallGridCanvas {

    public final int SCALE = 40;
    public final int NUMBER_COLUMNS_AND_ROWS = 40;

    private double horizontalOffset = 0;
    private double verticalOffset = 0;

    public final Pane pane = new Pane();
    private final ArrayList<Double> initialHorizontalDrag = new ArrayList<>();
    private final ArrayList<Double> initialVerticalDrag = new ArrayList<>();

    private Circle circle;

    private ArrayList<BasicShape> addedBasicShapes = new ArrayList<>();
    private ArrayList<NewCompositionShape> addedNewCompositionShapes = new ArrayList<>();

    private NewCompositionShape compositionShape;

    public void setCompositionShape(NewCompositionShape compositionShape) {
        this.compositionShape = compositionShape;
    }

    public NewCompositionShape getCompositionShape() {
        return compositionShape;
    }

    private void redraw(){
        pane.getChildren().clear();

        int width = SCALE;

        for (int i = 0; i < NUMBER_COLUMNS_AND_ROWS; i++) {
            for (int j = 0; j < NUMBER_COLUMNS_AND_ROWS; j++) {
                Rectangle customRectangle = new Rectangle();
                customRectangle.setId("smallGridCanvas");
                customRectangle.setX(width*j);
                customRectangle.setY(width*i);
                customRectangle.setWidth(width);
                customRectangle.setHeight(width);
                customRectangle.setFill(null);
                customRectangle.setStroke(Color.web("#4F4F4F"));
                customRectangle.setStrokeWidth(2);
                pane.getChildren().add(customRectangle);
            }
        }

        circle = new Circle(5);
        circle.setId("smallGridCanvas");
        circle.setFill(Color.web("#6C696F"));
        circle.setCenterX(width*NUMBER_COLUMNS_AND_ROWS / 2.0);
        circle.setCenterY(width*NUMBER_COLUMNS_AND_ROWS / 2.0);

        pane.getChildren().add(circle);

    }

    public Pane getGrid(Pane parent){
        redraw();


        parent.widthProperty().addListener((observable, oldValue, newValue) -> {
            double xTranslation = (newValue.doubleValue() - NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            pane.setTranslateX(xTranslation);
        });

        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            double yTranslation = (newValue.doubleValue() - NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            pane.setTranslateY(yTranslation);
        });

        parent.setOnMousePressed(event -> {
            horizontalOffset = event.getX();
            verticalOffset = event.getY();

            initialVerticalDrag.clear();
            initialHorizontalDrag.clear();

            for (int i = 0; i < pane.getChildren().size(); i++) {
                initialHorizontalDrag.add(i, pane.getChildren().get(i).getTranslateX());
                initialVerticalDrag.add(i, pane.getChildren().get(i).getTranslateY());
            }

        });

        parent.setOnMouseDragged(event -> {
            boolean notExceedingHorizontally = Math.abs(initialHorizontalDrag.get(1) + event.getX() - horizontalOffset) <= (NUMBER_COLUMNS_AND_ROWS * SCALE - parent.getWidth())/2;
            boolean notExceedingVertically = Math.abs(initialVerticalDrag.get(1) + event.getY() - verticalOffset) <= (NUMBER_COLUMNS_AND_ROWS * SCALE - parent.getHeight())/2;

            if(notExceedingHorizontally){
                for (int i = 0; i < pane.getChildren().size(); i++) {
                    pane.getChildren().get(i).setTranslateX(initialHorizontalDrag.get(i) + event.getX() - horizontalOffset);
                }
            }

            if(notExceedingVertically){
                for (int i = 0; i < pane.getChildren().size(); i++) {
                    pane.getChildren().get(i).setTranslateY(initialVerticalDrag.get(i) + event.getY() - verticalOffset);
                }
            }

        });


        return pane;
    }

    public void addShape(BasicShape basicShape, UUID uuid){

        double translateXBy = basicShape.getInitialTranslation().getX() * -1;
        double translateYBy = basicShape.getHeight() * basicShape.scaleYProperty().get() + basicShape.getInitialTranslation().getY() * -1;

        //translateXBy = 0;
        //translateYBy = basicShape.getHeight();

        basicShape.setTranslateX(circle.getCenterX() + circle.getTranslateX() - translateXBy);
        basicShape.setTranslateY(circle.getCenterY() + circle.getTranslateY() - translateYBy);

        Node toAdd = basicShape.getExtendedRectangle();

        //basicShape.addTranslationX(basicShape.getInitialTranslation().getX());
        //basicShape.translateXProperty.setValue(basicShape.getInitialTranslation().getX());
        //basicShape.writeTranslateX.apply(basicShape.getInitialTranslation().getX());

        pane.getChildren().add(toAdd);

        toAdd.setId("shape:" + uuid.toString());

        addedBasicShapes.add(basicShape);
    }

    public void removeBasicShapeFromList(BasicShape toRemove){
        addedBasicShapes.remove(toRemove);
    }

    public void removeCompositionShapeFromList(NewCompositionShape toRemove){
        addedNewCompositionShapes.remove(toRemove);
    }

    public void removeShapeWithID(UUID id){
        pane.getChildren().removeIf( p -> {
            if(p.getId().contains(id.toString())){
                return true;
            }else{
                return false;
            }
        });

    }

    public void addGroup(Pane basicShape, NewCompositionShape compositionShape) {
        basicShape.setTranslateX(basicShape.getTranslateX() + circle.getCenterX() + circle.getTranslateX());
        basicShape.setTranslateY(basicShape.getTranslateY() + circle.getCenterY() + circle.getTranslateY());
        basicShape.setId("shape");
        pane.getChildren().add(basicShape);

        addedNewCompositionShapes.add(compositionShape);
    }




    public void addArrow(Arrow arrow){

        arrow.setStartX(circle.getCenterX());
        arrow.setStartY(circle.getCenterY());

        arrow.setEndX(circle.getCenterX());
        arrow.setEndY(circle.getCenterY());

        pane.getChildren().add(arrow);
    }

    public void clearEverything(){
        redraw();
        addedBasicShapes.clear();
        //compositionShapes.clear();
    }

    public void addTranslation(double x, double y){
        pane.getChildren().forEach( children -> {
            if(children.getId() != null && children.getId().contains("shape")){
                children.setTranslateX(children.getTranslateX() + x);
                children.setTranslateY(children.getTranslateY() + y);

            }
        });
    }

}
