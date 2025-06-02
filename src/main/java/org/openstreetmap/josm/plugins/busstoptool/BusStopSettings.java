package org.openstreetmap.josm.plugins.busstoptool;

import java.util.ArrayList;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;

public class BusStopSettings {
    static final String SETTING_PREFIX = "busstoptool.";

    public static final StringProperty PLATFORM_FROM_STOP_COPY_MODE = new StringProperty(
        SETTING_PREFIX + "platform_from_stop_copy_mode",
        CopyAdditionalTagsMode.ALL_TAGS.getName()
    );

    public static final StringProperty STOP_FROM_PLATFORM_COPY_MODE = new StringProperty(
        SETTING_PREFIX + "stop_from_platform_copy_mode",
        CopyAdditionalTagsMode.NO_TAGS.getName()
    );

    public static final ListProperty PLATFORM_FROM_STOP_SELECTED_TAGS = new ListProperty(
        SETTING_PREFIX + "platform_from_selected_tags",
        new ArrayList<>()
    );

    public static final ListProperty STOP_FROM_PLATFORM_SELECTED_TAGS = new ListProperty(
        SETTING_PREFIX + "stop_from_platform_selected_tags",
        new ArrayList<>()
    );
}
