package software.amazon.transfer.connector;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.util.CollectionUtils;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-transfer-connector.json");
    }

    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (CollectionUtils.isNullOrEmpty(resourceModel.getTags())) {
            return Collections.emptyMap();
        }

        return resourceModel.getTags().stream()
                .collect(Collectors.toMap(Tag::getKey, Tag::getValue, (value1, value2) -> value2));
    }
}
