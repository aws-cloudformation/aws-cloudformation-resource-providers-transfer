package software.amazon.transfer.server;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.ListServersRequest;
import software.amazon.awssdk.services.transfer.model.ListServersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.Translator;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final ProxyClient<Ec2Client> proxyEc2Client,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        final ResourceModel resourceModel = request.getDesiredResourceState();

        return proxy.initiate(
                        "AWS-Transfer-Server::List", proxyClient, resourceModel, callbackContext)
                .translateToServiceRequest(m -> translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listServers)
                .handleError(
                        (ignored, exception, client, model, context) ->
                                handleError(LIST, exception, model, context, clientRequestToken))
                .done(
                        response ->
                                ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .resourceModels(translateFromListResponce(response))
                                        .nextToken(response.nextToken())
                                        .status(OperationStatus.SUCCESS)
                                        .build());
    }

    private ListServersRequest translateToListRequest(final String nextToken) {
        return ListServersRequest.builder().maxResults(10).nextToken(nextToken).build();
    }

    private ListServersResponse listServers(
            ListServersRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            return client.injectCredentialsAndInvokeV2(awsRequest, transferClient::listServers);
        }
    }

    private List<ResourceModel> translateFromListResponce(final ListServersResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.servers())
                .map(
                        resource ->
                                ResourceModel.builder()
                                        .arn(resource.arn())
                                        .serverId(resource.serverId())
                                        .build())
                .collect(Collectors.toList());
    }
}
