package org.openstreetmap.josm.plugins.busstoptool;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Set;


public abstract class BusStopAction extends JosmAction {

    protected static final Set<String> EXCLUDE_KEYS = Set.of("highway", "public_transport", "area");
    private final String title; // GUI dialog uses it too
    protected OsmPrimitive source;
    protected OsmPrimitive destination;
    protected BusStopToolGUI busStopToolGUI;

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

        busStopToolGUI = new BusStopToolGUI(title);
        busStopToolGUI.addSourceBtnAddActionListener(actionEvent -> {
            OsmPrimitive selectedPrimitive = getOneSelectedPrimitive();
            if (selectedPrimitive != null) {
                source = selectedPrimitive;
                busStopToolGUI.setSourceBtnText(getNameFromPrimitive(source));
                busStopToolGUI.setCreateBtnEnabled(isBothPrimitivesSelected());
            }
        });
        busStopToolGUI.addDestinationBtnAddActionListener(actionEvent -> {
            OsmPrimitive selectedPrimitive = getOneSelectedPrimitive();
            if (selectedPrimitive != null) {
                destination = selectedPrimitive;
                busStopToolGUI.setDestinationBtnText(getNameFromPrimitive(destination));
                busStopToolGUI.setCreateBtnEnabled(isBothPrimitivesSelected());
            }
        });
        busStopToolGUI.addCreateBtnAddActionListener(actionEvent -> {
            runAction();
            busStopToolGUI.close();
        });

        // Pre-selection
        ArrayList<OsmPrimitive> selectedPrimitives = new ArrayList<>(getLayerManager().getEditDataSet().getSelected());
        if (selectedPrimitives.size() == 2) {
            source = selectedPrimitives.get(0);
            destination = selectedPrimitives.get(1);

            busStopToolGUI.setSourceBtnText(getNameFromPrimitive(source));
            busStopToolGUI.setDestinationBtnText(getNameFromPrimitive(destination));
            busStopToolGUI.setCreateBtnEnabled(true);
        }
    }

    protected abstract void runAction();

    boolean isBothPrimitivesSelected() {
        return source != null && destination != null;
    }

    String getNameFromPrimitive(@Nonnull OsmPrimitive primitive) {
        String name = primitive.getName() != null ? primitive.getName() : "";
        name = name.substring(0, Math.min(15, name.length()));
        return String.format("[%s] %o (%s)", primitive.getType().toString(), primitive.getId(), name);
    }

}
