package software.amazon.transfer.server;

import static software.amazon.transfer.server.translators.ResourceModelAdapter.prepareDesiredResourceModel;
import static software.amazon.transfer.server.translators.ResourceModelAdapter.preparePreviousResourceModel;
import static software.amazon.transfer.server.translators.Translator.emptyListIfNull;
import static software.amazon.transfer.server.translators.Translator.emptyStringIfNull;
import static software.amazon.transfer.server.translators.Translator.translateToSdkProtocols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointRequest;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointResponse;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribedServer;
import software.amazon.awssdk.services.transfer.model.EndpointDetails;
import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.awssdk.services.transfer.model.IdentityProviderDetails;
import software.amazon.awssdk.services.transfer.model.State;
import software.amazon.awssdk.services.transfer.model.UpdateServerRequest;
import software.amazon.awssdk.services.transfer.model.UpdateServerResponse;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.EndpointDetailsTranslator;
import software.amazon.transfer.server.translators.IdentityProviderDetailsTranslator;
import software.amazon.transfer.server.translators.ProtocolDetailsTranslator;
import software.amazon.transfer.server.translators.S3StorageOptionsTranslator;
import software.amazon.transfer.server.translators.TagHelper;
import software.amazon.transfer.server.translators.Translator;
import software.amazon.transfer.server.translators.WorkflowDetailsTranslator;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final ProxyClient<Ec2Client> proxyEc2Client,
            final Logger logger) {
        this.logger = logger;

        final ResourceModel oldModel = request.getPreviousResourceState();
        final ResourceModel newModel = request.getDesiredResourceState();
        final String clientRequestToken = request.getClientRequestToken();

        Translator.ensureServerIdInModel(oldModel);
        Translator.ensureServerIdInModel(newModel);

        prepareDesiredResourceModel(request, newModel, false);
        preparePreviousResourceModel(request, oldModel);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> initialUpdate(
                        progress, oldModel, newModel, proxy, proxyClient, proxyEc2Client, clientRequestToken))
                .then(progress -> updateSecurityGroups(
                        progress, oldModel, newModel, proxy, proxyClient, proxyEc2Client, clientRequestToken))
                .then(progress -> addTags(progress, request, newModel, proxy, proxyClient))
                .then(progress -> removeTags(progress, request, newModel, proxy, proxyClient))
                .then(progress -> new ReadHandler()
                        .handleRequest(proxy, request, callbackContext, proxyClient, proxyEc2Client, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> initialUpdate(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceModel oldModel,
            ResourceModel newModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            ProxyClient<Ec2Client> proxyEc2Client,
            String clientRequestToken) {

        return proxy.initiate(
                        "AWS-Transfer-Server::Update::updateServer",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                .translateToServiceRequest(m -> translateToFirstUpdateRequest(oldModel, newModel))
                .makeServiceCall(this::updateServer)
                .stabilize((awsRequest, awsResponse, client, model, context) ->
                        stabilizeAfterUpdate(client, proxyEc2Client, model))
                .handleError((ignored, exception, client, model, context) ->
                        handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    private Boolean stabilizeAfterUpdate(
            ProxyClient<TransferClient> client, ProxyClient<Ec2Client> ec2Client, ResourceModel model) {

        String serverId = model.getServerId();
        DescribedServer describedServer = describeServer(client, model);

        // Always check if the VPC endpoint is ready to modify
        if (EndpointType.VPC.equals(describedServer.endpointType())) {
            String vpcEndpointId = describedServer.endpointDetails().vpcEndpointId();
            if (!waitForVpcEndpoint(vpcEndpointId, ec2Client, model)) {
                return false;
            }
        }

        List<String> proposedSubnetIds;
        List<String> proposedAddressAllocationIds;

        if (model.getEndpointDetails() != null) {
            proposedSubnetIds = emptyListIfNull(model.getEndpointDetails().getSubnetIds());
            proposedAddressAllocationIds =
                    emptyListIfNull(model.getEndpointDetails().getAddressAllocationIds());
        } else {
            proposedSubnetIds = Collections.emptyList();
            proposedAddressAllocationIds = Collections.emptyList();
        }

        List<String> currentSubnetIds;
        List<String> currentAddressAllocationIds;

        if (describedServer.endpointDetails() != null) {
            currentSubnetIds = describedServer.endpointDetails().subnetIds();
            currentAddressAllocationIds = describedServer.endpointDetails().addressAllocationIds();
        } else {
            currentSubnetIds = Collections.emptyList();
            currentAddressAllocationIds = Collections.emptyList();
        }

        log(String.format("Endpoint current subnet IDs: %s", currentSubnetIds), serverId);
        log(String.format("Endpoint proposed subnet IDs: %s", proposedSubnetIds), serverId);
        log(String.format("Endpoint current address allocation IDs: %s", currentAddressAllocationIds), serverId);
        log(String.format("Endpoint proposed address allocation IDs: %s", proposedAddressAllocationIds), serverId);

        State state = describedServer.state();
        switch (state) {
            case OFFLINE:
                if (!Objects.equals(proposedSubnetIds, currentSubnetIds)) {
                    if (!currentAddressAllocationIds.isEmpty()) {
                        EndpointDetails removeAddressAllocationIds = EndpointDetails.builder()
                                .addressAllocationIds(Collections.emptyList())
                                .build();
                        log("EIP address allocation IDs are removed for subnet update.", serverId);
                        updateServerEndpointDetails(client, serverId, removeAddressAllocationIds);
                        return false;
                    }
                    EndpointDetails updateSubnets = EndpointDetails.builder()
                            .subnetIds(proposedSubnetIds)
                            .build();
                    log("VPC endpoint subnetIds are being updated.", serverId);
                    updateServerEndpointDetails(client, serverId, updateSubnets);
                    return false;
                }

                if (!Objects.equals(proposedAddressAllocationIds, currentAddressAllocationIds)) {
                    EndpointDetails endpointDetails = EndpointDetails.builder()
                            .addressAllocationIds(proposedAddressAllocationIds)
                            .subnetIds(proposedSubnetIds)
                            .build();
                    log("EIP address allocation IDs are being updated.", serverId);
                    updateServerEndpointDetails(client, serverId, endpointDetails);
                    return false;
                }

                if (!privateIpsAvailable(currentAddressAllocationIds, ec2Client)) {
                    log("is waiting for endpoint private IPs", serverId);
                    return false;
                }

                startServer(client, serverId);
                log("is going ONLINE after update.", serverId);
                return false;
            case ONLINE:
                if (Objects.equals(proposedSubnetIds, currentSubnetIds)
                        && Objects.equals(proposedAddressAllocationIds, currentAddressAllocationIds)) {
                    log("update has been stabilized.", serverId);
                    return true; // no update needed, we are done
                }

                stopServer(client, serverId);

                log("is going OFFLINE for update.", serverId);
                return false;
            default:
                return handleStabilizeTransientStates(state, serverId);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateSecurityGroups(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceModel oldModel,
            ResourceModel newModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            ProxyClient<Ec2Client> proxyEc2Client,
            String clientRequestToken) {
        // Why not look at the one inside the oldModel? I think that is because
        // of the option to change VPC to PUBLIC and PUBLIC to VPC endpoints.
        final String vpcEndpointId = getVpcEndpointId(progress.getResourceModel(), proxyClient);
        if (StringUtils.isBlank(vpcEndpointId)) {
            return progress; // skip this step
        }

        List<String> requested = List.of();
        List<String> previous = List.of();
        if (isVpcServerEndpoint(oldModel)) {
            previous = emptyListIfNull(oldModel.getEndpointDetails().getSecurityGroupIds());
        }
        if (isVpcServerEndpoint(newModel)) {
            requested = emptyListIfNull(newModel.getEndpointDetails().getSecurityGroupIds());
        }

        List<String> toAdd = new ArrayList<>(requested);
        toAdd.removeAll(previous);
        List<String> toRemove = new ArrayList<>(previous);
        toRemove.removeAll(requested);

        String serverId = newModel.getServerId();
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            log("VPC endpoint has no modifications for security groups", serverId);
            return progress; // skip this step
        }

        if (!toAdd.isEmpty()) {
            log(String.format("security group IDs to add: %s", toAdd), serverId);
        }
        if (!toRemove.isEmpty()) {
            log(String.format("security group IDs to remove: %s", toRemove), serverId);
        }

        return proxy.initiate(
                        "AWS-Transfer-Server::Update::updateSecurityGroups",
                        proxyEc2Client,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                .translateToServiceRequest(m -> modifyVpcEndpointRequest(vpcEndpointId, toAdd, toRemove))
                .makeServiceCall((awsRequest, client) -> modifyVpcEndpoint(serverId, awsRequest, client))
                .stabilize((awsRequest, awsResponse, client, model, context) ->
                        waitForVpcEndpoint(awsRequest.vpcEndpointId(), client, model))
                .handleError((ignored, exception, proxyClient1, model1, callbackContext1) ->
                        handleError(UPDATE, exception, model1, callbackContext1, clientRequestToken))
                .progress();
    }

    private ModifyVpcEndpointResponse modifyVpcEndpoint(
            String serverId, ModifyVpcEndpointRequest awsRequest, ProxyClient<Ec2Client> client) {
        try (Ec2Client ec2Client = client.client()) {
            ModifyVpcEndpointResponse awsResponse =
                    client.injectCredentialsAndInvokeV2(awsRequest, ec2Client::modifyVpcEndpoint);
            log("VPC Endpoint has been updated successfully.", serverId);
            return awsResponse;
        }
    }

    private Boolean waitForVpcEndpoint(String vpcEndpointId, ProxyClient<Ec2Client> client, ResourceModel model) {
        if (!isVpcEndpointAvailable(vpcEndpointId, client)) {
            log("VPC Endpoint is not available yet.", model.getServerId());
            return false;
        }

        log("VPC endpoint stabilized after update", model.getServerId());
        return true;
    }

    private String getVpcEndpointId(ResourceModel model, ProxyClient<TransferClient> client) {
        DescribedServer describedServer = describeServer(client, model);

        if (describedServer.endpointDetails() != null) {
            return describedServer.endpointDetails().vpcEndpointId();
        }
        return null;
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param oldModel previous server model
     * @param newModel updated server model
     * @return awsRequest the aws service request to modify a resource
     */
    private UpdateServerRequest translateToFirstUpdateRequest(final ResourceModel oldModel, ResourceModel newModel) {
        EndpointType endpointType = EndpointType.valueOf(newModel.getEndpointType());
        EndpointDetails endpointDetails = EndpointDetailsTranslator.toSdk(newModel.getEndpointDetails(), false, true);

        IdentityProviderDetails identityProviderDetails =
                IdentityProviderDetailsTranslator.toSdk(newModel.getIdentityProviderDetails());

        if (isVpcServerEndpoint(oldModel) && isVpcServerEndpoint(newModel)) {
            endpointDetails = endpointDetails.toBuilder()
                    .securityGroupIds((Collection<String>) null)
                    .build();
        }

        if (addressAllocationIdAssociationRequested(oldModel, newModel)) {
            endpointDetails = endpointDetails.toBuilder()
                    .addressAllocationIds((Collection<String>) null)
                    .subnetIds((Collection<String>) null)
                    .build();
        }

        return UpdateServerRequest.builder()
                .certificate(newModel.getCertificate())
                .endpointType(endpointType)
                .endpointDetails(endpointDetails)
                .identityProviderDetails(identityProviderDetails)
                .loggingRole(emptyStringIfNull(newModel.getLoggingRole()))
                .preAuthenticationLoginBanner(emptyStringIfNull(newModel.getPreAuthenticationLoginBanner()))
                .postAuthenticationLoginBanner(emptyStringIfNull(newModel.getPostAuthenticationLoginBanner()))
                .protocols(translateToSdkProtocols(newModel.getProtocols()))
                .protocolDetails(ProtocolDetailsTranslator.toSdk(newModel.getProtocolDetails()))
                .securityPolicyName(newModel.getSecurityPolicyName())
                .serverId(newModel.getServerId())
                .structuredLogDestinations(emptyListIfNull(newModel.getStructuredLogDestinations()))
                .workflowDetails(WorkflowDetailsTranslator.toSdk(newModel.getWorkflowDetails(), true))
                .s3StorageOptions(S3StorageOptionsTranslator.toSdk(newModel.getS3StorageOptions()))
                .build();
    }

    private boolean addressAllocationIdAssociationRequested(ResourceModel oldModel, ResourceModel newModel) {
        if (!isVpcServerEndpoint(newModel)) {
            return false;
        }

        if (!isVpcServerEndpoint(oldModel)) {
            return !CollectionUtils.isNullOrEmpty(newModel.getEndpointDetails().getAddressAllocationIds());
        }

        return !Objects.equals(
                newModel.getEndpointDetails().getAddressAllocationIds(),
                oldModel.getEndpointDetails().getAddressAllocationIds());
    }

    private UpdateServerResponse updateServer(UpdateServerRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            UpdateServerResponse awsResponse =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::updateServer);
            log("has been updated successfully.", awsResponse.serverId());
            return awsResponse;
        }
    }

    private ModifyVpcEndpointRequest modifyVpcEndpointRequest(
            String vpcEndpointId, Collection<String> sgIdsToAdd, Collection<String> sgIdsToRemove) {
        var builder = ModifyVpcEndpointRequest.builder().vpcEndpointId(vpcEndpointId);
        if (!sgIdsToAdd.isEmpty()) {
            builder.addSecurityGroupIds(sgIdsToAdd);
        }
        if (!sgIdsToRemove.isEmpty()) {
            builder.removeSecurityGroupIds(sgIdsToRemove);
        }
        return builder.build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> addTags(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel newModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient) {
        if (TagHelper.shouldUpdateTags(request)) {

            Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
            Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);

            if (!tagsToAdd.isEmpty()) {
                progress = tagResource(proxy, proxyClient, newModel, request, progress.getCallbackContext(), tagsToAdd);
            }
        }
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<TransferClient> serviceClient,
            final ResourceModel resourceModel,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Map<String, String> addedTags) {
        logger.log(String.format(
                "[UPDATE][IN PROGRESS] Going to add tags for Transfer server: %s with AccountId: %s",
                resourceModel.getServerId(), request.getAwsAccountId()));
        final String clientRequestToken = request.getClientRequestToken();

        return proxy.initiate("AWS-Transfer-Server::TagOps", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.tagResourceRequest(model, addedTags))
                .makeServiceCall((tagRequest, client) -> {
                    try (TransferClient transferClient = client.client()) {
                        return client.injectCredentialsAndInvokeV2(tagRequest, transferClient::tagResource);
                    }
                })
                .handleError((ignored, exception, proxyClient, model, context) ->
                        handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> removeTags(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel newModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient) {
        if (TagHelper.shouldUpdateTags(request)) {

            Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
            Set<String> tagsKeysToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);

            if (!tagsKeysToRemove.isEmpty()) {
                progress = untagResource(
                        proxy, proxyClient, newModel, request, progress.getCallbackContext(), tagsKeysToRemove);
            }
        }
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> untagResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<TransferClient> serviceClient,
            final ResourceModel resourceModel,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Set<String> removedTags) {
        logger.log(String.format(
                "[UPDATE][IN PROGRESS] Going to remove tags for Transfer server: %s with AccountId: %s",
                resourceModel.getServerId(), request.getAwsAccountId()));
        final String clientRequestToken = request.getClientRequestToken();

        return proxy.initiate("AWS-Transfer-Server::TagOps", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.untagResourceRequest(model, removedTags))
                .makeServiceCall((untagRequest, client) -> {
                    try (TransferClient transferClient = client.client()) {
                        return client.injectCredentialsAndInvokeV2(untagRequest, transferClient::untagResource);
                    }
                })
                .handleError((ignored, exception, proxyClient, model, context) ->
                        handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }
}
