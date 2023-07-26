package software.amazon.transfer.connector;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.model.As2ConnectorConfig;
import software.amazon.awssdk.services.transfer.model.SftpConnectorConfig;

public class Converter {
    static class TagConverter {
        static software.amazon.awssdk.services.transfer.model.Tag toSdk(
                software.amazon.transfer.connector.Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
        }

        static Set<software.amazon.transfer.connector.Tag> translateTagfromMap(Map<String, String> tags) {
            if (tags == null) {
                return Collections.emptySet();
            }

            return tags.entrySet()
                    .stream()
                    .map(tag -> software.amazon.transfer.connector.Tag.builder()
                            .key(tag.getKey())
                            .value(tag.getValue())
                            .build())
                    .collect(Collectors.toSet());
        }

        static software.amazon.transfer.connector.Tag fromSdk(software.amazon.awssdk.services.transfer.model.Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.transfer.connector.Tag.builder()
                    .key(tag.key())
                    .value(tag.value())
                    .build();
        }
    }

    static class As2ConfigConverter {
        static As2ConnectorConfig toSdk(
                software.amazon.transfer.connector.As2Config as2Config) {
            if (as2Config == null) {
                return null;
            }

            return As2ConnectorConfig.builder()
                    .localProfileId(as2Config.getLocalProfileId())
                    .partnerProfileId(as2Config.getPartnerProfileId())
                    .messageSubject(as2Config.getMessageSubject())
                    .compression(as2Config.getCompression())
                    .encryptionAlgorithm(as2Config.getEncryptionAlgorithm())
                    .signingAlgorithm(as2Config.getSigningAlgorithm())
                    .mdnSigningAlgorithm(as2Config.getMdnSigningAlgorithm())
                    .mdnResponse(as2Config.getMdnResponse())
                    .basicAuthSecretId(as2Config.getBasicAuthSecretId())
                    .build();

        }

        static software.amazon.transfer.connector.As2Config fromSdk(As2ConnectorConfig as2ConnectorConfig) {
            if (as2ConnectorConfig == null) {
                return null;
            }
            software.amazon.transfer.connector.As2Config modelAs2Config = new software.amazon.transfer.connector.As2Config();

            modelAs2Config.setLocalProfileId(as2ConnectorConfig.localProfileId());
            modelAs2Config.setPartnerProfileId(as2ConnectorConfig.partnerProfileId());
            modelAs2Config.setMessageSubject(as2ConnectorConfig.messageSubject());
            modelAs2Config.setCompression(as2ConnectorConfig.compressionAsString());
            modelAs2Config.setEncryptionAlgorithm(as2ConnectorConfig.encryptionAlgorithmAsString());
            modelAs2Config.setSigningAlgorithm(as2ConnectorConfig.signingAlgorithmAsString());
            modelAs2Config.setMdnSigningAlgorithm(as2ConnectorConfig.mdnSigningAlgorithmAsString());
            modelAs2Config.setMdnResponse(as2ConnectorConfig.mdnResponseAsString());
            modelAs2Config.setBasicAuthSecretId(as2ConnectorConfig.basicAuthSecretId());

            return modelAs2Config;
        }
    }

    static class SftpConfigConverter {
        static SftpConnectorConfig toSdk(
                software.amazon.transfer.connector.SftpConfig sftpConfig) {
            if (sftpConfig == null) {
                return null;
            }

            return SftpConnectorConfig.builder()
                    .userSecretId(sftpConfig.getUserSecretId())
                    .trustedHostKeys(sftpConfig.getTrustedHostKeys())
                    .build();

        }

        static software.amazon.transfer.connector.SftpConfig fromSdk(SftpConnectorConfig sftpConnectorConfig) {
            if (sftpConnectorConfig == null) {
                return null;
            }
            software.amazon.transfer.connector.SftpConfig modelSftpConfig = new software.amazon.transfer.connector.SftpConfig();

            modelSftpConfig.setUserSecretId(sftpConnectorConfig.userSecretId());
            modelSftpConfig.setTrustedHostKeys(sftpConnectorConfig.trustedHostKeys());

            return modelSftpConfig;
        }
    }
}
