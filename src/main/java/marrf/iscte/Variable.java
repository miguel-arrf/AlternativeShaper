package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static marrf.iscte.BasicShape.colorToRGBString;
import static marrf.iscte.BasicShape.getRelativeLuminance;

public class Variable {

    private String unclicked = "-fx-background-color: rgb(79,79,79); -fx-background-radius: 10";
    private String hoverUnclicked = "-fx-background-color: rgb(107,107,107); -fx-background-radius: 10";

    private boolean isClicked = false;

    private String name;
    private Double value;

    private VBox thumbnail = new VBox();

    public Variable(String name, Double value){
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }

    public Pane getThumbnail(){
        thumbnail.setStyle(unclicked);
        thumbnail.setSpacing(10);
        thumbnail.setAlignment(Pos.CENTER_LEFT);

        thumbnail.setPadding(new Insets(10));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 12));
        nameLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)")));

        Label tagLabel = new Label(Double.toString(value));
        tagLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        tagLabel.setPadding(new Insets(3));
        tagLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        tagLabel.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");


        thumbnail.getChildren().clear();
        thumbnail.getChildren().addAll(nameLabel, tagLabel);

        thumbnail.setOnMouseExited(mouseEvent -> {
            if(!isClicked)
                thumbnail.setStyle(unclicked);
        } );

        thumbnail.setOnMouseEntered(mouseEvent -> {
            if(!isClicked)
                thumbnail.setStyle(hoverUnclicked);
        } );

        thumbnail.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(thumbnail, Priority.ALWAYS);

        return thumbnail;
    }

    public void setClicked(){
        isClicked = true;
        thumbnail.setStyle( "-fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 3; -fx-border-radius: 10px; " + unclicked);
    }

    public void setUnClicked(){
        isClicked = false;
        thumbnail.setStyle(unclicked);
    }

}
