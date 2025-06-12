/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.samples.streaming;

import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.transfer.spi.flow.DataFlowController;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.DataFlowResponse;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcess;
import org.eclipse.edc.dataaddress.kafka.spi.KafkaDataAddressSchema;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static org.eclipse.edc.dataaddress.kafka.spi.KafkaDataAddressSchema.KAFKA_TYPE;

class KafkaToKafkaDataFlowController implements DataFlowController {

    @Override
    public boolean canHandle(TransferProcess transferProcess) {
        return KAFKA_TYPE.equals(transferProcess.getContentDataAddress().getType()) && "KafkaBroker-PULL".equals(transferProcess.getTransferType());
    }

    @Override
    public StatusResult<DataFlowResponse> provision(TransferProcess transferProcess, Policy policy) {
        // here the flow can be provisioned, not something covered in this sample
        return StatusResult.success(null);
    }

    @Override
    public @NotNull StatusResult<DataFlowResponse> start(TransferProcess transferProcess, Policy policy) {
        // static credentials, in a production case these should be created dynamically and an ACLs entry should be added
        var username = "alice";
        var password = "alice-secret";

        var contentDataAddress = transferProcess.getContentDataAddress();
        var kafkaDataAddress = DataAddress.Builder.newInstance()
                .type(EndpointDataReference.EDR_SIMPLE_TYPE)
                .property(EndpointDataReference.ID, transferProcess.getCorrelationId())
                .property(EndpointDataReference.ENDPOINT, contentDataAddress.getStringProperty("kafka.bootstrap.servers"))
                .property(EndpointDataReference.AUTH_KEY, username)
                .property(EndpointDataReference.AUTH_CODE, password)
                .property(EndpointDataReference.CONTRACT_ID, transferProcess.getContractId())
                .property(KafkaDataAddressSchema.TOPIC, contentDataAddress.getStringProperty(KafkaDataAddressSchema.TOPIC))
                .build();

        return StatusResult.success(DataFlowResponse.Builder.newInstance().dataAddress(kafkaDataAddress).build());
    }

    @Override
    public StatusResult<Void> suspend(TransferProcess transferProcess) {
        // here the flow can be suspended, not something covered in this sample
        return StatusResult.success();
    }

    @Override
    public StatusResult<Void> terminate(TransferProcess transferProcess) {
        // here the flow can be terminated, not something covered in this sample
        return StatusResult.success();
    }

    @Override
    public Set<String> transferTypesFor(Asset asset) {
        return Set.of("Kafka-PULL");
    }

}
