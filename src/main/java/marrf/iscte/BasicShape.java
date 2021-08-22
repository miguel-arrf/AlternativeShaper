package marrf.iscte;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.horizontalGrower;

public class BasicShape implements CustomShape {

    private static final int SCALE = 40;
    private static final int NUMBER_COLUMNS_AND_ROWS = 40;
    private final Point2D translationOffset = new Point2D(0, 0); //-20 -60
    private final UUID uuid = UUID.randomUUID();
    private final Pane rectangle;
    private boolean isSelected = false;

    public final DoubleProperty translateXProperty = new SimpleDoubleProperty(0.0);
    public final DoubleProperty translateYProperty = new SimpleDoubleProperty(0.0);

    public DoubleProperty xTranslateOffsetProperty = new SimpleDoubleProperty(0.0);
    public DoubleProperty yTranslateOffsetProperty = new SimpleDoubleProperty(0.0);

    private final Function<Double, Double> writeTranslateX;
    private final Function<Double, Double> writeTranslateY;

    private HBox widthSection;
    private HBox heightSection;

    private HBox translationXSection;
    private HBox translationYSection;

    private double width;
    private double height;

    private final Paint color;
    private String shapeName = "defaultName";

    private boolean strokeShowing = false;

    private final VBox thumbnail = new VBox();

    public void setShapeName(String shapeName) {
        this.shapeName = shapeName;
    }

    public String getShapeName() {
        return shapeName;
    }

    public BasicShape(double width, double height, Paint color, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY) {
        this.writeTranslateX = writeTranslateX;
        this.writeTranslateY = writeTranslateY;

        this.width = width;
        this.height = height;

        this.color = color;

        rectangle = new Pane();
        rectangle.setPrefSize(width, height);

        BackgroundFill backgroundFill = new BackgroundFill(color, new CornerRadii(0), new Insets(0));
        Background background = new Background(backgroundFill);
        rectangle.setBackground(background);

        setUpComponents();
    }

    public Point2D getInitialTranslation(){
        return new Point2D(writeTranslateX.apply(null), writeTranslateY.apply(null));
    }

    public BasicShape(double width, double height, Paint color) {
        this.width = width;
        this.height = height;

        this.color = color;

        rectangle = new Pane();
        rectangle.setPrefSize(width, height);

        writeTranslateX = a -> 0.0;
        writeTranslateY = a -> 0.0;

        BackgroundFill backgroundFill = new BackgroundFill(color, new CornerRadii(0), new Insets(0));
        Background background = new Background(backgroundFill);
        rectangle.setBackground(background);
        setUpComponents();
    }

    public Paint getFill() {
        return color;
    }

    public Point2D getTranslationOffset() {
        return translationOffset;
    }

    public UUID getUUID() {
        return uuid;
    }


    private boolean isStrokeOn() {
        return rectangle.getStyle().contains("-fx-border-radius");
    }

    public HBox getWidthSection() {
        return widthSection;
    }

    public HBox getHeightSection() {
        return heightSection;
    }

    public HBox getTranslationXSection() {
        return translationXSection;
    }

    public HBox getTranslationYSection() {
        return translationYSection;
    }

    public void setUpWidthBox() {
        Label widthLabel = new Label("Width:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("1");
        textField.setPromptText("1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        Slider widthSectionSlider = new Slider();
        widthSectionSlider.setMax(10);
        widthSectionSlider.setMin(0.1);
        widthSectionSlider.setValue(1);


        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < widthSectionSlider.getMin()) {
                        textField.setText(String.valueOf(widthSectionSlider.getMin()));
                    }

                    widthSectionSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(widthSectionSlider.getMin()));
                    widthSectionSlider.setValue(widthSectionSlider.getMin());
                }

            }
        });

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane


        widthSectionSlider.setMajorTickUnit(0.1);
        widthSectionSlider.setMinorTickCount(0);
        widthSectionSlider.setSnapToTicks(true);

        widthSectionSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            widthSectionSlider.setValue(Double.parseDouble(df.format(t1.doubleValue())));
        }));

        widthSectionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            this.setWidth(newValue.doubleValue() * SCALE);

        });

        widthSection = new HBox(widthLabel, horizontalGrower(), widthSectionSlider, horizontalGrower(), textField);
        widthSection.setPadding(new Insets(10, 10, 10, 15));
        widthSection.setAlignment(Pos.CENTER_LEFT);
        widthSection.setMinHeight(30);

        widthSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }

    public void setUpHeightBox() {
        Label heightLabel = new Label("Height:");
        heightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        heightLabel.setTextFill(Color.web("#BDBDBD"));

        TextField textField = new TextField("1");
        textField.setPromptText("1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        Slider heightSectionSlider = new Slider();
        heightSectionSlider.setMax(10);
        heightSectionSlider.setMin(0.1);
        heightSectionSlider.setValue(1);

        heightSectionSlider.setMajorTickUnit(0.1);
        heightSectionSlider.setMinorTickCount(0);
        heightSectionSlider.setSnapToTicks(true);

        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < heightSectionSlider.getMin()) {
                        textField.setText(String.valueOf(heightSectionSlider.getMin()));
                    }

                    heightSectionSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(heightSectionSlider.getMin()));
                    heightSectionSlider.setValue(heightSectionSlider.getMin());
                }

            }
        });


        heightSectionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);

            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));
            
            this.setHeight(newValue.doubleValue() * SCALE);

            heightSectionSlider.setValue(Double.parseDouble(df.format(newValue.doubleValue())));

        });

        heightSection = new HBox(heightLabel, horizontalGrower(), heightSectionSlider, horizontalGrower(), textField);
        heightSection.setPadding(new Insets(10, 10, 10, 15));
        heightSection.setAlignment(Pos.CENTER_LEFT);
        heightSection.setMinHeight(30);

        heightSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
    }

    public void setUpTranslationXBox() {
        Label widthLabel = new Label("Translation X:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("0");
        textField.setPromptText("0");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        Slider translationXSlider = new Slider();
        translationXSlider.setMax(SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationXSlider.setMin(-SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationXSlider.setValue(0);

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

        translationXSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            translationXSlider.setValue(Double.parseDouble(df.format(t1.doubleValue())));
        }));

        translationXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            this.addTranslationX(newValue.doubleValue() - oldValue.doubleValue());
            translateXProperty.setValue(truncatedDouble);
            writeTranslateX.apply(truncatedDouble);
        });

        translationXSection = new HBox(widthLabel, horizontalGrower(), translationXSlider, horizontalGrower(), textField);
        translationXSection.setPadding(new Insets(10, 10, 10, 15));
        translationXSection.setAlignment(Pos.CENTER_LEFT);
        translationXSection.setMinHeight(30);
        translationXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }

    public void setUpTranslationYBox() {
        Label heightLabel = new Label("Translation Y:");
        heightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        heightLabel.setTextFill(Color.web("#BDBDBD"));

        TextField textField = new TextField("0");
        textField.setPromptText("0");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        Slider translationYSlider = new Slider();
        translationYSlider.setMax(SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationYSlider.setMin(-SCALE * NUMBER_COLUMNS_AND_ROWS / 2.0);
        translationYSlider.setValue(0);

        translationYSlider.setMajorTickUnit(0.1);
        translationYSlider.setMinorTickCount(0);
        translationYSlider.setSnapToTicks(true);

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


        translationYSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);

            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            translateYProperty.setValue(truncatedDouble);

            this.addTranslationY(newValue.doubleValue() - oldValue.doubleValue());

            writeTranslateY.apply(truncatedDouble);
        });

        translationYSection = new HBox(heightLabel, horizontalGrower(), translationYSlider, horizontalGrower(), textField);
        translationYSection.setPadding(new Insets(10, 10, 10, 15));
        translationYSection.setAlignment(Pos.CENTER_LEFT);
        translationYSection.setMinHeight(30);
        translationYSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }


    public void redrawThumbnail() {
        boolean wasSelected = isStrokeOn();

        if (wasSelected)
            temporarilyTurnOffStroke();

        if (isSelected)
            temporarilyTurnOffStroke();

        try {
            WritableImage writableImage = new WritableImage((int) getWidth(),
                    (int) getHeight());
            WritableImage snapshot = rectangle.snapshot(null, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);


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
            imageView.setPreserveRatio(true);

            thumbnail.getChildren().clear();
            thumbnail.getChildren().add(imageView);
        } catch (IOException ignored) {

        }

        if (isSelected)
            turnOnStroke();

        if (wasSelected)
            turnOnStroke();

        thumbnail.getChildren().add(new Label(getShapeName()));

    }

    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier) {

        boolean wasSelected = isStrokeOn();

        if (wasSelected) {
            temporarilyTurnOffStroke();
        }


        if (isSelected) {
            temporarilyTurnOffStroke();
        }

        try {
            WritableImage writableImage = new WritableImage((int) getWidth(),
                    (int) getHeight());
            WritableImage snapshot = rectangle.snapshot(null, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);


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


            thumbnail.getChildren().clear();
            thumbnail.getChildren().add(imageView);
        } catch (IOException ignored) {

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

        if (isSelected)
            turnOnStroke();

        if (wasSelected) {
            turnOnStroke();
        }

        thumbnail.getChildren().add(new Label(getShapeName()));

        return thumbnail;
    }

    public void toogleOffSelection() {
        isSelected = false;
    }

    public void toogleSelected() {
        isSelected = !isSelected;
    }

    public void turnOnStroke() {

        if(!strokeShowing){
            rectangle.setStyle("-fx-padding: 0;" + "-fx-border-style: solid inside;"
                    + "-fx-border-width: 5;" + "-fx-border-insets: -10;"
                    + "-fx-border-radius: 5;" + "-fx-border-color: rgba(255,255,255, 1);");
        }

        strokeShowing = true;
    }

    private void temporarilyTurnOffStroke() {
        rectangle.setStyle("");

        strokeShowing = false;
    }

    public void turnOffStroke() {

        rectangle.setStyle("");
        strokeShowing = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void turnOffStrokeIfNotSelected() {
        if (!isSelected) {
            rectangle.setStyle("");
            strokeShowing = false;
        }
    }

    public Pane getRectangle() {

        return rectangle;
    }

    private void setUpComponents() {
        setUpHeightBox();
        setUpWidthBox();

        setUpTranslationXBox();
        setUpTranslationYBox();
    }

    public DoubleProperty scaleXProperty() {
        return rectangle.scaleXProperty();
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> value) {
        rectangle.setOnMouseClicked(value);
    }

    public void setOnDragDetected(EventHandler<? super MouseEvent> value) {
        rectangle.setOnDragDetected(value);
    }

    public Dragboard startDragAndDrop(TransferMode... transferModes) {
        return rectangle.startDragAndDrop(transferModes);
    }

    public DoubleProperty scaleYProperty() {
        return rectangle.scaleYProperty();
    }

    public void addTranslationX(double value) {
        rectangle.setTranslateX(getTranslateX() + value);
    }

    public void addTranslationY(double value) {
        rectangle.setTranslateY(getTranslateY() + value);
    }

    public double getTranslateX() {
        return rectangle.getTranslateX();
    }

    public void setTranslateX(double value) {
        rectangle.setTranslateX(value + xTranslateOffsetProperty.get());
    }

    public double getTranslateY() {
        return rectangle.getTranslateY();
    }

    public void setTranslateY(double value) {
        rectangle.setTranslateY(value + xTranslateOffsetProperty.get());
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double newWidth) {
        this.width = newWidth;
        rectangle.setPrefWidth(width);
    }

    public void setHeight(double newHeight) {
        if(newHeight > height){
            this.addTranslationY(- Math.abs(height - newHeight));
        }else{
            this.addTranslationY( (height - newHeight));
        }
        this.height = newHeight;
        rectangle.setPrefHeight(height);
    }

    public double getHeight() {
        return  height;
    }



    public Point2D localToScene(double d1, double d2) {
        return rectangle.localToScene(d1, d2);
    }

    public double getX() {
        return rectangle.getLayoutBounds().getMinX();
    }


    public double getY() {
        return rectangle.getLayoutBounds().getMaxY();
    }



}
