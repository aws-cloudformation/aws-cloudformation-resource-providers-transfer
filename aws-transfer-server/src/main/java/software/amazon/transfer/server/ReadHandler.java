package software.amazon.transfer.server;

import static software.amazon.transfer.server.translators.Translator.translateFromSdkTags;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.SecurityGroupIdentifier;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeServerRequest;
import software.amazon.awssdk.services.transfer.model.DescribeServerResponse;
import software.amazon.awssdk.services.transfer.model.DescribedServer;
import software.amazon.awssdk.services.transfer.model.EndpointDetails;
import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.EndpointDetailsTranslator;
import software.amazon.transfer.server.translators.IdentityProviderDetailsTranslator;
import software.amazon.transfer.server.translators.ProtocolDetailsTranslator;
import software.amazon.transfer.server.translators.S3StorageOptionsTranslator;
import software.amazon.transfer.server.translators.Translator;
import software.amazon.transfer.server.translators.WorkflowDetailsTranslator;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final ProxyClient<Ec2Client> proxyEc2Client,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();
        Translator.ensureServerIdInModel(request.getDesiredResourceState());

        return proxy.initiate(
                        "AWS-Transfer-Server::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((r, c) -> readServer(r, c, proxyEc2Client))
                .handleError((_i, e, c, m, ctx) -> handleError(READ, e, m, ctx, clientRequestToken))
                .done(r -> ProgressEvent.defaultSuccessHandler(translateFromReadResponse(r)));
    }

    private ResourceModel translateFromReadResponse(final DescribeServerResponse response) {
        DescribedServer server = response.server();
        return ResourceModel.builder()
                .arn(server.arn())
                // For non AS2-servers, our API returns null for this property.
                // However, the AWS SDK will never return null for a map or collection property in a response.
                // Contract tests require all read-only properties to be returned by a READ request,
                // so we cannot mimic our API's behaviour here. Thus, we return an empty list instead of null.
                .as2ServiceManagedEgressIpAddresses(server.as2ServiceManagedEgressIpAddresses())
                .serverId(server.serverId())
                .certificate(server.certificate())
                .domain(server.domainAsString())
                .endpointType(server.endpointTypeAsString())
                .endpointDetails(EndpointDetailsTranslator.fromSdk(server.endpointDetails()))
                .identityProviderType(server.identityProviderTypeAsString())
                .identityProviderDetails(IdentityProviderDetailsTranslator.fromSdk(server.identityProviderDetails()))
                .loggingRole(server.loggingRole())
                .structuredLogDestinations(server.structuredLogDestinations())
                .preAuthenticationLoginBanner(server.preAuthenticationLoginBanner())
                .postAuthenticationLoginBanner(server.postAuthenticationLoginBanner())
                .protocols(server.protocolsAsStrings())
                .protocolDetails(ProtocolDetailsTranslator.fromSdk(server.protocolDetails()))
                .securityPolicyName(server.securityPolicyName())
                .tags(translateFromSdkTags(server.tags()))
                .workflowDetails(WorkflowDetailsTranslator.fromSdk(server.workflowDetails()))
                .structuredLogDestinations(server.structuredLogDestinations())
                .s3StorageOptions(S3StorageOptionsTranslator.fromSdk(server.s3StorageOptions()))
                .build();
    }

    private DescribeServerResponse readServer(
            DescribeServerRequest request, ProxyClient<TransferClient> client, ProxyClient<Ec2Client> ec2Client) {
        DescribeServerResponse response;
        try (TransferClient transferClient = client.client()) {
            response = client.injectCredentialsAndInvokeV2(request, transferClient::describeServer);
            log("has been read successfully.", request.serverId());
        }
        // Our API does not return assigned security groups but
        // contract tests demand it.
        if (response.server().endpointType() == EndpointType.VPC) {
            return readSecurityGroups(ec2Client, response);
        }
        return response;
    }

    private DescribeServerResponse readSecurityGroups(
            ProxyClient<Ec2Client> ec2Client, DescribeServerResponse response) {
        String vpceId = response.server().endpointDetails().vpcEndpointId();
        List<String> sgIds = getVpcEndpoint(vpceId, ec2Client).groups().stream()
                .map(SecurityGroupIdentifier::groupId)
                .collect(Collectors.toList());
        log(String.format("security group IDs read successfully: %s", sgIds), vpceId);

        EndpointDetails endpointDetails = response.server().endpointDetails().toBuilder()
                .securityGroupIds(sgIds)
                .build();
        DescribedServer server =
                response.server().toBuilder().endpointDetails(endpointDetails).build();
        return DescribeServerResponse.builder().server(server).build();
    }
}
