package software.amazon.transfer.agreement;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Converter {

    static class TagConverter {
        static Tag toSdk(
                Tag tag) {
            if (tag == null) {
                return null;
            }
            return Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
        }

        static Set<Tag> translateTagfromMap(Map<String, String> tags) {
            if (tags == null) {
                return Collections.emptySet();
            }

            return tags.entrySet()
                    .stream()
                    .map(tag -> Tag.builder()
                            .key(tag.getKey())
                            .value(tag.getValue())
                            .build())
                    .collect(Collectors.toSet());
        }

        static Tag fromSdk(Tag tag) {
            if (tag == null) {
                return null;
            }
            return Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .build();
        }
    }
}
