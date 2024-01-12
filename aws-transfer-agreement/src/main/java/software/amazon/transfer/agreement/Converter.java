package software.amazon.transfer.agreement;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Converter {

    static class TagConverter {
        static software.amazon.awssdk.services.transfer.model.Tag toSdk(software.amazon.transfer.agreement.Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.awssdk.services.transfer.model.Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
        }

        static Set<software.amazon.transfer.agreement.Tag> translateTagfromMap(Map<String, String> tags) {
            if (tags == null) {
                return Collections.emptySet();
            }

            return tags.entrySet().stream()
                    .map(tag -> software.amazon.transfer.agreement.Tag.builder()
                            .key(tag.getKey())
                            .value(tag.getValue())
                            .build())
                    .collect(Collectors.toSet());
        }

        static software.amazon.transfer.agreement.Tag fromSdk(software.amazon.awssdk.services.transfer.model.Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.transfer.agreement.Tag.builder()
                    .key(tag.key())
                    .value(tag.value())
                    .build();
        }
    }
}
