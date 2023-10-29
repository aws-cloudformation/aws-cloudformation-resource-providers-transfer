package software.amazon.transfer.server.translators;

import software.amazon.transfer.server.IdentityProviderDetails;

public final class IdentityProviderDetailsTranslator {
    private IdentityProviderDetailsTranslator() {}

    public static IdentityProviderDetails fromSdk(
            software.amazon.awssdk.services.transfer.model.IdentityProviderDetails
                    identityProviderDetails) {
        if (identityProviderDetails == null) {
            return null;
        }
        return IdentityProviderDetails.builder()
                .url(identityProviderDetails.url())
                .invocationRole(identityProviderDetails.invocationRole())
                .directoryId(identityProviderDetails.directoryId())
                .function(identityProviderDetails.function())
                .sftpAuthenticationMethods(
                        identityProviderDetails.sftpAuthenticationMethodsAsString())
                .build();
    }

    public static software.amazon.awssdk.services.transfer.model.IdentityProviderDetails toSdk(
            IdentityProviderDetails identityProviderDetails) {
        if (identityProviderDetails == null) {
            return null;
        }
        return software.amazon.awssdk.services.transfer.model.IdentityProviderDetails.builder()
                .url(identityProviderDetails.getUrl())
                .invocationRole(identityProviderDetails.getInvocationRole())
                .directoryId(identityProviderDetails.getDirectoryId())
                .function(identityProviderDetails.getFunction())
                .sftpAuthenticationMethods(identityProviderDetails.getSftpAuthenticationMethods())
                .build();
    }
}
