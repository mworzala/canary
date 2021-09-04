package com.mattworzala.canary.gradle.task;

import com.mattworzala.canary.gradle.CanaryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;

public class SandboxServerTask extends JavaExec { //todo switch to javaexec
    public SandboxServerTask() {
        dependsOn("testClasses", "classes"); // Do not depend on `build` because it depends on `test` (indirectly)

        setGroup(CanaryPlugin.GROUP);
        setDescription("Start a testing sandbox to create or execute Canary tests.");

        setWorkingDir(getProject().getBuildDir()); // Run in build directory so it is cleaned up with `clean`

        // Get the java plugin (to gather test classpath)
        JavaPluginExtension javaPlugin = getProject().getExtensions().getByType(JavaPluginExtension.class);
        SourceSet testSourceSet = javaPlugin.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        jvmArgs(); //todo
        args(); //todo

        classpath(testSourceSet.getRuntimeClasspath().getAsPath());
        getMainClass().set("com.mattworzala.canary.platform.launcher.SandboxLauncher");
    }
}
