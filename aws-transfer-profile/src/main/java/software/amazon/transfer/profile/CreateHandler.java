package software.amazon.transfer.profile;

import com.amazonaws.util.CollectionUtils;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateProfileRequest;
import software.amazon.awssdk.services.transfer.model.CreateProfileResponse;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.stream.Collectors;

@NoArgsConstructor
public class CreateHandler extends BaseHandler<CallbackContext> {
    private TransferClient client;

    public CreateHandler(TransferClient client) {
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
        model.setTags(Converter.TagConverter.translateTagfromMap(request.getDesiredResourceTags()));

        CreateProfileRequest createProfileRequest = CreateProfileRequest.builder()
                .as2Id(model.getAs2Id())
                .certificateIds(model.getCertificateIds())
                .tags((CollectionUtils.isNullOrEmpty(model.getTags())) ?
                        null : model.getTags()
                        .stream()
                        .map(Converter.TagConverter::toSdk)
                        .collect(Collectors.toList()))
                .profileType(model.getProfileType())
                .build();

        try {
            CreateProfileResponse response =
                    proxy.injectCredentialsAndInvokeV2(createProfileRequest, client::createProfile);
            model.setProfileId(response.profileId());
            logger.log(String.format("%s created successfully", ResourceModel.TYPE_NAME));
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(createProfileRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("createProfile", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    model.getProfileType());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
