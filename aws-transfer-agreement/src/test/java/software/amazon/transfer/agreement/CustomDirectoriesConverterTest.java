package software.amazon.transfer.agreement;

import static software.amazon.transfer.agreement.AbstractTestBase.TEST_CUSTOM_FAILED_DIRECTORY;
import static software.amazon.transfer.agreement.AbstractTestBase.TEST_CUSTOM_MDN_DIRECTORY;
import static software.amazon.transfer.agreement.AbstractTestBase.TEST_CUSTOM_PAYLOAD_DIRECTORY;
import static software.amazon.transfer.agreement.AbstractTestBase.TEST_CUSTOM_STATUS_DIRECTORY;
import static software.amazon.transfer.agreement.AbstractTestBase.TEST_CUSTOM_TEMPORARY_DIRECTORY;
import static software.amazon.transfer.agreement.AbstractTestBase.getCustomDirectories;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import software.amazon.awssdk.services.transfer.model.CustomDirectoriesType;

@ExtendWith(SoftAssertionsExtension.class)
public class CustomDirectoriesConverterTest {
    @InjectSoftAssertions
    BDDSoftAssertions softly;

    @Test
    public void testCustomDirectoriesConverterFromSdk() {
        CustomDirectoriesType customDirectoriesType = getCustomDirectories();
        CustomDirectories customDirectories = Converter.CustomDirectoriesConverter.fromSdk(customDirectoriesType);

        softly.then(customDirectories.getFailedFilesDirectory())
                .isEqualTo(customDirectoriesType.failedFilesDirectory());
        softly.then(customDirectories.getMdnFilesDirectory()).isEqualTo(customDirectoriesType.mdnFilesDirectory());
        softly.then(customDirectories.getPayloadFilesDirectory())
                .isEqualTo(customDirectoriesType.payloadFilesDirectory());
        softly.then(customDirectories.getStatusFilesDirectory())
                .isEqualTo(customDirectoriesType.statusFilesDirectory());
        softly.then(customDirectories.getTemporaryFilesDirectory())
                .isEqualTo(customDirectoriesType.temporaryFilesDirectory());
    }

    @Test
    public void testCustomDirectoriesConverterWithNullFromSdk() {
        CustomDirectories customDirectories = Converter.CustomDirectoriesConverter.fromSdk(null);

        softly.then(customDirectories).isNull();
    }

    @Test
    public void testCustomDirectoriesConverterToSdk() {
        CustomDirectories customDirectories = new CustomDirectories();
        customDirectories.setFailedFilesDirectory(TEST_CUSTOM_FAILED_DIRECTORY);
        customDirectories.setMdnFilesDirectory(TEST_CUSTOM_MDN_DIRECTORY);
        customDirectories.setPayloadFilesDirectory(TEST_CUSTOM_PAYLOAD_DIRECTORY);
        customDirectories.setStatusFilesDirectory(TEST_CUSTOM_STATUS_DIRECTORY);
        customDirectories.setTemporaryFilesDirectory(TEST_CUSTOM_TEMPORARY_DIRECTORY);

        CustomDirectoriesType customDirectoriesType = Converter.CustomDirectoriesConverter.toSdk(customDirectories);

        softly.then(customDirectoriesType.failedFilesDirectory())
                .isEqualTo(customDirectories.getFailedFilesDirectory());
        softly.then(customDirectoriesType.mdnFilesDirectory()).isEqualTo(customDirectories.getMdnFilesDirectory());
        softly.then(customDirectoriesType.payloadFilesDirectory())
                .isEqualTo(customDirectories.getPayloadFilesDirectory());
        softly.then(customDirectoriesType.statusFilesDirectory())
                .isEqualTo(customDirectories.getStatusFilesDirectory());
        softly.then(customDirectoriesType.temporaryFilesDirectory())
                .isEqualTo(customDirectories.getTemporaryFilesDirectory());
    }

    @Test
    public void testCustomDirectoriesConverterWithNullToSdk() {
        CustomDirectoriesType customDirectoriesType = Converter.CustomDirectoriesConverter.toSdk(null);

        softly.then(customDirectoriesType).isNull();
    }
}
