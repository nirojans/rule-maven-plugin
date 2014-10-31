package com.wso2.build.parser;

import com.wso2.build.interfaces.PluginConfigParser;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by uvindra on 2/9/14.
 */
public class SAXConfigParser extends DefaultHandler
                            implements PluginConfigParser {
    private static final String groupIdTag = "groupId";
    private static final String artifactIdTag = "artifactId";
    private static final String versionTag = "version";
    private static final String goalTag = "goal";
    private static final String idTag = "id";
    private static final String phaseTag = "phase";
    private static final String configurationTag = "configuration";

    private String groupID;
    private String artifactId;
    private String version;
    private String goal;
    private String id;
    private String phase;
    private Xpp3Dom configuration;

    private boolean isFoundGroupId;
    private boolean isFoundArtifactId;
    private boolean isFoundVersion;
    private boolean isFoundGoal;
    private boolean isFoundId;
    private boolean isFoundPhase;
    private boolean isFoundConfiguration;

    private List<Xpp3Dom> configChildren;
    private String configValue;

    public SAXConfigParser() {
        groupID = "";
        artifactId = "";
        version = "";
        goal = "";
        id = "";
        phase = "";
        configuration = new Xpp3Dom(configurationTag);
        configChildren = new LinkedList<Xpp3Dom>();
        configValue = "";

        isFoundGroupId = false;
        isFoundArtifactId = false;
        isFoundVersion = false;
        isFoundGoal = false;
        isFoundId = false;
        isFoundPhase = false;
        isFoundConfiguration = false;
    }

    @Override
    public void parseConfigs(InputSource ruleSource) {
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);


            xr.parse(ruleSource);

        } catch (SAXException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getGroupId() {
        return groupID;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getGoal() {
        return goal;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Xpp3Dom getConfiguration() {
        return configuration;
    }

    @Override
    public String getPhase() {
        return phase;
    }

    @Override
    public void startElement(String uri, String name,
                              String qName, Attributes attributes) {
        if (isFoundConfiguration) {
            configChildren.add(new Xpp3Dom(qName));
        }
        else {
            detectStartOfSiblingTag(qName);
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        if (isFoundConfiguration && true != configValue.isEmpty() &&
                true != qName.equalsIgnoreCase(configurationTag)) {
            if (configChildren.isEmpty()) {
                configuration.setValue(configValue);
            }
            else {
                Xpp3Dom lastChild = configChildren.get(configChildren.size() - 1);
                lastChild.setValue(configValue);
            }

            configValue = "";
        }
        else {
            detectEndOfSiblingTag(qName);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
                case '\\':
                case '"':
                case '\n':
                case '\r':
                case '\t':
                case ' ': // Ignore spaces as well
                    break;
                default:
                    valueCharacterDetected(ch[i]);
                    break;
            }
        }
    }

    private void detectStartOfSiblingTag(String qName) {
        if (qName.equalsIgnoreCase(groupIdTag)) {
            isFoundGroupId = true;
        }
        else if (qName.equalsIgnoreCase(artifactIdTag)) {
            isFoundArtifactId = true;
        }
        else if (qName.equalsIgnoreCase(versionTag)) {
            isFoundVersion = true;
        }
        else if (qName.equalsIgnoreCase(goalTag)) {
            isFoundGoal = true;
        }
        else if (qName.equalsIgnoreCase(idTag)) {
            isFoundId = true;
        }
        else if (qName.equalsIgnoreCase(phaseTag)) {
            isFoundPhase = true;
        }
        else if (qName.equalsIgnoreCase(configurationTag)) {
            isFoundConfiguration = true;
        }
    }


    private void detectEndOfSiblingTag(String qName) {
        if (qName.equalsIgnoreCase(groupIdTag)) {
            isFoundGroupId = false;
        }
        else if (qName.equalsIgnoreCase(artifactIdTag)) {
            isFoundArtifactId = false;
        }
        else if (qName.equalsIgnoreCase(versionTag)) {
            isFoundVersion = false;
        }
        else if (qName.equalsIgnoreCase(goalTag)) {
            isFoundGoal = false;
        }
        else if (qName.equalsIgnoreCase(idTag)) {
            isFoundId = false;
        }
        else if (qName.equalsIgnoreCase(phaseTag)) {
            isFoundPhase = false;
        }
        else if (qName.equalsIgnoreCase(configurationTag)) {
            isFoundConfiguration = false;

            // Child elements of the configuration element exist
            if (true != configChildren.isEmpty()) {
                int index = configChildren.size() - 1; // Start from last element

                // Initially both the child element and the parent element
                // will point to the last element in the configuration
                // child element list
                Xpp3Dom childTag = configChildren.remove(index--);
                Xpp3Dom parentTag = childTag;

                while (true != configChildren.isEmpty()) {
                    parentTag = configChildren.remove(index--);

                    parentTag.addChild(childTag);

                    childTag = parentTag;
                }

                // Finally the configuration element will contain its children
                configuration.addChild(parentTag);
            }
        }
    }


    private void valueCharacterDetected(char ch) {
        if (isFoundGroupId) {
            groupID += ch;
        }
        else if (isFoundArtifactId) {
            artifactId += ch;
        }
        else if (isFoundVersion) {
            version += ch;
        }
        else if (isFoundId) {
            id += ch;
        }
        else if (isFoundGoal) {
            goal += ch;
        }
        else if (isFoundPhase) {
            phase += ch;
        }
        else if (isFoundConfiguration) {
            configValue += ch;
        }
    }
}
