package software.amazon.transfer.server.clients;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.LambdaWrapper;

/** Create a TransferClient instance. */
public class ClientBuilder {
    /**
     * Returns the TransferClient instance.
     *
     * @return the TransferClient instance.
     */
    public static TransferClient getClient() {
        return TransferClient.builder().httpClient(LambdaWrapper.HTTP_CLIENT).build();
    }
}
