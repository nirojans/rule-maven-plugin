package com.wso2.build.factories;

import com.wso2.build.interfaces.Factory;
import com.wso2.build.interfaces.FactoryContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Map;


/**
 * Created by uvindra on 2/9/14.
 */
@Component( role = FactoryContainer.class )
public class FactoryContainerImpl implements FactoryContainer {

    /**
     * Inject all implementations of Factory.  The container automatically add all the components defined with the role <code>Factory.class</code>.
     */
    @Requirement( role = Factory.class )
    private Map<String, Factory> factories;

    @Override
    public Factory getFactory(String factoryName) {
        return factories.get(factoryName);
    }
}
