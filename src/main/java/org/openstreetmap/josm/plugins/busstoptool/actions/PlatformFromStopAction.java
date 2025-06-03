package org.openstreetmap.josm.plugins.busstoptool.actions;

import static org.openstreetmap.josm.data.osm.OsmPrimitive.getParentRelations;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.josm.command.ChangeMembersCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.busstoptool.BusStopToolPlugin;
import org.openstreetmap.josm.plugins.busstoptool.gui.views.BusStopActionDialog;
import org.openstreetmap.josm.tools.Logging;

public class PlatformFromStopAction extends BusStopAction {
    static final String TITLE = tr("Create platform from stop position");
    static final String DESCRIPTION =
        tr("Creates platform ({0}) from stop position ({1}) and add new platform to relations",
            "public_transport=platform", "public_transport=stop_position");

    public PlatformFromStopAction() {
        super(TITLE, DESCRIPTION, BusStopToolPlugin.pluginName + ":createplatform",
            BusStopToolPlugin.pluginName + ": " + tr("Open create platform dialog"));
    }

    static TagMap getBasePlatformTags(OsmPrimitive platform) {
        TagMap baseTags = new TagMap("public_transport", "platform");
        if (platform.getType().equals(OsmPrimitiveType.NODE)) {
            baseTags.put("highway", "bus_stop");
        } else if (platform.getType().equals(OsmPrimitiveType.WAY)) {
            baseTags.put("highway", "platform");
            if (((Way) platform).isClosed()) {
                baseTags.put("area", "yes");
            }
        } else {
            baseTags.put("highway", "platform");
        }
        return baseTags;
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
        if (!source.hasTag("public_transport", "stop_position")) {
            @SuppressWarnings("SpellCheckingInspection") // for single quote
            String msg = tr(
                "Action canceled. Source object doesn''t contain {0} tag!", "public_transport=stop_position"
            );
            Logging.warn(msg);
            BusStopActionDialog.errorDialog(msg);
            return;
        }

        List<Command> commands = new ArrayList<>();

        // Copy tags
        TagMap tags = new TagMap();
        source.keys().filter(key -> !EXCLUDE_KEYS.contains(key)).forEach(key -> tags.put(key, source.get(key)));
        commands.add(new ChangePropertyCommand(List.of(destination), tags));

        // Add base (required) tags to the platform
        commands.add(new ChangePropertyCommand(List.of(destination), getBasePlatformTags(destination)));

        // Create relation memberships with platform* role AFTER source stop_position member
        for (Relation sourceRel : getParentRelations(List.of(source))) {
            List<RelationMember> newMembers = new ArrayList<>();
            List<RelationMember> members = sourceRel.getMembers();
            for (int i = 0; i < members.size(); i++) {
                RelationMember member = members.get(i);
                OsmPrimitive nextMemberPrimitive = i + 1 < members.size() ? members.get(i + 1).getMember() : null;

                newMembers.add(member);

                if (member.getMember().equals(source)) {
                    String role;

                    switch (member.getRole()) {
                        case "stop":
                            role = "platform";
                            break;
                        case "stop_entry_only":
                            role = "platform_entry_only";
                            break;
                        case "stop_exit_only":
                            role = "platform_exit_only";
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
                    } else if (nextMemberPrimitive == null || !nextMemberPrimitive.equals(destination)) {
                        newMembers.add(new RelationMember(role, destination));
                    }
                }
            }
            commands.add(new ChangeMembersCommand(sourceRel, newMembers));
        }

        SequenceCommand cmd = new SequenceCommand(DESCRIPTION, commands);
        UndoRedoHandler.getInstance().add(cmd);
    }

}
