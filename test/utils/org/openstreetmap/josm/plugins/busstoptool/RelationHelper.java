package org.openstreetmap.josm.plugins.busstoptool;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;

public class RelationHelper {
    private final Relation relation;

    public RelationHelper(DataSet ds) {
        this.relation = new Relation();
        ds.addPrimitive(relation);
    }

    public RelationHelper addMember(String role, OsmPrimitive primitive) {
        relation.addMember(new RelationMember(role, primitive));
        return this;
    }


    public Relation getRelation() {
        return relation;
    }
}
