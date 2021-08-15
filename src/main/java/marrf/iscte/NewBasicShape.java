package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Supplier;

import static marrf.iscte.App.SCALE;
import static marrf.iscte.App.horizontalGrower;

public class NewBasicShape implements CustomShape{

    private final Rectangle rectangle = new Rectangle();

    private String name;
    private final UUID uuid = UUID.randomUUID();

    private double width = 0.0;
    private double height = 0.0;
    private double scaleX = 0.0;
    private double scaleY = 0.0;

    private HBox widthSection;
    private HBox heightSection;
    private HBox translationXSection;
    private HBox translationYSection;
    private HBox scaleXSection;
    private HBox scaleYSection;

    //Constructors
    public NewBasicShape(double width, double height){
        this.width = width;
        this.height = height;

        rectangle.setWidth(width);
        rectangle.setHeight(height);

        setUpSections();
    }

    public NewBasicShape(double width, double height, double scaleX, double scaleY){
        this.width = width;
        this.height = height;
        this.scaleX = scaleX;
        this.scaleY = scaleY;

        rectangle.setWidth(width * scaleX);
        rectangle.setHeight(height * scaleY);

        setUpSections();
    }


    //Other methods

    private void setUpSections(){
        setUpScaleXBox();
        setUpWidthBox();
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
        return null;
    }

    @Override
    public Pane getScaleYSection() {
        return null;
    }

    @Override
    public Pane getTranslationXSection() {
        return null;
    }

    @Override
    public Pane getTranslationYSection() {
        return null;
    }

    @Override
    public void redrawThumbnail() {

    }

    @Override
    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier) {
        return null;
    }


    private void setUpWidthBox(){
        Label widthLabel = new Label("Width:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
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

            this.width = newValue.doubleValue() * SCALE;
            rectangle.setWidth(this.width * scaleX);
        });

        widthSection = new HBox(widthLabel, horizontalGrower(), widthSectionSlider, horizontalGrower(), textField);
        widthSection.setPadding(new Insets(10, 10, 10, 15));
        widthSection.setAlignment(Pos.CENTER_LEFT);
        widthSection.setMinHeight(30);

        widthSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }

    public void setUpScaleXBox() {
        Label widthLabel = new Label("Scale X:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));


        TextField textField = new TextField("1");
        textField.setPromptText("1");
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_RIGHT);

        Slider scaleXSlider = new Slider();
        scaleXSlider.setMax(10);
        scaleXSlider.setMin(0.1);
        scaleXSlider.setValue(1);


        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {

                try {
                    if (Double.parseDouble(textField.getText()) < scaleXSlider.getMin()) {
                        textField.setText(String.valueOf(scaleXSlider.getMin()));
                    }

                    scaleXSlider.setValue(Double.parseDouble(textField.getText()));

                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(scaleXSlider.getMin()));
                    scaleXSlider.setValue(scaleXSlider.getMin());
                }

            }
        });

        //TODO: TextField should allow for 0.##, and slider only for 0.#.
        //TODO: Height pane


        scaleXSlider.setMajorTickUnit(0.1);
        scaleXSlider.setMinorTickCount(0);
        scaleXSlider.setSnapToTicks(true);

        scaleXSlider.valueProperty().addListener(((observableValue, number, t1) -> {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            scaleXSlider.setValue(Double.parseDouble(df.format(t1.doubleValue())));
        }));

        scaleXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            this.scaleX = newValue.doubleValue();

            rectangle.setWidth(width * scaleX);
        });

        scaleXSection = new HBox(widthLabel, horizontalGrower(), scaleXSlider, horizontalGrower(), textField);
        scaleXSection.setPadding(new Insets(10, 10, 10, 15));
        scaleXSection.setAlignment(Pos.CENTER_LEFT);
        scaleXSection.setMinHeight(30);
        scaleXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }



}
