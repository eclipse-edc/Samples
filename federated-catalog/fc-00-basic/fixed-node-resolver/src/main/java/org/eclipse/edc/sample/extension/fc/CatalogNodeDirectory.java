/*
 *  Copyright (c) 2024 Fraunhofer-Gesellschaft
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer-Gesellschaft - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension.fc;

import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;

import java.util.ArrayList;
import java.util.List;

public class CatalogNodeDirectory implements TargetNodeDirectory {

    @Override
    public List<TargetNode> getAll() {
        List<String> protocolList = new ArrayList<>();
        protocolList.add("dataspace-protocol-http");

        TargetNode participantNode = new TargetNode("https://w3id.org/edc/v0.0.1/ns/",
                "provider",
                "http://localhost:19194/protocol", protocolList);

        return List.of(participantNode);
    }

    @Override
    public void insert(TargetNode targetNode) {

    }
}
