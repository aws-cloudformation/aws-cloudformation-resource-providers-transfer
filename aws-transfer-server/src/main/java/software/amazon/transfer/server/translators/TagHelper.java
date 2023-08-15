package software.amazon.transfer.server.translators;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.ResourceModel;

public final class TagHelper {
    private TagHelper() {}

    /**
     * shouldUpdateTags
     *
     * <p>Determines whether user defined tags have been changed during update.
     */
    public static final boolean shouldUpdateTags(
            final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> previousTags = getPreviouslyAttachedTags(handlerRequest);
        final Map<String, String> desiredTags = getNewDesiredTags(handlerRequest);
        return ObjectUtils.notEqual(previousTags, desiredTags);
    }

    /**
     * getPreviouslyAttachedTags
     *
     * <p>If stack tags and resource tags are not merged together in Configuration class, we will
     * get previously attached system (with `aws:cloudformation` prefix) and user defined tags from
     * handlerRequest.getPreviousSystemTags() (system tags),
     * handlerRequest.getPreviousResourceTags() (stack tags),
     * handlerRequest.getPreviousResourceState().getTags() (resource tags).
     *
     * <p>System tags are an optional feature. Merge them to your tags if you have enabled them for
     * your resource. System tags can change on resource update if the resource is imported to the
     * stack.
     */
    public static Map<String, String> getPreviouslyAttachedTags(
            final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> previousTags = new HashMap<>();

        if (handlerRequest.getPreviousSystemTags() != null) {
            previousTags.putAll(handlerRequest.getPreviousSystemTags());
        }

        if (handlerRequest.getPreviousResourceTags() != null) {
            previousTags.putAll(handlerRequest.getPreviousResourceTags());
        }

        ResourceModel model = handlerRequest.getPreviousResourceState();
        if (model != null && model.getTags() != null) {
            model.getTags()
                    .forEach(
                            tag -> {
                                previousTags.put(tag.getKey(), tag.getValue());
                            });
        }
        return previousTags;
    }

    /**
     * getNewDesiredTags
     *
     * <p>If stack tags and resource tags are not merged together in Configuration class, we will
     * get new desired system (with `aws:cloudformation` prefix) and user defined tags from
     * handlerRequest.getSystemTags() (system tags), handlerRequest.getDesiredResourceTags() (stack
     * tags), handlerRequest.getDesiredResourceState().getTags() (resource tags).
     *
     * <p>System tags are an optional feature. Merge them to your tags if you have enabled them for
     * your resource. System tags can change on resource update if the resource is imported to the
     * stack.
     */
    public static Map<String, String> getNewDesiredTags(
            final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> desiredTags = new HashMap<>();

        if (handlerRequest.getSystemTags() != null) {
            desiredTags.putAll(handlerRequest.getSystemTags());
        }

        // get desired stack level tags from handlerRequest
        if (handlerRequest.getDesiredResourceTags() != null) {
            desiredTags.putAll(handlerRequest.getDesiredResourceTags());
        }

        if (handlerRequest.getDesiredResourceState().getTags() != null) {
            handlerRequest
                    .getDesiredResourceState()
                    .getTags()
                    .forEach(
                            tag -> {
                                desiredTags.put(tag.getKey(), tag.getValue());
                            });
        }
        return desiredTags;
    }

    /**
     * generateTagsToAdd
     *
     * <p>Determines the tags the customer desired to define or redefine.
     */
    public static Map<String, String> generateTagsToAdd(
            final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        return desiredTags.entrySet().stream()
                .filter(
                        e ->
                                !previousTags.containsKey(e.getKey())
                                        || !Objects.equals(
                                                previousTags.get(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * getTagsToRemove
     *
     * <p>Determines the tags the customer desired to remove from the function.
     */
    public static Set<String> generateTagsToRemove(
            final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        final Set<String> desiredTagNames = desiredTags.keySet();

        return previousTags.keySet().stream()
                .filter(tagName -> !desiredTagNames.contains(tagName))
                .collect(Collectors.toSet());
    }
}
