package org.eclipse.sample.extension.provision;

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

    public String getPathName() {
        return getDataAddress().getProperty(PATHNAME);
    }

    private LocalProvisionedResource() {
    }


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
