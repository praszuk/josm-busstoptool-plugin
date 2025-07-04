package org.openstreetmap.josm.plugins.busstoptool.gui.views;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.busstoptool.BusStopToolPlugin;
import org.openstreetmap.josm.tools.I18n;

public class SettingsDialog extends JFrame {
    static final int HEIGHT = 460;
    static final int WIDTH = 360;
    static final String TITLE = I18n.tr("{0} settings", BusStopToolPlugin.pluginName);

    private final JTabbedPane tabbedPane;

    public SettingsDialog() {
        super();
        this.tabbedPane = new JTabbedPane();
        add(tabbedPane);

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(MainApplication.getMainFrame());
        setTitle(TITLE);
        setVisible(true);
    }

    public void addTab(String title, Component tab) {
        tabbedPane.addTab(title, tab);
    }

}
