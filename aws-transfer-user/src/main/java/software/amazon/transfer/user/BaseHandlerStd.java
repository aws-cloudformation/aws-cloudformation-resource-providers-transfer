package software.amazon.transfer.user;

import static software.amazon.transfer.user.translators.Translator.streamOfOrEmpty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.AccessDeniedException;
import software.amazon.awssdk.services.transfer.model.ConflictException;
import software.amazon.awssdk.services.transfer.model.DeleteSshPublicKeyRequest;
import software.amazon.awssdk.services.transfer.model.DeleteSshPublicKeyResponse;
import software.amazon.awssdk.services.transfer.model.DescribeUserRequest;
import software.amazon.awssdk.services.transfer.model.DescribeUserResponse;
import software.amazon.awssdk.services.transfer.model.ImportSshPublicKeyRequest;
import software.amazon.awssdk.services.transfer.model.ImportSshPublicKeyResponse;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidNextTokenException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.ServiceUnavailableException;
import software.amazon.awssdk.services.transfer.model.SshPublicKey;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.clients.ClientBuilder;
import software.amazon.transfer.user.translators.TagHelper;
import software.amazon.transfer.user.translators.Translator;

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
                logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger);

    /**
     * Handle any service operation errors here.
     *
     * @param operation the operation name
     * @param exception the error
     * @param model ResourceModel of Listener
     * @param context Callback context from request
     * @param clientRequestToken the request token
     * @return ProgressEvent
     */
    protected ProgressEvent<ResourceModel, CallbackContext> handleError(
            final String operation,
            final Exception exception,
            final ResourceModel model,
            final CallbackContext context,
            final String clientRequestToken) {

        if (isRetryableServiceException(exception)) {
            log("Retryable service exception: " + exception, model.getPrimaryIdentifier());
            return handleRetryableServiceException(
                    model, context, operation, clientRequestToken, exception);
        }

        if (isThrottlingException(exception)) {
            log("Throttling Exception", model.getPrimaryIdentifier());
            return handleThrottling(model, context, operation, clientRequestToken, exception);
        }

        if (exception instanceof ResourceExistsException) {
            return translateToFailure(
                    operation,
                    exception,
                    HandlerErrorCode.AlreadyExists,
                    model,
                    clientRequestToken);
        }

        if (exception instanceof ResourceNotFoundException) {
            return translateToFailure(
                    operation, exception, HandlerErrorCode.NotFound, model, clientRequestToken);
        }

        if (exception instanceof AccessDeniedException) {
            return translateToFailure(
                    operation, exception, HandlerErrorCode.AccessDenied, model, clientRequestToken);
        }

        if (exception instanceof InvalidRequestException) {
            return translateToFailure(
                    operation,
                    exception,
                    HandlerErrorCode.InvalidRequest,
                    model,
                    clientRequestToken);
        }

        if (exception instanceof InvalidNextTokenException) {
            return translateToFailure(
                    operation,
                    exception,
                    HandlerErrorCode.InvalidRequest,
                    model,
                    clientRequestToken);
        }

        return translateToFailure(
                operation,
                exception,
                HandlerErrorCode.GeneralServiceException,
                model,
                clientRequestToken);
    }

    private ProgressEvent<ResourceModel, CallbackContext> translateToFailure(
            String operation,
            Exception exception,
            HandlerErrorCode errorCode,
            ResourceModel model,
            String clientRequestToken) {
        logger.log(
                String.format(
                        FAILURE_LOG_MESSAGE,
                        clientRequestToken,
                        model.getPrimaryIdentifier(),
                        operation,
                        exception));
        return ProgressEvent.defaultFailureHandler(exception, errorCode);
    }

    private boolean isRetryableServiceException(Exception exception) {
        return exception instanceof ConflictException
                || exception instanceof InternalServiceErrorException
                || exception instanceof ServiceUnavailableException;
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleRetryableServiceException(
            ResourceModel model,
            CallbackContext context,
            String operation,
            String clientRequestToken,
            Exception e) {
        int currentNumThrottlingRetries = context.getNumThrottlingRetries();
        if (currentNumThrottlingRetries > 0) {
            context.setNumThrottlingRetries(currentNumThrottlingRetries - 1);
            return ProgressEvent.defaultInProgressHandler(context, getDelaySeconds(e), model);
        }
        BaseHandlerException cfnEx = new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        logger.log(
                String.format(
                        FAILURE_LOG_MESSAGE,
                        clientRequestToken,
                        model.getPrimaryIdentifier(),
                        operation,
                        e));
        return ProgressEvent.failed(model, context, cfnEx.getErrorCode(), cfnEx.getMessage());
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
            ResourceModel model,
            CallbackContext context,
            String operation,
            String clientRequestToken,
            Exception e) {
        int currentNumThrottlingRetries = context.getNumThrottlingRetries();
        if (currentNumThrottlingRetries > 0) {
            context.setNumThrottlingRetries(currentNumThrottlingRetries - 1);
            return ProgressEvent.defaultInProgressHandler(context, getDelaySeconds(e), model);
        }
        BaseHandlerException cfnEx = new CfnThrottlingException(e);
        logger.log(
                String.format(
                        FAILURE_LOG_MESSAGE,
                        clientRequestToken,
                        model.getPrimaryIdentifier(),
                        operation,
                        e));
        return ProgressEvent.failed(model, context, cfnEx.getErrorCode(), cfnEx.getMessage());
    }

    private static int getDelaySeconds(Exception e) {
        if (e instanceof ThrottlingException) {
            String retryAfterSeconds = ((ThrottlingException) e).retryAfterSeconds();
            return Integer.parseInt(
                    Optional.ofNullable(retryAfterSeconds)
                            .orElse(String.valueOf(THROTTLE_CALLBACK_DELAY_SECONDS)));
        }
        return THROTTLE_CALLBACK_DELAY_SECONDS;
    }

    private String getErrorCode(Exception e) {
        if (e instanceof AwsServiceException
                && ((AwsServiceException) e).awsErrorDetails() != null) {
            return ((AwsServiceException) e).awsErrorDetails().errorCode();
        }
        return e.getMessage();
    }

    protected void log(String message, Object identifier) {
        logger.log(String.format("%s [%s] %s", ResourceModel.TYPE_NAME, identifier, message));
    }

    protected ProgressEvent<ResourceModel, CallbackContext> importSshPublicKeys(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            String clientRequestToken,
            String operation,
            List<String> keysToAdd,
            ProgressEvent<ResourceModel, CallbackContext> progress) {

        String serverId = progress.getResourceModel().getServerId();
        String userName = progress.getResourceModel().getUserName();

        for (String keyBody : keysToAdd) {
            progress =
                    proxy.initiate(
                                    "AWS-Transfer-User::importSshPublicKey",
                                    proxyClient,
                                    progress.getResourceModel(),
                                    progress.getCallbackContext())
                            .translateToServiceRequest(
                                    m -> translateToImportSshPublicKey(serverId, userName, keyBody))
                            .makeServiceCall(this::importSshPublicKey)
                            .handleError(
                                    (ignored, exception, client, model, context) ->
                                            handleError(
                                                    operation,
                                                    exception,
                                                    model,
                                                    context,
                                                    clientRequestToken))
                            .progress();
        }
        return progress;
    }

    private ImportSshPublicKeyRequest translateToImportSshPublicKey(
            String serverId, String userName, String sshPublicKeyBody) {
        return ImportSshPublicKeyRequest.builder()
                .serverId(serverId)
                .userName(userName)
                .sshPublicKeyBody(sshPublicKeyBody)
                .build();
    }

    private ImportSshPublicKeyResponse importSshPublicKey(
            ImportSshPublicKeyRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            return client.injectCredentialsAndInvokeV2(
                    awsRequest, transferClient::importSshPublicKey);
        }
    }

    protected ProgressEvent<ResourceModel, CallbackContext> deleteSshPublicKeys(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            String clientRequestToken,
            String operation,
            List<String> keysToDelete,
            ProgressEvent<ResourceModel, CallbackContext> progress) {

        if (keysToDelete.isEmpty()) {
            return progress;
        }

        String serverId = progress.getResourceModel().getServerId();
        String userName = progress.getResourceModel().getUserName();

        DescribeUserRequest readRequest = translateToReadRequest(progress.getResourceModel());
        List<SshPublicKey> currentKeys = readUser(readRequest, proxyClient).user().sshPublicKeys();

        List<String> keyIdsToDelete =
                currentKeys.stream()
                        .filter(key -> keysToDelete.contains(key.sshPublicKeyBody()))
                        .map(SshPublicKey::sshPublicKeyId)
                        .collect(Collectors.toList());

        for (String sshKeyId : keyIdsToDelete) {
            progress =
                    proxy.initiate(
                                    "AWS-Transfer-User::deleteSshPublicKey",
                                    proxyClient,
                                    progress.getResourceModel(),
                                    progress.getCallbackContext())
                            .translateToServiceRequest(
                                    m ->
                                            translateToDeleteSshPublicKey(
                                                    serverId, userName, sshKeyId))
                            .makeServiceCall(this::deleteSshPublicKey)
                            .handleError(
                                    (ignored, exception, client, model, context) ->
                                            handleError(
                                                    operation,
                                                    exception,
                                                    model,
                                                    context,
                                                    clientRequestToken))
                            .progress();
        }
        return progress;
    }

    private DeleteSshPublicKeyRequest translateToDeleteSshPublicKey(
            String serverId, String userName, String sshKeyId) {
        return DeleteSshPublicKeyRequest.builder()
                .serverId(serverId)
                .userName(userName)
                .sshPublicKeyId(sshKeyId)
                .build();
    }

    private DeleteSshPublicKeyResponse deleteSshPublicKey(
            DeleteSshPublicKeyRequest request, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            return client.injectCredentialsAndInvokeV2(request, transferClient::deleteSshPublicKey);
        }
    }

    protected ProgressEvent<ResourceModel, CallbackContext> addTags(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel newModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            CallbackContext callbackContext) {
        if (TagHelper.shouldUpdateTags(request)) {

            Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
            Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);

            if (!tagsToAdd.isEmpty()) {
                progress =
                        tagResource(
                                proxy, proxyClient, newModel, request, callbackContext, tagsToAdd);
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
        logger.log(
                String.format(
                        "[UPDATE][IN PROGRESS] Going to add tags for user: %s with AccountId: %s",
                        resourceModel.getUserName(), request.getAwsAccountId()));
        final String clientRequestToken = request.getClientRequestToken();

        return proxy.initiate(
                        "AWS-Transfer-User::TagOps", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.tagResourceRequest(model, addedTags))
                .makeServiceCall(
                        (tagRequest, client) -> {
                            try (TransferClient transferClient = client.client()) {
                                return client.injectCredentialsAndInvokeV2(
                                        tagRequest, transferClient::tagResource);
                            }
                        })
                .handleError(
                        (ignored, exception, proxyClient, model, context) ->
                                handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> removeTags(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel newModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            CallbackContext callbackContext) {
        if (TagHelper.shouldUpdateTags(request)) {

            Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
            Set<String> tagsKeysToRemove =
                    TagHelper.generateTagsToRemove(previousTags, desiredTags);

            if (!tagsKeysToRemove.isEmpty()) {
                progress =
                        untagResource(
                                proxy,
                                proxyClient,
                                newModel,
                                request,
                                callbackContext,
                                tagsKeysToRemove);
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
        logger.log(
                String.format(
                        "[UPDATE][IN PROGRESS] Going to remove tags for user: %s with AccountId: %s",
                        resourceModel.getUserName(), request.getAwsAccountId()));
        final String clientRequestToken = request.getClientRequestToken();

        return proxy.initiate(
                        "AWS-Transfer-User::TagOps", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(
                        model -> Translator.untagResourceRequest(model, removedTags))
                .makeServiceCall(
                        (untagRequest, client) -> {
                            try (TransferClient transferClient = client.client()) {
                                return client.injectCredentialsAndInvokeV2(
                                        untagRequest, transferClient::untagResource);
                            }
                        })
                .handleError(
                        (ignored, exception, proxyClient, model, context) ->
                                handleError(UPDATE, exception, model, context, clientRequestToken))
                .progress();
    }

    protected DescribeUserRequest translateToReadRequest(final ResourceModel model) {
        return DescribeUserRequest.builder()
                .serverId(model.getServerId())
                .userName(model.getUserName())
                .build();
    }

    protected DescribeUserResponse readUser(
            DescribeUserRequest awsRequest, ProxyClient<TransferClient> client) {
        try (TransferClient transferClient = client.client()) {
            DescribeUserResponse awsResponse =
                    client.injectCredentialsAndInvokeV2(awsRequest, transferClient::describeUser);
            log("has successfully been read.", awsRequest.userName());
            return awsResponse;
        }
    }

    protected static List<String> translateToSShPublicKeyBodies(ResourceModel newModel) {
        return streamOfOrEmpty(newModel.getSshPublicKeys()).collect(Collectors.toList());
    }

    protected static String userIdentifier(String serverId, String userName) {
        return String.format("%s/%s", serverId, userName);
    }
}
