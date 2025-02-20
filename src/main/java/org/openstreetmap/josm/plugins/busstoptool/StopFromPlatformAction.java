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

public class StopFromPlatformAction extends BusStopAction {
    static final String TITLE = tr("Create stop_position from platform");
    static final String DESCRIPTION = tr("Creates stop_position from platform and add new stop to relations");
    public StopFromPlatformAction() {
        super(TITLE, DESCRIPTION, "busstoptool:createstop", tr("Open create stop dialog"));
    }

    @Override
    protected void runAction() {
        if (source == null || destination == null){
            String msg = tr("Action canceled. Source or destination object is null!");
            Logging.warn(msg);
            BusStopToolGUI.errorDialog(msg);
            return;
        }
        if (!source.hasTag("public_transport","platform")){
            String msg = tr("Action canceled. Source object doesn't contains public_transport=platform tag!");
            Logging.warn(msg);
            BusStopToolGUI.errorDialog(msg);
            return;
        }

        List<Command> commands = new ArrayList<>();

        // Copy tags
        TagMap tags = new TagMap();
        source.keys().filter(key-> !EXCLUDE_KEYS.contains(key)).forEach(key -> tags.put(key, source.get(key)));
        commands.add(new ChangePropertyCommand(List.of(destination), tags));

        // Add missing (required?) tags to the stop
        TagMap missingTags = new TagMap("public_transport", "stop_position", "bus", "yes");
        commands.add(new ChangePropertyCommand(List.of(destination), missingTags));

        // Create relation memberships with platform* role BEFORE source platform member
        for (Relation sourceRel : getParentRelations(List.of(source))){
            List<RelationMember> newMembers = new ArrayList<>();
            List<RelationMember> members = sourceRel.getMembers();
            for (int i = 0; i < members.size(); i++) {
                RelationMember member = members.get(i);
                OsmPrimitive previousMemberPrimitive = i - 1 > 0 ? members.get(i - 1).getMember() : null;

                if (member.getMember().equals(source)) {
                    String role = null;

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
                    }

                    if (role == null) {
                        Logging.warn(String.format(
                            "Incorrect role (%s)! Skipping member (%o) in relation (%o)!",
                            member.getRole(),
                            member.getMember().getId(),
                            sourceRel.getId()
                        ));
                    } else if(previousMemberPrimitive == null || !previousMemberPrimitive.equals(destination)) {
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
