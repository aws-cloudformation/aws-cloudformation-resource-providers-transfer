package software.amazon.transfer.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateConnectorRequest;
import software.amazon.awssdk.services.transfer.model.CreateConnectorResponse;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.util.CollectionUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateHandler extends BaseHandlerStd {
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        Map<String, String> allTags = new HashMap<>();
        if (request.getDesiredResourceTags() != null) {
            allTags.putAll(request.getDesiredResourceTags());
        }
        if (request.getSystemTags() != null) {
            allTags.putAll(request.getSystemTags());
        }
        model.setTags(Converter.TagConverter.translateTagfromMap(allTags));

        CreateConnectorRequest createConnectorRequest = CreateConnectorRequest.builder()
                .url(model.getUrl())
                .as2Config(
                        model.getAs2Config() != null ? Converter.As2ConfigConverter.toSdk(model.getAs2Config()) : null)
                .sftpConfig(
                        model.getSftpConfig() != null
                                ? Converter.SftpConfigConverter.toSdk(model.getSftpConfig())
                                : null)
                .accessRole(model.getAccessRole())
                .loggingRole(model.getLoggingRole())
                .tags(
                        (CollectionUtils.isNullOrEmpty(model.getTags()))
                                ? null
                                : model.getTags().stream()
                                        .map(Converter.TagConverter::toSdk)
                                        .collect(Collectors.toList()))
                .build();

        try (TransferClient client = proxyClient.client()) {
            CreateConnectorResponse response =
                    proxy.injectCredentialsAndInvokeV2(createConnectorRequest, client::createConnector);
            model.setConnectorId(response.connectorId());
            logger.log(String.format("%s created successfully", ResourceModel.TYPE_NAME));
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + createConnectorRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("createConnector", e);
        } catch (ResourceExistsException e) {
            throw new CfnAlreadyExistsException(
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (ThrottlingException e) {
            throw new CfnThrottlingException("createConnector", e);
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
