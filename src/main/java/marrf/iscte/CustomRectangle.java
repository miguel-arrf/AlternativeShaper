package marrf.iscte;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;


public class CustomRectangle {

    StackPane stackPane = new StackPane();
    Rectangle rectangle = new Rectangle();
    Pane strokePane = new Pane();

    private final Point2D translationOffset = new Point2D(0   ,-40 );

    private final UUID uuid = UUID.randomUUID();
    private boolean isSimple = false;
    private boolean isSelected = false;

    private VBox thumbnail = new VBox();

    public Point2D getTranslationOffset() {
        return translationOffset;
    }

    public UUID getUuid() {
        return uuid;
    }



    private void setUpStackPane(){
        if(!stackPane.getChildren().contains(rectangle)){
            stackPane.setAlignment(Pos.CENTER);

            stackPane.getChildren().add(rectangle);
            stackPane.getChildren().add(strokePane);


            rectangle.widthProperty().addListener((observable, oldValue, newValue) -> stackPane.setPrefWidth(newValue.doubleValue() * rectangle.getScaleX() ));

            rectangle.heightProperty().addListener((observable, oldValue, newValue) -> stackPane.setPrefHeight(newValue.doubleValue() * rectangle.getScaleY()));

            rectangle.scaleXProperty().addListener((observable, oldValue, newValue) -> stackPane.setPrefWidth(newValue.doubleValue() * rectangle.getWidth()));

            rectangle.scaleYProperty().addListener((observable, oldValue, newValue) -> stackPane.setPrefHeight(newValue.doubleValue() * rectangle.getHeight()));

            stackPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE );

        }
    }


    public void redrawThumbnail(){
        if(isSelected){
            temporarlyTurnOffStroke();
        }


        try{
            WritableImage writableImage = new WritableImage((int)getWidth(),
                    (int)getHeight());
            WritableImage snapshot = stackPane.snapshot(null, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);


            Set<PosixFilePermission> fp = PosixFilePermissions.fromString("rwxrwxrwx");

            //Write the snapshot to the chosen file
            File file;
            if(SystemUtils.IS_OS_WINDOWS){
                file = Files.createTempFile("teste", ".png").toFile();
            }else{
                file = Files.createTempFile("teste", ".png",PosixFilePermissions.asFileAttribute(fp)).toFile();
            }

            ImageIO.write(renderedImage, "png", file);

            Image image = new Image(file.toURL().toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(true);

            thumbnail.getChildren().clear();
            thumbnail.getChildren().add(imageView);
        }catch (IOException ignored){

        }

        if(isSelected)
            turnOnStroke();
    }


    public Pane getThumbnail(Supplier<String> toPutIntoDragbord) {


        if(isSelected){
            temporarlyTurnOffStroke();
        }

        try{
            WritableImage writableImage = new WritableImage((int)getWidth(),
                    (int)getHeight());
            WritableImage snapshot = stackPane.snapshot(null, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);


            Set<PosixFilePermission> fp = PosixFilePermissions.fromString("rwxrwxrwx");

            //Write the snapshot to the chosen file
            File file;
            if(SystemUtils.IS_OS_WINDOWS){
                file = Files.createTempFile("teste", ".png").toFile();
            }else{
                file = Files.createTempFile("teste", ".png",PosixFilePermissions.asFileAttribute(fp)).toFile();
            }

            ImageIO.write(renderedImage, "png", file);

            Image image = new Image(file.toURL().toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setSmooth(true);


            thumbnail.getChildren().clear();
            thumbnail.getChildren().add(imageView);
        }catch (IOException ignored){

        }

        thumbnail.setMinWidth(0.0);


        thumbnail.setPadding(new Insets(10));
        thumbnail.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");

        HBox.setHgrow(thumbnail, Priority.NEVER);

        thumbnail.setOnDragDetected(event -> {
            Dragboard db = thumbnail.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            content.putString(toPutIntoDragbord.get());
            db.setContent(content);

            event.consume();
        });

        if(isSelected)
            turnOnStroke();

        return thumbnail;
    }

    public void toogleOffSelection(){
        isSelected = false;
    }

    public void toogleSelected(){
        isSelected = !isSelected;
    }

    public void turnOnStroke(){

        strokePane.setStyle("-fx-padding: 0;" + "-fx-border-style: solid inside;"
                + "-fx-border-width: 5;" + "-fx-border-insets: -10;"
                + "-fx-border-radius: 5;" + "-fx-border-color: rgba(255,255,255, 1); -fx-background-color: transparent");
    }

    private void temporarlyTurnOffStroke(){
        strokePane.setStyle("-fx-background-color: transparent");
    }

    public void turnOffStroke(){
        strokePane.setStyle("-fx-background-color: transparent");
    }

    public void turnOffStrokeIfNotSelected(){
        if(!isSelected){
            strokePane.setStyle("-fx-background-color: transparent");
        }
    }

    public StackPane getRectangle(){
        setUpStackPane();
        strokePane.setMouseTransparent(true);

        return stackPane;
    }

    public CustomRectangle(int width, int height){
        rectangle = new Rectangle(width, height);
    }

    public CustomRectangle(int width, int height, boolean isSimple){
        rectangle = new Rectangle(width, height);
        this.isSimple = isSimple;
    }

    public CustomRectangle(int width, int height, boolean isSimple, Paint color){
        rectangle = new Rectangle(width, height);
        rectangle.setFill(color);
        this.isSimple = isSimple;
    }

    public DoubleProperty scaleXProperty(){
        return rectangle.scaleXProperty();
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> value){
        rectangle.setOnMouseClicked(value);
    }

    public void setOnDragDetected(EventHandler<? super MouseEvent> value){
        rectangle.setOnDragDetected(value);
    }

    public Dragboard startDragAndDrop(TransferMode... transferModes){
        return rectangle.startDragAndDrop(transferModes);
    }

    public DoubleProperty scaleYProperty(){
        return rectangle.scaleYProperty();
    }

    public void setFill(Paint color){
        rectangle.setFill(color);
    }

    public void setScaleX(double scaleX){
        rectangle.setScaleX(scaleX);
    }

    public void setScaleY(double scaleY){
        rectangle.setScaleY(scaleY);
    }

    public void setWidth(double width){
        rectangle.setWidth(width);

        stackPane.setMaxWidth(width);
        stackPane.setMinWidth(0);
        stackPane.setPrefWidth(width);

        //redrawThumbnail();
    }

    public void setHeight(double height){
        rectangle.setHeight(height);

        stackPane.setMaxHeight(height);
        stackPane.setMinHeight(0);
        stackPane.setPrefHeight(height);

        //redrawThumbnail();
    }

    public DoubleProperty widthProperty(){
        return rectangle.widthProperty();
    }

    public double getX(){
        return stackPane.getLayoutBounds().getMinX();
    }

    public double getY(){
        return stackPane.getLayoutBounds().getMaxY();
    }

    public Point2D localToScene(double d1, double d2){
        return stackPane.localToScene(d1,d2);
    }

    public DoubleProperty heightProperty(){
        return rectangle.heightProperty();
    }

    public void setTranslateX(double value){
        stackPane.setTranslateX(value);
    }

    public void addTranslationX(double value){
        stackPane.setTranslateX(getTranslateX() + value);
    }

    public void addTranslationY(double value){
        stackPane.setTranslateY(getTranslateY() + value);
    }

    public void setX(double x){
        rectangle.setX(x);
    }

    public double getTranslateX(){
        return stackPane.getTranslateX();
    }

    public double getTranslateY(){
        return stackPane.getTranslateY();
    }

    public void setY(double y){
        rectangle.setY(y);
    }

    public void setTranslateY(double value){
        stackPane.setTranslateY(value);
    }

    public Paint getFill(){
        return rectangle.getFill();
    }

    public double getWidth(){
        return rectangle.getWidth();
        //return stackPane.getWidth();
    }

    public double getHeight(){
        return rectangle.getHeight();
        //return stackPane.getHeight();
    }

    public ObjectProperty<Paint> fillProperty(){
        return rectangle.fillProperty();
    }

    public CustomRectangle(){

    }

    public CustomRectangle(boolean isSimple){
        this.isSimple = isSimple;
    }

}