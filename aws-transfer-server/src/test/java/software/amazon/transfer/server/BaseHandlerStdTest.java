package software.amazon.transfer.server;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.stream.Stream;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.AccessDeniedException;
import software.amazon.awssdk.services.transfer.model.ConflictException;
import software.amazon.awssdk.services.transfer.model.InvalidNextTokenException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ResourceExistsException;
import software.amazon.awssdk.services.transfer.model.ResourceNotFoundException;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class BaseHandlerStdTest {

    @Mock
    private Logger logger;

    private ResourceModel model;
    private CallbackContext context;
    private MockTestHandler handler;

    static class MockTestHandler extends BaseHandlerStd {
        MockTestHandler(Logger logger) {
            this.logger = logger;
        }

        @Override
        protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
                AmazonWebServicesClientProxy proxy,
                ResourceHandlerRequest<ResourceModel> request,
                CallbackContext callbackContext,
                ProxyClient<TransferClient> proxyClient,
                ProxyClient<Ec2Client> proxyEc2Client,
                Logger logger) {
            return null;
        }
    }

    @BeforeEach
    public void setupState() {
        model = ResourceModel.builder().serverId("ServerId").build();

        context = new CallbackContext();
        handler = new MockTestHandler(logger);
    }

    private static Stream<Arguments> providerErrorLogicTestParams() {
        ThrottlingException throttlingException = ThrottlingException.builder()
                .retryAfterSeconds("5")
                .cause(new NullPointerException("Test exception"))
                .message("Throw NPE")
                .statusCode(404)
                .requestId("RequestID")
                .build();
        ConflictException conflictException = ConflictException.builder()
                .cause(new NullPointerException("Test exception"))
                .message("Throw NPE")
                .statusCode(404)
                .requestId("RequestID")
                .build();
        AwsServiceException awsServiceException = AwsServiceException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ThrottlingException")
                        .build())
                .build();

        return Stream.of(
                Arguments.of(throttlingException, true, 5, 30, 29),
                Arguments.of(conflictException, true, 15, 30, 29),
                Arguments.of(awsServiceException, true, 15, 30, 29),
                Arguments.of(conflictException, false, 0, 0, 0));
    }

    @ParameterizedTest
    @MethodSource({"providerErrorLogicTestParams"})
    public void simpleHandleErrorLogicTest(Exception thrown, boolean inProgress, int delay, int retries, int expected) {
        context.setNumRetries(retries);
        assertErrorHandled(thrown, inProgress, delay);
        assertThat(context.getNumRetries()).isEqualTo(expected);
    }

    private static Stream<Arguments> providerTestFailures() {
        return Stream.of(
                Arguments.of(ResourceExistsException.builder().build()),
                Arguments.of(ResourceNotFoundException.builder().build()),
                Arguments.of(AccessDeniedException.builder().build()),
                Arguments.of(InvalidRequestException.builder().build()),
                Arguments.of(InvalidNextTokenException.builder().build()),
                Arguments.of(new NullPointerException()));
    }

    @ParameterizedTest
    @MethodSource({"providerTestFailures"})
    public void testFailures(Exception thrown) {
        assertErrorHandled(thrown, false, 0);
    }

    private ProgressEvent<ResourceModel, CallbackContext> assertErrorHandled(
            Exception e, boolean inProgress, int delay) {

        ProgressEvent<ResourceModel, CallbackContext> progress =
                handler.handleError("TEST", e, model, context, "clntReqTkn");

        assertThat(progress.isInProgress()).isEqualTo(inProgress);
        assertThat(progress.isFailed()).isEqualTo(!inProgress);
        assertThat(progress.getCallbackDelaySeconds()).isEqualTo(delay);
        if (inProgress) {
            assertThat(progress.getResourceModel()).isEqualTo(model);
        }

        return progress;
    }
}
