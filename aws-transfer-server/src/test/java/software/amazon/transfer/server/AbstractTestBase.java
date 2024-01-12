package software.amazon.transfer.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static software.amazon.transfer.server.translators.ResourceModelAdapter.DEFAULT_IDENTITY_PROVIDER_TYPE;
import static software.amazon.transfer.server.translators.ResourceModelAdapter.DEFAULT_PROTOCOLS;
import static software.amazon.transfer.server.translators.ResourceModelAdapter.DEFAULT_SECURITY_POLICY;
import static software.amazon.transfer.server.translators.Translator.streamOfOrEmpty;

import java.time.Duration;
import java.util.Arrays;
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
import software.amazon.awssdk.regions.PartitionMetadata;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.ec2.model.SecurityGroupIdentifier;
import software.amazon.awssdk.services.ec2.model.State;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeServerResponse;
import software.amazon.awssdk.services.transfer.model.DescribedServer;
import software.amazon.awssdk.services.transfer.model.Domain;
import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.awssdk.services.transfer.model.IdentityProviderType;
import software.amazon.awssdk.services.transfer.model.Protocol;
import software.amazon.awssdk.services.transfer.model.SetStatOption;
import software.amazon.awssdk.services.transfer.model.SftpAuthenticationMethods;
import software.amazon.awssdk.services.transfer.model.TlsSessionResumptionMode;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.EndpointDetailsTranslator;
import software.amazon.transfer.server.translators.IdentityProviderDetailsTranslator;
import software.amazon.transfer.server.translators.ProtocolDetailsTranslator;
import software.amazon.transfer.server.translators.ServerArn;
import software.amazon.transfer.server.translators.Translator;
import software.amazon.transfer.server.translators.WorkflowDetailsTranslator;

import com.amazonaws.regions.Regions;

public class AbstractTestBase {

    protected static final List<Tag> MODEL_TAGS =
            ImmutableList.of(Tag.builder().key("key").value("value").build());

    protected static final Map<String, String> EXTRA_MODEL_TAGS = ImmutableMap.of("keyAdded", "value1");

    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected AmazonWebServicesClientProxy proxy;

    protected ProxyClient<TransferClient> proxyClient;

    @Mock
    protected TransferClient sdkClient;

    protected ProxyClient<Ec2Client> proxyEc2Client;

    @Mock
    protected Ec2Client sdkEc2Client;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(
                logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(TransferClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        sdkEc2Client = mock(Ec2Client.class);
        proxyEc2Client = MOCK_PROXY(proxy, sdkEc2Client);
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

    protected static DescribeServerResponse describeServerFromModel(
            String serverId, String state, ResourceModel model) {
        return DescribeServerResponse.builder()
                .server(DescribedServer.builder()
                        .arn(getTestServerArn(serverId))
                        .serverId(serverId)
                        .state(state)
                        .domain(model.getDomain())
                        .endpointType(EndpointType.valueOf(model.getEndpointType()))
                        .identityProviderType(IdentityProviderType.valueOf(model.getIdentityProviderType()))
                        .securityPolicyName(model.getSecurityPolicyName())
                        .protocols(model.getProtocols().stream()
                                .map(Protocol::fromValue)
                                .collect(Collectors.toList()))
                        .tags(Translator.translateToSdkTags(model.getTags()))
                        .structuredLogDestinations(model.getStructuredLogDestinations())
                        .loggingRole(model.getLoggingRole())
                        .preAuthenticationLoginBanner(model.getPreAuthenticationLoginBanner())
                        .postAuthenticationLoginBanner(model.getPostAuthenticationLoginBanner())
                        .protocolDetails(ProtocolDetailsTranslator.toSdk(model.getProtocolDetails()))
                        .certificate(model.getCertificate())
                        .identityProviderDetails(
                                IdentityProviderDetailsTranslator.toSdk(model.getIdentityProviderDetails()))
                        .workflowDetails(WorkflowDetailsTranslator.toSdk(model.getWorkflowDetails(), false))
                        .endpointDetails(EndpointDetailsTranslator.toSdk(model.getEndpointDetails(), false, false))
                        .build())
                .build();
    }

    protected static ResourceModel setupSimpleServerModel(String endpointType) {
        EndpointDetails endpointDetails = null;
        if (EndpointType.VPC.name().equals(endpointType)) {
            endpointDetails = getEndpointDetails(Collections.emptyList());
        }
        return ResourceModel.builder()
                .domain(Domain.S3.name())
                .endpointType(endpointType)
                .endpointDetails(endpointDetails)
                .identityProviderType(DEFAULT_IDENTITY_PROVIDER_TYPE)
                .securityPolicyName(DEFAULT_SECURITY_POLICY)
                .protocols(DEFAULT_PROTOCOLS)
                .structuredLogDestinations(Collections.emptyList())
                .build();
    }

    protected static ResourceModel fullyLoadedServerModel() {
        ProtocolDetails protocolDetails = ProtocolDetails.builder()
                .as2Transports(Collections.singletonList("HTTP"))
                .passiveIp("1.1.1.1")
                .setStatOption(SetStatOption.ENABLE_NO_OP.name())
                .tlsSessionResumptionMode(TlsSessionResumptionMode.ENFORCED.name())
                .build();

        IdentityProviderDetails identityProviderDetails = IdentityProviderDetails.builder()
                .sftpAuthenticationMethods(SftpAuthenticationMethods.PUBLIC_KEY_AND_PASSWORD.name())
                .directoryId("dir")
                .function("func")
                .url("url")
                .invocationRole("role")
                .build();

        WorkflowDetails workflowDetails = WorkflowDetails.builder()
                .onUpload(Collections.singletonList(WorkflowDetail.builder()
                        .workflowId("workflowId")
                        .executionRole("executionRole")
                        .build()))
                .onPartialUpload(Collections.singletonList(WorkflowDetail.builder()
                        .workflowId("workflowId")
                        .executionRole("executionRole")
                        .build()))
                .build();

        EndpointDetails endpointDetails = getEndpointDetails(Arrays.asList("addr1", "addr2"));

        return ResourceModel.builder()
                .certificate("certificate")
                .domain(Domain.S3.name())
                .endpointType(EndpointType.VPC.name())
                .identityProviderType(DEFAULT_IDENTITY_PROVIDER_TYPE)
                .securityPolicyName(DEFAULT_SECURITY_POLICY)
                .protocols(DEFAULT_PROTOCOLS)
                .tags(MODEL_TAGS)
                .structuredLogDestinations(Collections.singletonList("FooLog"))
                .loggingRole("loggingRole")
                .preAuthenticationLoginBanner("pre")
                .postAuthenticationLoginBanner("post")
                .protocolDetails(protocolDetails)
                .certificate("cert")
                .identityProviderDetails(identityProviderDetails)
                .workflowDetails(workflowDetails)
                .endpointDetails(endpointDetails)
                .build();
    }

    protected static EndpointDetails getEndpointDetails(List<String> addressAllocationIds) {
        return EndpointDetails.builder()
                .vpcId("vpc")
                .vpcEndpointId("vpcEndpointId")
                .subnetIds(Arrays.asList("sub1", "sub2"))
                .securityGroupIds(Arrays.asList("sec1", "sec2"))
                .addressAllocationIds(addressAllocationIds)
                .build();
    }

    protected void setupVpcEndpointStates(ResourceModel model) {
        if (!EndpointType.VPC.name().equals(model.getEndpointType())) {
            return;
        }
        DescribeVpcEndpointsResponse available = vpcEndpointResponse(model, State.AVAILABLE);
        DescribeVpcEndpointsResponse pending = vpcEndpointResponse(model, State.PENDING);
        lenient()
                .doReturn(pending, available)
                .when(sdkEc2Client)
                .describeVpcEndpoints(any(DescribeVpcEndpointsRequest.class));
    }

    protected static DescribeVpcEndpointsResponse vpcEndpointResponse(ResourceModel model, State state) {
        List<SecurityGroupIdentifier> securityGroupIdentifiers = streamOfOrEmpty(
                        model.getEndpointDetails().getSecurityGroupIds())
                .map(AbstractTestBase::sgFromId)
                .collect(Collectors.toList());

        return DescribeVpcEndpointsResponse.builder()
                .vpcEndpoints(VpcEndpoint.builder()
                        .vpcEndpointId(model.getEndpointDetails().getVpcEndpointId())
                        .groups(securityGroupIdentifiers)
                        .state(state)
                        .build())
                .build();
    }

    private static SecurityGroupIdentifier sgFromId(String id) {
        return SecurityGroupIdentifier.builder().groupId(id).groupName(id).build();
    }

    protected static DescribeServerResponse newStateResponse(DescribeServerResponse template, String state) {
        return DescribeServerResponse.builder()
                .server(template.server().toBuilder().state(state).build())
                .build();
    }

    protected void setupPrivateIpsStates() {
        DescribeAddressesResponse noAddress =
                DescribeAddressesResponse.builder().build();
        DescribeAddressesResponse oneAddress = DescribeAddressesResponse.builder()
                .addresses(Address.builder().privateIpAddress("10.0.0.1").build())
                .build();

        doReturn(noAddress, oneAddress).when(sdkEc2Client).describeAddresses(any(DescribeAddressesRequest.class));
    }

    protected static ResourceHandlerRequest.ResourceHandlerRequestBuilder<ResourceModel>
            getResourceHandlerRequestBuilder() {
        Region region = Region.of("us-east-1");
        return ResourceHandlerRequest.<ResourceModel>builder()
                .region(region.id())
                .awsPartition(PartitionMetadata.of(region).id())
                .awsAccountId("123456789012");
    }

    protected static String getTestServerArn(String serverId) {
        com.amazonaws.regions.Region region;
        region = com.amazonaws.regions.Region.getRegion(Regions.US_EAST_1);
        return new ServerArn(region, "123456789012", serverId).getArn();
    }
}
