package marrf.iscte.ShapeRules;

import javafx.scene.layout.Pane;
import marrf.iscte.NewCompositionShape;
import marrf.iscte.Orchestrator;

import java.util.UUID;
import java.util.function.Function;

public class ShapeShapeProc extends ShapeRule{


    public ShapeShapeProc(Orchestrator orchestrator, Pane transformersBox,Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        super(orchestrator, transformersBox,right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
    }

    public ShapeShapeProc(NewCompositionShape leftShape, NewCompositionShape rightShape, UUID id, String name, boolean matched, String processXML, String processCode, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        super( proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting, leftShape, rightShape);

        this.setId(id);
        this.setShapeRuleName(name);
        this.setMatched(matched);

        this.setProcessCode(processCode);
        this.setProcessXML(processXML);

    }

    @Override
    public String getCode() {
        StringBuilder toReturnString = new StringBuilder();

        toReturnString.append("shapeRule(").append(this.getShapeRuleName()).append(",").append("src(");


        toReturnString.append("[").append(getLeftShape().getPrologRepresentation(false, true)).append("],");
        toReturnString.append("[").append(getRightShape().getPrologRepresentation(false, true)).append("]");

        toReturnString.append(",").append(getProcessCode().replace(";",""));

        toReturnString.append(")").append(").");

        return toReturnString.toString().replace(System.getProperty("line.separator"), "");
    }
}
