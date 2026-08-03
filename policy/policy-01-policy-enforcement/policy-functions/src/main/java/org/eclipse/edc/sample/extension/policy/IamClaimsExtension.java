/*
 *  Copyright (c) 2026 Think-it GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Think-it GmbH - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension.policy;

import org.eclipse.edc.participantcontext.spi.config.ParticipantContextConfig;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.iam.AudienceResolver;
import org.eclipse.edc.spi.iam.ClaimToken;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.iam.VerificationContext;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.types.TypeManager;

import java.util.Map;

public class IamClaimsExtension implements ServiceExtension {

    @Inject
    private ParticipantContextConfig contextConfig;
    @Inject
    private TypeManager typeManager;
    @Inject
    private AudienceResolver audienceResolver; // this is needed in order to permit IdentityService override

    @Provider
    public IdentityService identityService() {
        return new MockClaimsIdentityService(contextConfig, typeManager);
    }

    private static class MockClaimsIdentityService implements IdentityService {
        private final ParticipantContextConfig contextConfig;
        private final TypeManager typeManager;

        MockClaimsIdentityService(ParticipantContextConfig contextConfig, TypeManager typeManager) {
            this.contextConfig = contextConfig;
            this.typeManager = typeManager;
        }

        @Override
        public Result<TokenRepresentation> obtainClientCredentials(String participantContextId, TokenParameters parameters) {

            var clientId = contextConfig.getString(participantContextId, "edc.participant.id");
            var token = Map.of(
                    "client_id", clientId,
                    "region", contextConfig.getString(participantContextId, "edc.mock.region", "")
            );
            var tokenRepresentation = TokenRepresentation.Builder.newInstance()
                    .token(typeManager.writeValueAsString(token))
                    .build();
            return Result.success(tokenRepresentation);
        }

        @Override
        public Result<ClaimToken> verifyJwtToken(String participantContextId, TokenRepresentation tokenRepresentation, VerificationContext context) {
            Map<String, Object> token = typeManager.readValue(tokenRepresentation.getToken(), Map.class);

            return Result.success(ClaimToken.Builder.newInstance()
                    .claim("client_id", token.get("client_id"))
                    .claim("region", token.get("region"))
                    .build());
        }

    }
}
