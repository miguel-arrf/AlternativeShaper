package marrf.iscte;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProcTemplate {

    private static Node getShapePane(){
        Label pane = new Label("Shape");
        pane.setPadding(new Insets(3));
        pane.setStyle("-fx-background-color: rgb(3,128,87); -fx-background-radius: 5px");
        pane.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        pane.setTextFill(Color.web("rgb(0,226,153)"));
        pane.setAlignment(Pos.CENTER);

        pane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pane, Priority.ALWAYS);

        return pane;
    }

    private static Node getProcPane(){
        Label pane = new Label("Proc");
        pane.setPadding(new Insets(3));
        pane.setStyle("-fx-background-color: rgb(45,107,128); -fx-background-radius: 5px");
        pane.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        pane.setTextFill(Color.web("rgb(86,204,242)"));
        pane.setAlignment(Pos.CENTER);

        pane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pane, Priority.ALWAYS);

        return pane;
    }

    private static Node getBoolPane(){
        Label pane = new Label("Bool");
        pane.setPadding(new Insets(3));
        pane.setStyle("-fx-background-color: rgb(128,114,40); -fx-background-radius: 5px");
        pane.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        pane.setTextFill(Color.web("rgb(231,206,73)"));
        pane.setAlignment(Pos.CENTER);

        pane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pane, Priority.ALWAYS);

        return pane;
    }

    private static void setPaneStyle(Node node){
        //node.setStyle("-fx-background-color: rgb(51,50,52); -fx-background-radius: 5px");
        node.setStyle("-fx-background-color: #333234; -fx-background-radius: 10px");

        node.setOnMouseEntered(mouseEvent -> {
            node.setStyle("-fx-background-color: #4D4B4E; -fx-background-radius: 10px");
        });

        node.setOnMouseExited(mouseEvent -> {
            node.setStyle("-fx-background-color: #333234; -fx-background-radius: 10px");
        });

    }

    private static Pane getPane(Node ... nodes){
        HBox toReturn = new HBox(nodes);
        toReturn.setPadding(new Insets(10));
        toReturn.setSpacing(5);

        toReturn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(toReturn, Priority.ALWAYS);

        setPaneStyle(toReturn);

        return toReturn;
    }

    public static Pane get_Shape_Shape(EventHandler<? super MouseEvent> value){
        Pane toReturn = getPane(getShapePane(), getShapePane());
        toReturn.setOnMouseClicked(value);

        return toReturn;
    }

    public static Pane get_Shape_Shape_Proc(EventHandler<? super MouseEvent> value){
        Pane toReturn = getPane(getShapePane(), getShapePane(), getProcPane());
        toReturn.setOnMouseClicked(value);

        return toReturn;
    }

    public static Pane get_Bool_Shape_Shape(EventHandler<? super MouseEvent> value){
        Pane toReturn = getPane(getBoolPane(), getShapePane(), getShapePane());
        toReturn.setOnMouseClicked(value);

        return toReturn;
    }

    public static Pane get_Bool_Shape_Shape_Proc(EventHandler<? super MouseEvent> value){
        Pane toReturn = getPane(getBoolPane(), getShapePane(), getShapePane(), getProcPane());
        toReturn.setOnMouseClicked(value);

        return toReturn;
    }


}
