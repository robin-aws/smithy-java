/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.runtime.auth.api.scheme;

import java.util.Optional;
import software.amazon.smithy.java.runtime.auth.api.Signer;
import software.amazon.smithy.java.runtime.auth.api.identity.AwsCredentialsIdentity;
import software.amazon.smithy.java.runtime.auth.api.identity.Identity;
import software.amazon.smithy.java.runtime.auth.api.identity.IdentityResolver;
import software.amazon.smithy.java.runtime.auth.api.identity.IdentityResolvers;
import software.amazon.smithy.java.runtime.auth.api.identity.TokenIdentity;

/**
 * An authentication scheme, composed of:
 *
 * <ol>
 *     <li>A scheme ID - A unique identifier for the authentication scheme.</li>
 *     <li>An identity provider - An API that can be queried to acquire the customer's identity.</li>
 *     <li>A signer - An API that can be used to sign HTTP requests.</li>
 * </ol>
 *
 * See example auth schemes defined <a href="https://smithy.io/2.0/spec/authentication-traits.html">here</a>.
 *
 * @param <IdentityT> The {@link Identity} used by this authentication scheme.
 * @param <RequestT>  The request to sign.
 */
public interface AuthScheme<RequestT, IdentityT extends Identity>  {
    /**
     * Retrieve the authentication scheme ID, a unique identifier for the authentication scheme (e.g., aws.auth#sigv4).
     */
    String schemeId();

    /**
     * Get the request type that this auth scheme can sign.
     *
     * @return the request type that can be signed.
     */
    Class<RequestT> requestType();

    /**
     * Get the identity class this auth scheme can sign.
     *
     * @return the identity type that can be signed.
     */
    Class<IdentityT> identityType();

    /**
     * Retrieve the identity provider associated with this authentication scheme. The identity generated by this
     * provider is guaranteed to be supported by the signer in this authentication scheme.
     *
     * <p>For example, if the scheme ID is aws.auth#sigv4, the provider returns an {@link AwsCredentialsIdentity}, if
     * the scheme ID is httpBearerAuth, the provider returns a {@link TokenIdentity}.
     *
     * <p>Note, the returned identity provider may differ from the type of identity provider retrieved from the
     * provided {@link IdentityResolvers}.
     *
     * @param resolvers Resolver repository.
     * @return the optionally located identity resolver.
     */
    Optional<IdentityResolver<IdentityT>> identityResolver(IdentityResolvers resolvers);

    /**
     * Retrieve the signer associated with this authentication scheme.
     *
     * <p>This signer is guaranteed to support the identity generated by the identity provider in this authentication
     * scheme.
     *
     * @return the signer.
     */
    Signer<RequestT, IdentityT> signer();
}
