package com.mattworzala.canary.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CanaryGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("hello", task -> {
            task.doLast(t -> {
                System.out.println("Hello from canary plugin");
            });
        });
    }
}
