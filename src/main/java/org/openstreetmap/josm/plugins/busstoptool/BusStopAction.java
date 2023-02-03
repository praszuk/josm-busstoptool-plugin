package org.openstreetmap.josm.plugins.busstoptool;

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
    private final String title; // used for GUI title too
    protected OsmPrimitive source;
    protected OsmPrimitive destination;

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
        if (primitives.size() == 0){
            Logging.info("No primitive selected");
            return null;
        }
        else if (primitives.size() > 1){
            Logging.info("Selected more than 1 primitive.");
            return null;
        }


        return primitives.stream().findFirst().get();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Pre-selection
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();
        if (selection.size() == 2){
            OsmPrimitive[] primitives = selection.toArray(OsmPrimitive[]::new);
            this.source = primitives[0];
            this.destination = primitives[1];
        }

        new BusStopToolGUI(this);
    }

    protected abstract void runAction();

    protected boolean selectSourcePrimitive(){
        OsmPrimitive primitive = this.getOneSelectedPrimitive();
        if (primitive != null){
            this.source = primitive;
            return true;
        }
        return false;
    }

    protected boolean selectDestinationPrimitive(){
        OsmPrimitive primitive = this.getOneSelectedPrimitive();
        if (primitive != null){
            this.destination = primitive;
            return true;
        }
        return false;
    }

    public OsmPrimitive getDestination() {
        return destination;
    }

    public OsmPrimitive getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }
}
