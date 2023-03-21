package org.eclipse.sample.extension.provision;

import dev.failsafe.RetryPolicy;
import org.eclipse.edc.connector.transfer.spi.provision.ProvisionManager;
import org.eclipse.edc.connector.transfer.spi.provision.ResourceManifestGenerator;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class LocalProvisionExtension implements ServiceExtension {

    public static final String NAME = "Local Resource Provision";
    @Setting
    private static final String PROVISION_MAX_RETRY = "10";
    @Inject
    private Monitor monitor;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();

        var provisionManager = context.getService(ProvisionManager.class);

        var retryPolicy = (RetryPolicy<Object>) context.getService(RetryPolicy.class);

        int maxRetries = context.getSetting(PROVISION_MAX_RETRY, 10);
        var provisionerConfiguration = new LocalResourceProvisionerConfiguration(maxRetries);
        var localResourceProvisioner = new LocalResourceProvisioner(monitor, retryPolicy, provisionerConfiguration);
        provisionManager.register(localResourceProvisioner);

        // register the generator
        var manifestGenerator = context.getService(ResourceManifestGenerator.class);
        manifestGenerator.registerGenerator(new LocalConsumerResourceDefinitionGenerator());
    }

    @Override
    public void shutdown() {

    }
}
