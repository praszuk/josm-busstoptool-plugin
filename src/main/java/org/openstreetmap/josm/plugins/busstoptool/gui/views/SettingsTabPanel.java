package org.openstreetmap.josm.plugins.busstoptool.gui.views;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import org.openstreetmap.josm.plugins.busstoptool.CopyAdditionalTagsMode;

public class SettingsTabPanel extends JPanel {
    private Map<String, JRadioButton> radioButtons;
    private ButtonGroup buttonGroup;

    private JList<Object> tagList;
    private JButton addTagBtn;
    private JButton removeTagBtn;

    public SettingsTabPanel() {
        super();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(tr("Copy additional tags mode"))
        ));
        add(createAdditionalTagModeChoice(), BorderLayout.NORTH);
        add(createTagListPanel(), BorderLayout.CENTER);
    }

    private Component createTagListPanel() {
        tagList = new JList<>();
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final JScrollPane jScrollPane = new JScrollPane(tagList);
        final JPanel buttonsPanel = new JPanel();
        addTagBtn = new JButton(tr("Add"));
        removeTagBtn = new JButton(tr("Remove"));
        removeTagBtn.setEnabled(false);
        buttonsPanel.add(addTagBtn);
        buttonsPanel.add(removeTagBtn);

        final JPanel tagListPanel = new JPanel(new BorderLayout());
        tagListPanel.add(new JLabel("Copyable tags"), BorderLayout.NORTH);
        tagListPanel.add(jScrollPane, BorderLayout.CENTER);
        tagListPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return tagListPanel;
    }

    private Component createAdditionalTagModeChoice() {
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));

        radioButtons = new LinkedHashMap<>();
        buttonGroup = new ButtonGroup();
        for (CopyAdditionalTagsMode mode : CopyAdditionalTagsMode.values()) {
            JRadioButton btn = new JRadioButton(mode.getLabel());
            radioButtons.put(mode.getName(), btn);
            buttonGroup.add(btn);
            modePanel.add(btn);
        }
        return modePanel;
    }

    public String getSelectedAdditionalTagMode() {
        ButtonModel selectedBtnModel = buttonGroup.getSelection();
        for (Map.Entry<String, JRadioButton> entry : radioButtons.entrySet()) {
            if (entry.getValue().getModel().equals(selectedBtnModel)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setSelectedAdditionalTagMode(String modeName) {
        buttonGroup.setSelected(radioButtons.get(modeName).getModel(), true);
    }

    public void copyAdditionalTagsModeButtonsAddActionListener(ActionListener listener) {
        for (JRadioButton btn : radioButtons.values()) {
            btn.addActionListener(listener);
        }
    }

    public void tagListSetEnabled(boolean enabled) {
        tagList.setEnabled(enabled);
    }

    public void removeTagBtnSetEnabled(boolean enabled) {
        removeTagBtn.setEnabled(enabled);
    }

    public void addTagBtnSetEnabled(boolean enabled) {
        addTagBtn.setEnabled(enabled);
    }

    public String promptNewTag() {
        return JOptionPane.showInputDialog(tr("Add new tag"));
    }

    public void addTagBtnAddActionListener(ActionListener listener) {
        addTagBtn.addActionListener(listener);
    }

    public void removeTagBtnAddActionListener(ActionListener listener) {
        removeTagBtn.addActionListener(listener);
    }

    public int getTagListSelectedIndex() {
        return tagList.getSelectedIndex();
    }

    public void setTagListModel(ListModel<Object> tagListModel) {
        tagList.setModel(tagListModel);
    }

    public void tagListAddListSelectionListener(ListSelectionListener listener) {
        tagList.addListSelectionListener(listener);
    }
}
