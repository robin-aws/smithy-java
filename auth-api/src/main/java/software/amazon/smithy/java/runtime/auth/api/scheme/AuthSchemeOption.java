/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.runtime.auth.api.scheme;

import software.amazon.smithy.java.runtime.auth.api.AuthProperties;

/**
 * An authentication scheme option, composed of the scheme ID and properties for use when resolving the identity and
 * signing the request.
 *
 * <p>This is used in the output from the auth scheme resolver. The resolver returns a list of these, in the order the
 * auth scheme resolver wishes to use them.
 *
 * @param schemeId The authentication scheme ID, a unique identifier for the authentication scheme
 *                 (aws.auth#sigv4, smithy.api#httpBearerAuth).
 * @param identityProperties The resolved identity properties.
 * @param signerProperties The resolved signer properties.
 *
 * @see AuthScheme
 */
public record AuthSchemeOption(String schemeId, AuthProperties identityProperties, AuthProperties signerProperties) {
}
