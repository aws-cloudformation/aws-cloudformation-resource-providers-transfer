package software.amazon.transfer.webapp;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.AccessDeniedException;
import software.amazon.awssdk.services.transfer.model.InvalidNextTokenException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.TagHelper;
import software.amazon.transfer.webapp.translators.Translator;

/**
 * The purpose of this base class is to allow a simple calling pattern that
 * makes testing Uluru handlers easier and avoids having to store context
 * in class fields. The {@link MockableBaseHandler} interface is used to
 * make isolation of the inner call such that in testing we guarantee that
 * the caller will not pick the wrong method and avoid human error.
 */
public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected static final String CREATE = "Create";
    protected static final String DELETE = "Delete";
    protected static final String READ = "Read";
    protected static final String LIST = "List";
    protected static final String UPDATE = "Update";
    private static final String FAILURE_LOG_MESSAGE =
            "[ClientRequestToken: %s] Resource %s failed in %s operation, Error: %s%n";
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDenied";
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

        if (exception instanceof ResourceExistsException) {
            return translateToFailure(operation, exception, HandlerErrorCode.AlreadyExists, model, clientRequestToken);
        }

        if (exception instanceof ResourceNotFoundException) {
            return translateToFailure(operation, exception, HandlerErrorCode.NotFound, model, clientRequestToken);
        }

        if (exception instanceof AccessDeniedException) {
            return translateToFailure(operation, exception, HandlerErrorCode.AccessDenied, model, clientRequestToken);
        }

        if (exception instanceof InvalidRequestException) {
            return translateToFailure(operation, exception, HandlerErrorCode.InvalidRequest, model, clientRequestToken);
        }

        if (exception instanceof InvalidNextTokenException) {
            return translateToFailure(operation, exception, HandlerErrorCode.InvalidRequest, model, clientRequestToken);
        }

        return translateToFailure(
                operation, exception, HandlerErrorCode.GeneralServiceException, model, clientRequestToken);
    }

    private ProgressEvent<ResourceModel, CallbackContext> translateToFailure(
            String operation,
            Exception exception,
            HandlerErrorCode errorCode,
            ResourceModel model,
            String clientRequestToken) {
        logger.log(String.format(
                FAILURE_LOG_MESSAGE, clientRequestToken, model.getPrimaryIdentifier(), operation, exception));
        return ProgressEvent.defaultFailureHandler(exception, errorCode);
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

    protected ProgressEvent<ResourceModel, CallbackContext> addTags(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel resourceModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            CallbackContext callbackContext) {
        if (TagHelper.shouldUpdateTags(request)) {

            Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
            Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);

            if (!tagsToAdd.isEmpty()) {
                progress = tagResource(proxy, proxyClient, resourceModel, request, callbackContext, tagsToAdd);
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
                "[UPDATE][IN PROGRESS] Going to add tags for web app: %s with AccountId: %s",
                resourceModel.getWebAppId(), request.getAwsAccountId()));
        final String clientRequestToken = request.getClientRequestToken();

        return proxy.initiate("AWS-Transfer-Web-Apps::TagOps", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.tagResourceRequest(model, addedTags))
                .makeServiceCall((tagRequest, client) -> {
                    try (TransferClient transferClient = client.client()) {
                        return client.injectCredentialsAndInvokeV2(tagRequest, transferClient::tagResource);
                    }
                })
                .handleError((ignored, exception, proxyClient, model, context) -> {
                    if (isEnvironmentTaggingException(exception)) {
                        return ProgressEvent.failed(
                                model, context, HandlerErrorCode.UnauthorizedTaggingOperation, exception.getMessage());
                    }
                    return handleError(UPDATE, exception, model, context, clientRequestToken);
                })
                .progress();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> removeTags(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            ResourceHandlerRequest<ResourceModel> request,
            ResourceModel resourceModel,
            AmazonWebServicesClientProxy proxy,
            ProxyClient<TransferClient> proxyClient,
            CallbackContext callbackContext) {
        if (TagHelper.shouldUpdateTags(request)) {

            Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
            Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
            Set<String> tagsKeysToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);

            if (!tagsKeysToRemove.isEmpty()) {
                progress = untagResource(proxy, proxyClient, resourceModel, request, callbackContext, tagsKeysToRemove);
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
                "[UPDATE][IN PROGRESS] Going to remove tags for web app: %s with AccountId: %s",
                resourceModel.getWebAppId(), request.getAwsAccountId()));
        final String clientRequestToken = request.getClientRequestToken();

        return proxy.initiate("AWS-Transfer-Web-App::TagOps", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.untagResourceRequest(model, removedTags))
                .makeServiceCall((untagRequest, client) -> {
                    try (TransferClient transferClient = client.client()) {
                        return client.injectCredentialsAndInvokeV2(untagRequest, transferClient::untagResource);
                    }
                })
                .handleError((ignored, exception, proxyClient, model, context) -> {
                    if (isEnvironmentTaggingException(exception)) {
                        return ProgressEvent.failed(
                                model, context, HandlerErrorCode.UnauthorizedTaggingOperation, exception.getMessage());
                    }
                    return handleError(UPDATE, exception, model, context, clientRequestToken);
                })
                .progress();
    }

    private boolean isEnvironmentTaggingException(Exception e) {
        return StringUtils.equals(ACCESS_DENIED_ERROR_CODE, getErrorCode(e));
    }
}
