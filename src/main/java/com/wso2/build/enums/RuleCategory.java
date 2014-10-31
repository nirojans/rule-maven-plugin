package com.wso2.build.enums;

/**
 * Created by uvindra on 2/22/14.
 */
public enum RuleCategory {
    DEFAULT, EXPLICIT;

    private static final String defaultRuleCategory = "Default";
    private static final String explicitRuleCategory = "Explicit";

    public static RuleCategory getValue(String valueString) {
        if (valueString.equalsIgnoreCase(defaultRuleCategory)) {
            return DEFAULT;
        }

        if (valueString.equalsIgnoreCase(explicitRuleCategory)) {
            return EXPLICIT;
        }

        return DEFAULT;
    }
}
