/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension.provision;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.edc.connector.transfer.spi.types.ProvisionedDataDestinationResource;


@JsonDeserialize(builder = LocalProvisionedResource.Builder.class)
@JsonTypeName("dataspaceconnector:datarequest")
public class LocalProvisionedResource extends ProvisionedDataDestinationResource {
    private static final String PATHNAME = "path";
    private static final String TYPE = "File";

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends ProvisionedDataDestinationResource.Builder<LocalProvisionedResource, Builder> {
        private Builder() {
            super(new LocalProvisionedResource());
            dataAddressBuilder.type(TYPE);
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder pathName(String pathName) {
            dataAddressBuilder.property(PATHNAME, pathName);
            return this;
        }
    }
}
