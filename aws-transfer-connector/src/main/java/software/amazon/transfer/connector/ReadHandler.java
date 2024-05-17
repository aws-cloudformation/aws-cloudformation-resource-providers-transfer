package software.amazon.transfer.connector;

import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeConnectorRequest;
import software.amazon.awssdk.services.transfer.model.DescribeConnectorResponse;
import software.amazon.awssdk.services.transfer.model.DescribedConnector;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.util.CollectionUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReadHandler extends BaseHandlerStd {
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        DescribeConnectorRequest describeConnectorRequest = DescribeConnectorRequest.builder()
                .connectorId(model.getConnectorId())
                .build();

        try (TransferClient client = proxyClient.client()) {
            DescribeConnectorResponse response =
                    proxy.injectCredentialsAndInvokeV2(describeConnectorRequest, client::describeConnector);
            logger.log(String.format(
                    "%s %s described successfully", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
            DescribedConnector describedConnector = response.connector();

            ResourceModel resourceModel = ResourceModel.builder()
                    .arn(describedConnector.arn())
                    .accessRole(describedConnector.accessRole())
                    .as2Config(
                            describedConnector.as2Config() != null
                                    ? Converter.As2ConfigConverter.fromSdk(describedConnector.as2Config())
                                    : null)
                    .sftpConfig(
                            describedConnector.sftpConfig() != null
                                    ? Converter.SftpConfigConverter.fromSdk(describedConnector.sftpConfig())
                                    : null)
                    .connectorId(describedConnector.connectorId())
                    .loggingRole(describedConnector.loggingRole())
                    .serviceManagedEgressIpAddresses(describedConnector.serviceManagedEgressIpAddresses())
                    .tags(
                            (CollectionUtils.isNullOrEmpty(describedConnector.tags()))
                                    ? null
                                    : describedConnector.tags().stream()
                                            .map(Converter.TagConverter::fromSdk)
                                            .collect(Collectors.toSet()))
                    .url(describedConnector.url())
                    .build();

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourceModel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + describeConnectorRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("describeConnector", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
