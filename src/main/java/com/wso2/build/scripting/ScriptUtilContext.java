package com.wso2.build.scripting;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


final public class ScriptUtilContext extends AbstractUtilContext {

    private static final String ruleStateFolder = "rulestate";
    private static final String mvnBundlePluginName = "maven-bundle-plugin";
    private static final String instructionsTag = "instructions";
    private static final String approvedState = "Approved";
    private static final String dependencyApprovalRule = "_dependencyApprovalRule_";

    public ScriptUtilContext(MavenProject mavenProject, Log log) {
        super(mavenProject, log);
    }

    public List<String> getBundlePluginInstructionValues(String searchInstruction) {
        List<Plugin> plugins = mavenProject.getBuildPlugins();

        List<String> instructionValues = new LinkedList<String>();

        for (Plugin plugin : plugins) {
            if (true != plugin.getArtifactId().equals(mvnBundlePluginName)) {
                continue;
            }

            Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();

            Xpp3Dom[] instructions = config.getChildren(instructionsTag);

            for (Xpp3Dom instruction : instructions) {
                Xpp3Dom[] matchingInstructions = instruction.getChildren(searchInstruction);

                for (Xpp3Dom matchingInstruction : matchingInstructions) {
                    String exportPackageValue = matchingInstruction.getValue();

                    instructionValues.add(exportPackageValue);
                }
            }
        }

        return instructionValues;
    }

    public boolean isBundlePluginInstructionExist(String searchInstruction) {
        List<Plugin> plugins = mavenProject.getBuildPlugins();

        for (Plugin plugin : plugins) {
            if (true != plugin.getArtifactId().equals(mvnBundlePluginName)) {
                continue;
            }

            Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();

            Xpp3Dom[] instructions = config.getChildren(instructionsTag);

            for (Xpp3Dom instruction : instructions) {
                Xpp3Dom[] matchingInstructions = instruction.getChildren(searchInstruction);

                if (0 < matchingInstructions.length) {
                    return true;
                }
            }
        }

        return false;
    }


    public boolean isElementSpecified(String element) {
        Model model = mavenProject.getModel();

        File pomFile = model.getPomFile();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            NodeList nodeList = doc.getElementsByTagName(element);

            return (0 < nodeList.getLength());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public boolean hasChildParentElement(String childElement, String parentElement) {
        Model model = mavenProject.getModel();

        File pomFile = model.getPomFile();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            NodeList childNodes = doc.getElementsByTagName(childElement);

            if (0 == childNodes.getLength()) { // Specified child element does not exist
                return false;
            }

            for (int i = 0; i < childNodes.getLength(); ++i) {

                Node node = childNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Node parentNode = node.getParentNode();

                    // The child elements parent is not the specified parent
                    if (!parentElement.equals(parentNode.getNodeName())) {
                        return false;
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public boolean hasParentChildElement(String parentElement, String childElement) {
        Model model = mavenProject.getModel();

        File pomFile = model.getPomFile();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            NodeList parentNodes = doc.getElementsByTagName(parentElement);

            if (0 == parentNodes.getLength()) { // Specified parent element does not exist
                return false;
            }

            for (int i = 0; i < parentNodes.getLength(); ++i) {

                Node parentNode = parentNodes.item(i);

                if (parentNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                NodeList childNodes = parentNode.getChildNodes();

                boolean isChildPresent = false;

                for (int j = 0; j < childNodes.getLength(); ++j) {
                    Node childNode = childNodes.item(j);

                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    if (true == childElement.equals(childNode.getNodeName())) {
                        isChildPresent = true;
                    }
                }

                if (false == isChildPresent) { // Specified child not present in parent element
                    return false;
                }

            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }


    public List<NodeList> getChildrenOfParent(String parentElement) {
        Model model = mavenProject.getModel();

        File pomFile = model.getPomFile();

        List<NodeList> childNodeList = new LinkedList<NodeList>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile);

            NodeList parentNodes = doc.getElementsByTagName(parentElement);

            if (0 == parentNodes.getLength()) { // Specified parent element does not exist
                return childNodeList;
            }

            for (int i = 0; i < parentNodes.getLength(); ++i) {
                Node parentNode = parentNodes.item(i);

                if (parentNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                NodeList childNodes = parentNode.getChildNodes();

                childNodeList.add(childNodes);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return childNodeList;
    }

/*
    public boolean isRuleExecuted(String ruleName) {
        String runStatePath = parameters.getHomePath()+ File.separator + ruleStateFolder;

        File directory = new File(runStatePath);

        if (!directory.exists()) {
            return false;
        }

        File file = new File(runStatePath + File.separator + ruleName);

        if (file.isFile()) {
            return true;
        }

        return false;
    }

    public void flagRuleExecution(String ruleName) {
        String runStatePath = parameters.getHomePath()+ File.separator + ruleStateFolder;

        File directory = new File(runStatePath);

        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(runStatePath + File.separator + ruleName);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

}
