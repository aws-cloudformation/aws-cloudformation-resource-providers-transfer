package software.amazon.transfer.server;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.AccessDeniedException;
import software.amazon.awssdk.services.transfer.model.ConflictException;
import software.amazon.awssdk.services.transfer.model.DescribeServerRequest;
import software.amazon.awssdk.services.transfer.model.DescribedServer;
import software.amazon.awssdk.services.transfer.model.EndpointDetails;
import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidNextTokenException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.ServiceUnavailableException;
import software.amazon.awssdk.services.transfer.model.StartServerRequest;
import software.amazon.awssdk.services.transfer.model.State;
import software.amazon.awssdk.services.transfer.model.StopServerRequest;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.UpdateServerRequest;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.clients.ClientBuilder;
import software.amazon.transfer.server.clients.Ec2ClientBuilder;
import software.amazon.transfer.server.translators.Translator;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected static final String CREATE = "Create";
    protected static final String DELETE = "Delete";
    protected static final String READ = "Read";
    protected static final String LIST = "List";
    protected static final String UPDATE = "Update";
    protected static final int THROTTLE_CALLBACK_DELAY_SECONDS = 15;
    private static final String FAILURE_LOG_MESSAGE =
            "[ClientRequestToken: %s] Resource %s failed in %s operation, Error: %s%n";
    private static final String THROTTLING_EXCEPTION_ERR_CODE = "ThrottlingException";
    protected Logger logger;

    protected static DescribedServer describeServer(ProxyClient<TransferClient> client, ResourceModel model) {
        try (TransferClient transferClient = client.client()) {
            DescribeServerRequest describeRequest = Translator.translateToReadRequest(model);
            return client.injectCredentialsAndInvokeV2(describeRequest, transferClient::describeServer)
                    .server();
        }
    }

    protected static List<String> getAddressAllocationIds(DescribedServer server) {
        if (server.endpointDetails() == null
                || CollectionUtils.isNullOrEmpty(server.endpointDetails().addressAllocationIds())) {
            return Collections.emptyList();
        }
        return server.endpointDetails().addressAllocationIds();
    }

    protected static List<String> getAddressAllocationIds(ResourceModel model) {
        if (model.getEndpointDetails() == null
                || CollectionUtils.isNullOrEmpty(model.getEndpointDetails().getAddressAllocationIds())) {
            return Collections.emptyList();
        }
        return model.getEndpointDetails().getAddressAllocationIds();
    }

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        this.logger = logger;

        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                proxy.newProxy(Ec2ClientBuilder::getClient),
                logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final ProxyClient<Ec2Client> proxyEc2Client,
            final Logger logger);

    /**
     * Handle any service operation errors here.
     *
     * @param op the operation name
     * @param ex the error
     * @param model ResourceModel of Listener
     * @param ctx Callback context from request
     * @param reqToken the request token
     * @return ProgressEvent
     */
    protected ProgressEvent<ResourceModel, CallbackContext> handleError(
            final String op,
            final Exception ex,
            final ResourceModel model,
            final CallbackContext ctx,
            final String reqToken) {

        if (isRetryableServiceException(ex)) {
            log("Retryable service exception: " + ex, model.getPrimaryIdentifier());
            return handleRetryableServiceException(model, ctx, op, reqToken, ex);
        }

        if (isThrottlingException(ex)) {
            log("Throttling Exception", model.getPrimaryIdentifier());
            return handleThrottling(model, ctx, op, reqToken, ex);
        }

        if (ex instanceof ResourceExistsException) {
            return translateToFailure(op, ex, HandlerErrorCode.AlreadyExists, model, ctx, reqToken);
        }

        if (ex instanceof ResourceNotFoundException) {
            return translateToFailure(op, ex, HandlerErrorCode.NotFound, model, ctx, reqToken);
        }

        if (ex instanceof AccessDeniedException) {
            return translateToFailure(op, ex, HandlerErrorCode.AccessDenied, model, ctx, reqToken);
        }

        if (ex instanceof InvalidRequestException) {
            return translateToFailure(op, ex, HandlerErrorCode.InvalidRequest, model, ctx, reqToken);
        }

        if (ex instanceof InvalidNextTokenException) {
            return translateToFailure(op, ex, HandlerErrorCode.InvalidRequest, model, ctx, reqToken);
        }

        return translateToFailure(op, ex, HandlerErrorCode.GeneralServiceException, model, ctx, reqToken);
    }

    private ProgressEvent<ResourceModel, CallbackContext> translateToFailure(
            String op,
            Exception ex,
            HandlerErrorCode errorCode,
            ResourceModel model,
            CallbackContext ctx,
            String reqToken) {
        logger.log(String.format(FAILURE_LOG_MESSAGE, reqToken, model.getPrimaryIdentifier(), op, ex));
        return ProgressEvent.failed(model, ctx, errorCode, ex.getMessage());
    }

    private boolean isRetryableServiceException(Exception exception) {
        return exception instanceof ConflictException
                || exception instanceof InternalServiceErrorException
                || exception instanceof ServiceUnavailableException;
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleRetryableServiceException(
            ResourceModel model, CallbackContext ctx, String op, String reqToken, Exception ex) {

        int currentNumThrottlingRetries = ctx.getNumRetries();
        if (currentNumThrottlingRetries > 0) {
            ctx.setNumRetries(currentNumThrottlingRetries - 1);
            return ProgressEvent.defaultInProgressHandler(ctx, getDelaySeconds(ex), model);
        }

        HandlerErrorCode code;
        if (ex instanceof ConflictException) {
            code = HandlerErrorCode.ResourceConflict;
        } else if (ex instanceof InternalServiceErrorException) {
            code = HandlerErrorCode.ServiceInternalError;
        } else if (ex instanceof ServiceUnavailableException) {
            code = HandlerErrorCode.ServiceLimitExceeded;
        } else {
            code = HandlerErrorCode.GeneralServiceException;
        }

        return translateToFailure(op, ex, code, model, ctx, reqToken);
    }

    private boolean isThrottlingException(Exception e) {
        if (StringUtils.contains(getErrorCode(e), THROTTLING_EXCEPTION_ERR_CODE)) {
            return true;
        }
        if (e instanceof ThrottlingException) {
            return true;
        }
        if (e instanceof AwsServiceException) {
            return ((AwsServiceException) e).isThrottlingException();
        }
        return false;
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleThrottling(
            ResourceModel model, CallbackContext ctx, String op, String reqToken, Exception ex) {

        int currentNumThrottlingRetries = ctx.getNumRetries();
        if (currentNumThrottlingRetries > 0) {
            ctx.setNumRetries(currentNumThrottlingRetries - 1);
            return ProgressEvent.defaultInProgressHandler(ctx, getDelaySeconds(ex), model);
        }

        BaseHandlerException cfnEx = new CfnThrottlingException(op, ex);
        return translateToFailure(op, ex, cfnEx.getErrorCode(), model, ctx, reqToken);
    }

    private static int getDelaySeconds(Exception e) {
        if (e instanceof ThrottlingException) {
            String retryAfterSeconds = ((ThrottlingException) e).retryAfterSeconds();
            return Integer.parseInt(
                    Optional.ofNullable(retryAfterSeconds).orElse(String.valueOf(THROTTLE_CALLBACK_DELAY_SECONDS)));
        }
        return THROTTLE_CALLBACK_DELAY_SECONDS;
    }

    private String getErrorCode(Exception e) {
        if (e instanceof AwsServiceException && ((AwsServiceException) e).awsErrorDetails() != null) {
            return ((AwsServiceException) e).awsErrorDetails().errorCode();
        }
        return e.getMessage();
    }

    protected void log(String message, Object identifier) {
        logger.log(String.format("%s [%s] %s", ResourceModel.TYPE_NAME, identifier, message));
    }

    /**
     * Handles the common transient stabilize states.
     *
     * @param state the state to respond to
     * @param serverId the server ID processing
     * @return false for transients
     * @throws CfnNotStabilizedException if failed states arrive
     */
    protected boolean handleStabilizeTransientStates(State state, String serverId) {
        switch (state) {
            case STARTING:
            case STOPPING:
                log(String.format("is still %s", state), serverId);
                slowDownRetries(state, serverId);
                return false;
            case START_FAILED:
            case STOP_FAILED:
            default:
                log(String.format("AWS Transfer server %s", state), serverId);
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, serverId);
        }
    }

    private void slowDownRetries(State state, String serverId) {
        // Avoid dead time when mock unit testing
        if (!Boolean.getBoolean("uluru.unit.tests")) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException e) {
                log(String.format("Interrupted while sleeping at %s", state), serverId);
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, serverId);
            }
        }
    }

    // There seems to be a bug in the EC2 SDK client. The returned state is a string
    // with all lowercase letters but the enumerated type in the SDK assumes the first
    // letter is uppercase. Calling the VpcEndpoint.state() method as a result will
    // always return null no matter what the actual state string is set as.
    private static final String VPC_ENDPOINT_AVAILABLE =
            software.amazon.awssdk.services.ec2.model.State.AVAILABLE.name().toLowerCase();

    protected boolean isVpcEndpointAvailable(String vpcEndpointId, ProxyClient<Ec2Client> ec2Client) {
        VpcEndpoint vpcEndpoint = getVpcEndpoint(vpcEndpointId, ec2Client);

        String state = vpcEndpoint.stateAsString().toLowerCase();
        return VPC_ENDPOINT_AVAILABLE.equals(state);
    }

    protected static VpcEndpoint getVpcEndpoint(String vpcEndpointId, ProxyClient<Ec2Client> ec2Client) {
        try (Ec2Client client = ec2Client.client()) {
            DescribeVpcEndpointsRequest request = DescribeVpcEndpointsRequest.builder()
                    .vpcEndpointIds(vpcEndpointId)
                    .build();
            DescribeVpcEndpointsResponse response =
                    ec2Client.injectCredentialsAndInvokeV2(request, client::describeVpcEndpoints);

            // We expect only one returned
            return response.vpcEndpoints().get(0);
        }
    }

    protected boolean isVpcServerEndpoint(ResourceModel model) {
        return EndpointType.VPC.name().equals(model.getEndpointType());
    }

    protected boolean privateIpsAvailable(List<String> allocationIds, ProxyClient<Ec2Client> ec2Client) {
        if (allocationIds.isEmpty()) {
            return true; // no IPs to check against, so assume available
        }
        DescribeAddressesRequest request =
                DescribeAddressesRequest.builder().allocationIds(allocationIds).build();
        try (Ec2Client client = ec2Client.client()) {
            List<Address> addresses = ec2Client
                    .injectCredentialsAndInvokeV2(request, client::describeAddresses)
                    .addresses();
            return !addresses.isEmpty() && addresses.stream().allMatch(a -> a.privateIpAddress() != null);
        }
    }

    protected void startServer(ProxyClient<TransferClient> client, String serverId) {
        try (TransferClient transferClient = client.client()) {
            StartServerRequest startServerRequest =
                    StartServerRequest.builder().serverId(serverId).build();
            client.injectCredentialsAndInvokeV2(startServerRequest, transferClient::startServer);
        }
    }

    protected void stopServer(ProxyClient<TransferClient> client, String serverId) {
        try (TransferClient transferClient = client.client()) {
            StopServerRequest stopServerRequest =
                    StopServerRequest.builder().serverId(serverId).build();
            client.injectCredentialsAndInvokeV2(stopServerRequest, transferClient::stopServer);
        }
    }

    protected void updateServerEndpointDetails(
            ProxyClient<TransferClient> client, String serverId, EndpointDetails endpointDetails) {
        try (TransferClient transferClient = client.client()) {
            UpdateServerRequest updateServerRequest = UpdateServerRequest.builder()
                    .endpointDetails(endpointDetails)
                    .serverId(serverId)
                    .build();

            client.injectCredentialsAndInvokeV2(updateServerRequest, transferClient::updateServer);
        }
    }
}
