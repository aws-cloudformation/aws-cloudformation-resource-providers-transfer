package software.amazon.transfer.user;

import static software.amazon.transfer.user.translators.Translator.ensureServerIdAndUserNameInModel;
import static software.amazon.transfer.user.translators.Translator.normalizeSshKeys;
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

        ensureServerIdAndUserNameInModel(request.getDesiredResourceState());

        final String serverId = request.getDesiredResourceState().getServerId();
        final List<String> expectedSshKeys =
                normalizeSshKeys(request.getDesiredResourceState().getSshPublicKeys());

        return proxy.initiate(
                        "AWS-Transfer-User::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(this::translateToReadRequest)
                .makeServiceCall(this::readUser)
                .handleError((ignored, exception, client, model, context) ->
                        handleError(READ, exception, model, context, clientRequestToken))
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(
                        translateFromReadResponse(serverId, awsResponse, expectedSshKeys)));
    }

    private ResourceModel translateFromReadResponse(
            final String serverId, final DescribeUserResponse awsResponse, List<String> expectedSshKeys) {
        DescribedUser user = awsResponse.user();

        // Handle the use case where a user might have SSH keys managed completely out of CloudFormation,
        // but we need to avoid removing them. We also want to avoid marking the User as having a drift, so
        // we make the result omit the extra keys. However, if the returned keys are missing expected CFN managed
        // keys we will allow this to return a list of keys that is incomplete and show the User as having a drift.
        List<String> responseSshKeys = normalizeSshKeys(translateFromSshPublicKeys(user.sshPublicKeys()));
        if (!responseSshKeys.isEmpty()) {
            if (expectedSshKeys.isEmpty()) {
                logger.log(String.format(
                        "User %s has keys, but the request model has no keys, assuming keys not managed by CloudFormation",
                        user.userName()));
                responseSshKeys = null;
            } else {
                responseSshKeys = responseSshKeys.stream()
                        .filter(expectedSshKeys::contains)
                        .collect(Collectors.toList());
                if (responseSshKeys.isEmpty()) {
                    logger.log(String.format(
                            "User %s has no keys that match the request model, possible drift detected",
                            user.userName()));
                    responseSshKeys = null;
                }
            }
        } else {
            responseSshKeys = null;
        }

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
                .sshPublicKeys(responseSshKeys)
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
