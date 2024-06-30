package software.amazon.transfer.server.clients;

import java.time.Duration;

import software.amazon.awssdk.awscore.retry.AwsRetryStrategy;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.retries.api.BackoffStrategy;
import software.amazon.awssdk.retries.api.RetryStrategy;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.cloudformation.LambdaWrapper;

/** Create an Ec2Client instance. */
public class Ec2ClientBuilder {
    private static final BackoffStrategy EC2_BACKOFF_THROTTLING_STRATEGY =
            BackoffStrategy.exponentialDelay(Duration.ofMillis(2000), SdkDefaultRetrySetting.MAX_BACKOFF);

    private static final RetryStrategy EC2_RETRY_STRATEGY = AwsRetryStrategy.adaptiveRetryStrategy().toBuilder()
            .backoffStrategy(EC2_BACKOFF_THROTTLING_STRATEGY)
            .maxAttempts(4)
            .build();

    /**
     * Returns the Ec2Client instance.
     *
     * @return the Ec2Client instance.
     */
    public static Ec2Client getClient() {
        return Ec2Client.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryStrategy(EC2_RETRY_STRATEGY)
                        .build())
                .build();
    }
}
