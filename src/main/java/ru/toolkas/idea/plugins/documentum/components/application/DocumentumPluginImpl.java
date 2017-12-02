package ru.toolkas.idea.plugins.documentum.components.application;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.apache.commons.beanutils.BeanUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import ru.toolkas.idea.plugins.documentum.components.DocumentumPluginConstants;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManagerImpl;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class DocumentumPluginImpl implements DocumentumPlugin {
    private static final String NAME = DocumentumPluginConstants.NAME.concat(".configuration");
    private static final String DOCBASE = "docbase";

    private DocbaseManager docbaseManager = new DocbaseManagerImpl();

    @Override
    public DocbaseManager getDocbaseManager() {
        return docbaseManager;
    }

    @Override
    public void initComponent() {
        //хака для корректной работы dfc из класслоадера плагина
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        ConcurrentUtils.init();
    }

    @Override
    public void disposeComponent() {
        docbaseManager.disconnectAll();
        ConcurrentUtils.shutdown();
    }

    @Override
    public String getExternalFileName() {
        return NAME;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        try {
            List list = element.getChildren(DOCBASE);
            if (list != null) {
                for (Object object : list) {
                    Element el = (Element) object;

                    Docbase configuration = new Docbase();
                    readPropertyFromElement(configuration, el, "name");
                    readPropertyFromElement(configuration, el, "docbaseName");
                    readPropertyFromElement(configuration, el, "login");
                    readPropertyFromElement(configuration, el, "password");

                    docbaseManager.addDocbase(configuration);

                    Element properties = el.getChild("properties");
                    if (properties != null) {
                        List elements = properties.getChildren("property");
                        if (elements != null) {
                            for (Object obj : elements) {
                                Element property = (Element) obj;
                                configuration.setDfcPropertyCustomValue(property.getAttributeValue("name"), property.getAttributeValue("value"));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new InvalidDataException(ex);
        }
    }

    private void readPropertyFromElement(Docbase configuration, Element element, String property) throws InvocationTargetException, IllegalAccessException {
        String value = element.getChildText(property);
        if (value != null) {
            value = value.trim();
        }

        BeanUtils.setProperty(configuration, property, value);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        try {
            for (Docbase configuration : docbaseManager.getDocbases()) {
                Element el = new Element(DOCBASE);

                writePropertyToElement(configuration, el, "name");
                writePropertyToElement(configuration, el, "docbaseName");
                writePropertyToElement(configuration, el, "login");
                writePropertyToElement(configuration, el, "password");

                element.addContent(el);

                Element properties = new Element("properties");

                for (Map.Entry<String, String> entry : configuration.getDfcPropertiesCustomValues().entrySet()) {
                    Element property = new Element("property");
                    property.setAttribute("name", entry.getKey());
                    property.setAttribute("value", entry.getValue());
                    properties.addContent(property);
                }

                el.addContent(properties);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writePropertyToElement(Docbase configuration, Element element, String property) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String value = BeanUtils.getProperty(configuration, property);

        if (value != null) {
            value = value.trim();
        }

        element.addContent(new Element(property).setText(value));
    }

    @NotNull
    @Override
    public String getComponentName() {
        return NAME;
    }
}
