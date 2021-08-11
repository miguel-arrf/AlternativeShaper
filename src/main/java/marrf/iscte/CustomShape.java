package marrf.iscte;

import javafx.scene.layout.Pane;

import java.util.UUID;
import java.util.function.Supplier;

public interface CustomShape {

    public void setShapeName(String shapeName);
    public String getShapeName();

    public UUID getUUID();

    public Pane getScaleXSection();
    public Pane getScaleYSection();
    public Pane getTranslationXSection();
    public Pane getTranslationYSection();

    public void redrawThumbnail();
    public Pane getThumbnail(Supplier<String> toPutIntoDragbord, Supplier<CustomShape> supplier);

}
