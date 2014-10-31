package com.wso2.build.enums;

/**
 * Created by uvindra on 4/2/14.
 */
public enum RuleType {
    PLUGIN, SCRIPT;

    private static final String pluginRuleType= "Maven Plugin";
    private static final String scriptRuleType = "Script";

    public static RuleType getValue(String valueString) {
        if (valueString.equalsIgnoreCase(pluginRuleType)) {
            return PLUGIN;
        }

        if (valueString.equalsIgnoreCase(scriptRuleType)) {
            return SCRIPT;
        }

        return PLUGIN;
    }
}
