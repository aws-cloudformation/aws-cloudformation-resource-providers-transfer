package software.amazon.transfer.server.translators;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.services.transfer.model.DescribeServerRequest;
import software.amazon.awssdk.services.transfer.model.Protocol;
import software.amazon.awssdk.services.transfer.model.TagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UntagResourceRequest;
import software.amazon.transfer.server.ResourceModel;
import software.amazon.transfer.server.Tag;

/**
 * This class is a centralized placeholder for - api request construction - object translation
 * to/from aws sdk - resource model construction for read/list handlers
 */
public final class Translator {
    private Translator() {}

    public static Collection<Protocol> translateToSdkProtocols(List<String> protocols) {
        return streamOfOrEmpty(protocols).map(Protocol::fromValue).collect(Collectors.toList());
    }

    public static List<software.amazon.awssdk.services.transfer.model.Tag> translateToSdkTags(List<Tag> tags) {
        return streamOfOrEmpty(tags)
                .map(tag -> software.amazon.awssdk.services.transfer.model.Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<software.amazon.awssdk.services.transfer.model.Tag> translateToSdkTags(
            Map<String, String> tags) {
        if (tags == null) {
            return null;
        }
        return tags.entrySet().stream()
                .map(tag -> software.amazon.awssdk.services.transfer.model.Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    public static DescribeServerRequest translateToReadRequest(final ResourceModel model) {
        return DescribeServerRequest.builder().serverId(model.getServerId()).build();
    }

    public static List<Tag> translateFromSdkTags(List<software.amazon.awssdk.services.transfer.model.Tag> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream()
                .map(tag -> Tag.builder().key(tag.key()).value(tag.value()).build())
                .collect(Collectors.toList());
    }

    public static List<Tag> translateTagMapToTagList(Map<String, String> tagMap) {
        if (tagMap == null) {
            return null;
        }
        return tagMap.entrySet().stream()
                .map(entry -> Tag.builder()
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static Map<String, String> translateTagListToTagMap(List<Tag> tagList) {
        if (tagList == null) {
            return null;
        }
        return tagList.stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    public static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        if (collection == null) {
            return Stream.empty();
        }
        return collection.stream();
    }

    public static String emptyStringIfNull(String nullableString) {
        if (nullableString == null) {
            return "";
        }
        return nullableString;
    }

    public static <T> List<T> nullIfEmptyList(final List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    public static <T> List<T> emptyListIfNull(final List<T> list) {
        if (list == null) {
            return List.of();
        }
        return list;
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    public static TagResourceRequest tagResourceRequest(
            final ResourceModel model, final Map<String, String> addedTags) {
        List<software.amazon.awssdk.services.transfer.model.Tag> tagsToAdd = translateToSdkTags(addedTags);
        return TagResourceRequest.builder().arn(model.getArn()).tags(tagsToAdd).build();
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    public static UntagResourceRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
        return UntagResourceRequest.builder()
                .arn(model.getArn())
                .tagKeys(removedTags)
                .build();
    }

    public static void ensureServerIdInModel(ResourceModel model) {
        if (StringUtils.isBlank(model.getServerId())) {
            ServerArn serverArn = ServerArn.fromString(model.getArn());
            model.setServerId(serverArn.getServerId());
        }
    }
}
