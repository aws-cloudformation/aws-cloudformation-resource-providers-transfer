package software.amazon.transfer.server.translators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import software.amazon.transfer.server.WorkflowDetail;
import software.amazon.transfer.server.WorkflowDetails;

public class WorkflowDetailsTranslatorTest {
    @Test
    public void verifyCreateBehaviors() {
        var details = WorkflowDetails.builder().build();

        // The default constructed should yield null
        var result = WorkflowDetailsTranslator.toSdk(details, false);
        assertThat(result).isNull();

        // Null also gets null
        result = WorkflowDetailsTranslator.toSdk(null, false);
        assertThat(result).isNull();

        // Empty both also null
        details.setOnUpload(List.of());
        details.setOnPartialUpload(List.of());
        result = WorkflowDetailsTranslator.toSdk(details, false);
        assertThat(result).isNull();

        details.setOnUpload(List.of(WorkflowDetail.builder().build()));
        result = WorkflowDetailsTranslator.toSdk(details, false);
        assertThat(result).isNotNull();
        assertThat(result.hasOnUpload()).isTrue();
        assertThat(result.onUpload()).hasSize(1);
        assertThat(result.hasOnPartialUpload()).isFalse();

        details.setOnUpload(null);
        details.setOnPartialUpload(List.of(WorkflowDetail.builder().build()));
        result = WorkflowDetailsTranslator.toSdk(details, false);
        assertThat(result).isNotNull();
        assertThat(result.hasOnUpload()).isFalse();
        assertThat(result.hasOnPartialUpload()).isTrue();
        assertThat(result.onPartialUpload()).hasSize(1);
    }

    @Test
    public void verifyUpdateBehaviors() {
        var details = WorkflowDetails.builder().build();

        // The default constructed should yield object with empty lists
        var result = WorkflowDetailsTranslator.toSdk(details, true);
        assertThat(result).isNotNull();
        assertThat(result.onUpload()).isEmpty();
        assertThat(result.onPartialUpload()).isEmpty();

        // Null gets empty too
        result = WorkflowDetailsTranslator.toSdk(null, true);
        assertThat(result).isNotNull();
        assertThat(result.onUpload()).isEmpty();
        assertThat(result.onPartialUpload()).isEmpty();

        // Empty both yields empty
        details.setOnUpload(List.of());
        details.setOnPartialUpload(List.of());
        result = WorkflowDetailsTranslator.toSdk(details, true);
        assertThat(result).isNotNull();
        assertThat(result.onUpload()).isEmpty();
        assertThat(result.onPartialUpload()).isEmpty();

        details.setOnUpload(List.of(WorkflowDetail.builder().build()));
        result = WorkflowDetailsTranslator.toSdk(details, true);
        assertThat(result).isNotNull();
        assertThat(result.onUpload()).hasSize(1);
        assertThat(result.onPartialUpload()).isEmpty();

        details.setOnUpload(null);
        details.setOnPartialUpload(List.of(WorkflowDetail.builder().build()));
        result = WorkflowDetailsTranslator.toSdk(details, true);
        assertThat(result).isNotNull();
        assertThat(result.onUpload()).isEmpty();
        assertThat(result.onPartialUpload()).hasSize(1);
    }
}
