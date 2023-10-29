package software.amazon.transfer.user;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.ListUsersRequest;
import software.amazon.awssdk.services.transfer.model.ListUsersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.Translator;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        final String clientRequestToken = request.getClientRequestToken();
        final String serverId = model.getServerId();

        return proxy.initiate("AWS-Transfer-User::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(
                        ignored -> translateToListRequest(serverId, request.getNextToken()))
                .makeServiceCall(this::listUsers)
                .handleError(
                        (ignored, exception, proxyClient1, model1, callbackContext1) ->
                                handleError(
                                        LIST,
                                        exception,
                                        model1,
                                        callbackContext1,
                                        clientRequestToken))
                .done(
                        response ->
                                ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .resourceModels(translateFromListResponse(response))
                                        .nextToken(response.nextToken())
                                        .status(OperationStatus.SUCCESS)
                                        .build());
    }

    private ListUsersRequest translateToListRequest(final String serverId, final String nextToken) {
        return ListUsersRequest.builder()
                .maxResults(10)
                .nextToken(nextToken)
                .serverId(serverId)
                .build();
    }

    private List<ResourceModel> translateFromListResponse(final ListUsersResponse response) {
        return Translator.streamOfOrEmpty(response.users())
                .map(
                        user ->
                                ResourceModel.builder()
                                        .arn(user.arn())
                                        .serverId(response.serverId())
                                        .userName(user.userName())
                                        .build())
                .collect(Collectors.toList());
    }

    private ListUsersResponse listUsers(
            ListUsersRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            return client.injectCredentialsAndInvokeV2(awsRequest, transferClient::listUsers);
        }
    }
}
