package org.openstreetmap.josm.plugins.busstoptool.actions;

import static org.openstreetmap.josm.data.osm.OsmPrimitive.getParentRelations;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.openstreetmap.josm.command.ChangeMembersCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.plugins.busstoptool.BusStopSettings;
import org.openstreetmap.josm.plugins.busstoptool.BusStopToolPlugin;
import org.openstreetmap.josm.plugins.busstoptool.CopyAdditionalTagsMode;
import org.openstreetmap.josm.plugins.busstoptool.gui.views.BusStopActionDialog;
import org.openstreetmap.josm.tools.Logging;

public class StopFromPlatformAction extends BusStopAction {
    static final String TITLE = tr("Create stop position from platform");
    static final String DESCRIPTION = tr(
        "Creates stop position ({0}) from platform ({1}) and add new stop to relations",
        "public_transport=stop_position",
        "public_transport=platform"
    );

    public StopFromPlatformAction() {
        super(TITLE, DESCRIPTION, BusStopToolPlugin.pluginName + ":createstop",
            BusStopToolPlugin.pluginName + ": " + tr("Open create stop dialog"));
    }

    @Override
    public void runAction() {
        if (source == null || destination == null) {
            @SuppressWarnings("SpellCheckingInspection") // for single quote
            String msg = tr("Action canceled. Source or destination object doesn''t exist!");
            Logging.warn(msg);
            BusStopActionDialog.errorDialog(msg);
            return;
        }
        if (!source.hasTag("public_transport", "platform")) {
            @SuppressWarnings("SpellCheckingInspection") // for single quote
            String msg = tr("Action canceled. Source object doesn''t contain {0} tag!", "public_transport=platform");
            Logging.warn(msg);
            BusStopActionDialog.errorDialog(msg);
            return;
        }

        List<Command> commands = new ArrayList<>();

        // Copy tags
        Predicate<String> filter;
        CopyAdditionalTagsMode mode = CopyAdditionalTagsMode.fromName(
            BusStopSettings.STOP_FROM_PLATFORM_COPY_MODE.get()
        );
        switch (mode) {
            case ALL_TAGS:
                filter = key -> !EXCLUDE_KEYS.contains(key);
                break;
            case SELECTED_TAGS:
                List<String> selectedTags = BusStopSettings.STOP_FROM_PLATFORM_SELECTED_TAGS.get();
                filter = key -> !EXCLUDE_KEYS.contains(key) && selectedTags.contains(key);
                break;
            case NO_TAGS:
            default:
                filter = key -> false;
                break;
        }
        TagMap tags = new TagMap();
        source.keys().filter(filter).forEach(key -> tags.put(key, source.get(key)));
        if (!tags.isEmpty()) {
            commands.add(new ChangePropertyCommand(List.of(destination), tags));
        }

        // Add missing (required?) tags to the stop
        TagMap missingTags = new TagMap("public_transport", "stop_position", "bus", "yes");
        commands.add(new ChangePropertyCommand(List.of(destination), missingTags));

        // Create relation memberships with platform* role BEFORE source platform member
        for (Relation sourceRel : getParentRelations(List.of(source))) {
            List<RelationMember> newMembers = new ArrayList<>();
            List<RelationMember> members = sourceRel.getMembers();
            for (int i = 0; i < members.size(); i++) {
                RelationMember member = members.get(i);
                OsmPrimitive previousMemberPrimitive = i - 1 > 0 ? members.get(i - 1).getMember() : null;

                if (member.getMember().equals(source)) {
                    String role;

                    switch (member.getRole()) {
                        case "platform":
                            role = "stop";
                            break;
                        case "platform_entry_only":
                            role = "stop_entry_only";
                            break;
                        case "platform_exit_only":
                            role = "stop_exit_only";
                            break;
                        default:
                            role = null;
                    }

                    if (role == null) {
                        Logging.warn(String.format(
                            "Incorrect role (%s)! Skipping member (%o) in relation (%o)!",
                            member.getRole(),
                            member.getMember().getId(),
                            sourceRel.getId()
                        ));
                    } else if (previousMemberPrimitive == null || !previousMemberPrimitive.equals(destination)) {
                        newMembers.add(new RelationMember(role, destination));
                    }
                }
                newMembers.add(member);
            }
            commands.add(new ChangeMembersCommand(sourceRel, newMembers));
        }

        SequenceCommand cmd = new SequenceCommand(DESCRIPTION, commands);
        UndoRedoHandler.getInstance().add(cmd);
    }
}
