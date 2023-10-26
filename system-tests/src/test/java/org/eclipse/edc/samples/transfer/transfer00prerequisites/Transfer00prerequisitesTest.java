/*
 *  Copyright (c) 2023 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial implementation
 *
 */

package org.eclipse.edc.samples.transfer.transfer00prerequisites;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.getProvider;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.registerDataPlaneConsumer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.registerDataPlaneProvider;

@EndToEndTest
public class Transfer00prerequisitesTest {

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    @Test
    void runSampleSteps() {
        registerDataPlaneProvider();
        registerDataPlaneConsumer();
    }
}
