package software.amazon.transfer.agreement;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DeleteAgreementRequest;
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
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeleteHandler extends BaseHandler<CallbackContext> {
    private TransferClient client;

    public DeleteHandler(TransferClient client) {
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

        DeleteAgreementRequest deleteAgreementRequest = DeleteAgreementRequest.builder()
                .agreementId(model.getAgreementId())
                .serverId(model.getServerId())
                .build();

        try {
            proxy.injectCredentialsAndInvokeV2(deleteAgreementRequest, client::deleteAgreement);
            logger.log(
                    String.format("%s %s deleted successfully", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + deleteAgreementRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("deleteAgreement", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
