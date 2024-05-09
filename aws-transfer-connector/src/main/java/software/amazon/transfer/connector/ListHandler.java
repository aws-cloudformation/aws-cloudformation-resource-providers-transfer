package software.amazon.transfer.connector;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListConnectorsRequest;
import software.amazon.awssdk.services.transfer.model.ListConnectorsResponse;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ListHandler extends BaseHandlerStd {
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {

        final List<ResourceModel> models = new ArrayList<>();

        ListConnectorsRequest listConnectorsRequest = ListConnectorsRequest.builder()
                .maxResults(10)
                .nextToken(request.getNextToken())
                .build();

        try (TransferClient client = proxyClient.client()) {
            ListConnectorsResponse response =
                    proxy.injectCredentialsAndInvokeV2(listConnectorsRequest, client::listConnectors);

            response.connectors().forEach(listedConnector -> {
                ResourceModel model = ResourceModel.builder()
                        .arn(listedConnector.arn())
                        .connectorId(listedConnector.connectorId())
                        .url(listedConnector.url())
                        .build();
                models.add(model);
            });

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .nextToken(response.nextToken())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + listConnectorsRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("listConnector", e);
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
