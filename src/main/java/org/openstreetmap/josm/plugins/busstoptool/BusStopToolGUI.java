package org.openstreetmap.josm.plugins.busstoptool;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;

import javax.swing.*;

import java.awt.*;

import static org.openstreetmap.josm.tools.I18n.tr;

public class BusStopToolGUI extends JFrame {
    final static int HEIGHT = 135;
    final static int WIDTH = 670;
    final static int MAX_NAME_CHARACTERS = 10;
    private final BusStopAction busStopAction;
    private final JButton sourceBtn;
    private final JButton destinationBtn;
    private final JButton moveBtn;

    public BusStopToolGUI(BusStopAction busStopAction) {
        super();

        this.busStopAction = busStopAction;

        setSize(WIDTH, HEIGHT);
        setTitle(busStopAction.getTitle());
        setLocationRelativeTo(MainApplication.getMainFrame());

        JPanel root = new JPanel(new GridBagLayout());

        JPanel setupPanel = new JPanel(new GridLayout(2, 2));

        setupPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel sourceLabel = new JLabel(tr("Source object") + ": ");
        JLabel destinationLabel = new JLabel(tr("Destination object") + ": ");

        sourceBtn = new JButton();
        destinationBtn = new JButton();

        sourceBtn.addActionListener(actionEvent -> {
            boolean isSuccess = busStopAction.selectSourcePrimitive();
            if (isSuccess){
                updateSourceBtn();
            }
            updateMoveBtnLock();
        });
        destinationBtn.addActionListener(actionEvent -> {
            boolean isSuccess = busStopAction.selectDestinationPrimitive();
            if (isSuccess){
                updateDestinationBtn();
            }
            updateMoveBtnLock();
        });

        setupPanel.add(sourceLabel);
        setupPanel.add(sourceBtn);

        setupPanel.add(destinationLabel);
        setupPanel.add(destinationBtn);

        updateDestinationBtn();
        updateSourceBtn();

        JPanel actionPanel = new JPanel();
        moveBtn = new JButton(tr("Create"));
        moveBtn.setEnabled(false);

        moveBtn.addActionListener(actionEvent -> {
            busStopAction.runAction();
            close();
        });
        actionPanel.add(moveBtn);

        updateMoveBtnLock();

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

    static void errorDialog(String msg){
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), msg, null, JOptionPane.ERROR_MESSAGE);
    }
    private void updateMoveBtnLock() {
        moveBtn.setEnabled(busStopAction.getSource() != null && busStopAction.getDestination() != null);
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    void updateSourceBtn() {
        OsmPrimitive srcPrimitive = busStopAction.getSource();
        if (busStopAction.getSource() == null) {
            sourceBtn.setText(tr("<Add source object from selection>"));
        } else {
            String name = srcPrimitive.getName();
            if (name != null){
                name = name.substring(0, Math.min(MAX_NAME_CHARACTERS, name.length()));
            }else {
                name = "";
            }
            sourceBtn.setText(String.format(
                    "[%s] %o (%s)",
                    srcPrimitive.getType().toString(),
                    srcPrimitive.getId(),
                    name)
            );
        }
    }

    void updateDestinationBtn() {
        OsmPrimitive dstPrimitive = busStopAction.getDestination();
        if (busStopAction.getDestination() == null) {
            destinationBtn.setText(tr("<Add destination object from selection>"));
        } else {
            String name = dstPrimitive.getName();
            if (name != null){
                name = name.substring(0, Math.min(MAX_NAME_CHARACTERS, name.length()));
            }else {
                name = "";
            }
            destinationBtn.setText(String.format(
                    "[%s] %o (%s)",
                    dstPrimitive.getType().toString(),
                    dstPrimitive.getId(),
                    name)
            );
        }
    }

}
