package com.wso2.build.rule.manage;


import com.wso2.build.beans.Rule;
import com.wso2.build.enums.RuleCategory;
import com.wso2.build.enums.RuleType;
import com.wso2.build.nexus.Reader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class RuleFetcher {

    private Log log;

    private final String localRepoLocation;
    private static final String groupID = "com.wso2.carbon";
    private static final String artifactID = "rule-checker";
    private static final String metaDataFile = "metaData.xml";
    private static final String nexusMetaURL="http://localhost:8080/nexus/service/local/repositories/releases/content/com/wso2/carbon/rule-checker/maven-metadata.xml";

    private static String nexusZipURL="http://localhost:8080/nexus/content/repositories/releases/com/wso2/carbon/rule-checker";
    private static String ruleLocation;

    private static String latestMetaData=null;
    private static String currentMetaData=null;
    private static String latestVersion=null;
    private static String currentVersion=null;

    private static final String definitionStartTag = "<definition>";
    private static final String definitionEndTag = "</definition>";
    private static final String nameStartTag = "<name>";
    private static final String nameEndTag = "</name>";
    private static final String typeStartTag = "<type>";
    private static final String typeEndTag = "</type>";
    private static final String compatibleMavenVersionStartTag = "<version>";
    private static final String compatibleMavenVersionEndTag = "</version>";
    private static final String latestVersionStartTag = "<latest>";
    private static final String latestVersionEndTag = "</latest>";

    public RuleFetcher(String localRepoLocation,Log log){

        this.log=log;
        this.localRepoLocation=localRepoLocation;

    }

    public List<Rule> getRules() throws IOException, MojoExecutionException {

        setRuleLocation();
        List<Rule> ruleList = new LinkedList<Rule>();
        Reader reader =new Reader();

        List<String> rules= reader.getStringRules(ruleLocation);

        for (String rule : rules) {

            String name = extractTagValue(rule, nameStartTag, nameEndTag);

            String type = extractTagValue(rule, typeStartTag, typeEndTag);

            String mvnVersion = extractTagValue(rule, compatibleMavenVersionStartTag, compatibleMavenVersionEndTag);

            String definition = extractTagValue(rule, definitionStartTag, definitionEndTag);

            definition = StringEscapeUtils.unescapeXml(definition);

            ruleList.add(new Rule(name, RuleType.getValue(type), mvnVersion, definition));
        }

        return ruleList;
    }

    public void setRuleLocation() throws MojoExecutionException {

        Reader reader =new Reader();
        String artifactDir = localRepoLocation +File.separator + groupID.replaceAll("\\.",File.separator)+ File.separator + artifactID;

        if( getLatestMetaData(reader , nexusMetaURL) != null )
            latestVersion = getVersion(latestMetaData );

        if( getCurrentMetaData(reader , artifactDir ) != null )
            currentVersion = getVersion(currentMetaData);

        /* If could not connect to nexus and if there is no existing rules found*/
        if(latestVersion==null && currentVersion ==null){
            throw new MojoExecutionException("NETWORK CONNECTION UNAVAILABLE AND  EXISTING RULES FOUND ABORTED BUILD");
        }

        /* If could not connect to nexus but rule repo exist in the .m2 directory*/
        if(latestVersion==null && currentVersion !=null){
            log.info("Could not resolve nexus latest rule version falling back to legacy behaviour ");
            ruleLocation = artifactDir + File.separator + "rules-" + currentVersion;
            return;
        }

        ruleLocation = artifactDir + File.separator + "rules-" + latestVersion;
        nexusZipURL += "/" + latestVersion + "/" + artifactID + "-" + latestVersion + ".zip";

        /* Latest version available from nexus but no current rule repo in the .m2 directory */
        if(latestVersion != null && currentVersion ==null){
            if (reader.getZipFile(ruleLocation, nexusZipURL)) {
                if (!createMetaDataXML(latestMetaData, artifactDir)) {
                    throw new MojoExecutionException("WARNING - METADATA.XML FILE CREATION FAILED ABORTING BUILD ");
                }
            } else {
                throw new MojoExecutionException("COULD NOT RETRIEVE RULE.ZIP ABORTING BUILD ");
            }
            return;
        }

        /* if current rules set is outdated than the nexus rule set */
        if (!currentVersion.equals(latestVersion)) {
            if (reader.getZipFile(ruleLocation, nexusZipURL)) {
                if (!createMetaDataXML(latestMetaData, artifactDir))
                    ruleLocation = artifactDir + File.separator + "rules-" + currentVersion;
            }
            else ruleLocation = artifactDir + File.separator + "rules-" + currentVersion;
        }

    }

    public String getLatestMetaData(Reader reader,String nexusMetaURL){

        try {
            latestMetaData = reader.getLatestMetaData(nexusMetaURL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("COULD NOT CONNECT TO NEXUS CONNECTION REFUSED CHECK INTERNET CONNECTIVITY ");
        } catch (HttpException e) {
            e.printStackTrace();
        }
        return latestMetaData;
    }

    public String getVersion(String latestMetaData){
        return extractTagValue(latestMetaData, latestVersionStartTag, latestVersionEndTag);
    }

    public String getCurrentMetaData(Reader reader , String artifactDir ){

        try {
            currentMetaData = reader.getCurrentMetaData(artifactDir + File.separator+ metaDataFile);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            log.info(metaDataFile + " IS NOT FOUND IN THE " + artifactDir);
        }
        return currentMetaData;
    }

    public Boolean createMetaDataXML(String latestMetaData,String artifactDir){
        Boolean status= false;
        try {
            File dir = new File(artifactDir);
            if (!dir.exists()) {
                FileUtils.forceMkdir(dir);
            }
            File file = new File(dir + File.separator + "metaData.xml");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(latestMetaData);
            output.close();
            status=true;

            log.info(metaDataFile + " IS CREATED IN " + artifactDir);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    private String extractTagValue(final String content, final String startTag, final String endTag) {
        int tagStart = content.indexOf(startTag);
        int tagEnd = content.indexOf(endTag);

        if (-1 != tagStart && -1 != tagEnd) {
            return content.substring(tagStart + startTag.length(), tagEnd);
        }
        return "";
    }
}
