package software.amazon.transfer.server.translators;

import software.amazon.transfer.server.S3StorageOptions;

public final class S3StorageOptionsTranslator {
    private S3StorageOptionsTranslator() {}

    public static software.amazon.awssdk.services.transfer.model.S3StorageOptions toSdk(
            S3StorageOptions s3StorageOptions) {
        if (s3StorageOptions == null) {
            return null;
        }

        var builder = software.amazon.awssdk.services.transfer.model.S3StorageOptions.builder();
        builder.directoryListingOptimization(s3StorageOptions.getDirectoryListingOptimization());
        return builder.build();
    }

    public static S3StorageOptions fromSdk(
            software.amazon.awssdk.services.transfer.model.S3StorageOptions s3StorageOptions) {
        if (s3StorageOptions == null || s3StorageOptions.directoryListingOptimization() == null) {
            return null;
        }
        return S3StorageOptions.builder()
                .directoryListingOptimization(
                        s3StorageOptions.directoryListingOptimization().name())
                .build();
    }
}
