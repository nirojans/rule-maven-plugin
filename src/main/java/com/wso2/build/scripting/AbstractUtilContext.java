package com.wso2.build.scripting;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.script.*;


public class AbstractUtilContext {
    private static final String scriptObjectVar = "_scriptObj";
    private static final String scriptFunctionVar = "_scriptFunction";
    private static final String scriptUtilObjectName = "utils";
    private static final String scriptPrefix = "var " + scriptObjectVar + " = new Object();\n" +
                                                scriptObjectVar +"." + scriptFunctionVar + " = ";

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");

    protected MavenProject mavenProject = null;
    protected MavenProject parentProject = null;
    protected Log log = null;
    private ScriptContext newContext = null;
    private String logString = "N/A";

    public AbstractUtilContext(MavenProject mavenProject, Log log) {
        this.mavenProject = mavenProject;
        this.log = log;
        parentProject = this.mavenProject.getParent();

        newContext = new SimpleScriptContext();
        Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

        engineScope.put(scriptUtilObjectName, this);
    }


    public boolean exec(String rule) throws MojoExecutionException {
        String script = scriptPrefix + rule;

        //System.out.println("Script out : ");
        //System.out.println(script);

        try {
            engine.eval(script, newContext);
        } catch (ScriptException e) {
            e.printStackTrace();
            throw new MojoExecutionException("ScriptException thrown during evaluation");
        }

        Invocable inv = (Invocable) engine;

        // get script object on which we want to call the method
        Object scriptObj = newContext.getAttribute(scriptObjectVar, ScriptContext.ENGINE_SCOPE);

        boolean isPassed;
        try {
            isPassed = (Boolean)inv.invokeMethod(scriptObj, scriptFunctionVar);
        } catch (ScriptException e) {
            e.printStackTrace();
            throw new MojoExecutionException("ScriptException thrown during method invocation");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new MojoExecutionException("NoSuchMethodException thrown during method invocation");
        }

        return isPassed;
    }

    public void setLogString(String logString) {
        this.logString = logString;
    }

    public String getLogString() {
        return logString;
    }

    public boolean isChildPOM() { return (null != parentProject); }

    public void logInfo(String logString) { log.info(logString); }
}
