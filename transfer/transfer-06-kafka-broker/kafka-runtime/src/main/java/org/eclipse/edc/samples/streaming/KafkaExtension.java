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

import org.eclipse.edc.connector.controlplane.transfer.spi.flow.DataFlowManager;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

/**
 * Kafka Broker flow extension
 */
public class KafkaExtension implements ServiceExtension {

    @Override
    public String name() {
        return "Kafka stream extension";
    }

    @Inject
    private DataFlowManager dataFlowManager;

    @Override
    public void initialize(ServiceExtensionContext context) {
        dataFlowManager.register(10, new KafkaToKafkaDataFlowController());
    }

}
