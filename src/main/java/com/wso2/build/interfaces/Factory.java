package com.wso2.build.interfaces;

import java.io.Serializable;



public interface Factory extends Serializable{
    PluginConfigParser getParser();
}
