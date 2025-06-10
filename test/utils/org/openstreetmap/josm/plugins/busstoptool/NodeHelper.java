package org.openstreetmap.josm.plugins.busstoptool;

import java.util.Map;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class NodeHelper {
    public static Node createNode(DataSet ds) {
        Node node = new Node(new LatLon(0, 0));
        ds.addPrimitive(node);

        return node;
    }

    public static Node createNode(DataSet ds, Map<String, String> tags) {
           Node node = createNode(ds);
           tags.forEach(node::put);

           return node;
    }

    public static Node createStop(DataSet ds) {
        return createNode(
            ds,
            Map.of(
                "public_transport", "stop_position",
                "bus", "yes"
            )
        );
    }

    public static Node createPlatform(DataSet ds) {
        return createNode(
            ds,
            Map.of(
                "highway", "bus_stop",
                "public_transport", "platform"
            )
        );
    }
}
