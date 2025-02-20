package org.openstreetmap.josm.plugins.busstoptool;

import org.openstreetmap.josm.command.ChangeMembersCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.Logging;

import java.util.ArrayList;
import java.util.List;

import static org.openstreetmap.josm.data.osm.OsmPrimitive.getParentRelations;
import static org.openstreetmap.josm.tools.I18n.tr;

public class PlatformFromStopAction extends BusStopAction{
    static final String TITLE = tr("Create platform from stop_position");
    static final String DESCRIPTION = tr("Creates platform from stop_position and add new platform to relations");
    public PlatformFromStopAction() {
        super(TITLE, DESCRIPTION, "busstoptool:createplatform", tr("Open create platform dialog"));
    }

    @Override
    protected void runAction() {
        if (source == null || destination == null){
            String msg = tr("Action canceled. Source or destination object is null!");
            Logging.warn(msg);
            BusStopToolGUI.errorDialog(msg);
            return;
        }
        if (!source.hasTag("public_transport","stop_position")){
            String msg = tr("Action canceled. Source object doesn't contains public_transport=stop_position tag!");
            Logging.warn(msg);
            BusStopToolGUI.errorDialog(msg);
            return;
        }

        List<Command> commands = new ArrayList<>();

        // Copy tags
        TagMap tags = new TagMap();
        source.keys().filter(key-> !EXCLUDE_KEYS.contains(key)).forEach(key -> tags.put(key, source.get(key)));
        commands.add(new ChangePropertyCommand(List.of(destination), tags));

        // Add missing (required?) tags to the platform
        TagMap missingTags = new TagMap("public_transport", "platform");
        if (destination.getType().equals(OsmPrimitiveType.NODE)){
            missingTags.put("highway", "bus_stop");
        }
        else if (destination.getType().equals(OsmPrimitiveType.WAY)){
            missingTags.put("highway", "platform");
            if (((Way) destination).isClosed()){
                missingTags.put("area", "yes");
            }
        }
        commands.add(new ChangePropertyCommand(List.of(destination), missingTags));

        // Create relation memberships with platform* role AFTER source stop_position member
        for (Relation sourceRel : getParentRelations(List.of(source))){
            List<RelationMember> newMembers = new ArrayList<>();
            List<RelationMember> members = sourceRel.getMembers();
            for (int i = 0; i < members.size(); i++) {
                RelationMember member = members.get(i);
                OsmPrimitive nextMemberPrimitive = i + 1 < members.size() ? members.get(i + 1).getMember() : null;

                newMembers.add(member);

                if (member.getMember().equals(source)) {
                    String role = null;

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
                    }

                    if (role == null) {
                        Logging.warn(String.format(
                            "Incorrect role (%s)! Skipping member (%o) in relation (%o)!",
                            member.getRole(),
                            member.getMember().getId(),
                            sourceRel.getId()
                        ));
                    } else if(nextMemberPrimitive == null || !nextMemberPrimitive.equals(destination)) {
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
