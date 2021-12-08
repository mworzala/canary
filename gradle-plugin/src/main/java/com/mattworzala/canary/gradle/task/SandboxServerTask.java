package com.mattworzala.canary.gradle.task;

import com.mattworzala.canary.gradle.CanaryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;

import java.io.File;

public class SandboxServerTask extends JavaExec {
    public SandboxServerTask() {
        dependsOn("testClasses", "classes"); // Do not depend on `build` because it depends on `test` (indirectly)

        setGroup(CanaryPlugin.GROUP);
        setDescription("Start a testing sandbox to create or execute Canary tests.");

        setWorkingDir(getProject().getBuildDir()); // Run in build directory so it is cleaned up with `clean`

        // Get the java plugin (to gather test classpath)
        JavaPluginExtension javaPlugin = getProject().getExtensions().getByType(JavaPluginExtension.class);
        SourceSet testSourceSet = javaPlugin.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        // Set options
        jvmArgs("-ea", "-Dminestom.extension.indevfolder.classes=classes/java/main", "-Dminestom.extension.indevfolder.resources=resources/main");
        args(); //todo
        environment("CANARY_TEST_RESOURCES", new File(getProject().getProjectDir(), "src/test/resources")); //todo this does not handle people with weird source sets.

        classpath(testSourceSet.getRuntimeClasspath().getAsPath());
        getMainClass().set("com.mattworzala.canary.internal.launch.SandboxLauncher");
    }
}
