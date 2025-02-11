package software.amazon.transfer.webapp;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppCustomizationResponse;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppResponse;
import software.amazon.awssdk.services.transfer.model.DescribedWebApp;
import software.amazon.awssdk.services.transfer.model.DescribedWebAppCustomization;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.translators.Translator;

public abstract class AbstractTestBase {
    public static final String TEST_APPLICATION_ARN =
            "arn:aws:sso::123456789012:application/ins-abcd1234efgh5678/apl-1234567890abcdef";
    public static final String TEST_INSTANCE_ARN = "arn:aws:sso:::instance/ins-abcd1234efgh5678";
    public static final String TEST_ROLE = "arn:aws:iam::123456789012:role/test-role";
    public static final String TEST_CUSTOMIZATION_TITLE = "Test Web App Customization Title";
    public static final String TEST_CUSTOMIZATION_LOGO_FILE = loadTestFile("logo.png");
    public static final String TEST_CUSTOMIZATION_FAVICON_FILE = loadTestFile("favicon.png");
    public static final Integer TEST_PROVISIONED = 3;
    public static final String TEST_ARN = "arn:aws:transfer:us-east-1:123456789012:webapp/webapp-12345678901234567";
    public static final String TEST_WEB_APP_ID = "webapp-12345678901234567";
    public static final String TEST_ACCESS_ENDPOINT = "https://x7k9pq4mnt5wy.cloudfront.net";
    protected static final Map<String, String> RESOURCE_TAG_MAP = Collections.singletonMap("key", "value");
    protected static final Map<String, String> TEST_TAG_MAP = ImmutableMap.of("key2", "value2");
    protected static final Map<String, String> SYSTEM_TAG_MAP =
            Collections.singletonMap("aws:cloudformation:stack-name", "StackName");
    protected static final List<Tag> MODEL_TAGS =
            ImmutableList.of(Tag.builder().key("key").value("value").build());
    protected static final Map<String, String> EXTRA_MODEL_TAGS = ImmutableMap.of("extrakey", "extravalue");
    protected static final software.amazon.awssdk.services.transfer.model.Tag SDK_MODEL_TAG =
            software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key("key")
                    .value("value")
                    .build();
    protected static final software.amazon.awssdk.services.transfer.model.Tag SDK_SYSTEM_TAG =
            software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key("aws:cloudformation:stack-name")
                    .value("StackName")
                    .build();

    public static IdentityProviderDetails getIdentityProviderDetails() {
        return IdentityProviderDetails.builder()
                .applicationArn(TEST_APPLICATION_ARN)
                .instanceArn(TEST_INSTANCE_ARN)
                .role(TEST_ROLE)
                .build();
    }

    public static WebAppUnits getWebAppUnits() {
        return WebAppUnits.builder().provisioned(TEST_PROVISIONED).build();
    }

    public static WebAppCustomization getWebAppCustomization() {
        return WebAppCustomization.builder()
                .title(TEST_CUSTOMIZATION_TITLE)
                .logoFile(TEST_CUSTOMIZATION_LOGO_FILE)
                .faviconFile(TEST_CUSTOMIZATION_FAVICON_FILE)
                .build();
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

    protected static ResourceHandlerRequest.ResourceHandlerRequestBuilder<ResourceModel> requestBuilder() {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId("123456789012")
                .awsPartition("aws")
                .region("us-east-1");
    }

    protected ResourceModel simpleWebAppModel() {
        return ResourceModel.builder()
                .webAppId(TEST_WEB_APP_ID)
                .identityProviderDetails(getIdentityProviderDetails())
                .webAppUnits(WebAppUnits.builder().provisioned(1).build())
                .tags(Collections.emptyList())
                .build();
    }

    protected ResourceModel fullyLoadedWebAppModel() {
        return ResourceModel.builder()
                .identityProviderDetails(getIdentityProviderDetails())
                .accessEndpoint(TEST_ACCESS_ENDPOINT)
                .webAppUnits(getWebAppUnits())
                .webAppCustomization(getWebAppCustomization())
                .tags(MODEL_TAGS)
                .build();
    }

    protected DescribeWebAppResponse describeWebAppResponseFromModel(ResourceModel model) {
        software.amazon.awssdk.services.transfer.model.WebAppUnits webAppUnits =
                software.amazon.awssdk.services.transfer.model.WebAppUnits.builder()
                        .provisioned(
                                model.getWebAppUnits() != null
                                        ? model.getWebAppUnits().getProvisioned()
                                        : 1) // Default to 1 if not provided
                        .build();

        software.amazon.transfer.webapp.WebAppUnits modelWebAppUnits =
                software.amazon.transfer.webapp.WebAppUnits.builder()
                        .provisioned(webAppUnits.provisioned())
                        .build();
        model.setWebAppUnits(modelWebAppUnits);

        IdentityProviderDetails currentModel = model.getIdentityProviderDetails();
        IdentityProviderDetails identityProviderDetails = IdentityProviderDetails.builder()
                .applicationArn(TEST_APPLICATION_ARN)
                .instanceArn(currentModel.getInstanceArn())
                .role(currentModel.getRole())
                .build();
        model.setIdentityProviderDetails(identityProviderDetails);

        return DescribeWebAppResponse.builder()
                .webApp(
                        DescribedWebApp.builder()
                                .webAppId(model.getWebAppId())
                                .accessEndpoint(model.getAccessEndpoint())
                                .arn(model.getArn())
                                .describedIdentityProviderDetails(
                                        software.amazon.awssdk.services.transfer.model
                                                .DescribedWebAppIdentityProviderDetails.builder()
                                                .identityCenterConfig(software.amazon.awssdk.services.transfer.model
                                                        .DescribedIdentityCenterConfig.builder()
                                                        .applicationArn(model.getIdentityProviderDetails()
                                                                .getApplicationArn())
                                                        .instanceArn(model.getIdentityProviderDetails()
                                                                .getInstanceArn())
                                                        .role(model.getIdentityProviderDetails()
                                                                .getRole())
                                                        .build())
                                                .build())
                                .webAppUnits(webAppUnits)
                                .tags(Translator.translateToSdkTags(model.getTags()))
                                .build())
                .build();
    }

    protected DescribeWebAppCustomizationResponse describeWebAppCustomizationResponseFromModel(ResourceModel model) {
        return DescribeWebAppCustomizationResponse.builder()
                .webAppCustomization(DescribedWebAppCustomization.builder()
                        .title(model.getWebAppCustomization().getTitle())
                        .logoFile(convertFromBase64String(
                                model.getWebAppCustomization().getLogoFile()))
                        .faviconFile(convertFromBase64String(
                                model.getWebAppCustomization().getFaviconFile()))
                        .build())
                .build();
    }

    protected static String loadTestFile(String resourcePath) {
        try {
            // Get file from resources folder
            ClassLoader classLoader = AbstractTestBase.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + resourcePath);
            }

            // Read the file bytes
            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();

            // Convert to Base64
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test file: " + resourcePath, e);
        }
    }

    private SdkBytes convertFromBase64String(String base64String) {
        if (base64String == null) {
            return null;
        }
        return SdkBytes.fromByteArray(Base64.getDecoder().decode(base64String));
    }
}
