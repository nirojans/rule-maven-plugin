package com.wso2.build.core;

import com.wso2.build.beans.Rule;
import com.wso2.build.enums.RuleType;
import com.wso2.build.interfaces.Factory;
import com.wso2.build.interfaces.PluginConfigParser;
import com.wso2.build.rule.manage.RuleFetcher;
import com.wso2.build.scripting.ScriptUtilContext;
import org.apache.http.HttpException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;


public final class RuleExecutor {
    private Log log;

    private final class RuleStat {
        public int executedCount = 0;
    }

    public RuleExecutor(Log log) {  this.log = log;    }

    public void executeAllRules(MavenProject project, MavenSession session, BuildPluginManager pluginManager,
                                         RuntimeInformation runtime, Factory factory, List reactorProjects,
                                            String localRepo)
                                throws MojoExecutionException, IOException, HttpException, URISyntaxException {

        logRuleBegin(project.getName());

        RuleFetcher ruleFetcher=new RuleFetcher(localRepo,log);
        List<Rule> ruleList = ruleFetcher.getRules();


        List<MojoExecutionException> exceptions = new LinkedList<MojoExecutionException>();

        RuleStat stat = new RuleStat();

        for (Rule rule : ruleList) {

            if (RuleType.PLUGIN == rule.getType()) {
                executeMavenPluginRule(project, session, pluginManager, factory, rule, stat, exceptions);
            }
            else if (RuleType.SCRIPT == rule.getType()) {
                executeScriptRule(project, rule, stat, exceptions);
            }
            else {
                exceptions.add(new MojoExecutionException("Unhandled Rule Type specified for rule"));
            }
        }

        for (MojoExecutionException exception : exceptions) {
            log.info(exception.getLocalizedMessage());
            log.info("======================================================");
            //throw exception;
        }
        logRuleEnd(project.getName());

        // only print stats once, on the very last project in the reactor
        final int size = reactorProjects.size();
        MavenProject lastProject = (MavenProject) reactorProjects.get(size - 1);
        if (lastProject == project) {
            logRuleStats(ruleList.size(), stat);
        }

        if (!exceptions.isEmpty()) {
            logRuleStats(ruleList.size(), stat);
            throw new MojoExecutionException("Rule failure detected");
        }
    }


    private void executeMavenPluginRule(MavenProject project, MavenSession session, BuildPluginManager pluginManager,
                                        Factory factory, Rule rule, RuleStat stat, List<MojoExecutionException> exceptions) {
        PluginConfigParser parser = factory.getParser();

        parser.parseConfigs(new InputSource(new StringReader(rule.getDefinition())));

        ++stat.executedCount;
        try {
            run(project, session, pluginManager, parser);
            logRulePassed(rule.getName());
        }
        catch (MojoExecutionException e) {
            logRuleFailed(rule.getName());
            exceptions.add(e);
        }
    }

    private void executeScriptRule(MavenProject project, Rule rule, RuleStat stat,  List<MojoExecutionException> exceptions) {
        ++stat.executedCount;


        ScriptUtilContext utils = new ScriptUtilContext(project, log);

        try {
            if (utils.exec(rule.getDefinition())) {
                logRulePassed(rule.getName());
            }
            else {
                logRuleFailed(rule.getName());
                exceptions.add(new MojoExecutionException(utils.getLogString()));
            }
        } catch (MojoExecutionException e) {
            exceptions.add(e);
        }
    }


    private void logRuleBegin(final String name) {
        log.info("=================================================================================");
        log.info("BEGIN RULE CHECK FOR : " + name);
        log.info("=================================================================================");
    }

    private void logRuleEnd(final String name) {
        log.info("=================================================================================");
        log.info("END RULE CHECK FOR : " + name);
        log.info("=================================================================================");

    }

    private void logRulePassed(final String name) {
        log.info("Rule Name : " + name + ", Status : PASSED");
    }

    private void logRuleFailed(final String name) {
        log.info("Rule Name : " + name + ", Status : FAILED");
    }


    private void logRuleStats(final int totalRuleCount, final RuleStat stat) {
        log.info("=================================================================================");
        log.info("RULE EXECUTION STATS");
        log.info("=================================================================================");
        log.info("Total Rule Count : " + totalRuleCount);
        log.info("Executed Rule Count : " + stat.executedCount);
    }

    private void run(MavenProject project, MavenSession session, BuildPluginManager pluginManager,
                                PluginConfigParser parser) throws MojoExecutionException{
        Plugin plugin = new Plugin();
        plugin.setGroupId(parser.getGroupId());
        plugin.setArtifactId(parser.getArtifactId());
        plugin.setVersion(parser.getVersion());

        PluginExecution pluginExecution = new PluginExecution();

        pluginExecution.setId(parser.getId());

        List<String> goals = new LinkedList<String>();
        goals.add(parser.getGoal());
        pluginExecution.setGoals(goals);

        plugin.addExecution(pluginExecution);

        executeMojo(plugin, parser.getGoal(), parser.getConfiguration(),
                executionEnvironment(project, session, pluginManager));
    }

}
