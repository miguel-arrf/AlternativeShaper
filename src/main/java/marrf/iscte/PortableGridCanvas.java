package marrf.iscte;


import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class PortableGridCanvas {

    public final int SCALE = 40;
    public static final int DOUBLE_NUMBER_COLUMNS_AND_ROWS = 80;

    private double horizontalOffset = 0;
    private double verticalOffset = 0;

    private final ArrayList<Double> initialHorizontalDrag = new ArrayList<>();
    private final ArrayList<Double> initialVerticalDrag = new ArrayList<>();

    private final ArrayList<BasicShape> basicShapes = new ArrayList<>();

    public final Pane pane = new Pane();

    public ArrayList<BasicShape> getCurrentRectangles() {
        return basicShapes;
    }


    public void addToCenter(Group toAdd){
        pane.getChildren().add(toAdd);

        toAdd.getChildren().forEach(children -> {
            children.setTranslateY(SCALE * DOUBLE_NUMBER_COLUMNS_AND_ROWS / 2.0);
            children.setTranslateX(SCALE * DOUBLE_NUMBER_COLUMNS_AND_ROWS / 2.0);

        });


    }

    public PortableGridCanvas() {
    }


    private void redraw() {
        pane.getChildren().clear();

        int width = SCALE;

        for (int i = 0; i < DOUBLE_NUMBER_COLUMNS_AND_ROWS; i++) {
            for (int j = 0; j < DOUBLE_NUMBER_COLUMNS_AND_ROWS; j++) {
                Rectangle CustomRectangle = new Rectangle();
                CustomRectangle.setX(width * j);
                CustomRectangle.setY(width * i);
                CustomRectangle.setWidth(width);
                CustomRectangle.setHeight(width);
                CustomRectangle.setFill(null);
                CustomRectangle.setStroke(Color.web("#4F4F4F"));
                CustomRectangle.setStrokeWidth(2);
                pane.getChildren().add(CustomRectangle);
            }
        }

        Circle circle = new Circle(5);
        circle.setFill(Color.web("#6C696F"));
        circle.setCenterX(width * DOUBLE_NUMBER_COLUMNS_AND_ROWS / 2.0);
        circle.setCenterY(width * DOUBLE_NUMBER_COLUMNS_AND_ROWS / 2.0);

        pane.getChildren().add(circle);

    }

    public Pane getGrid(Pane parent) {
        redraw();


        parent.widthProperty().addListener((observable, oldValue, newValue) -> {
            double xTranslation = (newValue.doubleValue() - DOUBLE_NUMBER_COLUMNS_AND_ROWS * SCALE) / 2;
            pane.setTranslateX(xTranslation);
        });

        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            double yTranslation = (newValue.doubleValue() - DOUBLE_NUMBER_COLUMNS_AND_ROWS * SCALE) / 2;
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
            boolean notExceedingHorizontally = Math.abs(initialHorizontalDrag.get(1) + event.getX() - horizontalOffset) <= (DOUBLE_NUMBER_COLUMNS_AND_ROWS * SCALE - parent.getWidth()) / 2;
            boolean notExceedingVertically = Math.abs(initialVerticalDrag.get(1) + event.getY() - verticalOffset) <= (DOUBLE_NUMBER_COLUMNS_AND_ROWS * SCALE - parent.getHeight()) / 2;

            if (notExceedingHorizontally) {
                for (int i = 0; i < pane.getChildren().size(); i++) {
                    pane.getChildren().get(i).setTranslateX(initialHorizontalDrag.get(i) + event.getX() - horizontalOffset);
                }
            }

            if (notExceedingVertically) {
                for (int i = 0; i < pane.getChildren().size(); i++) {
                    pane.getChildren().get(i).setTranslateY(initialVerticalDrag.get(i) + event.getY() - verticalOffset);
                }
            }

        });


        return pane;
    }


}

