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

import org.eclipse.edc.connector.transfer.spi.types.ResourceDefinition;

import java.util.Objects;

public class LocalResourceDefinition extends ResourceDefinition {
    private String pathName;

    private LocalResourceDefinition() {
    }

    public String getPathName() {
        return pathName;
    }

    private void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void updatePathName(String pathName) {
        setPathName(pathName);
    }

    @Override
    public Builder toBuilder() {
        return initializeBuilder(new Builder())
                .pathName(pathName);
    }

    public static class Builder extends ResourceDefinition.Builder<LocalResourceDefinition, Builder> {
        private Builder() {
            super(new LocalResourceDefinition());
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder pathName(String pathName) {
            resourceDefinition.pathName = pathName;
            return this;
        }

        @Override
        protected void verify() {
            super.verify();
            Objects.requireNonNull(resourceDefinition.pathName, "pathName");
        }
    }
}
