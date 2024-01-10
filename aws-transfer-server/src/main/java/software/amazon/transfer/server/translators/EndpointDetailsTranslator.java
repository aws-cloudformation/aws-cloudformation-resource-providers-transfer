package software.amazon.transfer.server.translators;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import software.amazon.transfer.server.EndpointDetails;

public final class EndpointDetailsTranslator {
    private EndpointDetailsTranslator() {}

    public static EndpointDetails fromSdk(
            software.amazon.awssdk.services.transfer.model.EndpointDetails endpointDetails) {

        if (endpointDetails == null) {
            return null;
        }
        return EndpointDetails.builder()
                .addressAllocationIds(Optional.ofNullable(endpointDetails.addressAllocationIds())
                        .orElse(Collections.emptyList()))
                .subnetIds(Optional.ofNullable(endpointDetails.subnetIds()).orElse(Collections.emptyList()))
                .securityGroupIds(
                        Optional.ofNullable(endpointDetails.securityGroupIds()).orElse(Collections.emptyList()))
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
