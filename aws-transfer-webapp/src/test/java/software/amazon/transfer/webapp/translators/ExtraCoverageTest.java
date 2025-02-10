package software.amazon.transfer.webapp.translators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.transfer.model.Tag;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.webapp.ResourceModel;

public class ExtraCoverageTest {
    @Test
    public void nullCheckingCodeCoverage() {
        assertThat(Translator.translateToSdkTags((Map<String, String>) null)).isNull();
        assertThat(Translator.translateFromSdkTags((List<Tag>) null)).isNull();
        assertThat(Translator.translateTagMapToTagList((Map<String, String>) null))
                .isNull();
        assertThat(Translator.translateTagListToTagMap((List<software.amazon.transfer.webapp.Tag>) null))
                .isNull();
        assertThat(Translator.nullIfEmptyList((List<Tag>) null)).isNull();
        assertThat(Translator.emptyListIfNull((List<Tag>) null)).isEmpty();
        assertThat(Translator.emptyStringIfNull((String) null)).isEmpty();

        ResourceModel state = ResourceModel.builder().build();
        ImmutableMap<String, String> tags = ImmutableMap.of("a", "b");
        ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(state)
                .desiredResourceState(state)
                .previousResourceTags(tags)
                .desiredResourceTags(tags)
                .previousSystemTags(tags)
                .systemTags(tags)
                .build();
        assertThat(TagHelper.getNewDesiredTags(request)).isEqualTo(tags);
        assertThat(TagHelper.getPreviouslyAttachedTags(request)).isEqualTo(tags);
    }
}
