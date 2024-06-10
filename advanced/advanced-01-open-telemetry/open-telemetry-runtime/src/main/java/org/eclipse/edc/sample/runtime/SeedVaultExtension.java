/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.sample.runtime;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class SeedVaultExtension implements ServiceExtension {

    @Inject
    private Vault vault;

    private static final String PUBLIC_KEY = """
            -----BEGIN CERTIFICATE-----
            MIIDazCCAlOgAwIBAgIUZ3/sZXYzW4PjmOXKrZn6WBmUJ+4wDQYJKoZIhvcNAQEL
            BQAwRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoM
            GEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAeFw0yMjAyMjMxNTA2MDNaFw0zMjAy
            MjExNTA2MDNaMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEw
            HwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwggEiMA0GCSqGSIb3DQEB
            AQUAA4IBDwAwggEKAoIBAQDBl6XaJnXTL+6DWip3aBhU+MzmY4d1V9hbTm1tiZ3g
            E0VbUrvGO3LoYaxpPv6zFmsg3uJv6JxVAde7EddidN0ITHB9cQNdAfdUJ5njmsGS
            PbdQuOQTHw0aG7/QvTI/nsvfEE6e0lbV/0e7DHacZT/+OztBH1RwkG2ymM94Hf8H
            I6x7q6yfRTAZOqeOMrPCYTcluAgE9NskoPvjX5qASakBtXISKIsOU84N0/2HDN3W
            EGMXvoHUQu6vrij6BwiwxKaw1AKwWENKoga775bPXN3M+JTSaIKE7dZbKzvx0Zi0
            h5X+bxc3BJi3Z/CsUBCzE+Y0SFetOiYmyl/2YmnneYoVAgMBAAGjUzBRMB0GA1Ud
            DgQWBBTvK1wVERwjni4B2vdH7KtEJeVWFzAfBgNVHSMEGDAWgBTvK1wVERwjni4B
            2vdH7KtEJeVWFzAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBn
            QHiPA7OBYukHd9gS7c0HXE+fsWcS3GZeLqcHfQQnV3pte1vTmu9//IVW71wNCJ1/
            rySRyODPQoPehxEcyHwupNZSzXK//nPlTdSgjMfFxscvt1YndyQLQYCfyOJMixAe
            Aqrb14GTFHUUrdor0PyElhkULjkOXUrSIsdBrfWrwLTkelE8NK3tb5ZG8KPzD9Jy
            +NwEPPr9d+iHkUkM7EFWw/cl56wka9ryBb97RI7DqbO6/j6OXHMk4GByxKv7DSIR
            IvF9/Dw20qytajtaHV0pluFcOBuFc0NfiDvCaQlbTsfjzbc6UmZWbOi9YOJl3VQ/
            g3h+15GuzbsSzOCOEYOT
            -----END CERTIFICATE-----
            """;

    private static final String PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDBl6XaJnXTL+6D
            Wip3aBhU+MzmY4d1V9hbTm1tiZ3gE0VbUrvGO3LoYaxpPv6zFmsg3uJv6JxVAde7
            EddidN0ITHB9cQNdAfdUJ5njmsGSPbdQuOQTHw0aG7/QvTI/nsvfEE6e0lbV/0e7
            DHacZT/+OztBH1RwkG2ymM94Hf8HI6x7q6yfRTAZOqeOMrPCYTcluAgE9NskoPvj
            X5qASakBtXISKIsOU84N0/2HDN3WEGMXvoHUQu6vrij6BwiwxKaw1AKwWENKoga7
            75bPXN3M+JTSaIKE7dZbKzvx0Zi0h5X+bxc3BJi3Z/CsUBCzE+Y0SFetOiYmyl/2
            YmnneYoVAgMBAAECggEBAJHXiN6bctAyn+DcoHlsNkhtVw+Jk5bXIutGXjHTJtiU
            K//siAGC78IZMyXmi0KndPVCdBwShROVW8xWWIiXuZxy2Zvm872xqX4Ah3JsN7/Q
            NrXdVBUDo38zwIGkxqIfIz9crZ4An+J/eq5zaTfRHzCLtswMqjRS2hFeBY5cKrBY
            4bkSDGTP/c5cP7xS/UwaiTR2Ptd41f4zTyd4l5rl30TYHpazQNlbdxcOV4jh2Rnp
            E0+cFEvEfeagVq7RmfBScKG5pk4qcRG0q2QHMyK5y00hdYvhdRjSgN7xIDkeO5B8
            s8/tSLU78nCl2gA9IKxTXYLitpISwZ81Q04mEAKRRtECgYEA+6lKnhn//aXerkLo
            ZOLOjWQZhh005jHdNxX7DZqLpTrrfxc8v15KWUkAK1H0QHqYvfPrbbsBV1MY1xXt
            sKmkeu/k8fJQzCIvFN4K2J5W5kMfq9PSw5d3XPeDaQuXUVaxBVp0gzPEPHmkKRbA
            AkUqY0oJwA9gMKf8dK+flmLZfbsCgYEAxO4Roj2G46/Oox1GEZGxdLpiMpr9rEdR
            JlSZ9kMGfddNLV7sFp6yPXDcyc/AOqeNj7tw1MyoT3Ar454+V0q83EZzCXvs4U6f
            jUrfFcoVWIwf9AV/J4KWzMIzfqPIeNwqymZKd6BrZgcXXvAEPWt27mwO4a1GhC4G
            oZv0t3lAsm8CgYAQ8C0IhSF4tgBN5Ez19VoHpDQflbmowLRt77nNCZjajyOokyzQ
            iI0ig0pSoBp7eITtTAyNfyew8/PZDi3IVTKv35OeQTv08VwP4H4EZGve5aetDf3C
            kmBDTpl2qYQOwnH5tUPgTMypcVp+NXzI6lTXB/WuCprjy3qvc96e5ZpT3wKBgQC8
            Xny/k9rTL/eYTwgXBiWYYjBL97VudUlKQOKEjNhIxwkrvQBXIrWbz7lh0Tcu49al
            BcaHxru4QLO6pkM7fGHq0fh3ufJ8EZjMrjF1xjdk26Q05o0aXe+hLKHVIRVBhlfo
            ArB4fRo+HcpdJXjox0KcDQCvHe+1v9DYBTWvymv4QQKBgBy3YH7hKz35DcXvA2r4
            Kis9a4ycuZqTXockO4rkcIwC6CJp9JbHDIRzig8HYOaRqmZ4a+coqLmddXr2uOF1
            7+iAxxG1KzdT6uFNd+e/j2cdUjnqcSmz49PRtdDswgyYhoDT+W4yVGNQ4VuKg6a3
            Z3pC+KTdoHSKeA2FyAGnSUpD
            -----END PRIVATE KEY-----
            """;

    @Override
    public void initialize(ServiceExtensionContext context) {
        vault.storeSecret("public-key", PUBLIC_KEY);
        vault.storeSecret("private-key", PRIVATE_KEY);
    }
}
