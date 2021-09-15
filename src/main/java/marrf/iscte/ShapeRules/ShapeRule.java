package marrf.iscte.ShapeRules;

import marrf.iscte.BasicShape;
import marrf.iscte.NewCompositionShape;

import java.util.ArrayList;
import java.util.UUID;

public abstract class ShapeRule {

    private final UUID id = UUID.randomUUID();
    private String shapeRuleName;

    private final ArrayList<BasicShape> leftBasicShapes = new ArrayList<>();
    private final ArrayList<NewCompositionShape> leftCompositionShapes = new ArrayList<>();

    private final ArrayList<BasicShape> rightBasicShapes = new ArrayList<>();
    private final ArrayList<NewCompositionShape> rightCompositionShapes = new ArrayList<>();


    public ShapeRule(){

    }

    public ArrayList<BasicShape> getLeftBasicShapes() {
        return leftBasicShapes;
    }

    public ArrayList<NewCompositionShape> getLeftCompositionShapes() {
        return leftCompositionShapes;
    }

    public ArrayList<BasicShape> getRightBasicShapes() {
        return rightBasicShapes;
    }

    public ArrayList<NewCompositionShape> getRightCompositionShapes() {
        return rightCompositionShapes;
    }

    public void addBasicShapeToLeft(BasicShape basicShape){
        leftBasicShapes.add(basicShape);
    }

    public void addBasicShapeToRight(BasicShape basicShape){
        rightBasicShapes.add(basicShape);
    }

    public void addCompositionShapeToRight(NewCompositionShape newCompositionShape){
        rightCompositionShapes.add(newCompositionShape);
    }

    public void addCompositionShapeToLeft(NewCompositionShape newCompositionShape){
        leftCompositionShapes.add(newCompositionShape);
    }



}
