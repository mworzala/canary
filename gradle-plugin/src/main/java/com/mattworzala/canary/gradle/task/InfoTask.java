package com.mattworzala.canary.gradle.task;

import com.mattworzala.canary.gradle.CanaryPlugin;
import com.mattworzala.canary.gradle.CanaryPluginExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.util.Collection;
import java.util.List;

public class InfoTask extends DefaultTask {
    public InfoTask() {
        setGroup(CanaryPlugin.GROUP);
        setDescription("Print some debug info about the current environment");

        doLast(t -> {
            CanaryPluginExtension extension = getProject().getExtensions().getByType(CanaryPluginExtension.class);

            System.out.println("Canary debug info:");
            System.out.println("==================");

            // Canary Version
            System.out.println("Version\t\t" + extension.getVersion());

            // Minestom Version
            List<String> minestomVersions = resolveMinestomVersions();
            switch (minestomVersions.size()) {
                case 0 -> System.out.println("WARNING: No Minestom version detected on classpath!");
                case 1 -> System.out.println("Minestom\t" + minestomVersions.get(0));
                default -> {
                    System.out.println("WARNING: Multiple Minestom versions detected on classpath.");
                    System.out.println("Minestom\t" + String.join(", ", minestomVersions));
                }
            }

            //todo Minestom UI integration?
        });
    }

    private List<String> resolveMinestomVersions() {
        return getProject().getConfigurations().stream()
                .map(Configuration::getAllDependencies)
                .flatMap(Collection::stream)
                .filter(dependency -> "com.github.Minestom".equals(dependency.getGroup()) && "Minestom".equals(dependency.getName()))
                .map(Dependency::getVersion)
                .distinct()
                .toList();
    }
}
