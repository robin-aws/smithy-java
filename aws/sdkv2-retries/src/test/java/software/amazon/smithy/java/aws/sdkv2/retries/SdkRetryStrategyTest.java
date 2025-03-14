/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.aws.sdkv2.retries;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.retries.DefaultRetryStrategy;
import software.amazon.awssdk.retries.api.RetryStrategy;
import software.amazon.smithy.java.retries.api.AcquireInitialTokenRequest;
import software.amazon.smithy.java.retries.api.RecordSuccessRequest;
import software.amazon.smithy.java.retries.api.RefreshRetryTokenRequest;
import software.amazon.smithy.java.retries.api.TokenAcquisitionFailedException;

public class SdkRetryStrategyTest {
    @Test
    public void bridgesAwsSdk() {
        RetryStrategy sdk = DefaultRetryStrategy.doNotRetry();
        var adapted = SdkRetryStrategy.of(sdk);

        assertThat(adapted.maxAttempts(), equalTo(sdk.maxAttempts()));
    }

    @Test
    public void acquiresToken() {
        var adapted = SdkRetryStrategy.of(DefaultRetryStrategy.doNotRetry());
        var attempt = new AcquireInitialTokenRequest("foo");
        var result = adapted.acquireInitialToken(attempt);

        assertThat(result.delay(), equalTo(Duration.ZERO));
        assertThat(result.token(), instanceOf(SdkRetryToken.class));
    }

    @Test
    public void refreshesToken() {
        var adapted = SdkRetryStrategy.of(DefaultRetryStrategy.doNotRetry());
        var acquire = adapted.acquireInitialToken(new AcquireInitialTokenRequest("foo"));
        var refresh = new RefreshRetryTokenRequest(acquire.token(), new RuntimeException("hi"), null);

        // Throws when the exception isn't retryable.
        Assertions.assertThrows(TokenAcquisitionFailedException.class, () -> {
            adapted.refreshRetryToken(refresh);
        });
    }

    @Test
    public void returnsTokens() {
        var adapted = SdkRetryStrategy.of(DefaultRetryStrategy.doNotRetry());
        var acquire = adapted.acquireInitialToken(new AcquireInitialTokenRequest("foo"));

        var result = adapted.recordSuccess(new RecordSuccessRequest(acquire.token()));

        assertThat(result.token(), instanceOf(SdkRetryToken.class));
    }
}
