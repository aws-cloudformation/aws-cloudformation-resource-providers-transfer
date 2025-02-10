package software.amazon.transfer.webapp;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.transfer.model.Tag;

public class Converter {
    static class TagConverter {
        static Tag toSdk(software.amazon.transfer.webapp.Tag tag) {
            if (tag == null) {
                return null;
            }
            return Tag.builder().key(tag.getKey()).value(tag.getValue()).build();
        }

        static List<software.amazon.transfer.webapp.Tag> translateTagfromMap(Map<String, String> tags) {
            if (tags == null) {
                return List.of();
            }

            return tags.entrySet().stream()
                    .map(tag -> software.amazon.transfer.webapp.Tag.builder()
                            .key(tag.getKey())
                            .value(tag.getValue())
                            .build())
                    .toList();
        }

        static software.amazon.transfer.webapp.Tag fromSdk(Tag tag) {
            if (tag == null) {
                return null;
            }
            return software.amazon.transfer.webapp.Tag.builder()
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
