package org.eclipse.sample.extension.provision;

public class LocalResourceProvisionerConfiguration {
    private final int maxRetries;


    public LocalResourceProvisionerConfiguration(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

}
