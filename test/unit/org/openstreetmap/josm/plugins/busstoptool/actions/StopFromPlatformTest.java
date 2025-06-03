package org.openstreetmap.josm.plugins.busstoptool.actions;

import static org.openstreetmap.josm.plugins.busstoptool.NodeHelper.createNode;
import static org.openstreetmap.josm.plugins.busstoptool.NodeHelper.createPlatform;
import static org.openstreetmap.josm.plugins.busstoptool.NodeHelper.createStop;

import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.busstoptool.RelationHelper;
import org.openstreetmap.josm.plugins.busstoptool.gui.views.BusStopActionDialog;

public class StopFromPlatformTest {
    private DataSet ds;

    @BeforeEach
    void setUp() {
        UndoRedoHandler.getInstance().clean();

        this.ds = new DataSet();
    }

    private void mockErrorDialog(String expectedMessage) {
        new MockUp<BusStopActionDialog>() {
            @Mock
            void errorDialog(String msg) {
                Assertions.assertEquals(expectedMessage, msg);
            }
        };
    }

    @Test
    void testMissingSourceOrDestinationObject() {
        mockErrorDialog("Action canceled. Source or destination object doesn't exist!");

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = createPlatform(ds);
        action.destination = null;
        action.runAction();

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());

        action.source = null;
        action.destination = createStop(ds);
        action.runAction();

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());

        action.source = null;
        action.destination = null;
        action.runAction();

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());
    }

    @Test
    void testSourceWithoutRequiredTag() {
        mockErrorDialog("Action canceled. Source object doesn't contain public_transport=platform tag!");

        Node platform = createNode(ds);
        Node stop = createNode(ds);

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.hasKeys());
        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());
    }

    @Test
    void testCreateTagsWithoutRelationNodeBusStop() {
        Node platform = createPlatform(ds);
        platform.put("name", "Bus stop 01");

        Node stop = createNode(ds);

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertEquals(3, stop.getKeys().size());
        Assertions.assertTrue(stop.hasTag("public_transport", "stop_position"));
        Assertions.assertTrue(stop.hasTag("name", "Bus stop 01"));
        Assertions.assertTrue(stop.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
    }

    @Test
    void testCreateTagsWithoutRelationWayPlatform() {
        StopFromPlatformAction action = new StopFromPlatformAction();

        Way platform = new Way();
        platform.put("name", "Bus stop 02");
        platform.put("highway", "platform");
        platform.put("public_transport", "platform");

        ds.addPrimitive(platform);
        for (int i = 0; i < 4; i++) {
            Node n = new Node(new LatLon(0, i));
            ds.addPrimitive(n);
            platform.addNode(n);
        }

        Node stop = createNode(ds);

        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertEquals(3, stop.getKeys().size());
        Assertions.assertTrue(stop.hasTag("public_transport", "stop_position"));
        Assertions.assertTrue(stop.hasTag("name", "Bus stop 02"));
        Assertions.assertTrue(stop.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
    }

    @Test
    void testCreateTagsWithoutRelationWayAreaPlatform() {
        StopFromPlatformAction action = new StopFromPlatformAction();

        Way platform = new Way();
        platform.put("name", "Bus stop 02");
        platform.put("highway", "platform");
        platform.put("public_transport", "platform");
        platform.put("area", "yes");

        ds.addPrimitive(platform);
        for (int i = 0; i < 4; i++) {
            platform.addNode(createNode(ds));
        }
        platform.addNode(platform.firstNode());

        Node stop = createNode(ds);

        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertEquals(3, stop.getKeys().size());
        Assertions.assertTrue(stop.hasTag("public_transport", "stop_position"));
        Assertions.assertTrue(stop.hasTag("name", "Bus stop 02"));
        Assertions.assertTrue(stop.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
    }

    @Test
    void testCreateTagsWithoutRelationRelationMultipolygonPlatform() {
        StopFromPlatformAction action = new StopFromPlatformAction();

        Relation platformMultipolygon = new Relation();
        Way way1 = new Way();
        Way way2 = new Way();
        Way way3 = new Way();
        ds.addPrimitive(platformMultipolygon);
        ds.addPrimitive(way1);
        ds.addPrimitive(way2);
        ds.addPrimitive(way3);
        way1.addNode(createNode(ds));
        way1.addNode(createNode(ds));
        way2.addNode(way1.lastNode());
        way2.addNode(createNode(ds));
        way3.addNode(way2.lastNode());
        way3.addNode(way1.lastNode());

        platformMultipolygon.addMember(new RelationMember("outline", way1));
        platformMultipolygon.addMember(new RelationMember("outline", way2));
        platformMultipolygon.addMember(new RelationMember("outline", way3));
        platformMultipolygon.put("name", "Bus stop 02");
        platformMultipolygon.put("highway", "platform");
        platformMultipolygon.put("public_transport", "platform");

        Node stop = createNode(ds);

        action.source = platformMultipolygon;
        action.destination = stop;
        action.runAction();

        Assertions.assertEquals(3, stop.getKeys().size());
        Assertions.assertTrue(stop.hasTag("public_transport", "stop_position"));
        Assertions.assertTrue(stop.hasTag("name", "Bus stop 02"));
        Assertions.assertTrue(stop.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
    }

    @Test
    void testAddStopWithRelation() {
        Node stop = createNode(ds);
        Node platform = createPlatform(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("stop", createStop(ds))
            .addMember("platform", createPlatform(ds))
            .addMember("platform", platform)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(8, r.getMembers().size());
        Assertions.assertEquals("stop", r.getMembers().get(4).getRole());
        Assertions.assertEquals(stop, r.getMembers().get(4).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
        Assertions.assertEquals(7, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(stop)));
    }

    @Test
    void testAddStopWithRelationMultipleStopMissing() {
        Node stop = createNode(ds);
        Node platform = createPlatform(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("platform", platform)
            .addMember("stop", createStop(ds))
            .addMember("platform", createPlatform(ds))
            .addMember("platform", platform)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(10, r.getMembers().size());
        Assertions.assertEquals("stop", r.getMembers().get(2).getRole());
        Assertions.assertEquals("stop", r.getMembers().get(6).getRole());
        Assertions.assertEquals(stop, r.getMembers().get(2).getMember());
        Assertions.assertEquals(stop, r.getMembers().get(6).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
        Assertions.assertEquals(8, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(stop)));

    }

    @Test
    void testAddStopWithRelationMultipleStopWithOnlyOneMissing() {
        Node stop = createNode(ds, Map.of("public_transport", "stop_position", "bus", "yes"));
        Node platform = createPlatform(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("stop", stop)
            .addMember("platform", platform)
            .addMember("stop", createStop(ds))
            .addMember("platform", createPlatform(ds))
            .addMember("platform", platform)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(10, r.getMembers().size());
        Assertions.assertEquals("stop", r.getMembers().get(2).getRole());
        Assertions.assertEquals("stop", r.getMembers().get(6).getRole());
        Assertions.assertEquals(stop, r.getMembers().get(2).getMember());
        Assertions.assertEquals(stop, r.getMembers().get(6).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(9, r.getMembers().size());
        Assertions.assertEquals("stop", r.getMembers().get(2).getRole());
        Assertions.assertEquals("platform", r.getMembers().get(6).getRole());
        Assertions.assertEquals(platform, r.getMembers().get(6).getMember());

    }

    @Test
    void testAddStopWithRelationForPlatformEntryOnly() {
        Node stop = createNode(ds);
        Node platform = createPlatform(ds);

        Relation r = new RelationHelper(ds)
            .addMember("platform_entry_only", platform)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(4, r.getMembers().size());
        Assertions.assertEquals("stop_entry_only", r.getMembers().get(0).getRole());
        Assertions.assertEquals(stop, r.getMembers().get(0).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
        Assertions.assertEquals(3, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(stop)));
    }

    @Test
    void testAddStopWithRelationForPlatformExitOnly() {
        Node stop = createNode(ds);
        Node platform = createPlatform(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("platform_exit_only", platform)
            .getRelation();

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(4, r.getMembers().size());
        Assertions.assertEquals("stop_exit_only", r.getMembers().get(2).getRole());
        Assertions.assertEquals(stop, r.getMembers().get(2).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
        Assertions.assertEquals(3, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(stop)));
    }

    @Test
    void testAddPlatformWithRelationWithBrokenRoleMembershipSkippedTagsAdded() {
        Node stop = createNode(ds);
        Node platform = createPlatform(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("wrong_role", platform)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        StopFromPlatformAction action = new StopFromPlatformAction();
        action.source = platform;
        action.destination = stop;
        action.runAction();

        Assertions.assertFalse(stop.getKeys().isEmpty());
        Assertions.assertEquals(5, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(stop)));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(stop.getKeys().isEmpty());
        Assertions.assertEquals(5, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(stop)));
    }
}
