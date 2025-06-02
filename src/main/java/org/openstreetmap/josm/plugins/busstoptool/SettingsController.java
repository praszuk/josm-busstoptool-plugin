package org.openstreetmap.josm.plugins.busstoptool;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsController {
    private final SettingsTabPanel settingsPlatformFromStopTabPanelView;
    private final SettingsTabPanel settingsStopFromPlatformTabPanelView;

    public SettingsController() {
        settingsPlatformFromStopTabPanelView = new SettingsTabPanel();
        settingsStopFromPlatformTabPanelView = new SettingsTabPanel();
        new SettingsTabController(
            new SettingsTabModel(
                BusStopSettings.PLATFORM_FROM_STOP_COPY_MODE,
                BusStopSettings.PLATFORM_FROM_STOP_SELECTED_TAGS
            ),
            settingsPlatformFromStopTabPanelView
        );
        new SettingsTabController(
            new SettingsTabModel(
                BusStopSettings.STOP_FROM_PLATFORM_COPY_MODE,
                BusStopSettings.STOP_FROM_PLATFORM_SELECTED_TAGS
            ),
            settingsStopFromPlatformTabPanelView
        );
    }

    public void initGui() {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.addTab(tr("Platform from stop"), settingsPlatformFromStopTabPanelView);
        settingsDialog.addTab(tr("Stop from platform"), settingsStopFromPlatformTabPanelView);
    }
}