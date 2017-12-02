package ru.toolkas.idea.plugins.documentum.docbase;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

import java.util.HashMap;
import java.util.Map;

public class LDmType {
    private final Map<String, ODmType> mapping = new HashMap<String, ODmType>();

    public static LDmType fetch(IDfSession session) throws DfException {
        LDmType lDmType = new LDmType();
        final String dql = "SELECT super_name, name FROM dm_type ORDER BY 1,2";

        IDfQuery query = new DfQuery();
        query.setDQL(dql);

        IDfCollection coll = null;
        try {
            coll = query.execute(session, IDfQuery.DF_EXEC_QUERY);
            while (coll.next()) {
                String name = coll.getString("name");
                String super_name = coll.getString("super_name");

                ODmType type = getODmtype(lDmType, super_name);
                type.addChild(name);
            }
        } finally {
            if (coll != null) {
                coll.close();
            }
        }
        return lDmType;
    }

    private static ODmType getODmtype(LDmType lDmType, String name) {
        ODmType type = lDmType.mapping.get(name);
        if (type == null) {
            lDmType.mapping.put(name, type = new ODmType(name));
        }
        return type;
    }

    public ODmType getDmType(String name) {
        ODmType type = mapping.get(name);
        if (type == null) {
            return new ODmType(name);
        }
        return type;
    }
}
