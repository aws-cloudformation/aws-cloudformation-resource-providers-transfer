package software.amazon.transfer.certificate;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.transfer.model.Tag;

public class Converter {
    static class TagConverter {
        static Tag toSdk(software.amazon.transfer.certificate.Tag tag) {
            if (tag == null) {
                return null;
            }
            return Tag.builder().key(tag.getKey()).value(tag.getValue()).build();
        }

        static Set<software.amazon.transfer.certificate.Tag> translateTagfromMap(Map<String, String> tags) {
            if (tags == null) {
                return Collections.emptySet();
            }

            return tags.entrySet().stream()
                    .map(tag -> software.amazon.transfer.certificate.Tag.builder()
                            .key(tag.getKey())
                            .value(tag.getValue())
                            .build())
                    .collect(Collectors.toSet());
        }

        static software.amazon.transfer.certificate.Tag fromSdk(Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.transfer.certificate.Tag.builder()
                    .key(tag.key())
                    .value(tag.value())
                    .build();
        }
    }

    static class DateConverter {
        static Instant toSdk(String date) {
            if (date == null) {
                return null;
            }
            return Instant.parse(date);
        }

        static String fromSdk(Instant date) {
            if (date == null) {
                return null;
            }
            return date.toString();
        }
    }
}
