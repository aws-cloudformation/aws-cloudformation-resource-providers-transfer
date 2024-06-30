package software.amazon.transfer.user.clients;

import java.time.Duration;

import software.amazon.awssdk.awscore.retry.AwsRetryStrategy;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.retries.api.BackoffStrategy;
import software.amazon.awssdk.retries.api.RetryStrategy;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.LambdaWrapper;

/** Create a TransferClient instance. */
public class ClientBuilder {
    private static final BackoffStrategy TRANSFER_BACKOFF_THROTTLING_STRATEGY =
            BackoffStrategy.exponentialDelay(Duration.ofMillis(2000), SdkDefaultRetrySetting.MAX_BACKOFF);

    private static final RetryStrategy TRANSFER_RETRY_STRATEGY = AwsRetryStrategy.adaptiveRetryStrategy().toBuilder()
            .backoffStrategy(TRANSFER_BACKOFF_THROTTLING_STRATEGY)
            .maxAttempts(4)
            .build();

    /**
     * Returns the TransferClient instance.
     *
     * @return the TransferClient instance.
     */
    public static TransferClient getClient() {
        return TransferClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryStrategy(TRANSFER_RETRY_STRATEGY)
                        .build())
                .build();
    }
}
