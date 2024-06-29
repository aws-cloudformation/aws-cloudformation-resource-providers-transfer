package software.amazon.transfer.server.translators;

import static software.amazon.transfer.server.translators.Translator.nullIfEmptyList;

import java.util.Collection;

import software.amazon.transfer.server.EndpointDetails;

public final class EndpointDetailsTranslator {
    private EndpointDetailsTranslator() {}

    public static EndpointDetails fromSdk(
            software.amazon.awssdk.services.transfer.model.EndpointDetails endpointDetails) {

        if (endpointDetails == null) {
            return null;
        }
        return EndpointDetails.builder()
                .addressAllocationIds(nullIfEmptyList(endpointDetails.addressAllocationIds()))
                .subnetIds(nullIfEmptyList(endpointDetails.subnetIds()))
                .securityGroupIds(nullIfEmptyList(endpointDetails.securityGroupIds()))
                .vpcId(endpointDetails.vpcId())
                .vpcEndpointId(endpointDetails.vpcEndpointId())
                .build();
    }

    public static software.amazon.awssdk.services.transfer.model.EndpointDetails toSdk(
            EndpointDetails endpointDetails, boolean forCreate, boolean forUpdate) {
        if (endpointDetails == null) {
            return null;
        }

        software.amazon.awssdk.services.transfer.model.EndpointDetails.Builder builder =
                software.amazon.awssdk.services.transfer.model.EndpointDetails.builder()
                        .subnetIds(endpointDetails.getSubnetIds())
                        .vpcId(endpointDetails.getVpcId())
                        .vpcEndpointId(endpointDetails.getVpcEndpointId());

        if (!forCreate) {
            builder.addressAllocationIds(endpointDetails.getAddressAllocationIds());
        } else {
            builder.addressAllocationIds((Collection<String>) null);
        }

        if (!forUpdate) {
            builder.securityGroupIds(endpointDetails.getSecurityGroupIds());
        } else {
            builder.securityGroupIds((Collection<String>) null);
        }

        return builder.build();
    }
}
