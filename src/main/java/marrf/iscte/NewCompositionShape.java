package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.NUMBER_COLUMNS_AND_ROWS;
import static marrf.iscte.App.SCALE;
import static marrf.iscte.BasicShape.colorToRGBString;
import static marrf.iscte.BasicShape.getRelativeLuminance;
import static marrf.iscte.ParametricCompositionShape.Information;

public class NewCompositionShape implements CustomShape, Serializable {

    private final Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();
    public final ArrayList<Information<Double>> compositionShapesXTranslation = new ArrayList<>();
    public final ArrayList<Information<Double>> compositionShapesYTranslation = new ArrayList<>();
    private final ArrayList<Information<Double>> basicShapesXTranslation = new ArrayList<>();
    private final ArrayList<Information<Double>> basicShapesYTranslation = new ArrayList<>();
    private final Orchestrator orchestrator;
    //Composition Shape Thumbnail
    private final HBox thumbnail = new HBox();
    private String name = "COMPLEX_DEFAULT";
    private final UUID ID;
    private Node selected;
    private Information<Double> selectedTranslationX;
    private Information<Double> selectedTranslationY;
    private Pane transformersBox;
    //Modifiers boxes
    private HBox translationXBox;
    private HBox translationYBox;
    //Deletion handlers
    private Function<String, Double> proceedWhenDeletingFromThumbnail;
    private Function<String, Double> proceedToRedrawWhenDeleting;
    private Function<Pane, Double> proceedWhenDeleting;

    private boolean isFigureVariable = false;

    public boolean isFigureVariable() {
        return isFigureVariable;
    }

    public Map<String, NewCompositionShape> getCompositionShapeMap() {
        return compositionShapeMap;
    }

    public NewCompositionShape getCopy(){
        NewCompositionShape toReturn = new NewCompositionShape(false, orchestrator, transformersBox, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);

        toReturn.isFigureVariable = toReturn.isFigureVariable;
        toReturn.name = name;
        toReturn.compositionShapeMap.putAll(compositionShapeMap);

        compositionShapesXTranslation.forEach(information -> {
            toReturn.compositionShapesXTranslation.add(new Information(information.getId(), information.getValue()));
        });

        compositionShapesYTranslation.forEach(information -> {
            toReturn.compositionShapesYTranslation.add(new Information(information.getId(), information.getValue()));
        });

        basicShapesXTranslation.forEach(information -> {
            toReturn.basicShapesXTranslation.add(new Information(information.getId(), information.getValue()));
        });

        basicShapesYTranslation.forEach(information -> {
            toReturn.basicShapesYTranslation.add(new Information(information.getId(), information.getValue()));
            System.out.println("copiei!");
        });


        return toReturn;
    }

    public JSONObject getJSONObject(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", getID().toString());
        jsonObject.put("name", getShapeName());
        jsonObject.put("basicShapes", getBasicShapesJSON());
        jsonObject.put("compositionShapes", getCompositionShapesJSON());

        return jsonObject;
    }

    public String getPrologRepresentation(boolean withFinalDot, boolean justInner){
        StringBuilder toReturn = new StringBuilder();

        if(!justInner){
            toReturn.append("shapeComposition(").append(getShapeName()).append("[");
        }

        basicShapesXTranslation.forEach(information -> {
            Information<Double> translationY = basicShapesYTranslation.stream().filter(inf -> inf.getId().equals(information.getId())).findFirst().get();

            toReturn.append("s(").append(orchestrator.getBasicShapeNameFromID(information.getId()));
            toReturn.append(",[1,0,0,0,1,0,").append(information.getValue()/SCALE).append(",").append(translationY.getValue()/SCALE).append(",1]");

            if(basicShapesXTranslation.lastIndexOf(information) == basicShapesXTranslation.size() - 1 || compositionShapeMap.size() == 0){
                toReturn.append(")");
            }else{
                toReturn.append("),");
            }

        });

        AtomicInteger added = new AtomicInteger();

        compositionShapeMap.forEach((randomID, newCompositionShape) -> {

            var translationX = compositionShapesXTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();
            var translationY = compositionShapesYTranslation.stream().filter(translation -> translation.getId().equals(randomID)).findFirst().get();

            toReturn.append("s(").append(newCompositionShape.getShapeName());
            toReturn.append(",[1,0,0,0,1,0,").append(translationX.getValue()).append(",").append(translationY.getValue()).append(",1]");


            if(added.get() == compositionShapesXTranslation.size() - 1){
                toReturn.append(")");
            }else{
                toReturn.append("),");
            }

            added.getAndIncrement();


        });



        if(!justInner){
            toReturn.append(withFinalDot ? "])." : "])");
        }


        return toReturn.toString();
    }

    public void setTransformersBox(Pane transformersBox) {
        this.transformersBox = transformersBox;
    }

    public NewCompositionShape(boolean isFigureVariable, Orchestrator orchestrator, Pane transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;

        ID = UUID.randomUUID();
        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;

        this.isFigureVariable = isFigureVariable;
        //setUpComponents();
    }

    public NewCompositionShape(boolean isFigureVariable, String name, Orchestrator orchestrator, Pane transformersBox, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;
        this.name = name;
        ID = UUID.randomUUID();
        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;

        this.isFigureVariable = isFigureVariable;
        //setUpComponents();
    }

    public NewCompositionShape(boolean isFigureVariable, Orchestrator orchestrator, Pane transformersBox, String name, UUID id, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        this.name = name;
        this.ID = id;

        this.orchestrator = orchestrator;
        this.transformersBox = transformersBox;
        //setUpComponents();
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;

        this.isFigureVariable = isFigureVariable;
    }

    public UUID getID() {
        return ID;
    }

    public JSONArray getBasicShapesJSON() {
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

    public JSONArray getCompositionShapesJSON() {
        JSONArray array = new JSONArray();

        compositionShapeMap.forEach((id, compositionShape) -> {
            JSONObject jsonObject = new JSONObject();

            int position = 0;
            for (Information information : compositionShapesXTranslation) {
                if (information.getId().equals(id))
                    position = compositionShapesXTranslation.indexOf(information);
            }

            jsonObject.put("id", compositionShape.getID().toString());
            jsonObject.put("translationX", compositionShapesXTranslation.get(position).getValue());
            jsonObject.put("translationY", compositionShapesYTranslation.get(position).getValue());

            array.add(jsonObject);
        });

        return array;
    }

    public BasicShape addBasicShape(String basicShapesID) {
        Information translationX = new Information(basicShapesID, 0.0);
        Information translationY = new Information(basicShapesID, 0.0);

        basicShapesXTranslation.add(translationX);
        basicShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfBasicShape(basicShapesID, translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeleting(basicShapesID));
    }

    public BasicShape addBasicShapeWithTranslation(String basicShapesID, double xTranslation, double yTranslation) {
        Information translationX = new Information(basicShapesID, xTranslation);
        Information translationY = new Information(basicShapesID, yTranslation);

        basicShapesXTranslation.add(translationX);
        basicShapesYTranslation.add(translationY);

        return orchestrator.getCopyOfBasicShape(basicShapesID, translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeleting(basicShapesID));
    }

    public void deleteBasicShape(String uuidToRemove) {

        if (getBasicShapesUUIDList().stream().anyMatch(p -> p.equals(uuidToRemove))) {
            Information basicShapeXTranslationToRemove = basicShapesXTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();
            Information basicShapeYTranslationToRemove = basicShapesYTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();

            basicShapesXTranslation.remove(basicShapeXTranslationToRemove);
            basicShapesYTranslation.remove(basicShapeYTranslationToRemove);
        }

    }

    public void deleteCompositionShape(String uuidToRemove){
        //TODO If we delete a basic shape from a composition shape that
        // only had it, and that composition shape is here, we should delete
        // the parameters that were defined.
        if (getCompositionShapesUUIDList().stream().anyMatch(p -> p.equals(uuidToRemove))) {

            ArrayList<String> toRemove = new ArrayList<>();

            compositionShapeMap.forEach((id, compositionShape) -> {
                if(compositionShape.getID().toString().equals(uuidToRemove)){
                    toRemove.add(id);
                }
            });

            toRemove.forEach(compositionShapeMap::remove);

            compositionShapesXTranslation.removeIf(p -> {
               if(toRemove.stream().anyMatch(a -> a.equals(p.getId()))){
                   return true;
               }
               return false;
            });

            compositionShapesYTranslation.removeIf(p -> {
                if(toRemove.stream().anyMatch(a -> a.equals(p.getId()))){
                    return true;
                }
                return false;
            });

            //Information compositionShapesXTranslationToRemove = compositionShapesXTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();
            //Information compositionShapesYTranslationToRemove = compositionShapesYTranslation.stream().filter(p -> p.getId().equals(uuidToRemove)).findFirst().get();

            //compositionShapesXTranslation.remove(compositionShapesXTranslationToRemove);
            //compositionShapesYTranslation.remove(compositionShapesYTranslationToRemove);

        }
    }

    public ArrayList<String> getBasicShapesUUIDList() {
        ArrayList<String> toReturn = new ArrayList<>();

        basicShapesXTranslation.forEach(information -> {
            toReturn.add(information.getId());
        });

        return toReturn;
    }

    public ArrayList<String> getCompositionShapesUUIDList() {
        ArrayList<String> toReturn = new ArrayList<>();

        compositionShapeMap.forEach((a,b) -> {
            toReturn.add(b.getID().toString());
        });

        return toReturn;
    }

    public ArrayList<BasicShape> getBasicShapes() {
        ArrayList<BasicShape> basicShapes = new ArrayList<>();

        basicShapesXTranslation.forEach(information -> {
            int position = basicShapesXTranslation.indexOf(information);

            Information<Double> translationX = basicShapesXTranslation.get(position);
            Information<Double> translationY = basicShapesYTranslation.get(position);

            //System.out.println("no get: " + "translationX: " + translationX.getValue() + ", translationY: " + translationY.getValue());

            basicShapes.add(orchestrator.getCopyOfBasicShape(information.getId(), translationX.getConsumer(), translationY.getConsumer(), getProceedWhenDeleting(translationY.getId())));
        });


        return basicShapes;
    }

    private Function<Pane, Double> getProceedWhenDeleting(String basicShapeID) {
        return a -> {
            Information<Double> xTranslationToRemove = basicShapesXTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            Information<Double> yTranslationToRemove = basicShapesYTranslation.stream().filter(p -> p.getId().equals(basicShapeID)).findFirst().get();
            basicShapesXTranslation.remove(xTranslationToRemove);
            basicShapesYTranslation.remove(yTranslationToRemove);
            System.err.println("VIM AQUI PARA APAGAR!");
            proceedToRedrawWhenDeleting.apply(null);
            return null;
        };
    }

    /*public NewCompositionShape getPaneWithBasicAndCompositionShapes(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY, Pane transformersBox){

        NewCompositionShape teste = new NewCompositionShape(false, orchestrator, transformersBox, a -> 0.0, a -> 0.0);

        teste.addNewCompositionShape(this);
        teste.getTeste(toAdd, true, 0,0);

        return teste;
    }

    public void setProceedToRedrawWhenDeleting(Function<String, Double> proceedToRedrawWhenDeleting) {
        this.proceedToRedrawWhenDeleting = proceedToRedrawWhenDeleting;
    }

    public void setProceedWhenDeletingFromThumbnail(Function<String, Double> proceedWhenDeletingFromThumbnail) {
        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
    }

    public void setProceedWhenDeleting(Function<Pane, Double> proceedWhenDeleting) {
        this.proceedWhenDeleting = proceedWhenDeleting;
    }*/

    public void getTeste(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY) {

        compositionShapeMap.forEach((newID, compositionShape) -> {
            Group addTo = new Group();

            int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Information<Double> translationX = compositionShapesXTranslation.get(position);
            Information<Double> translationY = compositionShapesYTranslation.get(position);

            //Adding basic shapes
            compositionShape.getBasicShapes().forEach(basicShape -> {
                double translateXBy = basicShape.getInitialTranslation().getX();
                double translateYBy = basicShape.getHeight() + basicShape.getInitialTranslation().getY() * -1;

                //Este ?? o translation X e Y final porque j?? n??o temos mais profundidade de composition shapes, visto que j?? ?? uma basic shape.
                basicShape.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
                basicShape.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

                Pane rectangle = basicShape.getRectangle();

                addTo.getChildren().add(rectangle);
            });

            if (toAdd instanceof Pane) {
                Pane toAddTemp = (Pane) toAdd;
                toAddTemp.getChildren().add(addTo);
            } else if (toAdd instanceof Group) {
                Group toAddTemp = (Group) toAdd;
                toAddTemp.getChildren().add(addTo);
            }


            compositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY);

            if (addHover) {
                Rectangle rectangle = new Rectangle(addTo.getLayoutBounds().getWidth() + 20, addTo.getLayoutBounds().getHeight() + 20);
                rectangle.setArcWidth(10);
                rectangle.setArcHeight(10);
                rectangle.setFill(Color.web("rgba(255,255,255,0.2)"));
                rectangle.setX(addTo.getLayoutBounds().getMinX() - 10);
                rectangle.setY(addTo.getLayoutBounds().getMinY() - 10);
                rectangle.setId("backgroundSize");


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
                    ////System.out.println("I've clicked on here. It should now be true!");

                    transformersBox.getChildren().clear();
                    transformersBox.getChildren().addAll(getTransformers());
                });

                ContextMenu contextMenu = new ContextMenu();
                contextMenu.setId("betterMenuItem");

                MenuItem menuItem = new MenuItem("Delete");
                menuItem.setStyle("-fx-text-fill: red");
                contextMenu.getItems().add(menuItem);

                menuItem.setOnAction(actionEvent -> {
                    ((Pane) addTo.getParent()).getChildren().remove(addTo);
                    compositionShapeMap.remove(newID);
                    Information xTranslationToRemove = compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get();
                    Information yTranslationToRemove = compositionShapesYTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get();
                    compositionShapesXTranslation.remove(xTranslationToRemove);
                    compositionShapesYTranslation.remove(yTranslationToRemove);

                    //proceedWhenDeleting.apply(null);
                });

                addTo.setOnContextMenuRequested(contextMenuEvent -> {
                    contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                });

            }

        });

    }

    public void getTesteForSpecific(Node toAdd, boolean addHover, double upperTranslationX, double upperTranslationY, String newID, NewCompositionShape compositionShape) {
            toAdd.setPickOnBounds(false);
            Group addTo = new Group();

            int position = compositionShapesXTranslation.indexOf(compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get());

            Information<Double> translationX = compositionShapesXTranslation.get(position);
            Information<Double> translationY = compositionShapesYTranslation.get(position);

            //Adding basic shapes
            compositionShape.getBasicShapes().forEach(basicShape -> {
                double translateXBy = basicShape.getInitialTranslation().getX();
                double translateYBy = basicShape.getHeight() + basicShape.getInitialTranslation().getY() * -1;

                //Este ?? o translation X e Y final porque j?? n??o temos mais profundidade de composition shapes, visto que j?? ?? uma basic shape.
                basicShape.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
                basicShape.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

                BasicShape copy = new BasicShape(basicShape.getWidth(), basicShape.getHeight(), basicShape.getFill(), basicShape.getSelectedImage(), basicShape.getWriteTranslateX(), basicShape.getWriteTranslateY(), basicShape.getProceedWhenDeleting());
                copy.setTranslateX(translationX.getValue() + translateXBy + upperTranslationX);
                copy.setTranslateY(translationY.getValue() - translateYBy + upperTranslationY);

                Pane rectangle = copy.getRectangle();

                addTo.getChildren().add(rectangle);
            });

            if (toAdd instanceof Pane) {
                Pane toAddTemp = (Pane) toAdd;
                toAddTemp.getChildren().add(addTo);
            } else if (toAdd instanceof Group) {
                Group toAddTemp = (Group) toAdd;
                toAddTemp.getChildren().add(addTo);
            }


            compositionShape.getTeste(addTo, false, translationX.getValue() + upperTranslationX, translationY.getValue() + upperTranslationY);

            if (addHover) {
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
                    //System.out.println("I've clicked on here. It should now be true!");

                    transformersBox.getChildren().clear();
                    transformersBox.getChildren().addAll(getTransformers());
                });

                ContextMenu contextMenu = new ContextMenu();
                contextMenu.setId("betterMenuItem");

                MenuItem menuItem = new MenuItem("Delete");
                menuItem.setStyle("-fx-text-fill: red");
                contextMenu.getItems().add(menuItem);

                menuItem.setOnAction(actionEvent -> {
                    ((Pane) addTo.getParent()).getChildren().remove(addTo);
                    compositionShapeMap.remove(newID);
                    Information<Double> xTranslationToRemove = compositionShapesXTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get();
                    Information<Double> yTranslationToRemove = compositionShapesYTranslation.stream().filter(p -> p.getId().equals(newID)).findFirst().get();
                    compositionShapesXTranslation.remove(xTranslationToRemove);
                    compositionShapesYTranslation.remove(yTranslationToRemove);

                });

                addTo.setOnContextMenuRequested(contextMenuEvent -> {
                    contextMenu.show(addTo, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                });

            }

    }

    public static double getMinimumTranslationX(Node node, double minimumValue){
        if(node instanceof Group){
            Group group = (Group) node;
            for(Node groupNode: group.getChildren()){
                if(!(groupNode instanceof Group)){
                    if(groupNode.getTranslateX() < minimumValue){
                        minimumValue = groupNode.getTranslateX();
                    }
                }else{
                    minimumValue = getMinimumTranslationX(groupNode, minimumValue);
                }
            }
        }

        return minimumValue;
    }


    ArrayList<Double> getMaximumBasicShapeYTranslation(){
        double maximumValue;
        if(basicShapesYTranslation.size() != 0){
            maximumValue = basicShapesYTranslation.get(0).getValue();
        }else {
            return new ArrayList<>();
        }
        ArrayList<Double> lowerValues = new ArrayList<>();
        lowerValues.add(maximumValue);

        double finalMaximumValue = maximumValue;
        basicShapesYTranslation.forEach(basicShape -> {
            BasicShape copy = orchestrator.getCopyOfBasicShape(basicShape.getId(), new Information("1", 0.0).getConsumer(), new Information("1", 0.0).getConsumer(), getProceedWhenDeleting(basicShape.getId()));;

            //if(basicShape.getValue() < finalMaximumValue){
                lowerValues.add(basicShape.getValue() );
            //}
        });
        return  lowerValues;
    }

    private double getMaximumTranslationY_inner_forCompositions(ArrayList<Information<Double>> compositionShapesY, double previousTranslation){
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesY.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationY = information.getValue();
            NewCompositionShape shape = compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMaximumBasicShapeYTranslation();
            tempValues.forEach(p -> values.add(p + translationY + previousTranslation));

            values.sort(Collections.reverseOrder());
            if(values.size() != 0){
                toReturn.add(values.get(0));
            }

            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMaximumTranslationY_inner_forCompositions(shape.compositionShapesYTranslation, translationY));

        });
        toReturn.sort(Comparator.reverseOrder());
        //Collections.sort(toReturn);

        if(toReturn.size() == 0){
            return 0;
        }else{
            return toReturn.get(0);
        }
    }

    private ArrayList<Double> getMaximumTranslationY_inner(){
        if(compositionShapesYTranslation.size() == 0 && basicShapesYTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMaximumTranslationY_inner_forCompositions(compositionShapesYTranslation, 0));

        basicShapesYTranslation.forEach(basicShape -> {
            System.out.println("value: " + basicShape.getValue());
            BasicShape copy = orchestrator.getCopyOfBasicShape(basicShape.getId(), new Information("1", 0.0).getConsumer(), new Information("1", 0.0).getConsumer(), getProceedWhenDeleting(basicShape.getId()));;

            maximumValues.add(basicShape.getValue() );
        });


        return maximumValues;
    }

    public double getMaximumTranslationY(){
        ArrayList<Double> minimumValues = getMaximumTranslationY_inner();

        if(minimumValues.size() == 0){
            return 0.0;
        }else{
            minimumValues.sort(Comparator.reverseOrder());
            return minimumValues.get(0);
        }
    }

    private ArrayList<Double> getMininumTranslationY(){
        double mininumValue;
        if(compositionShapesYTranslation.size() != 0){
            mininumValue = compositionShapesYTranslation.get(0).getValue();
        }else if(basicShapesYTranslation.size() != 0){
            mininumValue = basicShapesYTranslation.get(0).getValue();
        }else {
            return new ArrayList<>();
        }

        ArrayList<Double> lowerValues = new ArrayList<>();
        lowerValues.add(mininumValue);

        double finalMininumValue = mininumValue;
        compositionShapesYTranslation.stream().filter(p -> p.getValue() > finalMininumValue).forEach(p -> lowerValues.add(p.getValue()));
        basicShapesYTranslation.stream().filter(p -> p.getValue() > finalMininumValue).forEach(p -> lowerValues.add(p.getValue()));
        compositionShapeMap.forEach((id, compositionShape) -> lowerValues.addAll(compositionShape.getMininumTranslationY()));

        return lowerValues;
    }

    public double getMinimumTranslationY(){
       ArrayList<Double> minimumValues = getMininumTranslationY();

       if(minimumValues.size() == 0){
           return 0.0;
       }else{
           minimumValues.sort(Collections.reverseOrder());
           return minimumValues.get(0);
       }

    }



    private double getMinimumTranslationY_inner_forCompositions(ArrayList<Information<Double>> compositionShapesY, double previousTranslation){
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesY.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationY = information.getValue();
            NewCompositionShape shape = compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMinimumBasicShapeYTranslation();
            tempValues.forEach(p -> {
                values.add(p + translationY + previousTranslation);
                System.out.println("---> TRANSLATIONY: " + translationY + ", p: " + p);

            });
            Collections.sort(values);
            toReturn.add(values.size() == 0 ? 0 : values.get(0));
            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMinimumTranslationY_inner_forCompositions(shape.compositionShapesYTranslation, translationY));

        });
        Collections.sort(toReturn);
        if(toReturn.size() == 0){
            return 0;
        }else{
            return toReturn.get(0);
        }
    }


    private ArrayList<Double> getMinimumTranslationY_inner(){
        if(compositionShapesYTranslation.size() == 0 && basicShapesYTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMinimumTranslationY_inner_forCompositions(compositionShapesYTranslation, 0));

        basicShapesYTranslation.forEach(basicShape -> {
            BasicShape copy = orchestrator.getCopyOfBasicShape(basicShape.getId(), new Information("1", 0.0).getConsumer(), new Information("1", 0.0).getConsumer(), getProceedWhenDeleting(basicShape.getId()));;
            maximumValues.add(basicShape.getValue() - copy.getHeight() );
        });


        return maximumValues;
    }


    public double getMinimumTranslationY_new(){
        ArrayList<Double> minimumValues = getMinimumTranslationY_inner();

        if(minimumValues.size() == 0){
            return 0.0;
        }else{
            Collections.sort(minimumValues);
            return minimumValues.get(0);
        }
    }






    /*
    private ArrayList<Double> getMinimumTranslationX_inner(){
        double mininumValue;
        if(compositionShapesXTranslation.size() != 0){
            mininumValue = compositionShapesXTranslation.get(0).value;
        }else if(basicShapesXTranslation.size() != 0){
            mininumValue = basicShapesXTranslation.get(0).value;
        }else {
            return new ArrayList<>();
        }

        ArrayList<Double> lowerValues = new ArrayList<>();
        lowerValues.add(mininumValue);

        double finalMininumValue = mininumValue;
        compositionShapesXTranslation.stream().filter(p -> p.value < finalMininumValue).forEach(p -> lowerValues.add(p.value));
        basicShapesXTranslation.stream().filter(p -> p.value < finalMininumValue).forEach(p -> lowerValues.add(p.value));
        compositionShapeMap.forEach((id, compositionShape) -> lowerValues.addAll(compositionShape.getMinimumTranslationX_inner()));

        return lowerValues;
    }*/

    private double getMinimumTranslationX_inner_forCompositions(ArrayList<Information<Double>> compositionShapesX, double previousTranslation){
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesX.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationX = information.getValue();
            NewCompositionShape shape = compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMinimumBasicShapeXTranslation();
            tempValues.forEach(p -> {
                values.add(p + translationX + previousTranslation);
                System.out.println("---> TRANSLATIONX: " + translationX + ", p: " + p);

            });

            Collections.sort(values);
            toReturn.add(values.size() == 0 ? 0 : values.get(0));
            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMinimumTranslationX_inner_forCompositions(shape.compositionShapesXTranslation, translationX));

        });
        Collections.sort(toReturn);

        if(toReturn.size() == 0){
            return 0;
        }else{
            return toReturn.get(0);
        }
    }


    private ArrayList<Double> getMinimumTranslationX_inner(){
        if(compositionShapesXTranslation.size() == 0 && basicShapesXTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMinimumTranslationX_inner_forCompositions(compositionShapesXTranslation, 0));

        basicShapesXTranslation.forEach(basicShape -> {
            maximumValues.add(basicShape.getValue());
        });


        return maximumValues;
    }

    public double getMinimumTranslationX(){
        ArrayList<Double> minimumValues = getMinimumTranslationX_inner();

        if(minimumValues.size() == 0){
            return 0.0;
        }else{
            Collections.sort(minimumValues);
            return minimumValues.get(0);
        }

    }




    ArrayList<Double> getMaximumBasicShapeXTranslation(){
        double maximumValue;
        if(basicShapesXTranslation.size() != 0){
            maximumValue = basicShapesXTranslation.get(0).getValue();
        }else {
            return new ArrayList<>();
        }
        ArrayList<Double> lowerValues = new ArrayList<>();
        lowerValues.add(maximumValue);

        double finalMaximumValue = maximumValue;
        basicShapesXTranslation.forEach(basicShape -> {
            BasicShape copy = orchestrator.getCopyOfBasicShape(basicShape.getId(), new Information<Double>("1", 0.0).getConsumer(), new Information<Double>("1", 0.0).getConsumer(), getProceedWhenDeleting(basicShape.getId()));;

            //if(basicShape.getValue() < finalMaximumValue){
            //Shouldn't it be >=?
                lowerValues.add(basicShape.getValue() + copy.getWidth());
            //}
        });
        return  lowerValues;
    }

    ArrayList<Double> getMinimumBasicShapeXTranslation(){
        double maximumValue;
        if(basicShapesXTranslation.size() != 0){
            maximumValue = basicShapesXTranslation.get(0).getValue();
        }else {
            return new ArrayList<>();
        }
        ArrayList<Double> lowerValues = new ArrayList<>();
        lowerValues.add(maximumValue);

        basicShapesXTranslation.forEach(basicShape -> {
            lowerValues.add(basicShape.getValue());
        });
        return  lowerValues;
    }


    ArrayList<Double> getMinimumBasicShapeYTranslation(){
        double maximumValue;
        if(basicShapesYTranslation.size() != 0){
            maximumValue = basicShapesYTranslation.get(0).getValue();
        }else {
            return new ArrayList<>();
        }
        ArrayList<Double> lowerValues = new ArrayList<>();
        lowerValues.add(maximumValue);

        basicShapesYTranslation.forEach(basicShape -> {
            BasicShape copy = orchestrator.getCopyOfBasicShape(basicShape.getId(), new Information<Double>("1", 0.0).getConsumer(), new Information<Double>("1", 0.0).getConsumer(), getProceedWhenDeleting(basicShape.getId()));;

            lowerValues.add(basicShape.getValue() - copy.getHeight() );
        });
        return  lowerValues;
    }

    private double getMaximumTranslationX_inner_forCompositions(ArrayList<Information<Double>> compositionShapesX, double previousTranslation){
        ArrayList<Double> toReturn = new ArrayList<>();

        compositionShapesX.forEach(information -> {
            ArrayList<Double> values = new ArrayList<>();
            double translationX = information.getValue();
            NewCompositionShape shape = compositionShapeMap.get(information.getId());

            ArrayList<Double> tempValues = shape.getMaximumBasicShapeXTranslation();
            tempValues.forEach(p -> {
                values.add(p + translationX + previousTranslation);
                System.out.println("---> TRANSLATIONX: " + translationX + ", p: " + p);

            });

            values.sort(Collections.reverseOrder());
            toReturn.add(values.size() == 0 ? 0 : values.get(0));

            //Basic Shapes are now already dealt with.

            //Let's take care of composition shapes...
            toReturn.add(getMaximumTranslationX_inner_forCompositions(shape.compositionShapesXTranslation, translationX));

        });
        toReturn.sort(Collections.reverseOrder());

        if(toReturn.size() == 0){
            return 0;
        }else{
            return toReturn.get(0);
        }
    }

    private ArrayList<Double> getMaximumTranslationX_inner(){
        if(compositionShapesXTranslation.size() == 0 && basicShapesXTranslation.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<Double> maximumValues = new ArrayList<>();

        maximumValues.add(getMaximumTranslationX_inner_forCompositions(compositionShapesXTranslation, 0));

        basicShapesXTranslation.forEach(basicShape -> {
            System.out.println("value: " + basicShape.getValue());
            BasicShape copy = orchestrator.getCopyOfBasicShape(basicShape.getId(), new Information("1", 0.0).getConsumer(), new Information("1", 0.0).getConsumer(), getProceedWhenDeleting(basicShape.getId()));;
            System.out.println("i'm on a basic shape!");
            maximumValues.add(basicShape.getValue() + copy.getWidth());
        });


        return maximumValues;
    }

    public double getMaximumTranslationX(){
        ArrayList<Double> maximumValues = getMaximumTranslationX_inner();
        if(maximumValues.size() == 0){
            return 0.0;
        }else{
            maximumValues.sort(Collections.reverseOrder());
            return maximumValues.get(0);
        }
    }








    public Pane addNewCompositionShape(NewCompositionShape NewCompositionShape) {
        String id = UUID.randomUUID().toString();

        compositionShapeMap.put(id, NewCompositionShape);

        compositionShapesXTranslation.add(new Information(id, 0.0));
        compositionShapesYTranslation.add(new Information(id, 0.0));

        //Aqui tenho que fazer a transla????o necess??ria para que fique no zero zero!

        Pane toAdd = new Pane();
        getTesteForSpecific(toAdd, true, 0, 0, id, NewCompositionShape);


        return toAdd;
    }

    public Pane addNewCompositionShape(NewCompositionShape NewCompositionShape, boolean withHover) {
        String id = UUID.randomUUID().toString();

        compositionShapeMap.put(id, NewCompositionShape);

        compositionShapesXTranslation.add(new Information(id, 0.0));
        compositionShapesYTranslation.add(new Information(id, 0.0));

        //Aqui tenho que fazer a transla????o necess??ria para que fique no zero zero!

        Pane toAdd = new Pane();
        getTesteForSpecific(toAdd, withHover, 0, 0, id, NewCompositionShape);


        return toAdd;
    }

    public Pane addNewCompositionShapeWithTranslation(NewCompositionShape NewCompositionShape, double xTranslation, double yTranslation, String id) {
        compositionShapeMap.put(id, NewCompositionShape);

        compositionShapesXTranslation.add(new Information(id, xTranslation));
        compositionShapesYTranslation.add(new Information(id, yTranslation));

        Pane toAdd = new Pane();
        getTeste(toAdd, true, xTranslation, yTranslation);

        return toAdd;
    }

    private void setUpComponents() {
        setUpTranslationXBox();
        setUpTranslationYBox();
    }

    public ArrayList<Node> getTransformers() {
        ArrayList<Node> toReturn = new ArrayList<>();
        toReturn.add(translationXBox);
        toReturn.add(translationYBox);

        return toReturn;
    }

    private void setUpTranslationXBox() {
        Label translationLabel = new Label("Translation X:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(String.valueOf(selectedTranslationX.getValue() / SCALE));
        textField.setPromptText(String.valueOf(selectedTranslationX.getValue() / SCALE));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Information<Double> tempTranslationX = selectedTranslationX;

        Slider translationXSlider = new Slider();
        translationXSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setMin(- NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setValue(tempTranslationX.getValue() / SCALE);

        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < translationXSlider.getMin()) {
                        textField.setText(String.valueOf(translationXSlider.getMin()));
                    }

                    translationXSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(translationXSlider.getMin()));
                    translationXSlider.setValue(translationXSlider.getMin());
                }

            }
        });

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane

        translationXSlider.setMajorTickUnit(0.1);
        translationXSlider.setMinorTickCount(0);
        translationXSlider.setSnapToTicks(true);


        translationXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            tempTranslationX.setValue(truncatedDouble * SCALE);
            selected.setTranslateX(selected.getTranslateX() + (newValue.doubleValue() -oldValue.doubleValue()) * SCALE) ;
        });

        translationXSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationXSlider, Priority.ALWAYS);



        translationXBox = new HBox(translationLabel, translationXSlider, textField);
        translationXBox.setPadding(new Insets(10, 10, 10, 15));
        translationXBox.setAlignment(Pos.CENTER_LEFT);
        translationXBox.setSpacing(20);
        translationXBox.setMinHeight(30);
        translationXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
    }

    private void setUpTranslationYBox() {
        Label translationLabel = new Label("Translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(String.valueOf(-selectedTranslationY.getValue() / SCALE));
        textField.setPromptText(String.valueOf(-selectedTranslationY.getValue() / SCALE));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Information<Double> tempTranslationY = selectedTranslationY;

        Slider translationYSlider = new Slider();
        translationYSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setMin(- NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setValue(-tempTranslationY.getValue() / SCALE);

        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < translationYSlider.getMin()) {
                        textField.setText(String.valueOf(translationYSlider.getMin()));
                    }

                    translationYSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(translationYSlider.getMin()));
                    translationYSlider.setValue(translationYSlider.getMin());
                }

            }
        });

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane

        translationYSlider.setMajorTickUnit(0.1);
        translationYSlider.setMinorTickCount(0);
        translationYSlider.setSnapToTicks(true);

        translationYSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            tempTranslationY.setValue(truncatedDouble * -SCALE);
            selected.setTranslateY(selected.getTranslateY() + (newValue.doubleValue() - oldValue.doubleValue()) * -SCALE); ;
        });

        translationYSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationYSlider, Priority.ALWAYS);


        translationYBox = new HBox(translationLabel, translationYSlider, textField);
        translationYBox.setPadding(new Insets(10, 10, 10, 15));
        translationYBox.setAlignment(Pos.CENTER_LEFT);
        translationYBox.setMinHeight(40);
        translationYBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
    }

    private Slider getSlider(Information<Double> informationToUpdate, Consumer<Double> consumer, Consumer<Double> textFieldConsumer) {
        Slider slider = new Slider();
        slider.setMax(Orchestrator.getNumberColumnsAndRows());
        slider.setMin(- Orchestrator.getNumberColumnsAndRows());
        slider.setValue(informationToUpdate.getValue());
        slider.setMajorTickUnit(0.1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            informationToUpdate.setValue(newValue.doubleValue() * SCALE);
            System.err.println("new value - old value: " + (newValue.doubleValue() - oldValue.doubleValue()));
            consumer.accept((newValue.doubleValue() - oldValue.doubleValue()) * SCALE);
            textFieldConsumer.accept(newValue.doubleValue());
        });

        return slider;
    }

    @Override
    public String getShapeName() {
        return name;
    }

    @Override
    public void setShapeName(String shapeName) {
        this.name = shapeName;
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

        addScreenshootAndTags();
    }

    private void addScreenshootAndTags() {
        thumbnail.getChildren().add(GridCanvas.takeScreenshootWithRoundedCornersAndLoadTemporarily(this));

        //label tags
        VBox nameAndTagVBox = new VBox(StartMenu.verticalGrower());
        nameAndTagVBox.setSpacing(5);

        Label nameLabel = new Label(getShapeName());
        nameLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 12));
        nameLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)")));


        Label tagLabel = new Label(isFigureVariable ? "Figure Variable" : "Composition Shape");
        tagLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        tagLabel.setPadding(new Insets(3));
        tagLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        tagLabel.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");

        Label numberOfBasicShapes = new Label(basicShapesXTranslation.size() + "x");
        numberOfBasicShapes.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        numberOfBasicShapes.setPadding(new Insets(3));
        numberOfBasicShapes.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        numberOfBasicShapes.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");

        Label numberOfCompositionShapes = new Label(compositionShapeMap.size() + "x");
        numberOfCompositionShapes.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        numberOfCompositionShapes.setPadding(new Insets(3));
        numberOfCompositionShapes.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        numberOfCompositionShapes.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");

        HBox detailsHB = new HBox(tagLabel, numberOfBasicShapes, numberOfCompositionShapes);
        detailsHB.setSpacing(10);

        nameAndTagVBox.getChildren().addAll(nameLabel, detailsHB);

        thumbnail.getChildren().add(nameAndTagVBox);
    }

    @Override
    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier) {
        thumbnail.getChildren().clear();
        thumbnail.setMinWidth(0.0);
        thumbnail.setPadding(new Insets(10));
        thumbnail.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        thumbnail.setSpacing(10);
        thumbnail.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(thumbnail, Priority.NEVER);

        thumbnail.getChildren().clear();

        //label tags
        addScreenshootAndTags();
        //end label tags

        thumbnail.setOnDragDetected(event -> {
            Dragboard db = thumbnail.startDragAndDrop(TransferMode.ANY);
            supplier.get();
            ClipboardContent content = new ClipboardContent();
            content.putString(toPutIntoDragbord.get());
            db.setContent(content);

            event.consume();
        });

        setThumbnailDeleting();

        return thumbnail;
    }

    private void setThumbnailDeleting() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setId("betterMenuItem");

        MenuItem menuItem = new MenuItem("Delete Composition Shape Item");
        menuItem.setStyle("-fx-text-fill: red");
        contextMenu.getItems().add(menuItem);

        menuItem.setOnAction(actionEvent -> {
            proceedWhenDeletingFromThumbnail.apply(getUUID().toString());
        });

        thumbnail.setOnContextMenuRequested(contextMenuEvent -> {
            contextMenu.show(thumbnail, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }




}
