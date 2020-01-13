package ru.toolkas.idea.plugins.documentum.docbase;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;

import java.util.*;

public class Docbase implements Cloneable {
    private final Map<String, String> dfcPropertiesCustomValues = new HashMap<String, String>();
    private List<DfcProperty> dfcProperties = new ArrayList<DfcProperty>();

    private String name;
    private String docbaseName;
    private String login;
    private String password;

    private LDmType dmType;
    private IDfSession session;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocbaseName() {
        return docbaseName;
    }

    public void setDocbaseName(String docbaseName) {
        this.docbaseName = docbaseName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getDfcPropertiesCustomValues() {
        return Collections.unmodifiableMap(dfcPropertiesCustomValues);
    }

    public void setDfcPropertyCustomValue(String name, String value) {
        dfcPropertiesCustomValues.put(name, value);
    }

    public List<DfcProperty> getDfcProperties() {
        try {
            if (dfcProperties.isEmpty()) {
                IDfTypedObject config = new DfClientX().getLocalClient().getClientConfig();

                for (int index = 0; index < config.getAttrCount(); index++) {
                    dfcProperties.add(new DfcProperty(config.getAttr(index).getName(), config.getValueAt(index).asString()));
                }
            }
            return dfcProperties;
        } catch (DfException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void connect() throws DfException {
        disconnect();

        IDfClientX cx = new DfClientX();
        IDfClient client = cx.getLocalClient();

        IDfTypedObject config = client.getClientConfig();
        for (Map.Entry<String, String> entry : getDfcPropertiesCustomValues().entrySet()) {
            config.setString(entry.getKey(), entry.getValue());
        }

        IDfLoginInfo info = new DfLoginInfo();
        info.setUser(getLogin());
        info.setPassword(getPassword());

        session = client.newSession(getDocbaseName(), info);
    }

    public boolean isConnected() {
        return session != null;
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                session.disconnect();
            } catch (DfException e) {
                e.printStackTrace();
            } finally {
                session = null;
            }
        }
    }

    public void execute(final Action action) throws DfException {
        checkConnected();

        if (action != null) action.execute(session);
    }

    private void checkConnected() {
        if (!isConnected()) {
            throw new RuntimeException("Docbase '" + name + "' is not connected");
        }
    }

    public void select(final String dql, final Processor processor) throws DfException {
        checkConnected();

        IDfQuery query = new DfQuery();
        query.setDQL(dql);

        IDfCollection coll = null;
        try {
            coll = query.execute(session, IDfQuery.DF_QUERY);

            processor.init(coll);
            while (coll.next()) {
                IDfTypedObject object = coll.getTypedObject();
                if (!processor.process(object)) {
                    break;
                }
            }
        } finally {
            if (coll != null) {
                coll.close();
            }
        }
    }

    public LDmType getDmType() throws DfException {
        checkConnected();

        synchronized (this) {
            if (dmType == null) {
                execute(new Action() {
                    @Override
                    public void execute(IDfSession session) throws DfException {
                        dmType = LDmType.fetch(session);
                    }
                });
            }
        }
        return dmType;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Docbase docbase = (Docbase) super.clone();
        docbase.session = null;
        return docbase;
    }

    public class DfcProperty {
        private String name;
        private String value;

        public DfcProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            if (dfcPropertiesCustomValues.containsKey(name)) {
                return dfcPropertiesCustomValues.get(name);
            }
            return value;
        }

        public void setValue(String value) {
            if (isModifiable()) {
                dfcPropertiesCustomValues.put(name, value);
            }
        }

        public boolean isModifiable() {
            return true;
        }

        public boolean isChanged() {
            return dfcPropertiesCustomValues.containsKey(name);
        }
    }

    public static interface Action {
        void execute(IDfSession session) throws DfException;
    }

    public static abstract class Processor {
        public void init(IDfTypedObject object) throws DfException {

        }

        public abstract boolean process(IDfTypedObject object) throws DfException;
    }
}
