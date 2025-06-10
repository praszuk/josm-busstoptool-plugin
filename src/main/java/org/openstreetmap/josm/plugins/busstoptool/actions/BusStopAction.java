package org.openstreetmap.josm.plugins.busstoptool.actions;

import jakarta.annotation.Nonnull;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.busstoptool.gui.views.BusStopActionDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;


public abstract class BusStopAction extends JosmAction {

    protected static final Set<String> EXCLUDE_KEYS = Set.of("highway", "public_transport", "area");
    private final String title; // Gui dialog uses it too
    protected OsmPrimitive source;
    protected OsmPrimitive destination;
    protected BusStopActionDialog busStopActionDialog;

    public BusStopAction(String title, String description, String shortcutShort, String shortcutLong) {
        super(
            title,
            (ImageProvider) null,
            description,
            Shortcut.registerShortcut(shortcutShort, shortcutLong, KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
            true,
            title,
            false
        );
        this.title = title;
        setEnabled(false);
    }

    /**
     * @return selected 1 primitive or null if there is no selection or selection > 1
     */
    private OsmPrimitive getOneSelectedPrimitive() {
        Collection<OsmPrimitive> primitives = getLayerManager().getEditDataSet().getSelected();
        if (primitives.isEmpty()) {
            Logging.info("No primitive selected");
            return null;
        } else if (primitives.size() > 1) {
            Logging.info("Selected more than 1 primitive.");
            return null;
        }

        return primitives.stream().findFirst().get();
    }

    @Override
    public void actionPerformed(ActionEvent ignore) {
        source = null;
        destination = null;

        busStopActionDialog = new BusStopActionDialog(title);
        busStopActionDialog.addSourceBtnAddActionListener(actionEvent -> {
            OsmPrimitive selectedPrimitive = getOneSelectedPrimitive();
            if (selectedPrimitive != null) {
                source = selectedPrimitive;
                busStopActionDialog.setSourceBtnText(getNameFromPrimitive(source));
                busStopActionDialog.setCreateBtnEnabled(isBothPrimitivesSelected());
            }
        });
        busStopActionDialog.addDestinationBtnAddActionListener(actionEvent -> {
            OsmPrimitive selectedPrimitive = getOneSelectedPrimitive();
            if (selectedPrimitive != null) {
                destination = selectedPrimitive;
                busStopActionDialog.setDestinationBtnText(getNameFromPrimitive(destination));
                busStopActionDialog.setCreateBtnEnabled(isBothPrimitivesSelected());
            }
        });
        busStopActionDialog.addCreateBtnAddActionListener(actionEvent -> {
            runAction();
            busStopActionDialog.close();
        });

        // Pre-selection
        ArrayList<OsmPrimitive> selectedPrimitives = new ArrayList<>(getLayerManager().getEditDataSet().getSelected());
        if (selectedPrimitives.size() == 2) {
            source = selectedPrimitives.get(0);
            destination = selectedPrimitives.get(1);

            busStopActionDialog.setSourceBtnText(getNameFromPrimitive(source));
            busStopActionDialog.setDestinationBtnText(getNameFromPrimitive(destination));
            busStopActionDialog.setCreateBtnEnabled(true);
        }
    }

    public abstract void runAction();

    boolean isBothPrimitivesSelected() {
        return source != null && destination != null;
    }

    String getNameFromPrimitive(@Nonnull OsmPrimitive primitive) {
        String name = primitive.getName() != null ? primitive.getName() : "";
        name = name.substring(0, Math.min(15, name.length()));
        return String.format("[%s] %o (%s)", primitive.getType().toString(), primitive.getId(), name);
    }

}
