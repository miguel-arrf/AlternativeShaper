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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Process {

    private final UUID id;
    private String processName;

    private String blocklyXML;
    private String processCode;

    private final Pane thumbnail = new VBox();

    public static boolean hasDependencies(Process process){

        String code = process.getProcessCode();

        if(code.contains("existingproc") ||
        code.contains("existingproc0") ||
        code.contains("existingproc1") ||
        code.contains("existingbool")){
            //Can we simplify to just having "existing"...?
            return true;
        }
        return false;
    }

    /*    public static String solveDependency(ArrayList<Process> processes, Process process){
            String existingProc = "existingproc";

            String processCode = process.getProcessCode();

            Pattern pattern = Pattern.compile(existingProc + "\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(processCode);

            if(processes != null){
                while (matcher.find()){
                    String match = matcher.group(1);
                    String toReplace = existingProc + "(" + match + ")";
                    Optional<Process> seeIfExists = processes.stream().filter(p -> p.getProcessName().equals(match)).findFirst();


                    if(seeIfExists.isPresent()){
                        Process toPut = seeIfExists.get();

                        //toReplace: existingproc(miguel)
                        if(Process.hasDependencies(toPut)){
                            processCode = processCode.replaceAll(Pattern.quote(toReplace), solveDependency(processes, toPut));
                        }else{
                            processCode = processCode.replaceAll(Pattern.quote(toReplace), toPut.getProcessCode());
                        }

                    }

                }
            }


            if(processCode.chars().filter(ch -> ch == ';').count() >= 2){
                processCode = processCode.substring(0, processCode.lastIndexOf(";")).replaceAll(";" , "").concat(processCode.substring(processCode.lastIndexOf(";")));
            }

            return processCode;
        }
*/
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

    public Process(UUID id, String processName, String blocklyXML, String processCode) {
        this.id = id;
        this.processName = processName;
        this.blocklyXML = blocklyXML;
        this.processCode = processCode;
    }

    //Constructors
    public Process(){
        this.id = UUID.randomUUID();
    }


    //Setters

    public void setProcessCode(String processCode) {
        processCode = processCode.replaceAll("\n", "");
        this.processCode = processCode.trim();
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setBlocklyXML(String blocklyXML) {
        this.blocklyXML = blocklyXML;
    }

    //Getters

    public String getProcessCode() {
        return processCode;
    }

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
