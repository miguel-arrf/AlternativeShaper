package marrf.iscte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class Process {

    private final UUID id;
    private String processName;

    private String blocklyXML;

    private final Pane thumbnail = new VBox();

    public static void updateOrAdd(ArrayList<Process> processes, Process toAddOrUpdate){

        Optional<Process> processOptional = processes.stream().filter(p -> p.getId().equals(toAddOrUpdate.getId())).findFirst();

        if(processOptional.isPresent()){
            Process toUpdate = processOptional.get();
            toUpdate.setBlocklyXML(toAddOrUpdate.getBlocklyXML());
            System.out.println("It was updated.");
        }else{
            processes.add(toAddOrUpdate);
            System.out.println("It was added.");
        }

    }

    //Constructors
    public Process(){
        this.id = UUID.randomUUID();
    }

    public Process(String processName, String blocklyXML) {
        this.processName = processName;
        this.blocklyXML = blocklyXML;
        this.id = UUID.randomUUID();
    }

    public Process(UUID id, String processName, String blocklyXML) {
        this.id = id;
        this.processName = processName;
        this.blocklyXML = blocklyXML;
    }

    //Setters
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setBlocklyXML(String blocklyXML) {
        this.blocklyXML = blocklyXML;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public String getProcessName() {
        return processName;
    }

    public String getBlocklyXML() {
        return blocklyXML;
    }

    public Pane getThumbnail(){
        thumbnail.setId(getId().toString());
        thumbnail.setPadding(new Insets(5));
        ((VBox) thumbnail).setAlignment(Pos.CENTER);

        Label processNameLabel = new Label(processName);
        processNameLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        processNameLabel.setTextFill(Color.WHITE);

        thumbnail.getChildren().clear();
        thumbnail.getChildren().add(processNameLabel);
        thumbnail.setStyle("-fx-background-color: lightblue; -fx-background-radius: 10");

        return thumbnail;
    }


}
