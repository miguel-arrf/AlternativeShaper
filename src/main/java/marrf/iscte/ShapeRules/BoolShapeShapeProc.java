package marrf.iscte.ShapeRules;

import javafx.scene.layout.Pane;
import marrf.iscte.Orchestrator;
import marrf.iscte.ParametricCompositionShape;

import java.util.UUID;
import java.util.function.Function;

public class BoolShapeShapeProc extends ShapeRule {


    public BoolShapeShapeProc(Orchestrator orchestrator, Pane transformersBox, Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        super(orchestrator, transformersBox, right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
    }

    public BoolShapeShapeProc(ParametricCompositionShape leftShape, ParametricCompositionShape rightShape, UUID id, String name, boolean matched, String boolXML, String boolCode, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting, String processXML, String processCode) {
        super(proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting, leftShape, rightShape);

        this.setId(id);
        this.setShapeRuleName(name);
        this.setMatched(matched);
        this.setBoolCode(boolCode);
        this.setBoolXML(boolXML);

        this.setProcessCode(processCode);
        this.setProcessXML(processXML);
    }

    @Override
    public String getCode() {
        StringBuilder toReturnString = new StringBuilder();

        toReturnString.append("shapeRule(").append(this.getShapeRuleName()).append(",").append("src(");

        toReturnString.append(getBoolCode().replace(";", "")).append(",");


        toReturnString.append("[").append(getLeftShape().getPrologRepresentation(false, true)).append("],");
        toReturnString.append("[").append(getRightShape().getPrologRepresentation(false, true)).append("]");


        toReturnString.append(",").append(getProcessCode().replace(";", ""));


        toReturnString.append(")").append(").");

        return toReturnString.toString().replace(System.getProperty("line.separator"), "");
    }
}
