package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import static marrf.iscte.App.horizontalGrower;

public class CompositionShape {

    private final int SCALE = 40;
    private final int NUMBER_COLUMNS_AND_ROWS = 40;

    private final UUID uuid = UUID.randomUUID();

    private boolean isSelected = false;
    private ArrayList<BasicShape> basicShapes = new ArrayList<>();

    private HBox translationXBox;
    private HBox translationYBox;
    private HBox scaleXBox;
    private HBox scaleYBox;

    private CompositionShape(ArrayList<BasicShape> basicShapesToAdd){
        basicShapes.addAll(basicShapesToAdd);
        setUpComponents();
    }

    private void setUpComponents(){
        setUpTranslationXBox();
        setUpTranslationYBox();

        setUpScaleXBox();
        setUpScaleYBox();
    }

    private void setUpTranslationXBox(){

    }

    private void setUpTranslationYBox(){

    }

    private void setUpScaleXBox(){
        Label widthLabel = new Label("Scale X:");
        widthLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        widthLabel.setTextFill(Color.web("#BDBDBD"));

        TextField textField = new TextField("0.1");
        textField.setPromptText("0.1");
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

            this.setScaleX(newValue.doubleValue());

        });

        scaleXBox = new HBox(widthLabel, horizontalGrower(), scaleXSlider, horizontalGrower(), textField);
        scaleXBox.setPadding(new Insets(10, 10, 10, 15));
        scaleXBox.setAlignment(Pos.CENTER_LEFT);
        scaleXBox.setMinHeight(30);
        scaleXBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");

    }

    private void setUpScaleYBox(){

    }

    public void setScaleX(double scaleX){
        basicShapes.forEach(basicShape -> basicShape.setScaleX(9.0));
    }

}
