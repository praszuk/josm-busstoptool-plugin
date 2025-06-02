package org.openstreetmap.josm.plugins.busstoptool;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;

public class SettingsTabModel {
    public static final String TAGS_CHANGED = "tags_changed";
    public static final String COPY_MODE_CHANGED = "copy_mode_changed";

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final StringProperty copyAdditionalTagsModeProperty;
    private final ListProperty tagsListProperty;

    public SettingsTabModel(StringProperty copyAdditionalTagsModeProperty, ListProperty tagsListProperty) {
        this.copyAdditionalTagsModeProperty = copyAdditionalTagsModeProperty;
        this.tagsListProperty = tagsListProperty;

        this.copyAdditionalTagsModeProperty.addListener(valueChangeEvent ->
            propertyChangeSupport.firePropertyChange(COPY_MODE_CHANGED, null, copyAdditionalTagsModeProperty.get())
        );
        tagsListProperty.addListener(valueChangeEvent ->
            propertyChangeSupport.firePropertyChange(TAGS_CHANGED, null, copyAdditionalTagsModeProperty.get())
        );
    }

    public void addTag(String tag) {
        List<String> tagList = getTags();
        tagList.add(tag);
        Collections.sort(tagList);
        tagsListProperty.put(tagList);
    }

    public void removeTag(String value) {
        List<String> tagList = getTags();
        tagList.remove(value);
        tagsListProperty.put(tagList);
    }

    public List<String> getTags() {
        return new ArrayList<>(tagsListProperty.get());
    }

    public CopyAdditionalTagsMode getCopyAdditionalTagsMode() {
        return CopyAdditionalTagsMode.fromName(copyAdditionalTagsModeProperty.get());
    }

    public void setCopyAdditionalTagsMode(CopyAdditionalTagsMode mode) {
        copyAdditionalTagsModeProperty.put(mode.getName());
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
