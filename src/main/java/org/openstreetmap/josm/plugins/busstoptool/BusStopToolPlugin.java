package org.openstreetmap.josm.plugins.busstoptool;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class BusStopToolPlugin extends Plugin {
    public static final String pluginName = "BusStopTool";

    private final PlatformFromStopAction platformFromStopAction;
    private final StopFromPlatformAction stopFromPlatformAction;

    public BusStopToolPlugin(PluginInformation info) {
        super(info);
        platformFromStopAction = new PlatformFromStopAction();
        stopFromPlatformAction = new StopFromPlatformAction();
        MainMenu.add(MainApplication.getMenu().selectionMenu, platformFromStopAction);
        MainMenu.add(MainApplication.getMenu().selectionMenu, stopFromPlatformAction);
        MainMenu.add(MainApplication.getMenu().dataMenu, new BusStopSettingsAction(new SettingsController()));
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);

        platformFromStopAction.setEnabled(newFrame != null);
        stopFromPlatformAction.setEnabled(newFrame != null);
    }
}
