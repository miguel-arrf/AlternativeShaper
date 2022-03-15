package marrf.iscte;

import javafx.scene.layout.Pane;

import java.util.UUID;
import java.util.function.Supplier;

public interface CustomShape {

    void setShapeName(String shapeName);

    String getShapeName();

    UUID getUUID();

    Pane getTranslationXSection();

    Pane getTranslationYSection();

    void redrawThumbnail();

    Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier);

}
