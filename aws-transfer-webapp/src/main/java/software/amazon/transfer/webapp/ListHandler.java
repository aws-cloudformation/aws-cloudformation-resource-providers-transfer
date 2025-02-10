package software.amazon.transfer.webapp;

import java.util.List;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.ListWebAppsRequest;
import software.amazon.awssdk.services.transfer.model.ListWebAppsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.Translator;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        final ResourceModel resourceModel = request.getDesiredResourceState();

        return proxy.initiate("AWS-Transfer-Web-App::List", proxyClient, resourceModel, callbackContext)
                .translateToServiceRequest(m -> translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listWebApps)
                .handleError((ignored, exception, client, model, context) ->
                        handleError(LIST, exception, model, context, clientRequestToken))
                .done(response -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModels(translateFromListResponce(response))
                        .nextToken(response.nextToken())
                        .status(OperationStatus.SUCCESS)
                        .build());
    }

    private ListWebAppsRequest translateToListRequest(final String nextToken) {
        return ListWebAppsRequest.builder().maxResults(10).nextToken(nextToken).build();
    }

    private ListWebAppsResponse listWebApps(ListWebAppsRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            return client.injectCredentialsAndInvokeV2(awsRequest, transferClient::listWebApps);
        }
    }

    private List<ResourceModel> translateFromListResponce(final ListWebAppsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.webApps())
                .map(resource -> ResourceModel.builder()
                        .arn(resource.arn())
                        .webAppId(resource.webAppId())
                        .build())
                .toList();
    }
}
