package com.wso2.build.mojo;

import com.wso2.build.core.RuleExecutor;
import com.wso2.build.interfaces.Factory;
import com.wso2.build.interfaces.FactoryContainer;
import org.apache.http.HttpException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by uvindra on 3/4/14.
 * @goal checkall
 * @requiresDependencyResolution test
 */
public class AllRuleExecutorMojo  extends AbstractMojo {
    /**
     * The project currently being build.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The current Maven session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * The Maven BuildPluginManager component.
     *
     * @component
     * @required
     */
    private BuildPluginManager pluginManager;

    /**
     * The Maven RuntimeInformation component.
     *
     * @component
     * @required
     */
    private RuntimeInformation runtime;

    /**
     * The settings.xml file in .m2.
     *
     * @parameter expression="${settings}"
     * @required
     */
    private Settings settings;

    /**
     * The projects in the reactor.
     *
     * @parameter expression="${reactorProjects}"
     * @readonly
     */
    private List reactorProjects;

    private FactoryContainer factoryContainer = null;
    private RuleExecutor ruleExecutor = new RuleExecutor(getLog());

    @Override
    public void execute() throws MojoExecutionException {

        String localRepo=settings.getLocalRepository();

        try {
            PlexusContainer container = new DefaultPlexusContainer();

            factoryContainer = container.lookup(FactoryContainer.class);

            Factory factory = factoryContainer.getFactory("default");

            ruleExecutor.executeAllRules(project, session, pluginManager, runtime, factory, reactorProjects,localRepo);

            // stop the components and container
            container.dispose();
        }
        catch (PlexusContainerException e) {
            e.printStackTrace();

            throw new MojoExecutionException("Default Plexus container could not be instantiated");
        }
        catch (ComponentLookupException e) {
            e.printStackTrace();

            throw new MojoExecutionException("Factory container could not be instantiated");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
