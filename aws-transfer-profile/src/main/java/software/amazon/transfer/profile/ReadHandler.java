package software.amazon.transfer.profile;

import com.amazonaws.util.CollectionUtils;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeProfileRequest;
import software.amazon.awssdk.services.transfer.model.DescribeProfileResponse;
import software.amazon.awssdk.services.transfer.model.DescribedProfile;
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

import java.util.stream.Collectors;

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

        if (this.client == null){
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel model = request.getDesiredResourceState();
        DescribeProfileRequest describeProfileRequest = DescribeProfileRequest.builder()
                .profileId(model.getProfileId())
                .build();

        try {
            DescribeProfileResponse describeProfileResponse = proxy
                    .injectCredentialsAndInvokeV2(describeProfileRequest, client::describeProfile);
            logger.log(String.format("%s %s described successfully",
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
            DescribedProfile describedProfile = describeProfileResponse.profile();

            ResourceModel resourceModel = ResourceModel.builder()
                    .profileId(describedProfile.profileId())
                    .as2Id(describedProfile.as2Id())
                    .certificateIds(describedProfile.certificateIds())
                    .arn(describedProfile.arn())
                    .tags((CollectionUtils.isNullOrEmpty(describedProfile.tags())) ?
                            null : describedProfile.tags()
                            .stream()
                            .map(Converter.TagConverter::fromSdk)
                            .collect(Collectors.toSet()))
                    .profileType(describedProfile.profileTypeAsString())
                    .build();

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(resourceModel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(describeProfileRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("describeProfile", e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    model.getPrimaryIdentifier().toString());
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }


    }
}
