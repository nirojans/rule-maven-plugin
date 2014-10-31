package com.wso2.build.factories;

import com.wso2.build.interfaces.Factory;
import com.wso2.build.interfaces.PluginConfigParser;
import com.wso2.build.parser.SAXConfigParser;
import org.codehaus.plexus.component.annotations.Component;


@Component( role = Factory.class, hint = "default" )
public class DefaultFactory implements Factory {

    @Override
    public PluginConfigParser getParser() {
        return new SAXConfigParser();
    }

}
