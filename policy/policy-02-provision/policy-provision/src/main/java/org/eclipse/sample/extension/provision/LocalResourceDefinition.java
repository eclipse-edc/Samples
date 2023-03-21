package org.eclipse.sample.extension.provision;

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
