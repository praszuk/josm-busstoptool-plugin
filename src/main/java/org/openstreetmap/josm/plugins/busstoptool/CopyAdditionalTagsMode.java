package org.openstreetmap.josm.plugins.busstoptool;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

public enum CopyAdditionalTagsMode {
    ALL_TAGS("all_tags", tr("All tags")),
    SELECTED_TAGS("selected_tags", tr("Selected tags")),
    NO_TAGS("no_tags", trc("Copy not tags", "No tags"));

    private final String name;
    private final String label;

    CopyAdditionalTagsMode(final String name, final String label) {
        this.name = name;
        this.label = label;
    }

    public static CopyAdditionalTagsMode fromName(String name) {
        for (CopyAdditionalTagsMode mode : CopyAdditionalTagsMode.values()) {
            if (mode.name.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("No constant with text " + name + " found");
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
}
