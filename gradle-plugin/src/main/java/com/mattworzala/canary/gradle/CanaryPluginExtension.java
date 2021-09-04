package com.mattworzala.canary.gradle;

public class CanaryPluginExtension {
    private String version = "$CANARY_VERSION$";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
