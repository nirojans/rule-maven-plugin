package com.wso2.build.mojo;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by uvindra on 2/25/14.
 */

/**
 * Created by uvindra on 2/9/14.
 * @goal clean
 */
public class StateCleanMojo extends AbstractMojo {

    /**
     * The settings.xml file in .m2.
     *
     * @parameter expression="${settings}"
     * @required
     */
    private Settings settings;

    @Override
    public void execute() throws MojoExecutionException {
        Map<String, Profile> profileMap = settings.getProfilesAsMap();

        Profile profile = profileMap.get("rule");

        Properties properties = profile.getProperties();

        String runStatePath = properties.getProperty("home") + File.separator + "rulestate";

        File filePath = new File(runStatePath);

        if (true != filePath.exists()) { // File path doesnt exist
            return;
        }

        try {
            FileUtils.cleanDirectory(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
