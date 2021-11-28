package com.mattworzala.canary.gradle;

import com.mattworzala.canary.gradle.task.InfoTask;
import com.mattworzala.canary.gradle.task.SandboxServerTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;

public class CanaryPlugin implements Plugin<Project> {
    public static final String GROUP = "canary";
    public static final String VERSION = "$CANARY_GRADLE_VERSION$";

    @Override
    public void apply(Project project) {
        // Immediately apply java plugin since we depend on it
        project.getPlugins().apply(JavaBasePlugin.class);

        // todo when we need options
        CanaryPluginExtension extension = project.getExtensions()
                .create("canary", CanaryPluginExtension.class);

        project.afterEvaluate(p -> {
            // Ensure central repo is present
            project.getRepositories().mavenCentral();

            // Add canary dependency
            project.getDependencies().add("testImplementation", "com.mattworzala:canary:" + extension.getVersion());
        });

        // Register sandbox creation task
        project.getTasks().register("sandbox", SandboxServerTask.class);
        project.getTasks().register("info", InfoTask.class);
    }
}
