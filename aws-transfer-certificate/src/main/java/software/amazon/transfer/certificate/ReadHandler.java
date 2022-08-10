package software.amazon.transfer.certificate;

import java.util.stream.Collectors;

import com.amazonaws.util.CollectionUtils;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeCertificateRequest;
import software.amazon.awssdk.services.transfer.model.DescribeCertificateResponse;
import software.amazon.awssdk.services.transfer.model.DescribedCertificate;
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
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@NoArgsConstructor
public class ReadHandler extends BaseHandler<CallbackContext> {
    private TransferClient client;

    public ReadHandler(TransferClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel model = request.getDesiredResourceState();

        DescribeCertificateRequest describeCertificateRequest = DescribeCertificateRequest.builder()
                .certificateId(model.getCertificateId())
                .build();
        try {
            DescribeCertificateResponse response = proxy
                    .injectCredentialsAndInvokeV2(describeCertificateRequest, client::describeCertificate);
            logger.log(String.format("%s %s described successfully",
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
            DescribedCertificate describedCertificate = response.certificate();

            logger.log(String.format("Tags here %s", describedCertificate.tags()));

            ResourceModel resourceModel = ResourceModel.builder()
                    .arn(describedCertificate.arn())
                    .description(describedCertificate.description())
                    .usage(describedCertificate.usageAsString())
                    .status(describedCertificate.statusAsString())
                    .certificate(describedCertificate.certificate())
                    .certificateChain(describedCertificate.certificateChain())
                    .activeDate(describedCertificate.activeDate() != null
                            ? describedCertificate.activeDate().toString()
                            : null)
                    .inactiveDate(describedCertificate.inactiveDate() != null
                            ? describedCertificate.inactiveDate().toString()
                            : null)
                    .type(describedCertificate.typeAsString())
                    .tags((CollectionUtils.isNullOrEmpty(describedCertificate.tags())) ?
                            null : describedCertificate.tags()
                            .stream()
                            .map(Converter.TagConverter::fromSdk)
                            .collect(Collectors.toSet()))
                    .certificateId(model.getCertificateId())
                    .serial(describedCertificate.serial())
                    .notBeforeDate(describedCertificate.notBeforeDate() != null
                            ? describedCertificate.notBeforeDate().toString()
                            : null)
                    .notAfterDate(describedCertificate.notAfterDate() != null
                            ? describedCertificate.notAfterDate().toString()
                            : null)
                    .build();

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourceModel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(describeCertificateRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("describeCertificate", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    model.getPrimaryIdentifier().toString());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
