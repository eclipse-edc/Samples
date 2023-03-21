package org.eclipse.sample.extension.provision;

import org.eclipse.edc.connector.transfer.spi.provision.ConsumerResourceDefinitionGenerator;
import org.eclipse.edc.connector.transfer.spi.types.DataRequest;
import org.eclipse.edc.connector.transfer.spi.types.ResourceDefinition;
import org.eclipse.edc.policy.model.Policy;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.UUID.randomUUID;

public class LocalConsumerResourceDefinitionGenerator implements ConsumerResourceDefinitionGenerator {

    private static final String TYPE = "File";
    private static final String DESTINATION = "any path"; // this will get modified during the policy evaluation
                                                    // to notice the change, keep the path different from
                                                    // the path used in policy

    @Override
    public @Nullable ResourceDefinition generate(DataRequest dataRequest, Policy policy) {
        Objects.requireNonNull(dataRequest, "dataRequest must always be provided");
        Objects.requireNonNull(policy, "policy must always be provided");

        var id = randomUUID().toString();

        return LocalResourceDefinition.Builder.newInstance()
                .id(id)
                .pathName(DESTINATION)
                .build();
    }

    @Override
    public boolean canGenerate(DataRequest dataRequest, Policy policy) {
        Objects.requireNonNull(dataRequest, "dataRequest must always be provided");
        Objects.requireNonNull(policy, "policy must always be provided");

        return TYPE.equals(dataRequest.getDestinationType());
    }


}
