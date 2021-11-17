package marrf.iscte;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.SCALE;
import static marrf.iscte.BasicShape.colorToRGBString;
import static marrf.iscte.BasicShape.getRelativeLuminance;
import static marrf.iscte.NewCompositionShape.*;

public class Power implements CustomShape, ShapeWithVariables{

    private String name = "simplePower";
    private final UUID uuid;

    private final Group rightGroup = new Group();
    private final Group leftGroup = new Group();
    private final Group centerGroup = new Group();
    private VBox centerShape;

    private boolean hasLeft = true;
    private boolean hasRight = true;

    private boolean leftHasVariable = true;
    private boolean rightHasVariable = true;
    private boolean centerHasVariable = true;

    private String rightVariable = "X";
    private String leftVariable = "Y";
    private String centerVariable = "Z";

    private String leftValue = "0";
    private String rightValue = "0";

    private String rightTranslation = "1";
    private String leftTranslation = "1";

    private final Label rightLabel = new Label(rightVariable);
    private final Label leftLabel = new Label(leftVariable);
    private final Label customShapeLabel = new Label(centerVariable);

    private final Orchestrator orchestrator;

    private CustomShape customShape;

    private final HBox thumbnail = new HBox();

    private final SimpleBooleanProperty customShapeSelected = new SimpleBooleanProperty();

    private VBox verticalSection;

    private static final int NUMBER_COLUMNS_AND_ROWS = 40;


    public final Function<Double, Double> writeTranslateX;
    public final Function<Double, Double> writeTranslateY;

    private HBox translationXSection;
    private HBox translationYSection;

    public final DoubleProperty translateXProperty = new SimpleDoubleProperty(0.0);
    public final DoubleProperty translateYProperty = new SimpleDoubleProperty(0.0);


    public Function<String, String> writeTranslationXParametric;
    public Function<String, String> writeTranslationYParametric;

    private String verticalParametricTranslation = "";
    private String horizontalParametricTranslation = "";
    private HBox verticalParametricTranslationSection;
    private HBox horizontalParametricTranslationSection;

    private CustomShape selectedCustomShape;
    private double selectedCustomShapeWidth;
    private double selectedCustomShapeHeight;

    public UUID getUuid() {
        return uuid;
    }

    public Function<Double, Double> getWriteTranslateY() {
        return writeTranslateY;
    }

    public Function<Double, Double> getWriteTranslateX() {
        return writeTranslateX;
    }

    public String getRightTranslation() {
        return rightTranslation;
    }

    public String getLeftTranslation() {
        return leftTranslation;
    }

    public boolean getHasLeft() {
        return hasLeft;
    }

    public boolean getHasRight() {
        return hasRight;
    }

    public boolean getLeftHasVariable() {
        return leftHasVariable;
    }

    public boolean getRightHasVariable() {
        return rightHasVariable;
    }

    public boolean getCenterHasVariable() {
        return centerHasVariable;
    }

    public String getRightVariable() {
        return rightVariable;
    }

    public String getLeftVariable() {
        return leftVariable;
    }

    public String getCenterVariable() {
        return centerVariable;
    }

    public String getLeftValue() {
        return leftValue;
    }

    public String getRightValue() {
        return rightValue;
    }

    public void setUpTranslationXBox() {
        Label translationLabel = new Label("Translation X:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);


        System.out.println("valor do initial translation na box x da power: " + getInitialTranslation());
        TextField textField = new TextField(String.valueOf(getInitialTranslation().getX() / SCALE));
        textField.setPromptText(String.valueOf(getInitialTranslation().getX() / SCALE));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Slider translationXSlider = new Slider();
        translationXSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setMin(- NUMBER_COLUMNS_AND_ROWS);
        translationXSlider.setValue(getInitialTranslation().getX() / SCALE);

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

        translationXSlider.setMajorTickUnit(0.1);
        translationXSlider.setMinorTickCount(0);
        translationXSlider.setSnapToTicks(true);


        translationXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double truncatedDouble = BigDecimal.valueOf(newValue.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            textField.setText(String.valueOf(truncatedDouble));

            this.addTranslationX((newValue.doubleValue() - oldValue.doubleValue())* SCALE);
            translateXProperty.setValue(truncatedDouble * SCALE );
            writeTranslateX.apply(truncatedDouble * SCALE);
        });

        translationXSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationXSlider, Priority.ALWAYS);

        translationXSection = new HBox(translationLabel, translationXSlider, textField);
        translationXSection.setPadding(new Insets(10, 10, 10, 15));
        translationXSection.setAlignment(Pos.CENTER_LEFT);
        translationXSection.setMinHeight(30);
        translationXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        translationXSection.setSpacing(20);

    }


    public void setUpTranslationYBox() {
        Label translationLabel = new Label("Translation Y:");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField((String.valueOf(-getInitialTranslation().getY() / SCALE)));
        textField.setPromptText((String.valueOf(-getInitialTranslation().getY() / SCALE)));
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(60);
        textField.setAlignment(Pos.CENTER);

        Slider translationYSlider = new Slider();
        translationYSlider.setMax(NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setMin(- NUMBER_COLUMNS_AND_ROWS);
        translationYSlider.setValue(-getInitialTranslation().getY() / SCALE);

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

            translateYProperty.setValue(truncatedDouble * SCALE);

            this.addTranslationY((newValue.doubleValue() - oldValue.doubleValue())* -SCALE);

            writeTranslateY.apply(truncatedDouble * -SCALE);
        });

        translationYSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(translationYSlider, Priority.ALWAYS);

        translationYSection = new HBox(translationLabel, translationYSlider, textField);
        translationYSection.setPadding(new Insets(10, 10, 10, 15));
        translationYSection.setAlignment(Pos.CENTER_LEFT);
        translationYSection.setMinHeight(30);
        translationYSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        translationYSection.setSpacing(15);

    }


    public double getTranslateX() {
        return centerGroup.getTranslateX();
    }

    public double getTranslateY() {
        return centerGroup.getTranslateY();
    }


    public void addTranslationX(double value) {
        centerGroup.setTranslateX(getTranslateX() + value);
    }

    public void addTranslationY(double value) {
        centerGroup.setTranslateY(getTranslateY() + value);
    }

    public void setTranslateX(double value) {
        centerGroup.setTranslateX(value);
    }

    public void setTranslateY(double value) {
        centerGroup.setTranslateY(value);
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> value) {
        centerGroup.setOnMouseClicked(value);
    }

    public void setOnDragDetected(EventHandler<? super MouseEvent> value) {
        centerGroup.setOnDragDetected(value);
    }


    public Power(String name, UUID uuid, boolean hasLeft, boolean hasRight, boolean leftHasVariable, boolean rightHasVariable, boolean centerHasVariable, String rightVariable, String leftVariable, String centerVariable, String leftValue, String rightValue, String rightTranslation, String leftTranslation,  Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<String, Double> proceedWhenDeletingFromThumbnail, Orchestrator orchestrator) {
        this.name = name;
        this.uuid = uuid;

        this.hasLeft = hasLeft;
        this.hasRight = hasRight;

        this.orchestrator = orchestrator;

        this.leftHasVariable = leftHasVariable;
        this.rightHasVariable = rightHasVariable;
        this.centerHasVariable = centerHasVariable;

        this.rightVariable = rightVariable;
        this.leftVariable = leftVariable;
        this.centerVariable = centerVariable;

        this.leftValue = leftValue;
        this.rightValue = rightValue;

        this.rightTranslation = rightTranslation;
        this.leftTranslation = leftTranslation;

        if(!centerHasVariable){

        }

        this.writeTranslateX = writeTranslateX;
        this.writeTranslateY = writeTranslateY;

        setUpTranslationYBox();
        setUpTranslationXBox();

        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
        setUpVerticalSection();

        setUpTranslationYBox();
        setUpTranslationXBox();
    }

    public Power(Function<String, String> parametricXTranslation, Function<String, String> parametricYTranslation, String name, UUID uuid, boolean hasLeft, boolean hasRight, boolean leftHasVariable, boolean rightHasVariable, boolean centerHasVariable, String rightVariable, String leftVariable, String centerVariable, String leftValue, String rightValue, String rightTranslation, String leftTranslation, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<Node, Double> proceedWhenDeleting, Orchestrator orchestrator) {
        this.name = name;
        this.uuid = uuid;

        this.proceedWhenDeleting = proceedWhenDeleting;

        this.writeTranslationXParametric = parametricXTranslation;
        this.writeTranslationYParametric = parametricYTranslation;

        this.verticalParametricTranslation = parametricYTranslation.apply(null);
        this.horizontalParametricTranslation = parametricXTranslation.apply(null);

        this.orchestrator = orchestrator;

        this.hasLeft = hasLeft;
        this.hasRight = hasRight;

        this.leftHasVariable = leftHasVariable;
        this.rightHasVariable = rightHasVariable;
        this.centerHasVariable = centerHasVariable;

        this.rightVariable = rightVariable;
        this.leftVariable = leftVariable;
        this.centerVariable = centerVariable;

        this.leftValue = leftValue;
        this.rightValue = rightValue;

        this.rightTranslation = rightTranslation;
        this.leftTranslation = leftTranslation;

        if(!centerHasVariable){

        }

        this.writeTranslateX = writeTranslateX;
        this.writeTranslateY = writeTranslateY;

        setUpTranslationYBox();
        setUpTranslationXBox();

        setUpVerticalParametricTranslationSectionBox();
        setUpHorizontalParametricTranslationSectionBox();

    }

    public HBox getVerticalParametricTranslationSection() {
        return verticalParametricTranslationSection;
    }

    public HBox getHorizontalParametricTranslationSection() {
        return horizontalParametricTranslationSection;
    }

    public void setUpVerticalParametricTranslationSectionBox() {
        Label translationLabel = new Label("Parametric translation Y: ");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(verticalParametricTranslation);
        textField.setPromptText(verticalParametricTranslation);
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER);

        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                verticalParametricTranslation = textField.getText() + keyEvent.getText();
            }
            writeTranslationYParametric.apply(textField.getText() + keyEvent.getText() );

        });

        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);

        verticalParametricTranslationSection = new HBox(translationLabel, textField);
        verticalParametricTranslationSection.setPadding(new Insets(10, 10, 10, 15));
        verticalParametricTranslationSection.setAlignment(Pos.CENTER_LEFT);
        verticalParametricTranslationSection.setMinHeight(30);
        verticalParametricTranslationSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        verticalParametricTranslationSection.setSpacing(20);

    }

    public void setUpHorizontalParametricTranslationSectionBox() {
        Label translationLabel = new Label("Parametric translation X: ");
        translationLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        translationLabel.setTextFill(Color.web("#BDBDBD"));
        translationLabel.setWrapText(false);

        TextField textField = new TextField(horizontalParametricTranslation);
        textField.setPromptText(horizontalParametricTranslation);
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setAlignment(Pos.CENTER);

        textField.setOnKeyPressed(keyEvent -> {
            /*if (keyEvent.getCode().equals(KeyCode.ENTER)) {*/
            horizontalParametricTranslation = textField.getText() + keyEvent.getText();
            /*}*/
            writeTranslationXParametric.apply(textField.getText() + keyEvent.getText());
        });

        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);

        horizontalParametricTranslationSection = new HBox(translationLabel, textField);
        horizontalParametricTranslationSection.setPadding(new Insets(10, 10, 10, 15));
        horizontalParametricTranslationSection.setAlignment(Pos.CENTER_LEFT);
        horizontalParametricTranslationSection.setMinHeight(30);
        horizontalParametricTranslationSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        horizontalParametricTranslationSection.setSpacing(20);

    }

    public Point2D getInitialTranslation(){
        return new Point2D(writeTranslateX.apply(null), writeTranslateY.apply(null));
    }

    private Function<String, Double> proceedWhenDeletingFromThumbnail;
    private Function<Node, Double> proceedWhenDeleting;

    public Power(Function<String, Double> proceedWhenDeletingFromThumbnail, Orchestrator orchestrator){
        writeTranslateX = a -> 0.0;
        writeTranslateY = a -> 0.0;

        uuid = UUID.randomUUID();

        this.orchestrator = orchestrator;

        setUpVerticalSection();

        setUpTranslationYBox();
        setUpTranslationXBox();

        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
    }

    private void getRightGroup(double translationX, double translationY){
        System.out.println("translation x hehe: " + translationX);
        Circle firstRightCircle = new Circle();
        firstRightCircle.setRadius(3);
        firstRightCircle.setFill(Color.web("#B2B2B2"));
        firstRightCircle.setTranslateX(translationX + 9 );
        firstRightCircle.setTranslateY(- translationY / 2.0);


        Circle secondRightCircle = new Circle();
        secondRightCircle.setRadius(3);
        secondRightCircle.setFill(Color.web("#999999"));
        secondRightCircle.setTranslateX(translationX + 20 );
        secondRightCircle.setTranslateY(- translationY / 2.0);

        Circle thirdRightCircle = new Circle();
        thirdRightCircle.setRadius(3);
        thirdRightCircle.setFill(Color.web("#737373"));
        thirdRightCircle.setTranslateX(translationX + 31 );
        thirdRightCircle.setTranslateY(- translationY / 2.0);

        VBox addRight = new VBox();
        addRight.setPrefSize(translationX, translationY);
        addRight.setTranslateX(translationX + SCALE);
        addRight.setTranslateY(- translationY);
        addRight.setStyle(getDashStyle());

        addRight.getChildren().add(rightLabel);
        addRight.setAlignment(Pos.CENTER);

        rightGroup.getChildren().clear();
        rightGroup.getChildren().addAll(firstRightCircle, secondRightCircle, thirdRightCircle, addRight);

    }

    private void getRightGroup(){
        Circle firstRightCircle = new Circle();
        firstRightCircle.setRadius(3);
        firstRightCircle.setFill(Color.web("#B2B2B2"));
        firstRightCircle.setTranslateX(SCALE + 9 );
        firstRightCircle.setTranslateY(- SCALE / 2.0);


        Circle secondRightCircle = new Circle();
        secondRightCircle.setRadius(3);
        secondRightCircle.setFill(Color.web("#999999"));
        secondRightCircle.setTranslateX(SCALE + 20 );
        secondRightCircle.setTranslateY(- SCALE / 2.0);

        Circle thirdRightCircle = new Circle();
        thirdRightCircle.setRadius(3);
        thirdRightCircle.setFill(Color.web("#737373"));
        thirdRightCircle.setTranslateX(SCALE + 31 );
        thirdRightCircle.setTranslateY(- SCALE / 2.0);

        VBox addRight = new VBox();
        addRight.setPrefSize(SCALE, SCALE);
        addRight.setTranslateX(SCALE * 2);
        addRight.setTranslateY(- SCALE);
        addRight.setStyle(getDashStyle());

        addRight.getChildren().add(rightLabel);
        addRight.setAlignment(Pos.CENTER);

        rightGroup.getChildren().clear();
        rightGroup.getChildren().addAll(firstRightCircle, secondRightCircle, thirdRightCircle, addRight);
    }

    private void getLeftGroup(double translationX, double translationY){
        Circle firstLeftCircle = new Circle();
        firstLeftCircle.setRadius(3);
        firstLeftCircle.setFill(Color.web("#B2B2B2"));
        firstLeftCircle.setTranslateY(- (translationY + 9) );
        firstLeftCircle.setTranslateX( translationX / 2.0);


        Circle secondLeftCircle = new Circle();
        secondLeftCircle.setRadius(3);
        secondLeftCircle.setFill(Color.web("#999999"));
        secondLeftCircle.setTranslateY( - (translationY + 20) );
        secondLeftCircle.setTranslateX( translationX / 2.0);

        Circle thirdLeftCircle = new Circle();
        thirdLeftCircle.setRadius(3);
        thirdLeftCircle.setFill(Color.web("#737373"));
        thirdLeftCircle.setTranslateY(- (translationY + 31) );
        thirdLeftCircle.setTranslateX( translationX / 2.0);


        VBox addLeft = new VBox();
        addLeft.setPrefSize(translationX, translationY);
        addLeft.setTranslateY(- translationY  * 2 - SCALE);
        addLeft.setStyle(getDashStyle());

        addLeft.getChildren().add(leftLabel);
        addLeft.setAlignment(Pos.CENTER);

        leftGroup.getChildren().clear();
        leftGroup.getChildren().addAll(firstLeftCircle, secondLeftCircle, thirdLeftCircle, addLeft);

    }

    private void getLeftGroup(){
        Circle firstLeftCircle = new Circle();
        firstLeftCircle.setRadius(3);
        firstLeftCircle.setFill(Color.web("#B2B2B2"));
        firstLeftCircle.setTranslateY(- (SCALE + 9) );
        firstLeftCircle.setTranslateX( SCALE / 2.0);

        Circle secondLeftCircle = new Circle();
        secondLeftCircle.setRadius(3);
        secondLeftCircle.setFill(Color.web("#999999"));
        secondLeftCircle.setTranslateY( - (SCALE + 20) );
        secondLeftCircle.setTranslateX( SCALE / 2.0);

        Circle thirdLeftCircle = new Circle();
        thirdLeftCircle.setRadius(3);
        thirdLeftCircle.setFill(Color.web("#737373"));
        thirdLeftCircle.setTranslateY(- (SCALE + 31) );
        thirdLeftCircle.setTranslateX( SCALE / 2.0);


        VBox addLeft = new VBox();
        addLeft.setPrefSize(SCALE, SCALE);
        addLeft.setTranslateY(- SCALE * 3);
        addLeft.setStyle(getDashStyle());

        addLeft.getChildren().add(leftLabel);
        addLeft.setAlignment(Pos.CENTER);

        leftGroup.getChildren().clear();
        leftGroup.getChildren().addAll(firstLeftCircle, secondLeftCircle, thirdLeftCircle, addLeft);
    }

    private String getDashStyle(){
        return  "-fx-background-color: #737373;-fx-background-radius: 6px; -fx-border-color: rgba(255,255,255,0.5); -fx-border-style: dashed; -fx-border-width: 3; -fx-border-radius: 3; -fx-stroke-dash-offset: 5; -fx-stroke-line-cap: round;";
    }

    private void setUpLabels(){
        leftLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        rightLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        customShapeLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
    }

    public Group getCenterGroup() {
        return centerGroup;
    }

    private void removeShapeFromCenterShapeAndAddVariableShapeLabel(){
        centerShape.getChildren().clear();

        VBox toPut = new VBox(customShapeLabel);
        toPut.setAlignment(Pos.CENTER);
        toPut.setPrefSize(SCALE, SCALE);
        toPut.setStyle("-fx-background-color: #B2B2B2; -fx-background-radius: 6px;");
        centerShape.getChildren().add(toPut);
        centerShape.setTranslateY(-SCALE);
        getRightGroup();
        getLeftGroup();
    }

    private void updateBoxesAndTranslations(){
        centerShape.getChildren().clear();

        if(selectedCustomShape instanceof BasicShape){
            setUpBasicShapeOnCenterGroup();
        }else if(selectedCustomShape instanceof NewCompositionShape){
            setUpCompositionShapeOnCenterGroup();
        }
    }

    public void setUpBasicShapeOnCenterGroup(BasicShape dropped){

        this.selectedCustomShape = dropped;
        setUpBasicShapeOnCenterGroup();
    }

    public void setUpBasicShapeOnCenterGroup(){
        BasicShape dropped = (BasicShape) selectedCustomShape;

        NewCompositionShape.Information translationX = new NewCompositionShape.Information(dropped.getUUID().toString(), 0.0);
        NewCompositionShape.Information translationY = new NewCompositionShape.Information(dropped.getUUID().toString(), 0.0);

        BasicShape toUse = orchestrator.getCopyOfBasicShape(dropped.getUUID().toString(), translationX.getConsumer(), translationY.getConsumer(), null);
        centerShape.getChildren().add(toUse.getRectangle());

        selectedCustomShapeWidth = toUse.getWidth();
        selectedCustomShapeHeight = toUse.getHeight();
        centerShape.setTranslateY(-selectedCustomShapeHeight);
        getRightGroup(selectedCustomShapeWidth, selectedCustomShapeHeight);
        getLeftGroup(selectedCustomShapeWidth, selectedCustomShapeHeight);
    }

    public void setUpCompositionShapeOnCenterGroup(){
        System.err.println("HERE I AM!");
        centerShape.setTranslateY(0);
        centerShape.setTranslateX(0);


        NewCompositionShape dropped = (NewCompositionShape) selectedCustomShape;
        Pane toAdd;
        NewCompositionShape newCompositionShape = new NewCompositionShape(orchestrator, new VBox(), teste -> null, teste -> null);
        toAdd = newCompositionShape.addNewCompositionShape(dropped, false);

        double maximumTranslationX = -1000;
        double minimumTranslationX = 10000;
        double maximumTranslationY = -1000;
        double minimumTranslationY = 10000;

        for(Node node: toAdd.getChildren()){
            maximumTranslationX  = getMaximumTranslationX(node, maximumTranslationX);
            minimumTranslationX = getMinimumTranslationX(node, minimumTranslationX);
            maximumTranslationY = getMaximumTranslationY(node, maximumTranslationY);
            minimumTranslationY = getMinimumTranslationY(node, minimumTranslationY);
        }
        System.out.println("old minimum translationY: " + minimumTranslationY + ", old maximum translationY: " + maximumTranslationY);

        toAdd.setTranslateX(toAdd.getTranslateX() - minimumTranslationX);

        centerShape.getChildren().add(toAdd);

        toAdd.layout();
        centerShape.layout();

        double finalMinimumTranslationX = minimumTranslationX;
        double finalMinimumTranslationY = minimumTranslationY;
        Platform.runLater(() -> {
            double newMaximumX = -1000;
            double newMaximumY = -1000;
            for(Node node: toAdd.getChildren()){
                newMaximumX  = getMaximumTranslationX(node, newMaximumX);
                newMaximumY = getMaximumTranslationY(node, newMaximumY);
            }
            System.out.println("new minimum translationY: " + finalMinimumTranslationY +", new maximum translationY: " + newMaximumY);
            toAdd.setTranslateY(toAdd.getTranslateY() - newMaximumY);
            double width = Math.abs(finalMinimumTranslationX - newMaximumX);


            double height = Math.abs(finalMinimumTranslationY) + Math.abs(newMaximumY);

            if(finalMinimumTranslationY < 0 && newMaximumY < 0){
                height = Math.abs(finalMinimumTranslationY) - Math.abs(newMaximumY);
            }

            //getRightGroup(width, toAdd.getBoundsInParent().getHeight());
            //getLeftGroup(width, toAdd.getBoundsInParent().getHeight());
            getRightGroup(width, height);
            getLeftGroup(width, height);

        });

    }

    public Node getEditorVisualization() {
        setUpLabels();

        VBox toPut = new VBox();
        toPut.setPrefSize(SCALE, SCALE);
        toPut.setStyle("-fx-background-color: #B2B2B2; -fx-background-radius: 6px;");

        centerShape = new VBox(toPut);

        centerShape.getChildren().add(customShapeLabel);
        centerShape.setAlignment(Pos.CENTER);

        if(selectedCustomShape != null && selectedCustomShape instanceof BasicShape){
            addSelectedCustomShapeToCenterShape();
        }else{
            centerShape.setTranslateY(-SCALE);
        }

        centerShape.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (db.hasString()) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        });

        centerShape.setOnDragDropped(dragEvent -> {
            dragEvent.acceptTransferModes(TransferMode.ANY);
            System.out.println("dragDropped in Power");

            Dragboard db = dragEvent.getDragboard();
            if (db.hasString()) {
                System.out.println("this was dropped: " + db.getString());


                if (!db.getString().contains("[")) {
                    System.out.println("I was dropped a simple shape in the power");
                } else {
                    System.out.println("I was dropped a composition shape");
                }

                customShape = App.inDragCustomShape;
                customShapeLabel.setText(customShape.getShapeName());

                centerShape.getChildren().clear();
                if(customShape instanceof BasicShape){
                    setUpBasicShapeOnCenterGroup((BasicShape) customShape);
                }else if(customShape instanceof NewCompositionShape){
                    this.selectedCustomShape = customShape;
                    setUpCompositionShapeOnCenterGroup();
                }

                customShapeSelected.set(true);
                centerHasVariable = false;

            }
            dragEvent.consume();
        });

        setUpCenterGroup();

        if (selectedCustomShape != null){
            centerShape.getChildren().clear();

            System.out.println("centerShape.getLayoutBounds().getWidth(): " + centerShape.getLayoutBounds().getWidth());


            if(selectedCustomShape instanceof NewCompositionShape){
                setUpCompositionShapeOnCenterGroup();
            }else{
                setUpBasicShapeOnCenterGroup();
            }

        }else{
            getRightGroup();
            getLeftGroup();

        }


            centerGroup.setTranslateX(0);
            centerGroup.setTranslateY(0);





        if(proceedWhenDeleting != null){
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setId("betterMenuItem");

            MenuItem menuItem = new MenuItem("Delete");
            menuItem.setStyle("-fx-text-fill: red");
            contextMenu.getItems().add(menuItem);

            menuItem.setOnAction(actionEvent -> {
                System.err.println("VOU APAGAR AQUI!");

                proceedWhenDeleting.apply(centerGroup);
                ((Pane) centerGroup.getParent()).getChildren().remove(centerGroup);
            });

            centerGroup.setOnContextMenuRequested(contextMenuEvent -> {
                contextMenu.show(centerGroup, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

        }



        return centerGroup;
    }

    private void setUpCenterGroup(){
        centerGroup.getChildren().clear();
        if (hasRight && hasLeft) {
            centerGroup.getChildren().addAll(centerShape, rightGroup, leftGroup);
        }else{
            if (hasRight)
                centerGroup.getChildren().addAll(centerShape, rightGroup);
            if (hasLeft)
                centerGroup.getChildren().addAll(centerShape, leftGroup);
        }

        if (!hasRight && !hasLeft) {
            centerGroup.getChildren().addAll(centerShape);

        }
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
    public Pane getTranslationXSection() {
        return verticalSection;
    }

    public Pane getRealTranslationSection() {
        VBox vBox = new VBox();
        vBox.setSpacing(20);

        vBox.getChildren().addAll(translationXSection, translationYSection);
        return vBox;
    }

    public Pane getParametricTranslationSection(){
        VBox vBox = new VBox();
        vBox.setSpacing(20);

        vBox.getChildren().addAll(horizontalParametricTranslationSection, verticalParametricTranslationSection);
        return vBox;
    }

    private void setUpVerticalSection(){
        VBox horizontalBox = getHorizontalBox();
        horizontalBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        horizontalBox.setPadding(new Insets(10, 10, 10, 15));

        VBox verticalBox = getVerticalBox();
        verticalBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        verticalBox.setPadding(new Insets(10, 10, 10, 15));

        VBox centerBox = getCenterBox();
        centerBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 20");
        centerBox.setPadding(new Insets(10, 10, 10, 15));


        verticalSection = new VBox(horizontalBox, verticalBox, centerBox);
        verticalSection.setAlignment(Pos.CENTER_LEFT);
        verticalSection.setMinHeight(30);
        verticalSection.setSpacing(20);
    }

    private VBox getHorizontalBox(){
        Label horizontalPowerCheckboxToUseLabel = new Label("Horizontal power: ");
        styleLabel(horizontalPowerCheckboxToUseLabel);

        CheckBox checkboxToUse = new CheckBox("Horizontal power");
        checkboxToUse.setSelected(true);

        Label horizontalVariableCheckboxLabel = new Label("Horizontal Variable: ");
        styleLabel(horizontalVariableCheckboxLabel);

        CheckBox checkBoxVariable = new CheckBox("Horizontal Variable");
        checkBoxVariable.setSelected(true);


        Label variableLabel = new Label("Variable: ");
        styleLabel(variableLabel);

        TextField textFieldVariable = new TextField(rightVariable);
        textFieldVariable.setPromptText("1");
        styleTextField(textFieldVariable);

        textFieldVariable.textProperty().addListener((observable, oldValue, newValue) -> {
            if(rightHasVariable){
                if (!newValue.matches("[a-zA-Z]+")){
                    textFieldVariable.setText(oldValue);
                    rightLabel.setText(oldValue);
                }else{
                    rightVariable = newValue;
                    rightLabel.setText(newValue);
                }
            }else{
                if(!newValue.matches("[0-9]+")){
                    textFieldVariable.setText(oldValue);
                    rightLabel.setText(oldValue);
                }else{
                    rightValue = newValue;
                    rightLabel.setText(newValue);
                }
            }
        });

        checkBoxVariable.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            rightHasVariable = newValue;
            rightLabel.setText(rightHasVariable ? rightVariable : rightValue);
            textFieldVariable.setText(rightHasVariable ? rightVariable : rightValue);
        });

        Label translationLabel = new Label("Translation: ");
        styleLabel(translationLabel);

        TextField textFieldTranslation = new TextField(rightTranslation);
        textFieldTranslation.setPromptText("1");
        styleTextField(textFieldTranslation);

        textFieldTranslation.textProperty().addListener((observableValue, s, t1) -> rightTranslation = t1);


        HBox fistHBox = new HBox(horizontalPowerCheckboxToUseLabel,checkboxToUse);
        fistHBox.setSpacing(10);

        HBox secondHBox = new HBox(horizontalVariableCheckboxLabel, checkBoxVariable);
        secondHBox.setSpacing(10);

        HBox thirdHBox = new HBox(variableLabel, textFieldVariable, getVerticalSeparator(), translationLabel, textFieldTranslation);
        thirdHBox.setSpacing(10);

        VBox vBox = new VBox(fistHBox, secondHBox, thirdHBox);
        vBox.setSpacing(10);

        checkboxToUse.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(!newValue && !hasLeft){
                checkboxToUse.setSelected(true);
            }else{
                hasRight = newValue;
                setUpCenterGroup();
                if(oldValue != null && oldValue != newValue){
                    if(!hasRight){
                        vBox.getChildren().clear();
                        vBox.getChildren().add(fistHBox);
                    }else{
                        vBox.getChildren().clear();
                        vBox.getChildren().addAll(fistHBox, secondHBox,thirdHBox);
                    }
                }
            }


        });

        return vBox;
    }

    private VBox getVerticalBox(){
        Label verticalPowerCheckboxToUseLabel = new Label("Vertical power: ");
        styleLabel(verticalPowerCheckboxToUseLabel);

        CheckBox checkboxToUse = new CheckBox("Vertical power");
        checkboxToUse.setSelected(true);

        Label verticalVariableCheckboxLabel = new Label("Vertical Variable: ");
        styleLabel(verticalVariableCheckboxLabel);

        CheckBox checkBoxVariable = new CheckBox("Vertical Variable");
        checkBoxVariable.setSelected(true);


        TextField textFieldVariable = new TextField(leftVariable);
        textFieldVariable.setPromptText("1");
        styleTextField(textFieldVariable);

        textFieldVariable.textProperty().addListener((observable, oldValue, newValue) -> {
            if(leftHasVariable){
                if (!newValue.matches("[a-zA-Z]+")){
                    textFieldVariable.setText(oldValue);
                    leftLabel.setText(oldValue);
                }else{
                    leftVariable = newValue;
                    leftLabel.setText(newValue);
                }

            }else{
                if(!newValue.matches("[0-9]+")){
                    textFieldVariable.setText(oldValue);
                    leftLabel.setText(oldValue);
                }else{
                    leftValue = newValue;
                    leftLabel.setText(newValue);
                }
            }
        });

        checkBoxVariable.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            leftHasVariable = newValue;
            leftLabel.setText(leftHasVariable ? leftVariable : leftValue);
            textFieldVariable.setText(leftHasVariable ? leftVariable : leftValue);
        });

        TextField textFieldTranslation = new TextField(leftTranslation);
        textFieldTranslation.setPromptText("1");
        styleTextField(textFieldTranslation);

        textFieldTranslation.textProperty().addListener((observableValue, s, t1) -> leftTranslation = t1);


        HBox fistHBox = new HBox(verticalPowerCheckboxToUseLabel,checkboxToUse);
        fistHBox.setSpacing(10);

        HBox secondHBox = new HBox(verticalVariableCheckboxLabel, checkBoxVariable);
        secondHBox.setSpacing(10);

        Label variableLabel = new Label("Variable: ");
        styleLabel(variableLabel);

        Label translationLabel = new Label("Translation: ");
        styleLabel(translationLabel);

        HBox thirdHBox = new HBox(variableLabel, textFieldVariable, getVerticalSeparator(),translationLabel, textFieldTranslation);
        thirdHBox.setSpacing(10);
        thirdHBox.setMaxHeight(30);

        VBox vBox = new VBox(fistHBox, secondHBox, thirdHBox);
        vBox.setSpacing(10);

        checkboxToUse.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(!newValue && !hasRight){
                checkboxToUse.setSelected(true);
            }else{
                hasLeft = newValue;
                setUpCenterGroup();
                if(oldValue != null && oldValue != newValue){
                    if(!hasLeft){
                        vBox.getChildren().clear();
                        vBox.getChildren().add(fistHBox);
                    }else{
                        vBox.getChildren().clear();
                        vBox.getChildren().addAll(fistHBox, secondHBox,thirdHBox);
                    }
                }
            }


        });

        return vBox;
    }

    private Pane getVerticalSeparator(){
        Pane rectangle = new Pane();
        rectangle.setPrefWidth(8);
        rectangle.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        rectangle.setMaxHeight(Double.MAX_VALUE);
        rectangle.setId("separator");

        rectangle.setMinWidth(6);

        return rectangle;
    }

    private void addSelectedCustomShapeToCenterShape(){
        centerShape.getChildren().clear();
        BasicShape dropped = (BasicShape) selectedCustomShape;

        NewCompositionShape.Information translationX = new NewCompositionShape.Information(dropped.getUUID().toString(), 0.0);
        NewCompositionShape.Information translationY = new NewCompositionShape.Information(dropped.getUUID().toString(), 0.0);


        BasicShape toUse = orchestrator.getCopyOfBasicShape(dropped.getUUID().toString(), translationX.getConsumer(), translationY.getConsumer(), null);
        Pane toAdd = toUse.getRectangle();
        System.out.println("width coiso: " + toUse.getWidth());

        selectedCustomShapeWidth = toUse.getWidth();
        selectedCustomShapeHeight = toUse.getHeight();

        centerShape.getChildren().add(toAdd);

        centerShape.setTranslateY(-selectedCustomShapeHeight);
    }

    private VBox getCenterBox(){
        Label shapeVariableToUseLabel = new Label("Shape variable: ");
        styleLabel(shapeVariableToUseLabel);

        CheckBox checkboxToUse = new CheckBox("Shape variable");
        checkboxToUse.setSelected(true);


        Label variableLabel = new Label("Variable: ");
        styleLabel(variableLabel);

        TextField textFieldVariable = new TextField(centerVariable);
        textFieldVariable.setPromptText("1");
        styleTextField(textFieldVariable);

        textFieldVariable.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if(centerHasVariable){
                if (!newValue.matches("[a-zA-Z]+")){
                    textFieldVariable.setText(oldValue);
                    customShapeLabel.setText(oldValue);

                }else{
                    centerVariable = newValue;
                    customShapeLabel.setText(newValue);
                }
            }
        });


        HBox fistHBox = new HBox(shapeVariableToUseLabel,checkboxToUse);
        fistHBox.setSpacing(10);

        HBox secondHBox = new HBox(variableLabel, textFieldVariable);
        secondHBox.setSpacing(10);

        VBox vBox = new VBox(fistHBox, secondHBox);
        vBox.setSpacing(10);

        checkboxToUse.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            centerHasVariable = newValue;
            if(oldValue != null && oldValue != newValue){
                if(!centerHasVariable & customShape == null){
                    checkboxToUse.setSelected(true);
                }

                if(!centerHasVariable && customShape != null){
                    //Use shape
                    vBox.getChildren().clear();
                    vBox.getChildren().add(fistHBox);
                    //addSelectedCustomShapeToCenterShape();
                    updateBoxesAndTranslations();
                }else{
                    //Use variable
                    vBox.getChildren().clear();
                    vBox.getChildren().addAll(fistHBox, secondHBox);

                    removeShapeFromCenterShapeAndAddVariableShapeLabel();
                    customShapeLabel.setText(centerVariable);

                }
            }

        });

        customShapeSelected.addListener((observableValue, oldValue, newValue) -> {
            if(newValue){
                centerHasVariable = false;
                checkboxToUse.setSelected(false);
            }
        });

        return vBox;
    }

    private void styleTextField(TextField textField){
        textField.setStyle("-fx-background-color: #333234; -fx-text-fill: #BDBDBD; -fx-highlight-text-fill: #078D55; -fx-highlight-fill: #6FCF97;");
        textField.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        textField.setPrefWidth(50);
        textField.setAlignment(Pos.CENTER_LEFT);
        textField.setMaxHeight(30);
    }

    public static void styleLabel(Label label){
        label.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 15));
        label.setTextFill(Color.web("#BDBDBD"));
        label.setMaxHeight(30);
    }

    public String getPowerToString(){
        StringBuilder toReturn = new StringBuilder();

        if((leftHasVariable && hasLeft) || (rightHasVariable && hasRight) || centerHasVariable){
            toReturn.append("shapeComposition(").append(name).append("(");

            StringJoiner joiner = new StringJoiner(",");

            if(leftHasVariable && hasLeft)
                joiner.add(leftVariable);

            if(rightHasVariable && hasRight)
                joiner.add(rightVariable);

            if(centerHasVariable)
                joiner.add(centerVariable);

            toReturn.append(joiner);

            toReturn.append("),");

            if(hasRight && hasLeft){
                toReturn.append("pot(pot");

                toReturn.append(centerHasVariable ? centerVariable : customShape.getShapeName());
                toReturn.append(",");
                toReturn.append(leftHasVariable ? leftVariable : leftValue);
                toReturn.append(",");
                toReturn.append("[1,0,0,0,1,0,0,").append(leftTranslation).append(",1]),");
                toReturn.append(rightHasVariable ? rightVariable : rightValue).append(",");
                toReturn.append("[1,0,0,0,1,0,").append(rightTranslation).append(",0,1])).");


            }else if(hasRight){
                toReturn.append("pot(");
                toReturn.append(centerHasVariable ? centerVariable : customShape.getShapeName());
                toReturn.append(",");
                toReturn.append(rightHasVariable ? rightVariable : rightValue);
                toReturn.append(",");
                toReturn.append("[1,0,0,0,1,0,").append(rightTranslation).append(",0,1])).");

            }else if(hasLeft){
                toReturn.append("pot(");
                toReturn.append(centerHasVariable ? centerVariable : customShape.getShapeName());
                toReturn.append(",");
                toReturn.append(leftHasVariable ? leftVariable : leftValue);
                toReturn.append(",");
                toReturn.append("[1,0,0,0,1,0,0,").append(leftTranslation).append(",1])).");

            }

        }else{
            //Caso não haja nenhuma variável!
            toReturn.append("shapeComposition(").append(name).append(",");

            if(hasLeft && hasRight){
                toReturn.append("pot(pot");

                toReturn.append(customShape.getShapeName());
                toReturn.append(",");
                toReturn.append(leftValue);
                toReturn.append(",");
                toReturn.append("[1,0,0,0,1,0,0,").append(leftTranslation).append(",1]),");
                toReturn.append( rightValue);
                toReturn.append("[1,0,0,0,1,0,").append(rightTranslation).append(",0,1])).");
            }else if(hasLeft){
                toReturn.append("pot(");
                toReturn.append(customShape.getShapeName());
                toReturn.append(",");
                toReturn.append(leftValue);
                toReturn.append(",");
                toReturn.append("[1,0,0,0,1,0,0,").append(leftTranslation).append(",1])).");
            }else if(hasRight){
                toReturn.append("pot(");
                toReturn.append(customShape.getShapeName());
                toReturn.append(",");
                toReturn.append(rightValue);
                toReturn.append(",");
                toReturn.append("[1,0,0,0,1,0,").append(rightTranslation).append(",0,1])).");
            }
        }

        return toReturn.toString();
    }


    public JSONObject getJSONObject(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", getUUID().toString());
        jsonObject.put("name", name);

        jsonObject.put("hasLeft", hasLeft);
        jsonObject.put("hasRight", hasRight);

        jsonObject.put("leftHasVariable", leftHasVariable);
        jsonObject.put("rightHasVariable", rightHasVariable);
        jsonObject.put("centerHasVariable", centerHasVariable);

        jsonObject.put("rightVariable", rightVariable);
        jsonObject.put("leftVariable", leftVariable);
        jsonObject.put("centerVariable", centerVariable);

        jsonObject.put("leftValue", leftValue);
        jsonObject.put("rightValue", rightValue);

        jsonObject.put("rightTranslation", rightTranslation);
        jsonObject.put("leftTranslation", leftTranslation);

        if(!centerHasVariable){
            jsonObject.put("customShapeID", customShape.getUUID().toString());
            jsonObject.put("customShapeName", customShape.getShapeName());
        }

        return jsonObject;
    }

    @Override
    public Pane getTranslationYSection() {
        return new VBox();
    }

    @Override
    public void redrawThumbnail() {
        thumbnail.getChildren().clear();

        addScreenshootAndTags();
    }


    private void addScreenshootAndTags() {
        thumbnail.getChildren().add(GridCanvas.takeScreenshootWithRoundedCornersAndLoadTemporarilyWithNode(centerGroup));

        //label tags
        VBox nameAndTagVBox = new VBox(StartMenu.verticalGrower());
        nameAndTagVBox.setSpacing(5);

        Label nameLabel = new Label(getShapeName());
        nameLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 12));
        nameLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)")));


        Label tagLabel = new Label("Power Shape");
        tagLabel.setFont(Font.font("SF Pro Rounded", FontWeight.BLACK, 10));
        tagLabel.setPadding(new Insets(3));
        tagLabel.setTextFill(getRelativeLuminance(Color.web("rgb(79,79,79)").darker()));
        tagLabel.setStyle("-fx-background-color: " + colorToRGBString(Color.web("rgb(79,79,79)").darker()) + "; -fx-background-radius: 3");

        HBox detailsHB = new HBox(tagLabel);
        detailsHB.setSpacing(10);

        nameAndTagVBox.getChildren().addAll(nameLabel, detailsHB);

        thumbnail.getChildren().add(nameAndTagVBox);
    }

    private void setThumbnailDeleting() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setId("betterMenuItem");

        MenuItem menuItem = new MenuItem("Delete Power Shape Item");
        menuItem.setStyle("-fx-text-fill: red");
        contextMenu.getItems().add(menuItem);

        menuItem.setOnAction(actionEvent -> {
            proceedWhenDeletingFromThumbnail.apply(getUUID().toString());
        });

        thumbnail.setOnContextMenuRequested(contextMenuEvent -> {
            contextMenu.show(thumbnail, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }

    @Override
    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier) {
        thumbnail.getChildren().clear();
        thumbnail.setMinWidth(0.0);
        thumbnail.setPadding(new Insets(10));
        thumbnail.setStyle("-fx-background-color: rgb(79,79,79); -fx-background-radius: 10");
        thumbnail.setSpacing(10);
        thumbnail.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(thumbnail, Priority.NEVER);

        thumbnail.getChildren().clear();

        //label tags
        addScreenshootAndTags();
        //end label tags

        thumbnail.setOnDragDetected(event -> {
            Dragboard db = thumbnail.startDragAndDrop(TransferMode.ANY);
            supplier.get();
            ClipboardContent content = new ClipboardContent();
            content.putString(toPutIntoDragbord.get());
            db.setContent(content);

            event.consume();
        });

        setThumbnailDeleting();

        return thumbnail;
    }

    @Override
    public int getVariablesNumber() {
        int numberOfVariables = 0;

        if(centerHasVariable)
            numberOfVariables++;
        if(hasRight && rightHasVariable)
            numberOfVariables++;
        if(hasLeft && leftHasVariable)
            numberOfVariables++;

        return numberOfVariables;
    }
}
