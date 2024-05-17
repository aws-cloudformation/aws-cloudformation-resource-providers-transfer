package software.amazon.transfer.certificate;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * Interface exposing the only handler method that should be used when
 * testing Uluru handlers. This provides a mechanism to feed the call chain
 * a mock client as needed without complex static mocking of the ClientBuilder.
 *
 * @param <CallbackT>
 */
interface MockableBaseHandler<CallbackT> {
    ProgressEvent<ResourceModel, CallbackT> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackT callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger);
}
