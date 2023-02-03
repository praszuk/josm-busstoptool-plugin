package org.openstreetmap.josm.plugins.busstoptool;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class BusStopToolPlugin extends Plugin {
    public BusStopToolPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().selectionMenu, new PlatformFromStopAction());
        MainMenu.add(MainApplication.getMenu().selectionMenu, new StopFromPlatformAction());
    }
}
