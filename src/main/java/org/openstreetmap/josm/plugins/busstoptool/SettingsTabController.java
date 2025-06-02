package org.openstreetmap.josm.plugins.busstoptool;

public class SettingsTabController {
    private final SettingsTabModel settingsTabModel;
    private final SettingsTabPanel settingsTabPanelView;
    private final SettingsTagListModel settingsTagListModel;

    public SettingsTabController(SettingsTabModel settingsTabModel, SettingsTabPanel settingsTabPanelView) {
        this.settingsTabModel = settingsTabModel;
        this.settingsTabPanelView = settingsTabPanelView;
        this.settingsTagListModel = new SettingsTagListModel();

        settingsTabPanelView.setTagListModel(settingsTagListModel);
        settingsTabPanelView.setSelectedAdditionalTagMode(settingsTabModel.getCopyAdditionalTagsMode().getName());

        initModelListeners();
        initViewListeners();

        updateTagList();
    }

    private void initModelListeners() {
        settingsTabModel.addPropertyChangeListener(
            SettingsTabModel.COPY_MODE_CHANGED,
            evt -> updateCopyAdditionalTagMode()
        );
        settingsTabModel.addPropertyChangeListener(SettingsTabModel.TAGS_CHANGED, evt -> updateTagList());
    }

    private void initViewListeners() {
        settingsTabPanelView.copyAdditionalTagsModeButtonsAddActionListener(
            actionEvent -> updateCopyAdditionalTagModeAction()
        );
        settingsTabPanelView.addTagBtnAddActionListener(actionEvent -> addTagAction());
        settingsTabPanelView.removeTagBtnAddActionListener(actionEvent -> removeTagAction());
        settingsTabPanelView.tagListAddListSelectionListener(
            listSelectionEvent -> updateTagListButtons(settingsTabModel.getCopyAdditionalTagsMode())
        );
    }

    private void updateCopyAdditionalTagModeAction() {
        CopyAdditionalTagsMode mode = CopyAdditionalTagsMode.fromName(
            settingsTabPanelView.getSelectedAdditionalTagMode()
        );
        settingsTabModel.setCopyAdditionalTagsMode(mode);
    }

    private void addTagAction() {
        String newTag = settingsTabPanelView.promptNewTag();
        if (newTag == null || settingsTagListModel.contains(newTag)) {
            return;
        }
        settingsTabModel.addTag(newTag);
    }

    private void removeTagAction() {
        int tagIndex = settingsTabPanelView.getTagListSelectedIndex();
        String selectedTag = (String) settingsTagListModel.getElementAt(tagIndex);
        if (selectedTag != null) {
            settingsTabModel.removeTag(selectedTag);
        }
    }

    private void updateCopyAdditionalTagMode() {
        settingsTabPanelView.setSelectedAdditionalTagMode(settingsTabModel.getCopyAdditionalTagsMode().getName());
        updateTagList();
    }

    private void updateTagList() {
        settingsTagListModel.clear();
        settingsTagListModel.addAll(settingsTabModel.getTags());

        CopyAdditionalTagsMode mode = settingsTabModel.getCopyAdditionalTagsMode();
        settingsTabPanelView.tagListSetEnabled(mode == CopyAdditionalTagsMode.SELECTED_TAGS);
        updateTagListButtons(mode);
    }

    private void updateTagListButtons(CopyAdditionalTagsMode mode) {
        if (mode != CopyAdditionalTagsMode.SELECTED_TAGS) {
            settingsTabPanelView.addTagBtnSetEnabled(false);
            settingsTabPanelView.removeTagBtnSetEnabled(false);
        } else {
            settingsTabPanelView.addTagBtnSetEnabled(true);
            settingsTabPanelView.removeTagBtnSetEnabled(settingsTabPanelView.getTagListSelectedIndex() != -1);
        }
    }

}
