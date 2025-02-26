package org.openstreetmap.josm.plugins.busstoptool;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openstreetmap.josm.gui.MainApplication;

public class BusStopToolGUI extends JFrame {
    final static int HEIGHT = 135;
    final static int WIDTH = 670;
    private final JButton sourceBtn;
    private final JButton destinationBtn;
    private final JButton createBtn;

    public BusStopToolGUI(String title) {
        setSize(WIDTH, HEIGHT);
        setTitle(title);
        setLocationRelativeTo(MainApplication.getMainFrame());

        JPanel root = new JPanel(new GridBagLayout());

        JPanel setupPanel = new JPanel(new GridLayout(2, 2));

        setupPanel.setBorder(BorderFactory.createEtchedBorder());

        sourceBtn = new JButton(tr("<Add source object from selection>"));
        destinationBtn = new JButton(tr("<Add destination object from selection>"));

        setupPanel.add(new JLabel(tr("Source object") + ": "));
        setupPanel.add(sourceBtn);

        setupPanel.add(new JLabel(tr("Destination object") + ": "));
        setupPanel.add(destinationBtn);

        JPanel actionPanel = new JPanel();
        createBtn = new JButton(tr("Create"));
        createBtn.setEnabled(false);
        actionPanel.add(createBtn);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 5, 0, 5);

        root.add(setupPanel, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 2;
        root.add(actionPanel, c);

        add(root);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    static void errorDialog(String msg) {
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), msg, null, JOptionPane.ERROR_MESSAGE);
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public void addSourceBtnAddActionListener(ActionListener listener) {
        sourceBtn.addActionListener(listener);
    }

    public void addDestinationBtnAddActionListener(ActionListener listener) {
        destinationBtn.addActionListener(listener);
    }

    public void addCreateBtnAddActionListener(ActionListener listener) {
        createBtn.addActionListener(listener);
    }

    public void setCreateBtnEnabled(boolean enabled) {
        createBtn.setEnabled(enabled);
    }

    public void setSourceBtnText(String text) {
        sourceBtn.setText(text);
    }

    public void setDestinationBtnText(String text) {
        destinationBtn.setText(text);
    }

}
