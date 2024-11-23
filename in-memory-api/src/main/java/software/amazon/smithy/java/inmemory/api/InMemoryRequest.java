/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.inmemory.api;

import software.amazon.smithy.java.io.datastream.DataStream;

/**
 */
public interface InMemoryRequest {
   DataStream body();
}
