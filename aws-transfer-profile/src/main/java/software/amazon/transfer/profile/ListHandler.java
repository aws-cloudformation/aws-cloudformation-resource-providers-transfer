package software.amazon.transfer.profile;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListProfilesRequest;
import software.amazon.awssdk.services.transfer.model.ListProfilesResponse;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ListHandler extends BaseHandler<CallbackContext> {

    private TransferClient client;

    public ListHandler(TransferClient client) {
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

        final List<ResourceModel> models = new ArrayList<>();
        ListProfilesRequest listProfilesRequest = ListProfilesRequest.builder()
                .maxResults(10)
                .nextToken(request.getNextToken())
                .profileType(request.getDesiredResourceState().getProfileType())
                .build();

        try {
            ListProfilesResponse response =
                    proxy.injectCredentialsAndInvokeV2(listProfilesRequest, client::listProfiles);

            response.profiles().forEach(listedProfile -> {
                ResourceModel model = ResourceModel.builder()
                        .arn(listedProfile.arn())
                        .as2Id(listedProfile.as2Id())
                        .profileId(listedProfile.profileId())
                        .profileType(listedProfile.profileTypeAsString())
                        .build();
                models.add(model);
            });

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .nextToken(response.nextToken())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + listProfilesRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("listProfiles", e);
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(
                    ResourceModel.TYPE_NAME, request.getDesiredResourceState().getProfileType());
        }
    }
}
