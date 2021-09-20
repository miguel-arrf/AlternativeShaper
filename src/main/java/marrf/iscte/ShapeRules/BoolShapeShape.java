package marrf.iscte.ShapeRules;

import javafx.scene.layout.Pane;
import marrf.iscte.Orchestrator;

import java.util.function.Function;

public class BoolShapeShape extends ShapeRule{


    public BoolShapeShape(Orchestrator orchestrator, Pane transformersBox,Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        super(orchestrator, transformersBox,right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
    }

    @Override
    public String getCode() {
        StringBuilder toReturnString = new StringBuilder();

        toReturnString.append("shapeRule(").append(this.getShapeRuleName()).append(",").append("src(");

        toReturnString.append(getBoolCode().replace(";","")).append(",");

        toReturnString.append("[").append(getLeftShape().getPrologRepresentation(false, true)).append("],");
        toReturnString.append("[").append(getRightShape().getPrologRepresentation(false, true)).append("]");

        toReturnString.append(")").append(").");

        return toReturnString.toString();
    }
}
