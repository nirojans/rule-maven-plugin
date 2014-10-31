package com.wso2.build.interfaces;

import com.wso2.build.beans.Rule;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.Serializable;
import java.util.List;


public interface RuleRegistry extends Serializable {
    List<Rule> getRules() throws MojoExecutionException;
}
