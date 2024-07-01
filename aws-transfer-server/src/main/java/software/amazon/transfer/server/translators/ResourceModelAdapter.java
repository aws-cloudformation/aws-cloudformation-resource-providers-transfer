package software.amazon.transfer.server.translators;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import software.amazon.awssdk.services.transfer.model.EndpointType;
import software.amazon.awssdk.services.transfer.model.IdentityProviderType;
import software.amazon.awssdk.services.transfer.model.Protocol;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.ResourceModel;

public final class ResourceModelAdapter {
    private ResourceModelAdapter() {}

    public static final String DEFAULT_ENDPOINT_TYPE = EndpointType.PUBLIC.name();
    public static final String DEFAULT_IDENTITY_PROVIDER_TYPE = IdentityProviderType.SERVICE_MANAGED.name();

    /**
     * If the customer omits the {@code Protocols} from their {@code Server}, these are the default
     * ones that should be associated with it.
     */
    public static final List<String> DEFAULT_PROTOCOLS = Collections.singletonList(Protocol.SFTP.name());

    /**
     * If the customer omits the {@code SecurityPolicyName} from their {@code Server}, this is the
     * default one that should be associated with it.
     */
    public static final String DEFAULT_SECURITY_POLICY = "TransferSecurityPolicy-2018-11";

    public static void prepareDesiredResourceModel(
            ResourceHandlerRequest<ResourceModel> request, ResourceModel resourceModel, boolean create) {
        setDefaults(resourceModel, create);
        setDesiredTags(request, resourceModel);
    }

    public static void preparePreviousResourceModel(
            ResourceHandlerRequest<ResourceModel> request, ResourceModel resourceModel) {
        setPreviousTags(request, resourceModel);
    }

    private static void setDesiredTags(
            ResourceHandlerRequest<ResourceModel> request, ResourceModel desiredResourceModel) {
        Map<String, String> tagsMap = TagHelper.getNewDesiredTags(request);
        desiredResourceModel.setTags(Translator.translateTagMapToTagList(tagsMap));
    }

    private static void setPreviousTags(
            ResourceHandlerRequest<ResourceModel> request, ResourceModel previousResourceModel) {
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.putAll(Optional.ofNullable(Translator.translateTagListToTagMap(previousResourceModel.getTags()))
                .orElseGet(HashMap::new));
        tagsMap.putAll(Optional.ofNullable(request.getPreviousSystemTags()).orElseGet(HashMap::new));
        previousResourceModel.setTags(Translator.translateTagMapToTagList(tagsMap));
    }

    private static void setDefaults(ResourceModel resourceModel, boolean create) {
        if (resourceModel.getEndpointType() == null) {
            resourceModel.setEndpointType(DEFAULT_ENDPOINT_TYPE);
        }
        if (resourceModel.getIdentityProviderType() == null) {
            resourceModel.setIdentityProviderType(DEFAULT_IDENTITY_PROVIDER_TYPE);
        }
        if (resourceModel.getProtocols() == null) {
            resourceModel.setProtocols(DEFAULT_PROTOCOLS);
        }
        // Handle update differently because of https://i.amazon.com/XFER-10446
        if (create) {
            if (resourceModel.getSecurityPolicyName() == null) {
                resourceModel.setSecurityPolicyName(DEFAULT_SECURITY_POLICY);
            }
        }
    }
}
