/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial test implementation for sample
 *       Mercedes-Benz Tech Innovation GmbH - refactor test cases
 *
 */

package org.eclipse.edc.samples.common;

import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Encapsulates common settings, test steps, and helper methods for transfer samples
 */
public class FileTransferCommon {

    /**
     * Resolves a {@link File} instance from a relative path.
     */
    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(TestUtils.findBuildRoot(), relativePath);
    }

    /**
     * Resolves a {@link File} instance from a relative path.
     */
    @NotNull
    public static String getFileContentFromRelativePath(String relativePath) {
        var fileFromRelativePath = getFileFromRelativePath(relativePath);
        try {
            return Files.readString(Paths.get(fileFromRelativePath.toURI()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
