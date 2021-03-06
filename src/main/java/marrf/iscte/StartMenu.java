package marrf.iscte;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class StartMenu extends Application {

    private App app = new App();

    private Scene scene;
    public static Stage primaryStage;

    private HBox basicShapePane;
    private HBox loadFilePane;

    private boolean fileDirectoryChoosen = false;


    public static String getRGBColor(){
        String LIGHT_GREENISH_BLUE = "#55efc4";
        String FADED_POSTER = "#81ecec";
        String GREEN_DARNER_TAIL = "#74b9ff";
        String SHY_MOMENT = "#a29bfe";
        String SOUR_LEMON = "#ffeaa7";
        String FIRST_DATE = "#fab1a0";
        String PINK_GLAMOUR = "#ff7675";
        String PICO_8_PINK = "#fd79a8";
        String YUE_GUANG_LAN_BLUE = "#1e3799";

        ArrayList<String> colors = new ArrayList<>();
        colors.add(LIGHT_GREENISH_BLUE);
        colors.add(FADED_POSTER);
        colors.add(GREEN_DARNER_TAIL);
        colors.add(SHY_MOMENT);
        colors.add(SOUR_LEMON);
        colors.add(FIRST_DATE);
        colors.add(PINK_GLAMOUR);
        colors.add(PICO_8_PINK);
        colors.add(YUE_GUANG_LAN_BLUE);

        Random random = new Random();

        return colors.get(random.nextInt(colors.size()));
    }

    private Effect getDarkerEffect(){
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(-1);
        colorAdjust.setBrightness(-0.3);

        return colorAdjust;
    }

    private Pane getSelectFolder(){
        Image basicPlus = new Image(App.class.getResource("/icons/icons8-folder-384.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(18);

        Label basicShape = new Label("Select Save Folder");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
        basicShape.setTextFill(Color.web("#F2C94B"));

        HBox basicShapeHBox = new HBox(basicPlusImageView, basicShape);
        basicShapeHBox.setAlignment(Pos.CENTER);
        basicShapeHBox.setSpacing(5);
        basicShapeHBox.setStyle("-fx-background-color: #644832;-fx-background-radius: 10");
        basicShapeHBox.setPrefHeight(30);

        basicShapeHBox.setOnMouseClicked(event -> {

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectFile = directoryChooser.showDialog(primaryStage);

            if(selectFile != null){
                System.out.println("new path: " + selectFile.getPath());
                Orchestrator.path = selectFile.getPath();
                fileDirectoryChoosen = true;

                basicShapePane.setEffect(null);
                loadFilePane.setEffect(null);


            }else{
                fileDirectoryChoosen = false;
                basicShapePane.setEffect(getDarkerEffect());
                loadFilePane.setEffect(getDarkerEffect());
            }

        });

        HBox.setHgrow(basicShapeHBox, Priority.ALWAYS);
        basicShapeHBox.setMinHeight(45);

        return basicShapeHBox;
    }

    private void getBasicShape(){
        Image basicPlus = new Image(App.class.getResource("/icons/icons8-plus-math-96.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(12);

        Label basicShape = new Label("Basic Shape");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
        basicShape.setTextFill(Color.web("#56CCF2"));

        basicShapePane = new HBox(basicPlusImageView, basicShape);
        basicShapePane.setAlignment(Pos.CENTER);
        basicShapePane.setSpacing(5);
        basicShapePane.setStyle("-fx-background-color: #355765;-fx-background-radius: 10");
        basicShapePane.setPrefHeight(30);

        basicShapePane.setOnMouseClicked(event -> {
            if(fileDirectoryChoosen){
                System.out.println("Add Basic Shape button clicked");

                scene.setRoot(app.getScenePanel(scene));
                primaryStage.setResizable(true);
                primaryStage.setMinWidth(300);
                primaryStage.sizeToScene();
                primaryStage.setMaximized(true);
            }
        });

        basicShapePane.setEffect(getDarkerEffect());

        HBox.setHgrow(basicShapePane, Priority.ALWAYS);
        basicShapePane.setMinHeight(45);
    }

    private void getLoadFile(){
        Image basicPlus = new Image(App.class.getResource("/icons/jsonIcon.png").toExternalForm());
        ImageView basicPlusImageView = new ImageView(basicPlus);
        basicPlusImageView.setSmooth(true);
        basicPlusImageView.setPreserveRatio(true);
        basicPlusImageView.setFitWidth(18);

        Label basicShape = new Label("Load JSON file");
        basicShape.setFont(Font.font("SF Pro Rounded", FontWeight.BOLD, 15));
        basicShape.setTextFill(Color.web("#56f28f"));

        loadFilePane = new HBox(basicPlusImageView, basicShape);
        loadFilePane.setAlignment(Pos.CENTER);
        loadFilePane.setSpacing(5);
        loadFilePane.setStyle("-fx-background-color: #35654f;-fx-background-radius: 10");
        loadFilePane.setPrefHeight(30);

        loadFilePane.setOnMouseClicked(event -> {
            if(fileDirectoryChoosen){
                System.out.println("Load JSON file button clicked");

                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                File selectedFile = fileChooser.showOpenDialog(loadFilePane.getScene().getWindow());

                System.out.println("Selected file: " + selectedFile);

                if(selectedFile != null){
                    scene.setRoot(app.getScenePanelWithLoadedFile(scene));
                    primaryStage.setResizable(true);
                    primaryStage.setMinWidth(300);
                    primaryStage.sizeToScene();
                    primaryStage.setMaximized(true);

                    app.loadBasicShapes(selectedFile);
                    app.loadNewCompositionShapes(selectedFile);
                    app.loadProcesses(selectedFile);
                    app.loadVariables(selectedFile);
                    app.loadPowerShapes(selectedFile);
                    app.loadParametricShapes(selectedFile);
                    app.loadShapeRules(selectedFile);

                }

            }

        });

        loadFilePane.setEffect(getDarkerEffect());

        HBox.setHgrow(loadFilePane, Priority.ALWAYS);
        loadFilePane.setMinHeight(45);
    }

    private Pane getBottomPane(){
        Label title = new Label("ALTERNATIVE SHAPER");
        title.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 20));
        title.setTextFill(Color.WHITE);

        Label subTitle = new Label("Create amazing designs with a simple system!");
        subTitle.setFont(Font.font("SF Pro Rounded", FontWeight.LIGHT, 15));
        subTitle.setTextFill(Color.WHITE);

        getBasicShape();
        getLoadFile();
        var horizontalPanel = new HBox(basicShapePane, loadFilePane);
        horizontalPanel.setSpacing(20);

        var verticalPanel = new VBox(title, subTitle,verticalGrower(), horizontalPanel, getSelectFolder());
        verticalPanel.setSpacing(20);
        verticalPanel.setPadding(new Insets(30,20,20,20));

        verticalPanel.setMaxHeight(300);
        verticalPanel.setMinHeight(300);

        return verticalPanel;
    }


    public static VBox verticalGrower(){
        var verticalGrower = new VBox();

        VBox.setVgrow(verticalGrower, Priority.ALWAYS);
        verticalGrower.setMaxHeight(Double.MAX_VALUE);

        return verticalGrower;
    }

    private Pane getTopPane(){

        Image iconImage = new Image(App.class.getResource("/icons/bigIcon.png").toExternalForm());
        ImageView iconImageView = new ImageView(iconImage);
        iconImageView.setSmooth(true);
        iconImageView.setPreserveRatio(true);
        iconImageView.setFitWidth(208);

        var verticalPanel = new VBox(iconImageView);
        verticalPanel.setMaxHeight(260);
        verticalPanel.setMinHeight(260);
        verticalPanel.setStyle("-fx-background-color: #333234");
        verticalPanel.setAlignment(Pos.CENTER);

        return verticalPanel;
    }

    @Override
    public void start(Stage primaryStage) {
        /*String toReplace = "existingproc(miguel)";
        String from = "pickone((existingproc(miguel)));";
        String with = "initialShape((shape( default )));";
        System.out.println(from.replaceAll(Pattern.quote(toReplace), with));
        Process teste = new Process("miguel", "istoFoiUmTeste", "oi");
        ArrayList<Process> arrayList = new ArrayList<>();
        arrayList.add(teste);
        Process.solveDependency(arrayList, new Process("pickone((existingProc(miguel)))"));*/

        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(App.class.getResource("/icons/appIcon.png").toExternalForm()));


        var mainPanel = new VBox(getTopPane(), getBottomPane());
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setStyle("-fx-background-color: #262528");

        scene = new Scene(mainPanel, 377, 560);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        scene.setFill(Color.web("#262528"));

        primaryStage.setTitle("Alternative Shaper");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.sizeToScene();

        primaryStage.setResizable(false);

        for(int i = 0; i < Screen.getScreens().size(); i++){
            Screen screen = Screen.getScreens().get(i);
            Rectangle2D bounds = screen.getVisualBounds();

            System.out.println("screen: " + screen);

            if(i == 1){
                primaryStage.setX(bounds.getMinX());
                primaryStage.setY(bounds.getMinY());

            }

        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}



