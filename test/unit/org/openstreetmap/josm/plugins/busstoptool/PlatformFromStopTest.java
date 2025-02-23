package org.openstreetmap.josm.plugins.busstoptool;

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

public class PlatformFromStopTest {
    private DataSet ds;

    @BeforeEach
    void setUp() {
        UndoRedoHandler.getInstance().clean();

        this.ds = new DataSet();

        new MockUp<BusStopToolGUI>()
        {
            @Mock
            void errorDialog(String msg) {}
        };
    }

    @Test
    void testMissingSourceOrDestinationObject() {
        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = createStop(ds);
        action.destination = null;
        action.runAction();

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());

        action.source = null;
        action.destination = createPlatform(ds);
        action.runAction();

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());

        action.source = null;
        action.destination = null;
        action.runAction();

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());
    }

    @Test
    void testSourceWithoutRequiredTag() {
        Node stop = createNode(ds);
        Node platform = createNode(ds);

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stop;
        action.destination = platform;
        action.runAction();

        Assertions.assertFalse(platform.hasKeys());
        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());
    }

    @Test
    void testCreateTagsWithoutRelationNodeBusStop() {
        Node stop = createStop(ds);
        stop.put("name", "Bus stop 01");

        Node platform = createNode(ds);

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stop;
        action.destination = platform;
        action.runAction();

        Assertions.assertEquals(4, platform.getKeys().size());
        Assertions.assertTrue(platform.hasTag("public_transport", "platform"));
        Assertions.assertTrue(platform.hasTag("highway", "bus_stop"));
        Assertions.assertTrue(platform.hasTag("name", "Bus stop 01"));
        Assertions.assertTrue(platform.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(platform.getKeys().isEmpty());
    }

    @Test
    void testCreateTagsWithoutRelationWayPlatform() {
        Node stop = createStop(ds);
        stop.put("name", "Bus stop 02");
        PlatformFromStopAction action = new PlatformFromStopAction();

        Way platformArea = new Way();
        ds.addPrimitive(platformArea);
        for (int i = 0; i < 4;i++) {
            Node n = new Node(new LatLon(0, i));
            ds.addPrimitive(n);
            platformArea.addNode(n);
        }
        action.source = stop;

        action.destination = platformArea;
        action.runAction();

        Assertions.assertEquals(4, platformArea.getKeys().size());
        Assertions.assertTrue(platformArea.hasTag("public_transport", "platform"));
        Assertions.assertTrue(platformArea.hasTag("highway", "platform"));
        Assertions.assertTrue(platformArea.hasTag("name", "Bus stop 02"));
        Assertions.assertTrue(platformArea.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(platformArea.getKeys().isEmpty());
    }

    @Test
    void testCreateTagsWithoutRelationWayAreaPlatform() {
        Node stop = createStop(ds);
        stop.put("name", "Bus stop 02");
        PlatformFromStopAction action = new PlatformFromStopAction();

        Way platformArea = new Way();
        ds.addPrimitive(platformArea);
        for (int i = 0; i < 4;i++) {
            platformArea.addNode(createNode(ds));
        }
        platformArea.addNode(platformArea.firstNode());
        action.source = stop;

        action.destination = platformArea;
        action.runAction();

        Assertions.assertEquals(5, platformArea.getKeys().size());
        Assertions.assertTrue(platformArea.hasTag("public_transport", "platform"));
        Assertions.assertTrue(platformArea.hasTag("highway", "platform"));
        Assertions.assertTrue(platformArea.hasTag("area", "yes"));
        Assertions.assertTrue(platformArea.hasTag("name", "Bus stop 02"));
        Assertions.assertTrue(platformArea.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(platformArea.getKeys().isEmpty());
    }

    @Test
    void testCreateTagsWithoutRelationRelationMultipolygonPlatform() {
        Node stop = createStop(ds);
        stop.put("name", "Bus stop 02");
        PlatformFromStopAction action = new PlatformFromStopAction();

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

        action.source = stop;

        action.destination = platformMultipolygon;
        action.runAction();

        Assertions.assertEquals(4, platformMultipolygon.getKeys().size());
        Assertions.assertTrue(platformMultipolygon.hasTag("public_transport", "platform"));
        Assertions.assertTrue(platformMultipolygon.hasTag("highway", "platform"));
        Assertions.assertTrue(platformMultipolygon.hasTag("name", "Bus stop 02"));
        Assertions.assertTrue(platformMultipolygon.hasTag("bus", "yes"));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(platformMultipolygon.getKeys().isEmpty());
    }

    @Test
    void testAddPlatformWithRelation() {
        Node stopNode = createStop(ds);
        Node newPlatformNode = createNode(ds, Map.of());

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("stop", createStop(ds))
            .addMember("platform", createPlatform(ds))
            .addMember("stop", stopNode)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stopNode;
        action.destination = newPlatformNode;
        action.runAction();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(8, r.getMembers().size());
        Assertions.assertEquals("platform", r.getMembers().get(5).getRole());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(5).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(7, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(newPlatformNode)));
    }

    @Test
    void testAddPlatformWithRelationMultiplePlatformMissing() {
        Node stopNode = createStop(ds);
        Node newPlatformNode = createNode(ds, Map.of());

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("stop", stopNode)
            .addMember("stop", createStop(ds))
            .addMember("platform", createPlatform(ds))
            .addMember("stop", stopNode)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stopNode;
        action.destination = newPlatformNode;
        action.runAction();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(10, r.getMembers().size());
        Assertions.assertEquals("platform", r.getMembers().get(3).getRole());
        Assertions.assertEquals("platform", r.getMembers().get(7).getRole());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(3).getMember());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(7).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(8, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(newPlatformNode)));

    }

    @Test
    void testAddPlatformWithRelationMultiplePlatformWithOnlyOneMissing() {
        Node stopNode = createStop(ds);
        Node newPlatformNode = createNode(ds, Map.of("highway", "bus_stop", "public_transport", "platform"));

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("stop", stopNode)
            .addMember("platform", newPlatformNode)
            .addMember("stop", createStop(ds))
            .addMember("platform", createPlatform(ds))
            .addMember("stop", stopNode)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stopNode;
        action.destination = newPlatformNode;
        action.runAction();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(10, r.getMembers().size());
        Assertions.assertEquals("platform", r.getMembers().get(3).getRole());
        Assertions.assertEquals("platform", r.getMembers().get(7).getRole());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(3).getMember());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(7).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(9, r.getMembers().size());
        Assertions.assertEquals("platform", r.getMembers().get(3).getRole());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(3).getMember());
    }

    @Test
    void testAddPlatformWithRelationForStopEntryOnly() {
        Node stopNode = createStop(ds);
        Node newPlatformNode = createNode(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", stopNode)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stopNode;
        action.destination = newPlatformNode;
        action.runAction();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(4, r.getMembers().size());
        Assertions.assertEquals("platform_entry_only", r.getMembers().get(1).getRole());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(1).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(3, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(newPlatformNode)));
    }
    @Test
    void testAddPlatformWithRelationForStopExitOnly() {
        Node stopNode = createStop(ds);
        Node newPlatformNode = createNode(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("stop_exit_only", stopNode)
            .getRelation();

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stopNode;
        action.destination = newPlatformNode;
        action.runAction();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(4, r.getMembers().size());
        Assertions.assertEquals("platform_exit_only", r.getMembers().get(3).getRole());
        Assertions.assertEquals(newPlatformNode, r.getMembers().get(3).getMember());

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(3, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(newPlatformNode)));
    }

    @Test
    void testAddPlatformWithRelationWithBrokenRoleMembershipSkippedTagsAdded() {
        Node stopNode = createStop(ds);
        Node newPlatformNode = createNode(ds);

        Relation r = new RelationHelper(ds)
            .addMember("stop_entry_only", createStop(ds))
            .addMember("platform_entry_only", createPlatform(ds))
            .addMember("wrong_role", stopNode)
            .addMember("stop_exit_only", createStop(ds))
            .addMember("platform_exit_only", createPlatform(ds))
            .getRelation();

        PlatformFromStopAction action = new PlatformFromStopAction();
        action.source = stopNode;
        action.destination = newPlatformNode;
        action.runAction();

        Assertions.assertFalse(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(5, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(newPlatformNode)));

        UndoRedoHandler.getInstance().getLastCommand().undoCommand();

        Assertions.assertTrue(newPlatformNode.getKeys().isEmpty());
        Assertions.assertEquals(5, r.getMembers().size());
        Assertions.assertFalse(r.getMembers().stream().anyMatch(rm -> rm.getMember().equals(newPlatformNode)));
    }
}
