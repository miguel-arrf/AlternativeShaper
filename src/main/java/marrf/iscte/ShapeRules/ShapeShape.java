package marrf.iscte.ShapeRules;

import javafx.scene.layout.Pane;
import marrf.iscte.Orchestrator;

import java.util.function.Function;

public class ShapeShape extends ShapeRule{


    public ShapeShape(Orchestrator orchestrator, Pane transformersBox,Pane right, Function<String, Double> proceedWhenDeletingFromThumbnail, Function<String, Double> proceedToRedrawWhenDeleting) {
        super(orchestrator, transformersBox,right, proceedWhenDeletingFromThumbnail, proceedToRedrawWhenDeleting);
    }
}
