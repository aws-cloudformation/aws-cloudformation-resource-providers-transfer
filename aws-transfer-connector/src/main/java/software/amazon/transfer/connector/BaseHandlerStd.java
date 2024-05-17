package software.amazon.transfer.connector;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * The purpose of this base class is to allow a simple calling pattern that
 * makes testing Uluru handlers easier and avoids having to store context
 * in class fields. The {@link MockableBaseHandler} interface is used to
 * make isolation of the inner call such that in testing we guarantee that
 * the caller will not pick the wrong method and avoid human error.
 */
public abstract class BaseHandlerStd extends BaseHandler<CallbackContext>
        implements MockableBaseHandler<CallbackContext> {
    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger);
    }

    @Override
    public abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger);
}
