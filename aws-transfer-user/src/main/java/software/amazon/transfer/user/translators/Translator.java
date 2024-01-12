package software.amazon.transfer.user.translators;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.services.transfer.model.HomeDirectoryMapEntry;
import software.amazon.awssdk.services.transfer.model.TagResourceRequest;
import software.amazon.awssdk.services.transfer.model.UntagResourceRequest;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.user.PosixProfile;
import software.amazon.transfer.user.ResourceModel;
import software.amazon.transfer.user.Tag;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

/**
 * This class is a centralized placeholder for - api request construction - object translation
 * to/from aws sdk - resource model construction for read/list handlers
 */
public final class Translator {
    private Translator() {}

    public static Collection<HomeDirectoryMapEntry> translateToSdkHomeDirectoryMappings(
            List<software.amazon.transfer.user.HomeDirectoryMapEntry> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        return mappings.stream()
                .map(mapping -> HomeDirectoryMapEntry.builder()
                        .entry(mapping.getEntry())
                        .target(mapping.getTarget())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<software.amazon.transfer.user.HomeDirectoryMapEntry> translateFromSdkHomeDirectoryMappings(
            List<HomeDirectoryMapEntry> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        return mappings.stream()
                .map(mapping -> software.amazon.transfer.user.HomeDirectoryMapEntry.builder()
                        .entry(mapping.entry())
                        .target(mapping.target())
                        .build())
                .collect(Collectors.toList());
    }

    public static software.amazon.awssdk.services.transfer.model.PosixProfile translateToSdkPosixProfile(
            PosixProfile posixProfile) {
        if (posixProfile == null) {
            return null;
        }
        return software.amazon.awssdk.services.transfer.model.PosixProfile.builder()
                .gid(posixProfile.getGid().longValue())
                .uid(posixProfile.getUid().longValue())
                .secondaryGids(streamOfOrEmpty(posixProfile.getSecondaryGids())
                        .map(Double::longValue)
                        .collect(Collectors.toList()))
                .build();
    }

    public static PosixProfile translateFromSdkPosixProfile(
            software.amazon.awssdk.services.transfer.model.PosixProfile posixProfile) {
        if (posixProfile == null) {
            return null;
        }
        return PosixProfile.builder()
                .gid(posixProfile.gid().doubleValue())
                .uid(posixProfile.uid().doubleValue())
                .secondaryGids(streamOfOrEmpty(posixProfile.secondaryGids())
                        .map(Long::doubleValue)
                        .collect(Collectors.toList()))
                .build();
    }

    public static List<software.amazon.awssdk.services.transfer.model.Tag> translateToSdkTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .map(tag -> software.amazon.awssdk.services.transfer.model.Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<software.amazon.awssdk.services.transfer.model.Tag> translateToSdkTags(
            Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.entrySet().stream()
                .map(tag -> software.amazon.awssdk.services.transfer.model.Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<Tag> translateFromSdkTags(List<software.amazon.awssdk.services.transfer.model.Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .map(tag -> Tag.builder().key(tag.key()).value(tag.value()).build())
                .collect(Collectors.toList());
    }

    public static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection).map(Collection::stream).orElseGet(Stream::empty);
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

    public static String generateUserArn(ResourceHandlerRequest<ResourceModel> request) {
        Region region = Region.getRegion(Regions.fromName(request.getRegion()));
        return new UserArn(
                        region,
                        request.getAwsAccountId(),
                        request.getDesiredResourceState().getServerId(),
                        request.getDesiredResourceState().getUserName())
                .getArn();
    }

    public static void ensureServerIdAndUserNameInModel(ResourceModel model) {
        if (StringUtils.isBlank(model.getServerId()) || StringUtils.isBlank(model.getUserName())) {
            UserArn userArn = UserArn.fromString(model.getArn());
            model.setServerId(userArn.getServerId());
            model.setUserName(userArn.getUserName());
        }
    }
}
