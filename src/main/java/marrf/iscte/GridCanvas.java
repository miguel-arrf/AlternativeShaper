package marrf.iscte;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Set;

public class GridCanvas {

    public static final int SCALE = 40;
    public static final int NUMBER_COLUMNS_AND_ROWS = 40;

    private static double horizontalOffset = 0;
    private static double verticalOffset = 0;

    private static final ArrayList<Double> initialHorizontalDrag = new ArrayList<>();
    private static final ArrayList<Double> initialVerticalDrag = new ArrayList<>();

    private final static ArrayList<BasicShape> basicShapes = new ArrayList<>();
    //private final ArrayList<NewCompositionShape> compositionShapes = new ArrayList<>();

    private static double xOriginTranslation = 0.0;
    private static double yOriginTranslation = 0.0;

    private static double initialTranslationXCircle = 0.0;
    private static double initialTranslationYCircle = 0.0;

    public static final Pane pane = new Pane();
    private static Circle circle;

    public ArrayList<BasicShape> getCurrentRectangles(){
        return basicShapes;
    }

    public BasicShape getSimpleRectangle(){
        if(basicShapes.size()>1){
            try {
                throw new Exception("SOMETHING IS NOT RIGHT :c");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return basicShapes.get(0);
    }


    public static void addShape(BasicShape basicShape){

        double translateXBy = basicShape.getInitialTranslation().getX() * -1;
        double translateYBy = basicShape.getHeight() * basicShape.scaleYProperty().get() + basicShape.getInitialTranslation().getY() * -1;

        basicShape.setTranslateX(circle.getCenterX() + circle.getTranslateX() - translateXBy);
        basicShape.setTranslateY(circle.getCenterY() + circle.getTranslateY() - translateYBy);

        Pane toAdd = basicShape.getRectangle();

        pane.getChildren().add(toAdd);
        basicShapes.add(basicShape);
    }

    public static void addGroup(Pane basicShape){
        basicShape.setTranslateX(basicShape.getTranslateX() + circle.getCenterX() + circle.getTranslateX());
        basicShape.setTranslateY(basicShape.getTranslateY() + circle.getCenterY() + circle.getTranslateY());

        pane.getChildren().add(basicShape);
    }

    public static void addNode(Node basicShape){
        basicShape.setTranslateX(basicShape.getTranslateX() + circle.getCenterX() + circle.getTranslateX());
        basicShape.setTranslateY(basicShape.getTranslateY() + circle.getCenterY() + circle.getTranslateY());

        pane.getChildren().add(basicShape);
    }


    public GridCanvas(){
    }

    public static void clearEverything(){
        redraw();
        basicShapes.clear();
        //compositionShapes.clear();

    }

    public static void clearEverythingWithoutRedrawingGrid(){
        basicShapes.clear();
        pane.getChildren().clear();
    }


    private static void redraw(){
        pane.getChildren().clear();

        int width = SCALE;

        for (int i = 0; i < NUMBER_COLUMNS_AND_ROWS; i++) {
            for (int j = 0; j < NUMBER_COLUMNS_AND_ROWS; j++) {
                Rectangle CustomRectangle = new Rectangle();
                CustomRectangle.setX(width*j);
                CustomRectangle.setY(width*i);
                CustomRectangle.setWidth(width);
                CustomRectangle.setHeight(width);
                CustomRectangle.setFill(null);
                CustomRectangle.setStroke(Color.web("#4F4F4F"));
                CustomRectangle.setStrokeWidth(2);
                pane.getChildren().add(CustomRectangle);
            }
        }

        circle = new Circle(5);
        circle.setFill(Color.web("#6C696F"));
        circle.setCenterX(width*NUMBER_COLUMNS_AND_ROWS / 2.0);
        circle.setCenterY(width*NUMBER_COLUMNS_AND_ROWS / 2.0);

        pane.getChildren().add(circle);

        initialTranslationXCircle = circle.getTranslateX();
        initialTranslationYCircle = circle.getTranslateY();
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




    public static void recenterEverything(){
        Circle circle = null;

        for (Node children : pane.getChildren()) {
            if (children instanceof Circle) {
                circle = (Circle) children;
            }
        }

        assert circle != null;
        xOriginTranslation = initialTranslationXCircle - circle.getTranslateX();
        yOriginTranslation = initialTranslationYCircle - circle.getTranslateY();

        pane.getChildren().forEach( p -> {
            p.setTranslateX(p.getTranslateX() + xOriginTranslation);
            p.setTranslateY(p.getTranslateY() + yOriginTranslation);
        });

    }

    public static void toOriginalPosition(){
        pane.getChildren().forEach( p -> {
            p.setTranslateX(p.getTranslateX() - xOriginTranslation);
            p.setTranslateY(p.getTranslateY() - yOriginTranslation);
        });
    }

    public static Rectangle takeScreenshootWithRoundedCornersAndLoadTemporarilyWithNode(Node node){
        double nodeX = node.getTranslateX();
        double nodeY = node.getTranslateY();

        ArrayList<Node> nodesToThenAdd = new ArrayList<>(pane.getChildren());
        Circle circleCopy = circle;
        double xOriginTranslation = GridCanvas.xOriginTranslation;
        double yOriginTranslation = GridCanvas.yOriginTranslation;
        double initialTranslationXCircle = GridCanvas.initialTranslationXCircle;
        double initialTranslationYCircle = GridCanvas.initialTranslationYCircle;
        double horizontalOffset = GridCanvas.horizontalOffset;
        double verticalOffset = GridCanvas.verticalOffset;

        clearEverything();

        node.setTranslateX(node.getTranslateX() - (circleCopy.getCenterX() + circleCopy.getTranslateX() + xOriginTranslation));
        node.setTranslateY(node.getTranslateY() - (circleCopy.getCenterY() + circleCopy.getTranslateY() + yOriginTranslation));

        addNode(node);

        Rectangle toReturn = takeScreenshootWithRoundedCorners();


        clearEverythingWithoutRedrawingGrid();
        GridCanvas.circle = circleCopy;
        GridCanvas.xOriginTranslation = xOriginTranslation;
        GridCanvas.yOriginTranslation = yOriginTranslation;
        GridCanvas.initialTranslationYCircle = initialTranslationYCircle;
        GridCanvas.initialTranslationXCircle = initialTranslationXCircle;
        GridCanvas.horizontalOffset = horizontalOffset;
        GridCanvas.verticalOffset = verticalOffset;

        pane.getChildren().addAll(nodesToThenAdd);
        System.out.println(pane.getChildren().contains(node));
        node.setTranslateX(nodeX);
        node.setTranslateY(nodeY);

        return toReturn;
    }

    public static Rectangle takeScreenshootWithRoundedCornersAndLoadTemporarily(NewCompositionShape compositionShape){
        ArrayList<Node> nodesToThenAdd = new ArrayList<>(pane.getChildren());
        ArrayList<BasicShape> basicShapesToThenAdd = new ArrayList<>(basicShapes);
        Circle circleCopy = circle;
        double xOriginTranslation = GridCanvas.xOriginTranslation;
        double yOriginTranslation = GridCanvas.yOriginTranslation;
        double initialTranslationXCircle = GridCanvas.initialTranslationXCircle;
        double initialTranslationYCircle = GridCanvas.initialTranslationYCircle;
        double horizontalOffset = GridCanvas.horizontalOffset;
        double verticalOffset = GridCanvas.verticalOffset;

        clearEverything();


        compositionShape.getBasicShapes().forEach(GridCanvas::addShape);
        Pane toAdd = new Pane();
        compositionShape.getTeste(toAdd, true, 0,0);
        System.out.println("tamanho de basic shapes: " + compositionShape.getBasicShapes().size());

        addGroup(toAdd);

        Rectangle toReturn = takeScreenshootWithRoundedCorners();


        clearEverythingWithoutRedrawingGrid();
        GridCanvas.circle = circleCopy;
        GridCanvas.xOriginTranslation = xOriginTranslation;
        GridCanvas.yOriginTranslation = yOriginTranslation;
        GridCanvas.initialTranslationYCircle = initialTranslationYCircle;
        GridCanvas.initialTranslationXCircle = initialTranslationXCircle;
        GridCanvas.horizontalOffset = horizontalOffset;
        GridCanvas.verticalOffset = verticalOffset;

        basicShapes.addAll(basicShapesToThenAdd);
        pane.getChildren().addAll(nodesToThenAdd);



        return toReturn;
    }

    public static Rectangle takeScreenshootWithRoundedCorners(){
        try {
            //TODO AO Tirar o screenshot, deviamos centrar tudo novamente...
            GridCanvas.recenterEverything();

            WritableImage writableImage = new WritableImage((int) GridCanvas.pane.getWidth(),
                    (int) GridCanvas.pane.getHeight());


            WritableImage snapshot = GridCanvas.pane.snapshot(null, writableImage);
            //TODO Tirar screenshoot ao gridCanvas e não ao elemento!
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            GridCanvas.toOriginalPosition();

            Set<PosixFilePermission> fp = PosixFilePermissions.fromString("rwxrwxrwx");

            //Write the snapshot to the chosen file
            File file;
            if (SystemUtils.IS_OS_WINDOWS) {
                file = Files.createTempFile("teste", ".png").toFile();
            } else {
                file = Files.createTempFile("teste", ".png", PosixFilePermissions.asFileAttribute(fp)).toFile();
            }

            ImageIO.write(renderedImage, "png", file);

            Image image = new Image(file.toURL().toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setSmooth(true);


            //TODO ALTERAR
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(100);


            var initialWidth = image.getWidth() / imageView.getFitWidth();
            var newHeight = image.getHeight()/initialWidth;

            Rectangle imageViewRectangle = new Rectangle(0, 0, 100,newHeight);
            imageViewRectangle.setArcWidth(30.0);   // Corner radius
            imageViewRectangle.setArcHeight(30.0);

            ImagePattern pattern = new ImagePattern(
                    image, 0,0,1,1,true // Resizing
            );

            imageViewRectangle.setFill(pattern);


            return imageViewRectangle;
        } catch (IOException ignored) {

        }
        return null;
    }

}
