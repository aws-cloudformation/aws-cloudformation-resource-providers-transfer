package software.amazon.transfer.user;

import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeUserResponse;
import software.amazon.awssdk.services.transfer.model.DescribedUser;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.Translator;
import software.amazon.transfer.user.translators.UserArn;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class AbstractTestBase {

    protected static final Map<String, String> RESOURCE_TAG_MAP = Collections.singletonMap("key", "value");
    protected static final Map<String, String> TEST_TAG_MAP = ImmutableMap.of("key2", "value2");
    protected static final Map<String, String> SYSTEM_TAG_MAP =
            Collections.singletonMap("aws:cloudformation:stack-name", "StackName");
    protected static final List<Tag> MODEL_TAGS =
            ImmutableList.of(Tag.builder().key("key").value("value").build());
    protected static final List<Tag> SYSTEM_TAGS = ImmutableList.of(Tag.builder()
            .key("aws:cloudformation:stack-name")
            .value("StackName")
            .build());
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

    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected AmazonWebServicesClientProxy proxy;
    protected ProxyClient<TransferClient> proxyClient;

    @Mock
    TransferClient sdkClient;

    static ProxyClient<TransferClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy, final TransferClient sdkClient) {
        return new ProxyClient<TransferClient>() {
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
            public TransferClient client() {
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

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(
                logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(TransferClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    protected ResourceModel simpleUserModel() {
        Region region = Region.getRegion(Regions.US_EAST_1);
        UserArn userArn = new UserArn(region, "123456789012", "testServerId", "testUser");

        return ResourceModel.builder()
                .arn(userArn.getArn())
                .serverId("testServerId")
                .userName("testUser")
                .build();
    }

    protected ResourceModel fullyLoadedUserModel() {
        return ResourceModel.builder()
                .arn("testArn")
                .serverId("testServerId")
                .userName("testUserName")
                .role("userRole")
                .policy("userPolicy")
                .posixProfile(PosixProfile.builder()
                        .uid(1d)
                        .gid(1d)
                        .secondaryGids(Arrays.asList(2d, 3d, 4d))
                        .build())
                .homeDirectoryType("LOGICAL")
                .homeDirectoryMappings(Arrays.asList(
                        HomeDirectoryMapEntry.builder()
                                .entry("/bucket1")
                                .target("/my-bucket")
                                .build(),
                        HomeDirectoryMapEntry.builder()
                                .entry("/bucket2")
                                .target("/my-other-bucket")
                                .build()))
                .sshPublicKeys(Arrays.asList("ssh-rsa abcdefgh", "ssh-rsa foobar", "ssh-rsa nullkeydate"))
                .tags(MODEL_TAGS)
                .build();
    }

    protected DescribeUserResponse describeUserResponseFromModel(ResourceModel model) {
        return DescribeUserResponse.builder()
                .serverId(model.getServerId())
                .user(DescribedUser.builder()
                        .arn(model.getArn())
                        .userName(model.getUserName())
                        .role(model.getRole())
                        .policy(model.getPolicy())
                        .homeDirectoryType(model.getHomeDirectoryType())
                        .homeDirectory(model.getHomeDirectory())
                        .homeDirectoryMappings(
                                Translator.translateToSdkHomeDirectoryMappings(model.getHomeDirectoryMappings()))
                        .posixProfile(Translator.translateToSdkPosixProfile(model.getPosixProfile()))
                        .sshPublicKeys(translateToSdkSshPublicKeys(model.getSshPublicKeys()))
                        .tags(Translator.translateToSdkTags(model.getTags()))
                        .build())
                .build();
    }

    private Collection<software.amazon.awssdk.services.transfer.model.SshPublicKey> translateToSdkSshPublicKeys(
            List<String> sshPublicKeys) {
        if (sshPublicKeys == null || sshPublicKeys.isEmpty()) {
            return null;
        }
        return sshPublicKeys.stream()
                .map(key -> software.amazon.awssdk.services.transfer.model.SshPublicKey.builder()
                        .sshPublicKeyBody(key)
                        .build())
                .collect(Collectors.toList());
    }
}
