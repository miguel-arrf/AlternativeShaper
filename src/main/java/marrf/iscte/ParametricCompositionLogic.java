package marrf.iscte;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static marrf.iscte.ParametricCompositionShape.Information;

public class ParametricCompositionLogic {

    public  Map<String, Power> powerShapesMap = new HashMap<>();

    public final ArrayList<Information<Double>> powerShapesXTranslation = new ArrayList<>();
    public final ArrayList<Information<Double>> powerShapesYTranslation = new ArrayList<>();

    public final ArrayList<Information<ParametricVariable>> powerShapesXParametricTranslation = new ArrayList<>();
    public final ArrayList<Information<ParametricVariable>> powerShapesYParametricTranslation = new ArrayList<>();


    public  Map<String, NewCompositionShape> compositionShapeMap = new HashMap<>();
    //Temporary translation
    public final ArrayList<Information<Double>> compositionShapesXTranslation = new ArrayList<>();
    public final ArrayList<Information<Double>> compositionShapesYTranslation = new ArrayList<>();
    //Parametric translations
    public final ArrayList<Information<ParametricVariable>> compositionShapesXParametricTranslation = new ArrayList<>();
    public final ArrayList<Information<ParametricVariable>> compositionShapesYParametricTranslation = new ArrayList<>();

    //Parametric Figures
    //Map between random id and figure
    public final Map<String, ParametricCompositionShape> parametricCompositionShapesMap = new HashMap<>();
    //Temporary Translation
    public final ArrayList<Information<Double>> parametricShapesXTranslation = new ArrayList<>();
    public final ArrayList<Information<Double>> parametricShapesYTranslation = new ArrayList<>();
    //Parametric translations
    public final ArrayList<Information<ParametricVariable>> parametricShapesXParametricTranslation = new ArrayList<>();
    public final ArrayList<Information<ParametricVariable>> parametricShapesYParametricTranslation = new ArrayList<>();

    //Parametric variables assigned to parametric variables from other figures...
    public ArrayList<Information<ArrayList<Pair<ParametricVariable, ParametricVariable>>>> variablePairs = new ArrayList<>();

    public final ObservableList<ParametricVariable> variables = FXCollections.observableList(new ArrayList<>());
    public final Map<String, SimpleBooleanProperty> compositionShapeParametricVariableHover = new HashMap<>();
    public final Map<String, SimpleBooleanProperty> parametricCompositionShapeParametricVariableHover = new HashMap<>();
    public final Map<String, SimpleBooleanProperty> powerShapeParametricVariableHover = new HashMap<>();


    public String addShape(CustomShape shape, Map map,
                           ArrayList<Information<Double>> xTranslation,
                           ArrayList<Information<Double>> yTranslation,
                           ArrayList<Information<ParametricVariable>> xParametricTranslation,
                           ArrayList<Information<ParametricVariable>> yParametricTranslation,
                           Map<String, SimpleBooleanProperty> variableHover
    ) {
        String id = UUID.randomUUID().toString();

        return addShape(id,shape, map, xTranslation, yTranslation, xParametricTranslation, yParametricTranslation, variableHover);
    }



    public String addShape(String id,CustomShape shape, Map map,
                         ArrayList<Information<Double>> xTranslation,
                         ArrayList<Information<Double>> yTranslation,
                         ArrayList<Information<ParametricVariable>> xParametricTranslation,
                         ArrayList<Information<ParametricVariable>> yParametricTranslation,
                         Map<String, SimpleBooleanProperty> variableHover
                         ){


        map.put(id, shape);

        xTranslation.add(new Information<>(id, 0.0));
        yTranslation.add(new Information<>(id, 0.0));

        xParametricTranslation.add(new Information<>(id, null));
        //System.out.println("id que coloquei é: " + id);
        yParametricTranslation.add(new Information<>(id, null));

        System.out.println("---START 2---:");
        if(variablePairs.stream().noneMatch(p -> p.getId().equals(id))){
            System.out.println("\tNão encontrou igual!");
            variablePairs.add(new Information<>(id, new ArrayList<>()));
        }else{
            System.out.println("\tEncontrou igual!");
        }
        System.out.println("---END 2---");

        variableHover.put(id, new SimpleBooleanProperty(false));


            return id;

    }

}


