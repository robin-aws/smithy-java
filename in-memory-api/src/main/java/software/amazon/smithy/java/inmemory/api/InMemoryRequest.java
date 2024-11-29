/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.inmemory.api;

import software.amazon.smithy.java.server.core.Request;

/**
 */
public interface InMemoryRequest {
   public Request request();
}
