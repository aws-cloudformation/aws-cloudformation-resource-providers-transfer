package software.amazon.transfer.user.clients;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.LambdaWrapper;

public final class ClientBuilder {
    private ClientBuilder() {}

    public static TransferClient getClient() {
        return TransferClient.builder().httpClient(LambdaWrapper.HTTP_CLIENT).build();
    }
}
