package software.amazon.transfer.user;

import static software.amazon.transfer.user.translators.Translator.translateFromSdkHomeDirectoryMappings;
import static software.amazon.transfer.user.translators.Translator.translateFromSdkPosixProfile;
import static software.amazon.transfer.user.translators.Translator.translateFromSdkTags;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.DescribeUserResponse;
import software.amazon.awssdk.services.transfer.model.DescribedUser;
import software.amazon.awssdk.services.transfer.model.SshPublicKey;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.translators.Translator;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        final String clientRequestToken = request.getClientRequestToken();

        Translator.ensureServerIdAndUserNameInModel(request.getDesiredResourceState());

        final String serverId = request.getDesiredResourceState().getServerId();

        return proxy.initiate(
                        "AWS-Transfer-User::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(this::translateToReadRequest)
                .makeServiceCall(this::readUser)
                .handleError((ignored, exception, client, model, context) ->
                        handleError(READ, exception, model, context, clientRequestToken))
                .done(awsResponse ->
                        ProgressEvent.defaultSuccessHandler(translateFromReadResponse(serverId, awsResponse)));
    }

    private ResourceModel translateFromReadResponse(final String serverId, final DescribeUserResponse awsResponse) {
        DescribedUser user = awsResponse.user();
        return ResourceModel.builder()
                .arn(user.arn())
                .serverId(serverId)
                .userName(user.userName())
                .policy(user.policy())
                .role(user.role())
                .homeDirectoryType(user.homeDirectoryTypeAsString())
                .homeDirectory(user.homeDirectory())
                .homeDirectoryMappings(translateFromSdkHomeDirectoryMappings(user.homeDirectoryMappings()))
                .posixProfile(translateFromSdkPosixProfile(user.posixProfile()))
                .sshPublicKeys(translateFromSshPublicKeys(user.sshPublicKeys()))
                .tags(translateFromSdkTags(user.tags()))
                .build();
    }

    private List<String> translateFromSshPublicKeys(List<SshPublicKey> sshPublicKeys) {
        if (sshPublicKeys == null || sshPublicKeys.isEmpty()) {
            return null;
        }

        return sshPublicKeys.stream().map(SshPublicKey::sshPublicKeyBody).collect(Collectors.toList());
    }
}
