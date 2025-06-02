package org.openstreetmap.josm.plugins.busstoptool;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class BusStopSettingsAction extends JosmAction {
    public static final String DESCRIPTION = tr("Show settings");
    public static final String TITLE = BusStopToolPlugin.pluginName + " " + tr("Settings");

    private final SettingsController settingsController;

    public BusStopSettingsAction(SettingsController settingsController) {
        super(
            TITLE,
            (ImageProvider) null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                BusStopToolPlugin.pluginName + ":settings",
                TITLE,
                KeyEvent.CHAR_UNDEFINED,
                Shortcut.NONE
            ),
            true,
            BusStopToolPlugin.pluginName + ":settings",
            false
        );
        this.settingsController = settingsController;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        settingsController.initGui();
    }
}
