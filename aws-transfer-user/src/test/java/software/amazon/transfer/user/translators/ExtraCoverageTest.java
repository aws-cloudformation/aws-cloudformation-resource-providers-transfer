package software.amazon.transfer.user.translators;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.transfer.model.Tag;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.ResourceModel;

public class ExtraCoverageTest {

    @Test
    public void nullCheckingCodeCoverage() {
        assertThat(Translator.translateToSdkTags((Map<String, String>) null)).isNull();
        assertThat(Translator.translateFromSdkTags((List<Tag>) null)).isNull();

        ResourceModel state = ResourceModel.builder().build();
        ImmutableMap<String, String> tags = ImmutableMap.of("a", "b");
        ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder()
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
