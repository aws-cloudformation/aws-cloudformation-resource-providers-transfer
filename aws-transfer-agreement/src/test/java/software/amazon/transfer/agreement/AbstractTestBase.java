package software.amazon.transfer.agreement;

import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class AbstractTestBase {

    public static final String TEST_ARN = "arn:test-arn";
    public static final String TEST_DESCRIPTION = "unit test";
    public static final String TEST_DESCRIPTION_2 = "another unit test";
    public static final String TEST_ACCESS_ROLE = "access-role";
    public static final String TEST_BASE_DIRECTORY = "/";
    public static final String TEST_LOCAL_PROFILE = "local-profile";
    public static final String TEST_PARTNER_PROFILE = "partner-profile";
    public static final String TEST_SERVER_ID = "test-server-id";
    public static final String TEST_STATUS = "ACTIVE";
    public static final String TEST_AGREEMENT_ID = "id";
    public static final Map<String, String> RESOURCE_TAG_MAP = Collections.singletonMap("key", "value");
    public static final Map<String, String> SYSTEM_TAG_MAP =
            Collections.singletonMap("aws:cloudformation:stack-name", "StackName");
    public static final Map<String, String> TEST_TAG_MAP =
            ImmutableMap.of("key", "value", "aws:cloudformation:stack-name", "StackName");
    public static final Set<Tag> MODEL_TAGS =
            ImmutableSet.of(Tag.builder().key("key").value("value").build());
    public static final software.amazon.awssdk.services.transfer.model.Tag SDK_MODEL_TAG =
            software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key("key")
                    .value("value")
                    .build();
    public static final software.amazon.awssdk.services.transfer.model.Tag SDK_SYSTEM_TAG =
            software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key("aws:cloudformation:stack-name")
                    .value("StackName")
                    .build();

    abstract MockableBaseHandler<CallbackContext> getHandler();

    protected ProgressEvent<ResourceModel, CallbackContext> callHandler(ResourceHandlerRequest<ResourceModel> request) {
        return getHandler().handleRequest(proxy, request, null, proxyClient, logger);
    }

    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected AmazonWebServicesClientProxy proxy;

    protected ProxyClient<TransferClient> proxyClient;

    @Mock
    protected TransferClient client;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(
                logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        client = mock(TransferClient.class);
        proxyClient = MOCK_PROXY(proxy, client);
    }

    static <T> ProxyClient<T> MOCK_PROXY(final AmazonWebServicesClientProxy proxy, final T sdkClient) {
        return new ProxyClient<T>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT injectCredentialsAndInvokeV2(
                    RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
                    CompletableFuture<ResponseT> injectCredentialsAndInvokeV2Async(
                            RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <
                            RequestT extends AwsRequest,
                            ResponseT extends AwsResponse,
                            IterableT extends SdkIterable<ResponseT>>
                    IterableT injectCredentialsAndInvokeIterableV2(
                            RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
                    ResponseInputStream<ResponseT> injectCredentialsAndInvokeV2InputStream(
                            RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
                    ResponseBytes<ResponseT> injectCredentialsAndInvokeV2Bytes(
                            RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public T client() {
                return sdkClient;
            }
        };
    }
}
