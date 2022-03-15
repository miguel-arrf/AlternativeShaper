package marrf.iscte.JavaAndProlog;

import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import marrf.iscte.PortableGridCanvas;
import marrf.iscte.StartMenu;
import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static marrf.iscte.App.getAnchorPaneClip;
import static marrf.iscte.PopupWindow.startBlurAnimation;
import static marrf.iscte.PortableGridCanvas.DOUBLE_NUMBER_COLUMNS_AND_ROWS;


public class DesignToProlog {

    private static final int SCALE = 40;

    private final Stage stage = new Stage();
    private final Scene scene;

    private final ArrayList<ImageView> ultimosAAdicionar = new ArrayList<>();
    private final Group group = new Group();

    public DesignToProlog(Scene scene){
        this.scene = scene;
    }

    private void createQuery(){
        Query query = new Query("consult", new Term[] {new Atom(this.getClass().getResource("prolog/AlternativeShaper.pl").getPath())});
        System.out.println( "Consult:  " + (query.hasSolution() ? "succeeded" : "failed"));

        Variable FinalShapeComposition = new Variable("FinalShapeComposition");


        String initialMemory = "[var(floorxright,9),var(floorxleft,-9.6),var(floorytop,4.5),var(floorybottom,-4.5),var(rid,horizontal),var(north,right),var(south,left),var(vesx,4),var(vesy,5),var(garx,10),var(gary,10),var(toix,3),var(toiy,2),var(tecx,3),var(tecy,2),var(kitx,7),var(kity,5),var(livx,7),var(livy,7),var(dinx,5),var(diny,7),var(offx,5),var(offy,7),var(sbelx,7),var(sbely,7),var(sbesx,7),var(sbesy,5),var(dbex,7),var(dbey,7),var(pbatx,3),var(pbaty,5),var(batx,4),var(baty,7),var(hal,[])]";

        Query q4 = new Query("memory(global, InitialMemory),\n" +
                "InitialShapeComposition = [s(cell,[1,0,0,0,1,0,-9.6,-4.5,1])],\n" +
                "applyRuleProcedure(placeBasement, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory),\n" +
                "applyRuleProcedure(placeVestibule, FinalShapeComposition, FinalShapeComposition_1, FinalMemory, FinalMemory_1).");

        q4 = new Query("memory(global, InitialMemory),\n" +
                "InitialShapeComposition = [s(cell,[1,0,0,0,1,0,-9.6,-4.5,1])],\n" +
                "applyRuleProcedure(home, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory).");

        q4 = new Query("memory(global, InitialMemory),\n" +
                "InitialShapeComposition = [s(cell,[1,0,0,0,1,0,-9.6,-4.5,1])],\n" +
                "applyRuleProcedure(home, InitialShapeComposition, FinalShapeComposition, InitialMemory, FinalMemory),\n" +
                "applyRuleProcedure(placeWalls, FinalShapeComposition, FinalShapeComposition_1, FinalMemory, FinalMemory_1),\n" +
                "applyRuleProcedure(placeAllDoors, FinalShapeComposition_1, FinalShapeComposition_2, FinalMemory_1, FinalMemory_2),\n" +
                "applyRuleProcedure(placeAllWindows, FinalShapeComposition_2, FinalShapeComposition_3, FinalMemory_2, FinalMemory_3).");


        while( q4.hasNext()){
            Map<String, Term> binding = q4.next();
            Term t = (Term) binding.get("FinalShapeComposition_3");

            System.out.println(t);

            break;
        }

    }

    private ArrayList<Coordenadas> loadPrologDesign() throws IOException {
        LoadPrologResult textos = new LoadPrologResult();

        textos.obterStringGrande();
        textos.tirarAspasEParentesisRetos();
        return textos.obterValores();
    }

    private void addImage(Group group, Image image, Coordenadas c) {

        ImageView imageView = new ImageView();
        imageView.setImage(image);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        imageView.setFitWidth(0.6 * SCALE);

        imageView.setX(c.getX() * 40.0);
        imageView.setY(c.getY() * 40.0);

        //imageView.toFront();

        String[] bringToFront = {"sihwall","sivwall","sehwall","sevwall","sehwallcorner","sevwallcorner","sihdoor","sivdoor","sehdoor","sevdoor","sgaragedoor","shwindow","svwindow"};

        if(Arrays.asList(bringToFront).contains(c.getTipo())){

            switch (c.getTipo()) {
                case "sehwall":
                    imageView.setFitWidth(SCALE * 0.6);
                    imageView.setFitHeight(SCALE * 0.15);

                    break;
                case "sehwallcorner":
                    imageView.setFitWidth(SCALE * 0.675);
                    imageView.setFitHeight(SCALE * 0.15);

                    break;
                case "sivwall":
                    imageView.setFitWidth(SCALE * 0.075);
                    imageView.setFitHeight(SCALE * 0.6);

                    break;
                case "sevwall":
                    imageView.setFitWidth(SCALE * 0.15);
                    imageView.setFitHeight(SCALE * 0.6);

                    break;
                case "sevwallcorner":
                    imageView.setFitWidth(SCALE * 0.15);
                    imageView.setFitHeight(SCALE * 0.525);

                    break;
                case "sihwall":
                    imageView.setFitWidth(SCALE * 0.6);
                    imageView.setFitHeight(SCALE * 0.075);

                    //NOVOS
                    break;
                case "sihdoor":
                    imageView.setFitWidth(SCALE * 1.2);
                    imageView.setFitHeight(SCALE * 0.075);

                    break;
                case "sivdoor":
                    imageView.setFitWidth(SCALE * 0.075);
                    imageView.setFitHeight(SCALE * 1.2);

                    break;
                case "sehdoor":
                    imageView.setFitWidth(SCALE * 1.2);
                    imageView.setFitHeight(SCALE * 0.15);

                    break;
                case "sevdoor":
                    imageView.setFitWidth(SCALE * 0.15);
                    imageView.setFitHeight(SCALE * 1.2);

                    break;
                case "sgaragedoor":
                    imageView.setFitWidth(SCALE * 2.4);
                    imageView.setFitHeight(SCALE * 0.15);

                    break;
                case "shwindow":
                    imageView.setFitWidth(SCALE * 1.2);
                    imageView.setFitHeight(SCALE * 0.15);

                    break;
                case "svwindow":
                    imageView.setFitWidth(SCALE * 0.15);
                    imageView.setFitHeight(SCALE * 1.2);

                    break;
            }

            ultimosAAdicionar.add(imageView);

            imageView.toFront();

        }else{
            group.getChildren().add(imageView);

        }

    }

    public void drawGraph(ArrayList<Coordenadas> coordenadas,PortableGridCanvas grid) {

        for (Coordenadas c : coordenadas) {
            String nomeShape = c.getTipo();
            nomeShape = nomeShape.substring(1);
            Image image = new Image("file:/Users/miguelferreira/Downloads/AlternativeShaper_HTML_Files/AlternativeShaperProfessor/shapes/" + nomeShape + ".gif");

            addImage(group, image, c);

        }

        for(ImageView imageView: ultimosAAdicionar){
            group.getChildren().add(imageView);
        }

        grid.addToCenter(group);

    }

    public Shape getCustomRectangleClip(Pane parent){
        Rectangle clip = new Rectangle(300,300);

        clip.setTranslateX(SCALE);
        clip.setTranslateY(SCALE/2.0);

        parent.widthProperty().addListener((observable, oldValue, newValue) -> {
            double xTranslation = (newValue.doubleValue() - DOUBLE_NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            clip.setWidth(newValue.doubleValue());
            clip.setTranslateX(-xTranslation);
        });

        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            double yTranslation = (newValue.doubleValue() - DOUBLE_NUMBER_COLUMNS_AND_ROWS*SCALE)/2;
            clip.setHeight(newValue.doubleValue());
            clip.setTranslateY(-yTranslation);

        });


        return clip;
    }

    private Pane getViewer(){

        VBox mainPanel = new VBox();

        Pane pane = new Pane();

        PortableGridCanvas portableGridCanvas = new PortableGridCanvas();
        Pane grid = portableGridCanvas.getGrid(pane);
        grid.setClip(getCustomRectangleClip(pane));

        pane.getChildren().add(grid);
        VBox.setVgrow(pane, Priority.ALWAYS);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPickOnBounds(false);
        pane.getChildren().add(anchorPane);
        getAnchorPaneClip(anchorPane, "#232225");

        pane.widthProperty().addListener((observable, oldValue, newValue) -> anchorPane.setPrefWidth(newValue.doubleValue()));

        pane.heightProperty().addListener((observable, oldValue, newValue) -> anchorPane.setPrefHeight(newValue.doubleValue()));

        pane.setStyle("-fx-background-radius: 20; -fx-background-color: #333234; -fx-border-color: #4F4F4F; -fx-border-radius: 20;");

        try{

            pane.setScaleY(-1);
            drawGraph(loadPrologDesign(), portableGridCanvas);

        }catch (IOException e){
            e.printStackTrace();
        }

        mainPanel.getChildren().add(pane);
        mainPanel.setStyle("-fx-background-color: #262528");
        mainPanel.setPadding(new Insets(20));

        return mainPanel;
    }

    public void openPopup(){
        scene.getRoot().setCache(true);
        scene.getRoot().setCacheHint(CacheHint.SPEED);
        startBlurAnimation(scene.getRoot(), 0.0, 30.0, Duration.millis(100), false);
        scene.getRoot().setCache(false);
        scene.getRoot().setCacheHint(CacheHint.DEFAULT);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(StartMenu.primaryStage);

        Scene dialogScene = new Scene(getViewer(), 1280, 720);
        dialogScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        //Scene dialogScene = new Scene(getTemplate(), 230, 240);
        startBlurAnimation(scene.getRoot(), 30.0, 0.0, Duration.millis(100), false);


        stage.setScene(dialogScene);
        stage.show();

        stage.sizeToScene();
        stage.setTitle("Design Viewer");

        stage.setResizable(true);

        stage.setOnCloseRequest(event -> {
            scene.getRoot().setCache(true);
            scene.getRoot().setCacheHint(CacheHint.SPEED);
            startBlurAnimation(scene.getRoot(), 30.0, 0.0, Duration.millis(100), true);
            scene.getRoot().setCache(false);
            scene.getRoot().setCacheHint(CacheHint.DEFAULT);
        });


    }

}
