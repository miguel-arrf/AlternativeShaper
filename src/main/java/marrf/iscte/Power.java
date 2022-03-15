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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static marrf.iscte.App.SCALE;
import static marrf.iscte.BasicShape.colorToRGBString;
import static marrf.iscte.BasicShape.getRelativeLuminance;
import static marrf.iscte.ParametricCompositionShape.Information;


public class Power implements CustomShape, ShapeWithVariables{

    private String name = "simplePower";
    private final UUID uuid;

    private final Group rightGroup = new Group();
    private final Group leftGroup = new Group();
    private final Group centerGroup = new Group();
    private StackPane centerShape;

    private boolean hasLeft = true;
    private boolean hasRight = true;

    private boolean leftHasVariable = true;
    private boolean rightHasVariable = true;
    private boolean centerHasVariable = true;

    private ParametricVariable rightVariable = new ParametricVariable("X", "right variable");
    private ParametricVariable leftVariable = new ParametricVariable("Y", "left variable");
    private ParametricVariable centerVariable = new ParametricVariable("Z", "center variable");

    private String leftValue = "1";
    private String rightValue = "1";

    private String rightTranslation = "1";
    private TextField rightTextFieldTranslation;
    private String leftTranslation = "1";
    private TextField leftTextFieldTranslation;

    private final Label rightLabel = new Label(rightVariable.getVariableName());
    private final Label leftLabel = new Label(leftVariable.getVariableName());
    private final Label customShapeLabel = new Label(centerVariable.getVariableName());

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

    private CheckBox checkBoxCenterShapeVariable;

    private boolean isFigureVariable = false;

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

    public ParametricVariable getRightVariable() {
        return rightVariable;
    }

    public ParametricVariable getLeftVariable() {
        return leftVariable;
    }

    public ParametricVariable getCenterVariable() {
        return centerVariable;
    }

    public String getLeftValue() {
        return leftValue;
    }

    public String getRightValue() {
        return rightValue;
    }

    public CustomShape getCustomShape() {
        return customShape;
    }

    public boolean isFigureVariable() {
        return isFigureVariable;
    }

    public ArrayList<ParametricVariable> getOutputVariables() {
        ArrayList<ParametricVariable> toReturn = new ArrayList<>();
        if(centerHasVariable)
            toReturn.add(centerVariable);
        if(rightHasVariable && hasRight)
            toReturn.add(rightVariable);
        if(leftHasVariable && hasLeft)
            toReturn.add(leftVariable);
        return toReturn;
    }

    public ArrayList<ParametricVariable> getAllOutputVariables(){
        ArrayList<ParametricVariable> toReturn = new ArrayList<>();
        toReturn.add(centerVariable);
        toReturn.add(rightVariable);
        toReturn.add(leftVariable);

        return toReturn;
   }

    public void deleteCustomShape(String uuidToRemove){
        System.out.println("to remove: " + uuidToRemove);
        if(getCustomShape() != null && getCustomShape().getUUID().toString().equals(uuidToRemove)){
            if(!centerHasVariable){
                customShape = null;
                centerHasVariable = true;
                checkBoxCenterShapeVariable.setSelected(true);
            }
            //TODO Even if the center has a variable, we should remove the customShape no?


        }
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
        translationXSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
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
        translationYSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
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

    public void addShape(CustomShape shapeToAdd){
        customShape = shapeToAdd;

    }

    private void setUpCenterDesign(){
        customShapeLabel.setText(customShape.getShapeName());

        centerShape.getChildren().clear();
        if(customShape instanceof BasicShape){
            setUpBasicShapeOnCenterGroup((BasicShape) customShape);
        }else if(customShape instanceof NewCompositionShape ){
            this.selectedCustomShape = customShape;
            setUpCompositionShapeOnCenterGroup();
        }

        customShapeSelected.set(true);
        centerHasVariable = false;
    }

    public Power(boolean isFigureVariable, String name, UUID uuid, boolean hasLeft, boolean hasRight, boolean leftHasVariable, boolean rightHasVariable, boolean centerHasVariable, ParametricVariable rightVariable, ParametricVariable leftVariable, ParametricVariable centerVariable, String leftValue, String rightValue, String rightTranslation, String leftTranslation,  Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Orchestrator orchestrator, Function<String, Double> proceedWhenDeletingFromThumbnail) {
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

        this.isFigureVariable = isFigureVariable;
        if(isFigureVariable){
            System.out.println("ESTOU A CRIAR COPIA DE FIGURA VARIABEL");
            this.hasLeft = false;
            this.hasRight = false;
            this.leftHasVariable = false;
            this.rightHasVariable = false;
        }

        this.writeTranslateX = writeTranslateX;
        this.writeTranslateY = writeTranslateY;

        setUpTranslationYBox();
        setUpTranslationXBox();

        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;

        if(!isFigureVariable){
            setUpVerticalSection();
            setUpTranslationYBox();
            setUpTranslationXBox();
        }

    }

    public Power(boolean isFigureVariable, String name, UUID uuid, boolean hasLeft, boolean hasRight, boolean leftHasVariable, boolean rightHasVariable, boolean centerHasVariable, ParametricVariable rightVariable, ParametricVariable leftVariable, ParametricVariable centerVariable, String leftValue, String rightValue, String rightTranslation, String leftTranslation, Function<Double, Double> writeTranslateX, Function<Double, Double> writeTranslateY, Function<Node, Double> proceedWhenDeleting, Orchestrator orchestrator) {
        this.name = name;
        this.uuid = uuid;

        this.proceedWhenDeleting = proceedWhenDeleting;



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

        this.isFigureVariable = isFigureVariable;
        if(isFigureVariable){
            this.hasLeft = false;
            this.hasRight = false;
            this.leftHasVariable = false;
            this.rightHasVariable = false;
        }

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
        verticalParametricTranslationSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
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
        horizontalParametricTranslationSection.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
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

    public Power(String name, Function<String, Double> proceedWhenDeletingFromThumbnail, Orchestrator orchestrator){
        writeTranslateX = a -> 0.0;
        writeTranslateY = a -> 0.0;

        this.name = name;
        uuid = UUID.randomUUID();

        this.orchestrator = orchestrator;
        this.isFigureVariable = true;

        setUpVerticalSection();

        setUpTranslationYBox();
        setUpTranslationXBox();

        this.hasLeft = false;
        this.hasRight = false;
        this.leftHasVariable = false;
        this.rightHasVariable = false;

        this.proceedWhenDeletingFromThumbnail = proceedWhenDeletingFromThumbnail;
    }

    private void getRightGroup(double width, double height, double spacing){

        Circle firstRightCircle = new Circle();
        firstRightCircle.setRadius(3);
        firstRightCircle.setFill(Color.web("#B2B2B2"));
        firstRightCircle.setTranslateX(width + 9 );
        firstRightCircle.setTranslateY(- height / 2.0);

        Circle secondRightCircle = new Circle();
        secondRightCircle.setRadius(3);
        secondRightCircle.setFill(Color.web("#999999"));
        secondRightCircle.setTranslateX(width + 20 );
        secondRightCircle.setTranslateY(- height / 2.0);

        Circle thirdRightCircle = new Circle();
        thirdRightCircle.setRadius(3);
        thirdRightCircle.setFill(Color.web("#737373"));
        thirdRightCircle.setTranslateX(width + 31 );
        thirdRightCircle.setTranslateY(- height / 2.0);


        VBox addRight = new VBox();
        addRight.setPrefSize(width, height);
        addRight.setTranslateX(width + SCALE);
        addRight.setTranslateY(- height);
        addRight.setStyle(getDashStyle());

        addRight.getChildren().add(rightLabel);
        addRight.setAlignment(Pos.CENTER);

        Group toAdd = new Group();
        toAdd.getChildren().addAll(firstRightCircle, secondRightCircle, thirdRightCircle, addRight);

        rightGroup.setTranslateY(0);
        rightGroup.setTranslateX(0);
        rightGroup.getChildren().clear();
        rightGroup.getChildren().addAll(toAdd);
    }

    private void getRightGroup(double translationX, double translationY){
        getRightGroup(translationX, translationY, 0);
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
        getLeftGroup(translationX, translationY, 0);
    }

    private void getLeftGroup(double width, double height, double spacing){
        Circle firstLeftCircle = new Circle();
        firstLeftCircle.setRadius(3);
        firstLeftCircle.setFill(Color.web("#B2B2B2"));
        firstLeftCircle.setTranslateY(- (height + 9) );
        firstLeftCircle.setTranslateX( width / 2.0);


        Circle secondLeftCircle = new Circle();
        secondLeftCircle.setRadius(3);
        secondLeftCircle.setFill(Color.web("#999999"));
        secondLeftCircle.setTranslateY( - (height + 20) );
        secondLeftCircle.setTranslateX( width / 2.0);

        Circle thirdLeftCircle = new Circle();
        thirdLeftCircle.setRadius(3);
        thirdLeftCircle.setFill(Color.web("#737373"));
        thirdLeftCircle.setTranslateY(- (height + 31) );
        thirdLeftCircle.setTranslateX( width / 2.0);


        VBox addLeft = new VBox();
        addLeft.setPrefSize(width, height);
        addLeft.setTranslateY(- height  * 2 - SCALE);
        addLeft.setStyle(getDashStyle());

        addLeft.getChildren().add(leftLabel);
        addLeft.setAlignment(Pos.CENTER);

        Group toAdd = new Group();
        toAdd.getChildren().addAll(firstLeftCircle, secondLeftCircle, thirdLeftCircle, addLeft);


        leftGroup.setTranslateY(0);
        leftGroup.setTranslateX(0);
        leftGroup.getChildren().clear();
        leftGroup.getChildren().addAll(toAdd);

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

    public static String getDashStyle(){
        return  "-fx-background-color: #737373;-fx-background-radius: 6px; -fx-border-color: rgba(255,255,255,0.5); -fx-border-style: dashed; -fx-border-width: 3; -fx-border-radius: 3; -fx-stroke-dash-offset: 5; -fx-stroke-line-cap: round;";
    }

    public static String getDashStyle(int backgroundCornerRadius){
        return  "-fx-background-color: #737373;-fx-background-radius: " + backgroundCornerRadius + "px; -fx-border-color: rgba(255,255,255,0.5); -fx-border-style: dashed; -fx-border-width:  3 ; -fx-border-radius: " + backgroundCornerRadius + "; -fx-stroke-dash-offset: 5; -fx-stroke-line-cap: round;";
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

        leftGroup.setTranslateX(0);
        leftGroup.setTranslateY(0);

        rightGroup.setTranslateX(0);
        rightGroup.setTranslateY(0);

        updateLeftPowerUIWithoutDetail();
        updateLeftPowerUIWithDetail();
        updateRightPowerUIWithDetail();
        updateRightPowerUIWithoutDetail();
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

        Information translationX = new Information(dropped.getUUID().toString(), 0.0);
        Information translationY = new Information(dropped.getUUID().toString(), 0.0);

        BasicShape toUse = orchestrator.getCopyOfBasicShape(dropped.getUUID().toString(), translationX.getConsumer(), translationY.getConsumer(), null);
        centerShape.getChildren().add(toUse.getRectangle());

        selectedCustomShapeWidth = toUse.getWidth();
        selectedCustomShapeHeight = toUse.getHeight();
        centerShape.setTranslateY(-selectedCustomShapeHeight);
        getRightGroup(selectedCustomShapeWidth, selectedCustomShapeHeight);
        getLeftGroup(selectedCustomShapeWidth, selectedCustomShapeHeight);
    }

    private void correctLeftGroup(double minimumTranslationX, double maximumTranslationX, double minimumTranslationY, double maximumTranslationY){
        leftGroup.setTranslateX(0);
        leftGroup.setTranslateY(0);
        if(minimumTranslationY <= 0 && maximumTranslationY >= 0){
            //Quando estou a meio
            leftGroup.setTranslateY(leftGroup.getTranslateY() + maximumTranslationY);
        }else if(minimumTranslationY >= 0 && maximumTranslationY >= 0){
            //Quando está tudo bem baixo!
            leftGroup.setTranslateY(leftGroup.getTranslateY() + maximumTranslationY);
        }

        if(minimumTranslationX <= 0 && maximumTranslationX <= 0){
            //We are on the left side
            leftGroup.setTranslateX(leftGroup.getTranslateX() + minimumTranslationX);
        }else if(minimumTranslationX <= 0 && maximumTranslationX >= 0){
            //We are on both sides!
            leftGroup.setTranslateX(leftGroup.getTranslateX() + minimumTranslationX);
        }
    }

    private void correctRightGroup(double minimumTranslationX, double maximumTranslationX, double minimumTranslationY, double maximumTranslationY){
        rightGroup.setTranslateX(0);
        rightGroup.setTranslateY(0);
        if(minimumTranslationY <= 0 && maximumTranslationY >= 0){
            //Quando estou a meio
            rightGroup.setTranslateY(rightGroup.getTranslateY() + maximumTranslationY);
        }else if(minimumTranslationY >= 0 && maximumTranslationY >= 0){
            //Quando está tudo bem baixo!
            rightGroup.setTranslateY(rightGroup.getTranslateY() + maximumTranslationY);
        }

        if(minimumTranslationX <= 0 && maximumTranslationX <= 0){
            //We are on the left side
            rightGroup.setTranslateX(rightGroup.getTranslateX() + minimumTranslationX);
        }else if(minimumTranslationX <= 0 && maximumTranslationX >= 0){
            //We are on both sides!
            rightGroup.setTranslateX(rightGroup.getTranslateX() + minimumTranslationX);
        }
    }

    private void correctRightAndLeftGroups(double minimumTranslationX, double maximumTranslationX, double minimumTranslationY, double maximumTranslationY){
        rightGroup.setTranslateX(0);
        rightGroup.setTranslateY(0);

        leftGroup.setTranslateX(0);
        leftGroup.setTranslateY(0);
        if(minimumTranslationY <= 0 && maximumTranslationY >= 0){
            //Quando estou a meio
            rightGroup.setTranslateY(rightGroup.getTranslateY() + maximumTranslationY);
            leftGroup.setTranslateY(leftGroup.getTranslateY() + maximumTranslationY);
        }else if(minimumTranslationY >= 0 && maximumTranslationY >= 0){
            //Quando está tudo bem baixo!
            rightGroup.setTranslateY(rightGroup.getTranslateY() + maximumTranslationY);
            leftGroup.setTranslateY(leftGroup.getTranslateY() + maximumTranslationY);
        }

        if(minimumTranslationX <= 0 && maximumTranslationX <= 0){
            //We are on the left side
            rightGroup.setTranslateX(rightGroup.getTranslateX() + minimumTranslationX);
            leftGroup.setTranslateX(leftGroup.getTranslateX() + minimumTranslationX);
        }else if(minimumTranslationX <= 0 && maximumTranslationX >= 0){
            //We are on both sides!
            rightGroup.setTranslateX(rightGroup.getTranslateX() + minimumTranslationX);
            leftGroup.setTranslateX(leftGroup.getTranslateX() + minimumTranslationX);
        }
    }

    public void setRightGroupWithDetail(int repetitions, double translation){
        ArrayList<Double> values = getDroppedCompositionValues();

        var width = values.get(0);
        var height = values.get(1);
        var minimumTranslationX = values.get(2);
        var maximumTranslationX = values.get(3);
        var minimumTranslationY = values.get(4);
        var maximumTranslationY = values.get(5);

        if(centerHasVariable){
            width =  0.0 + SCALE;
            height = 0.0 + SCALE;
        }

        Pane hBox = new Pane();
        for(int i = 1; i <= repetitions; i++){
            VBox box = new VBox();
            box.setPrefSize(width, height);
            box.setTranslateX(i * translation * SCALE);
            box.setTranslateY(- height);
            box.setStyle(getDashStyle());

            hBox.getChildren().add(box);
        }

        rightGroup.setTranslateY(0);
        rightGroup.setTranslateX(0);
        rightGroup.getChildren().clear();
        rightGroup.getChildren().addAll(hBox);

        if(!centerHasVariable)
            correctRightGroup(minimumTranslationX, maximumTranslationX, minimumTranslationY, maximumTranslationY);

    }

    public void setLeftGroupWithDetail(int repetitions, double translation){
        ArrayList<Double> values = getDroppedCompositionValues();

        var width = values.get(0);
        var height = values.get(1);
        var minimumTranslationX = values.get(2);
        var maximumTranslationX = values.get(3);
        var minimumTranslationY = values.get(4);
        var maximumTranslationY = values.get(5);

        if(centerHasVariable){
            width =  0.0 + SCALE;
            height = 0.0 + SCALE;
        }

        Pane vBox = new Pane();
        for(int i = 1; i <= repetitions; i++){
            VBox box = new VBox();
            box.setPrefSize(width, height);
            box.setTranslateY(- height - i * translation * SCALE);
            box.setStyle(getDashStyle());

            vBox.getChildren().add(box);
        }


        leftGroup.setTranslateY(0);
        leftGroup.setTranslateX(0);
        leftGroup.getChildren().clear();
        leftGroup.getChildren().addAll(vBox);

        if(!centerHasVariable)
            correctLeftGroup(minimumTranslationX, maximumTranslationX, minimumTranslationY, maximumTranslationY);



    }

    private double getCorrectHeight(double minimumTranslationY, double maximumTranslationY){
        double tempHeight = 0;
        if(minimumTranslationY <= 0 && maximumTranslationY <= 0){
            tempHeight = Math.abs(minimumTranslationY);
        }else if(minimumTranslationY <= 0 && maximumTranslationY >= 0){
            tempHeight = Math.abs(minimumTranslationY) + maximumTranslationY;
        }else if(minimumTranslationY >= 0 && maximumTranslationY >= 0){
            tempHeight = maximumTranslationY;
        }
        return tempHeight;
    }

    private double getCorrectWidth(double minimumTranslationX, double maximumTranslationX){
        double tempWidth = 0;
        if(minimumTranslationX >= 0 && maximumTranslationX >= 0){
            tempWidth = maximumTranslationX;
            //We are on the right side
        }else if(minimumTranslationX <= 0 && maximumTranslationX <= 0){
            tempWidth = Math.abs(minimumTranslationX);
            //We are on the left side
        }else if(minimumTranslationX <= 0 && maximumTranslationX >= 0){
            //We are on both sides!
            tempWidth = Math.abs(minimumTranslationX) + maximumTranslationX;
        }
        return tempWidth;
    }

    public Pane getPaneToAddFromDroppedComposition(){
        if(selectedCustomShape instanceof NewCompositionShape){
            NewCompositionShape dropped = (NewCompositionShape) selectedCustomShape;
            Pane toAdd;

            NewCompositionShape newCompositionShape = new NewCompositionShape(dropped.isFigureVariable(), orchestrator, new VBox(), teste -> null, teste -> null);

            toAdd = newCompositionShape.addNewCompositionShape(dropped, false);
            return toAdd;

        }else if(selectedCustomShape instanceof BasicShape){

            BasicShape dropped = (BasicShape) selectedCustomShape;

            Information<Double> translationX = new Information<>(dropped.getUUID().toString(), 0.0);
            Information<Double> translationY = new Information<>(dropped.getUUID().toString(), 0.0);

            BasicShape toUse = orchestrator.getCopyOfBasicShape(dropped.getUUID().toString(), translationX.getConsumer(), translationY.getConsumer(), null);
            return toUse.getRectangle();

        }
        return null;

    }

    public ArrayList<Double> getDroppedCompositionValues(){
        ArrayList<Double> toReturn = new ArrayList<>();

        if(selectedCustomShape instanceof NewCompositionShape){
            NewCompositionShape dropped = (NewCompositionShape) selectedCustomShape;

            double minimumTranslationY = dropped.getMinimumTranslationY_new();
            double maximumTranslationY = dropped.getMaximumTranslationY();
            double tempHeight = getCorrectHeight(minimumTranslationY, maximumTranslationY);

            double minimumTranslationX = dropped.getMinimumTranslationX();
            double maximumTranslationX = dropped.getMaximumTranslationX();
            double tempWidth = getCorrectWidth(minimumTranslationX, maximumTranslationX);

            toReturn.add(tempWidth);
            toReturn.add(tempHeight);
            toReturn.add(minimumTranslationX);
            toReturn.add(maximumTranslationX);
            toReturn.add(minimumTranslationY);
            toReturn.add(maximumTranslationY);

            return toReturn;
        }else{
            return null;
        }

    }


    public void setUpCompositionShapeOnCenterGroup(){
        centerShape.setTranslateY(0);
        centerShape.setTranslateX(0);

        Pane toAdd = getPaneToAddFromDroppedComposition();
        ArrayList<Double> values = getDroppedCompositionValues();

        var tempWidth = values.get(0);
        var tempHeight = values.get(1);
        var minimumTranslationX = values.get(2);
        var maximumTranslationX = values.get(3);
        var minimumTranslationY = values.get(4);
        var maximumTranslationY = values.get(5);

        Rectangle rectangle = new Rectangle(tempWidth, tempHeight);
        rectangle.setFill(Color.web("rgba(255,255,255,0.15)"));
        centerShape.getChildren().add(rectangle);
        rectangle.setTranslateX(minimumTranslationX < 0 ? minimumTranslationX : 0);
        rectangle.setTranslateY(minimumTranslationY <= 0 ? minimumTranslationY : 0);

        centerShape.getChildren().add(toAdd);
        toAdd.layout();
        centerShape.layout();

        double finalTempHeight = tempHeight;
        double finalTempWidth = tempWidth;

        Platform.runLater(() -> {
            getRightGroup(finalTempWidth, finalTempHeight);
            getLeftGroup(finalTempWidth, finalTempHeight);


             if(minimumTranslationY <= 0 && maximumTranslationY >= 0){
                //Quando estou a meio
                rightGroup.setTranslateY(rightGroup.getTranslateY() + maximumTranslationY);
                leftGroup.setTranslateY(leftGroup.getTranslateY() + maximumTranslationY);
            }else if(minimumTranslationY >= 0 && maximumTranslationY >= 0){
                //Quando está tudo bem baixo!
                rightGroup.setTranslateY(rightGroup.getTranslateY() + maximumTranslationY);
                leftGroup.setTranslateY(leftGroup.getTranslateY() + maximumTranslationY);
            }

             if(minimumTranslationX <= 0 && maximumTranslationX <= 0){
                //We are on the left side
                rightGroup.setTranslateX(rightGroup.getTranslateX() + minimumTranslationX);
                leftGroup.setTranslateX(leftGroup.getTranslateX() + minimumTranslationX);
            }else if(minimumTranslationX <= 0 && maximumTranslationX >= 0){
                //We are on both sides!
                rightGroup.setTranslateX(rightGroup.getTranslateX() + minimumTranslationX);
                leftGroup.setTranslateX(leftGroup.getTranslateX() + minimumTranslationX);
            }

            if(rightHasVariable){
                updateRightPowerUIWithoutDetail();
            }else{
                updateRightPowerUIWithDetail();
            }

            if(leftHasVariable){
                updateLeftPowerUIWithoutDetail();
            }else{
                updateLeftPowerUIWithDetail();
            }


        });

    }

    public Node getEditorVisualization() {
        setUpLabels();

        VBox toPut = new VBox();
        toPut.setPrefSize(SCALE, SCALE);
        toPut.setStyle("-fx-background-color: #B2B2B2; -fx-background-radius: 6px;");

        centerShape = new StackPane(toPut);

        if(centerHasVariable){
            customShapeLabel.setText(centerVariable.getVariableName());
        }
        centerShape.getChildren().add(customShapeLabel);
        centerShape.setAlignment(Pos.CENTER);

        if(selectedCustomShape != null && selectedCustomShape instanceof BasicShape){
            addSelectedCustomShapeToCenterShape();
        }else{
            centerShape.setTranslateY(-SCALE);
        }

        centerShape.setOnDragOver(dragEvent -> {
            System.out.println("I WAS DROPPED HERE!!!_1");
            Dragboard db = dragEvent.getDragboard();
            if (db.hasString()) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        });

        centerShape.setOnDragDropped(dragEvent -> {
            System.out.println("I WAS DROPPED HERE!!!_2");
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
                }else if(customShape instanceof NewCompositionShape ){
                    this.selectedCustomShape = customShape;
                    setUpCompositionShapeOnCenterGroup();
                }



                customShapeSelected.set(true);
                centerHasVariable = false;

            }
            dragEvent.consume();
        });

        if(customShape != null)
            setUpCenterDesign();

        setUpCenterGroup();

        if (selectedCustomShape != null && !centerHasVariable){
            centerShape.getChildren().clear();

            System.out.println("centerShape.getLayoutBounds().getWidth(): " + centerShape.getLayoutBounds().getWidth());


            if(selectedCustomShape instanceof NewCompositionShape ){
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

            updateRightPowerUIWithoutDetail();
            updateRightPowerUIWithDetail();
            updateLeftPowerUIWithDetail();
            updateLeftPowerUIWithoutDetail();

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

    private void updateRightPowerUIWithoutDetail(){

        //Right label!
        if(rightHasVariable){
            if(customShape instanceof NewCompositionShape || selectedCustomShape instanceof NewCompositionShape){
                if(centerHasVariable){
                    getRightGroup();
                    rightGroup.setTranslateX(0);
                    rightGroup.setTranslateY(0);
                }else{
                    ArrayList<Double> values = getDroppedCompositionValues();

                    var width = values.get(0);
                    var height = values.get(1);
                    var minimumTranslationX = values.get(2);
                    var maximumTranslationX = values.get(3);
                    var minimumTranslationY = values.get(4);
                    var maximumTranslationY = values.get(5);

                    getRightGroup(width, height);
                    correctRightGroup(minimumTranslationX, maximumTranslationX, minimumTranslationY, maximumTranslationY);
                }

            }
        }

       }

    private void updateRightPowerUIWithDetail(){
        if(!rightHasVariable){
            if(customShape instanceof NewCompositionShape || selectedCustomShape instanceof NewCompositionShape){
                try{
                    double translation = Double.parseDouble(rightTranslation);
                    int repetitions = Integer.parseInt(rightValue);
                    setRightGroupWithDetail(repetitions, translation);
                }catch (NumberFormatException e){
                    System.err.println("since the righ translation has a strange value, we are now doing assumptons!");
                    ArrayList<Double> values = getDroppedCompositionValues();
                    setRightGroupWithDetail(1, values.get(0)/SCALE);
                    rightTranslation = String.valueOf(values.get(0)/SCALE);
                    rightTextFieldTranslation.setText(rightTranslation);
                }
            }
        }
    }

    private void updateLeftPowerUIWithoutDetail(){
        if(leftHasVariable){
            if(customShape instanceof NewCompositionShape || selectedCustomShape instanceof NewCompositionShape){
                if(centerHasVariable){
                    getLeftGroup();
                    leftGroup.setTranslateX(0);
                    leftGroup.setTranslateY(0);
                }else{
                    ArrayList<Double> values = getDroppedCompositionValues();

                    var width = values.get(0);
                    var height = values.get(1);
                    var minimumTranslationX = values.get(2);
                    var maximumTranslationX = values.get(3);
                    var minimumTranslationY = values.get(4);
                    var maximumTranslationY = values.get(5);

                    getLeftGroup(width, height);
                    correctLeftGroup(minimumTranslationX, maximumTranslationX, minimumTranslationY, maximumTranslationY);
                }

            }
        }
    }



    private void updateLeftPowerUIWithDetail(){
        if(!leftHasVariable){
            if(customShape instanceof NewCompositionShape || selectedCustomShape instanceof NewCompositionShape){
                try{
                    double translation = Double.parseDouble(leftTranslation);
                    int repetitions = Integer.parseInt(leftValue);
                    setLeftGroupWithDetail(repetitions, translation);
                }catch (NumberFormatException e){
                    System.err.println("since the left translation has a strange value, we are now doing assumptons!");
                    ArrayList<Double> values = getDroppedCompositionValues();
                    setLeftGroupWithDetail(1, values.get(1)/SCALE);
                    leftTranslation = String.valueOf(values.get(1)/SCALE);
                    leftTextFieldTranslation.setText(leftTranslation);
                }
            }
        }
    }


    private void setUpVerticalSection(){
        VBox horizontalBox = getHorizontalBox();
        horizontalBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
        horizontalBox.setPadding(new Insets(10, 10, 10, 15));

        VBox verticalBox = getVerticalBox();
        verticalBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
        verticalBox.setPadding(new Insets(10, 10, 10, 15));

        VBox centerBox = getCenterBox();
        centerBox.setStyle("-fx-background-color: #333234;-fx-background-radius: 10");
        centerBox.setPadding(new Insets(10, 10, 10, 15));

        if(isFigureVariable){
            verticalSection = new VBox(centerBox);
        }else{
            verticalSection = new VBox(horizontalBox, verticalBox, centerBox);
        }
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

        TextField textFieldVariable = new TextField(rightVariable.getVariableName());
        textFieldVariable.setPromptText("1");
        styleTextField(textFieldVariable);

        textFieldVariable.textProperty().addListener((observable, oldValue, newValue) -> {
            if(rightHasVariable){
                if (!newValue.matches("[a-zA-Z]+")){
                    textFieldVariable.setText(oldValue);
                    rightLabel.setText(oldValue);
                }else{
                    rightVariable.setVariableName(newValue);
                    rightLabel.setText(newValue);
                    updateRightPowerUIWithoutDetail();
                }
            }else{
                if(!newValue.matches("[0-9]+")){
                    textFieldVariable.setText(oldValue);
                    rightLabel.setText(oldValue);
                }else{
                    rightValue = newValue;
                    rightLabel.setText(newValue);
                    updateRightPowerUIWithDetail();
                }
            }
        });

        checkBoxVariable.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            rightHasVariable = newValue;
            rightLabel.setText(rightHasVariable ? rightVariable.getVariableName() : rightValue);
            textFieldVariable.setText(rightHasVariable ? rightVariable.getVariableName() : rightValue);
        });

        Label translationLabel = new Label("Translation: ");
        styleLabel(translationLabel);

        rightTextFieldTranslation = new TextField(rightTranslation);
        rightTextFieldTranslation.setPromptText("1");
        styleTextField(rightTextFieldTranslation);

        rightTextFieldTranslation.textProperty().addListener((observableValue, s, t1) -> {
            rightTranslation = t1;
            updateRightPowerUIWithDetail();
            updateRightPowerUIWithoutDetail();
        });

        HBox fistHBox = new HBox(horizontalPowerCheckboxToUseLabel,checkboxToUse);
        fistHBox.setSpacing(10);

        HBox secondHBox = new HBox(horizontalVariableCheckboxLabel, checkBoxVariable);
        secondHBox.setSpacing(10);

        HBox thirdHBox = new HBox(variableLabel, textFieldVariable, getVerticalSeparator(), translationLabel, rightTextFieldTranslation);
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

        checkboxToUse.setSelected(hasRight);
        checkBoxVariable.setSelected(rightHasVariable);

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


        TextField textFieldVariable = new TextField(leftVariable.getVariableName());
        textFieldVariable.setPromptText("1");
        styleTextField(textFieldVariable);

        textFieldVariable.textProperty().addListener((observable, oldValue, newValue) -> {
            if(leftHasVariable){
                if (!newValue.matches("[a-zA-Z]+")){
                    textFieldVariable.setText(oldValue);
                    leftLabel.setText(oldValue);
                }else{
                    leftVariable.setVariableName(newValue);
                    leftLabel.setText(newValue);
                    updateLeftPowerUIWithoutDetail();
                }

            }else{
                if(!newValue.matches("[0-9]+")){
                    textFieldVariable.setText(oldValue);
                    leftLabel.setText(oldValue);
                }else{
                    leftValue = newValue;
                    leftLabel.setText(newValue);
                    updateLeftPowerUIWithDetail();
                }
            }
        });

        checkBoxVariable.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            leftHasVariable = newValue;
            leftLabel.setText(leftHasVariable ? leftVariable.getVariableName() : leftValue);
            textFieldVariable.setText(leftHasVariable ? leftVariable.getVariableName() : leftValue);
        });

        leftTextFieldTranslation = new TextField(leftTranslation);
        leftTextFieldTranslation.setPromptText("1");
        styleTextField(leftTextFieldTranslation);

        leftTextFieldTranslation.textProperty().addListener((observableValue, s, t1) -> {
            leftTranslation = t1;
            updateLeftPowerUIWithDetail();
            updateLeftPowerUIWithoutDetail();
        });


        HBox fistHBox = new HBox(verticalPowerCheckboxToUseLabel,checkboxToUse);
        fistHBox.setSpacing(10);

        HBox secondHBox = new HBox(verticalVariableCheckboxLabel, checkBoxVariable);
        secondHBox.setSpacing(10);

        Label variableLabel = new Label("Variable: ");
        styleLabel(variableLabel);

        Label translationLabel = new Label("Translation: ");
        styleLabel(translationLabel);

        HBox thirdHBox = new HBox(variableLabel, textFieldVariable, getVerticalSeparator(),translationLabel, leftTextFieldTranslation);
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

        checkboxToUse.setSelected(hasLeft);
        checkBoxVariable.setSelected(leftHasVariable);

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

        Information translationX = new Information(dropped.getUUID().toString(), 0.0);
        Information translationY = new Information(dropped.getUUID().toString(), 0.0);


        BasicShape toUse = orchestrator.getCopyOfBasicShape(dropped.getUUID().toString(), translationX.getConsumer(), translationY.getConsumer(), null);
        Pane toAdd = toUse.getRectangle();

        selectedCustomShapeWidth = toUse.getWidth();
        selectedCustomShapeHeight = toUse.getHeight();

        centerShape.getChildren().add(toAdd);

        centerShape.setTranslateY(-selectedCustomShapeHeight);
    }

    private VBox getCenterBox(){
        Label shapeVariableToUseLabel = new Label("Shape variable: ");
        styleLabel(shapeVariableToUseLabel);

        checkBoxCenterShapeVariable = new CheckBox("Shape variable");
        checkBoxCenterShapeVariable.setSelected(true);


        Label variableLabel = new Label("Variable: ");
        styleLabel(variableLabel);

        TextField textFieldVariable = new TextField(centerVariable.getVariableName());
        textFieldVariable.setPromptText("1");
        styleTextField(textFieldVariable);

        textFieldVariable.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if(centerHasVariable){
                if (!newValue.matches("[a-zA-Z]+")){
                    textFieldVariable.setText(oldValue);
                    customShapeLabel.setText(oldValue);
                    centerVariable.setVariableName(oldValue);
                }else{
                    centerVariable.setVariableName(newValue);
                    customShapeLabel.setText(newValue);
                    centerVariable.setVariableName(newValue);

                }
            }
        });


        HBox fistHBox = new HBox(shapeVariableToUseLabel,checkBoxCenterShapeVariable);
        fistHBox.setSpacing(10);

        HBox secondHBox = new HBox(variableLabel, textFieldVariable);
        secondHBox.setSpacing(10);

        VBox vBox = new VBox(fistHBox, secondHBox);
        vBox.setSpacing(10);

        checkBoxCenterShapeVariable.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            centerHasVariable = newValue;
            if(oldValue != null && oldValue != newValue){
                if(!centerHasVariable & customShape == null){
                    checkBoxCenterShapeVariable.setSelected(true);
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
                    customShapeLabel.setText(centerVariable.getVariableName());

                }
            }

        });

        customShapeSelected.addListener((observableValue, oldValue, newValue) -> {
            if(newValue){
                centerHasVariable = false;
                checkBoxCenterShapeVariable.setSelected(false);
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
                joiner.add(leftVariable.getVariableName());

            if(rightHasVariable && hasRight)
                joiner.add(rightVariable.getVariableName());

            if(centerHasVariable)
                joiner.add(centerVariable.getVariableName());

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


        jsonObject.put("isFigureVariable", isFigureVariable);


        jsonObject.put("id", getUUID().toString());
        jsonObject.put("name", name);

        jsonObject.put("hasLeft", hasLeft);
        jsonObject.put("hasRight", hasRight);

        jsonObject.put("leftHasVariable", leftHasVariable);
        jsonObject.put("rightHasVariable", rightHasVariable);
        jsonObject.put("centerHasVariable", centerHasVariable);

        jsonObject.put("rightVariable", rightVariable.toJSON());
        jsonObject.put("leftVariable", leftVariable.toJSON());
        jsonObject.put("centerVariable", centerVariable.toJSON());

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


        Label tagLabel = new Label(isFigureVariable ? "Figure Template" : "Power Shape");
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

        menuItem.setOnAction(actionEvent -> proceedWhenDeletingFromThumbnail.apply(getUUID().toString()));

        thumbnail.setOnContextMenuRequested(contextMenuEvent -> contextMenu.show(thumbnail, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));
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
