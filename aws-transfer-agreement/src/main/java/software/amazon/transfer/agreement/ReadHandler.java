package software.amazon.transfer.agreement;

import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeAgreementRequest;
import software.amazon.awssdk.services.transfer.model.DescribeAgreementResponse;
import software.amazon.awssdk.services.transfer.model.DescribedAgreement;
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
        DescribeAgreementRequest describeAgreementRequest = DescribeAgreementRequest.builder()
                .agreementId(model.getAgreementId())
                .serverId(model.getServerId())
                .build();
        try (TransferClient client = proxyClient.client()) {
            DescribeAgreementResponse response =
                    proxy.injectCredentialsAndInvokeV2(describeAgreementRequest, client::describeAgreement);
            logger.log(String.format(
                    "%s %s described successfully", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
            DescribedAgreement describedAgreement = response.agreement();

            ResourceModel resourceModel = ResourceModel.builder()
                    .arn(describedAgreement.arn())
                    .description(describedAgreement.description())
                    .accessRole(describedAgreement.accessRole())
                    .baseDirectory(describedAgreement.baseDirectory())
                    .description(describedAgreement.description())
                    .localProfileId(describedAgreement.localProfileId())
                    .partnerProfileId(describedAgreement.partnerProfileId())
                    .serverId(describedAgreement.serverId())
                    .status(describedAgreement.status().name())
                    .tags(
                            (CollectionUtils.isNullOrEmpty(describedAgreement.tags()))
                                    ? null
                                    : describedAgreement.tags().stream()
                                            .map(Converter.TagConverter::fromSdk)
                                            .collect(Collectors.toSet()))
                    .agreementId(model.getAgreementId())
                    .build();

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourceModel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + describeAgreementRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("describeAgreement", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
