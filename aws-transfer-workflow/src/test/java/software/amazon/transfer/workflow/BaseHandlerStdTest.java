package software.amazon.transfer.workflow;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class BaseHandlerStdTest {
    @Mock
    private AmazonWebServicesClientProxy proxy;

    static class MockTestHandler extends BaseHandlerStd {
        @Override
        public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
                AmazonWebServicesClientProxy proxy,
                ResourceHandlerRequest<ResourceModel> request,
                CallbackContext callbackContext,
                ProxyClient<TransferClient> proxyClient,
                Logger logger) {
            assertNotNull(callbackContext);
            return null;
        }
    }

    @Test
    void justToCover3LinesOfCode() {
        var uut = new MockTestHandler();

        // Check with null CallbackContext
        uut.handleRequest(proxy, null, null, null);
        verify(proxy, times(1)).newProxy(any());
        verifyNoMoreInteractions(proxy);
        reset(proxy);

        // Check with non-null CallbackContext
        uut.handleRequest(proxy, null, new CallbackContext(), null);
        verify(proxy, times(1)).newProxy(any());
        verifyNoMoreInteractions(proxy);
        reset(proxy);
    }
}
