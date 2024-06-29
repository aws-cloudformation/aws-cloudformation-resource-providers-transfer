package software.amazon.transfer.server;

import static software.amazon.transfer.server.translators.ResourceModelAdapter.prepareDesiredResourceModel;
import static software.amazon.transfer.server.translators.Translator.translateToSdkProtocols;
import static software.amazon.transfer.server.translators.Translator.translateToSdkTags;

import java.util.List;
import java.util.Objects;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.CreateServerRequest;
import software.amazon.awssdk.services.transfer.model.CreateServerResponse;
import software.amazon.awssdk.services.transfer.model.DescribedServer;
import software.amazon.awssdk.services.transfer.model.EndpointDetails;
import software.amazon.awssdk.services.transfer.model.State;
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
import software.amazon.transfer.server.translators.ServerArn;
import software.amazon.transfer.server.translators.WorkflowDetailsTranslator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

public class CreateHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final ProxyClient<Ec2Client> proxyEc2Client,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        ResourceModel newModel = request.getDesiredResourceState();

        prepareDesiredResourceModel(request, newModel, true);

        return ProgressEvent.progress(newModel, callbackContext)
                .then(progress -> proxy.initiate(
                                "AWS-Transfer-Server::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(this::translateToCreateRequest)
                        .makeServiceCall(this::createServer)
                        .stabilize((createRequest, createResponse, client, model, context) ->
                                stabilizeAfterCreate(request, createResponse, client, proxyEc2Client, model, context))
                        .handleError((ignored, exception, client, model, context) ->
                                handleError(CREATE, exception, model, context, clientRequestToken))
                        .progress())
                .then(progress -> new ReadHandler()
                        .handleRequest(proxy, request, callbackContext, proxyClient, proxyEc2Client, logger));
    }

    private CreateServerRequest translateToCreateRequest(final ResourceModel model) {

        EndpointDetails endpointDetails = EndpointDetailsTranslator.toSdk(model.getEndpointDetails(), true, false);

        return CreateServerRequest.builder()
                .certificate(model.getCertificate())
                .domain(model.getDomain())
                .endpointType(model.getEndpointType())
                .endpointDetails(endpointDetails)
                .identityProviderType(model.getIdentityProviderType())
                .identityProviderDetails(IdentityProviderDetailsTranslator.toSdk(model.getIdentityProviderDetails()))
                .loggingRole(model.getLoggingRole())
                .preAuthenticationLoginBanner(model.getPreAuthenticationLoginBanner())
                .postAuthenticationLoginBanner(model.getPostAuthenticationLoginBanner())
                .protocols(translateToSdkProtocols(model.getProtocols()))
                .protocolDetails(ProtocolDetailsTranslator.toSdk(model.getProtocolDetails()))
                .securityPolicyName(model.getSecurityPolicyName())
                .tags(translateToSdkTags(model.getTags()))
                .workflowDetails(WorkflowDetailsTranslator.toSdk(model.getWorkflowDetails(), false))
                .structuredLogDestinations(model.getStructuredLogDestinations())
                .s3StorageOptions(S3StorageOptionsTranslator.toSdk(model.getS3StorageOptions()))
                .build();
    }

    private CreateServerResponse createServer(CreateServerRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            CreateServerResponse awsResponse =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::createServer);
            log("successfully created.", awsResponse.serverId());
            return awsResponse;
        }
    }

    private boolean stabilizeAfterCreate(
            ResourceHandlerRequest<ResourceModel> request,
            CreateServerResponse awsResponse,
            ProxyClient<TransferClient> client,
            ProxyClient<Ec2Client> ec2Client,
            ResourceModel model,
            CallbackContext ignored2) {

        String serverId = awsResponse.serverId();
        Region region = RegionUtils.getRegion(request.getRegion());
        ServerArn serverArn = new ServerArn(region, request.getAwsAccountId(), serverId);
        model.setArn(serverArn.getArn());
        model.setServerId(serverId);

        DescribedServer describedServer = describeServer(client, model);

        List<String> proposed = getAddressAllocationIds(model);
        List<String> current = getAddressAllocationIds(describedServer);

        State state = describedServer.state();
        switch (state) {
            case OFFLINE:
                if (!Objects.equals(proposed, current)) {
                    log("updating the address allocation IDs", serverId);
                    updateServerWithAddressAllocationIds(client, serverId, model);
                } else if (!privateIpsAvailable(current, ec2Client)) {
                    log("is waiting for endpoint private IPs", serverId);
                } else {
                    log("is going ONLINE after update", serverId);
                    startServer(client, serverId);
                }

                return false;

            case ONLINE:
                if (isVpcServerEndpoint(model)) {
                    String vpcEndpointId = describedServer.endpointDetails().vpcEndpointId();
                    if (!isVpcEndpointAvailable(vpcEndpointId, ec2Client)) {
                        log("VPC Endpoint is not available yet", serverId);
                        return false;
                    }
                }

                if (addressAllocationIdAssociationRequested(model)) {
                    if (!Objects.equals(proposed, current)) {
                        log("is going OFFLINE for update", serverId);
                        stopServer(client, serverId);
                        return false;
                    }
                }

                log("create has been stabilized.", serverId);
                return true;

            default:
                return handleStabilizeTransientStates(state, serverId);
        }
    }

    private void updateServerWithAddressAllocationIds(
            ProxyClient<TransferClient> client, String serverId, ResourceModel model) {
        EndpointDetails endpointDetails = EndpointDetailsTranslator.toSdk(model.getEndpointDetails(), false, true);
        updateServerEndpointDetails(client, serverId, endpointDetails);
    }

    private boolean addressAllocationIdAssociationRequested(ResourceModel model) {
        return isVpcServerEndpoint(model)
                && model.getEndpointDetails() != null
                && !CollectionUtils.isNullOrEmpty(model.getEndpointDetails().getAddressAllocationIds());
    }
}
