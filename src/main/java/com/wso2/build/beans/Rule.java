package com.wso2.build.beans;

import com.wso2.build.enums.RuleType;
import java.io.Serializable;

public class Rule implements Serializable {
    private String name = "";
    private RuleType type = RuleType.PLUGIN;
    private String compatibleMavenVersion = "";
    private String definition;

    public Rule(String name, RuleType type,String compatibleMavenVersion, String definition) {
        this.name = name;
        this.type = type;
        this.compatibleMavenVersion = compatibleMavenVersion;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompatibleMavenVersion() {
        return compatibleMavenVersion;
    }

    public void setCompatibleMavenVersion(String compatibleMavenVersion) {
        this.compatibleMavenVersion = compatibleMavenVersion;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }
}
