package software.amazon.transfer.server.translators;

import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.transfer.model.As2Transport;
import software.amazon.transfer.server.ProtocolDetails;

public final class ProtocolDetailsTranslator {
    private ProtocolDetailsTranslator() {}

    public static software.amazon.awssdk.services.transfer.model.ProtocolDetails toSdk(
            ProtocolDetails protocolDetails) {
        if (protocolDetails == null) {
            return null;
        }
        return software.amazon.awssdk.services.transfer.model.ProtocolDetails.builder()
                .passiveIp(protocolDetails.getPassiveIp())
                .tlsSessionResumptionMode(protocolDetails.getTlsSessionResumptionMode())
                .setStatOption(protocolDetails.getSetStatOption())
                .as2Transports(toSdk(protocolDetails.getAs2Transports()))
                .build();
    }

    private static List<As2Transport> toSdk(List<String> as2) {
        if (as2 == null || as2.isEmpty()) {
            return null;
        }
        return as2.stream().map(As2Transport::fromValue).collect(Collectors.toList());
    }

    public static ProtocolDetails fromSdk(
            software.amazon.awssdk.services.transfer.model.ProtocolDetails protocolDetails) {
        if (protocolDetails == null) {
            return null;
        }
        List<String> as2Transports = protocolDetails.as2TransportsAsStrings();
        if (as2Transports != null && as2Transports.isEmpty()) {
            // This can happen with SDK V2, and we do not allow empty in the model
            as2Transports = null;
        }
        return ProtocolDetails.builder()
                .passiveIp(protocolDetails.passiveIp())
                .tlsSessionResumptionMode(protocolDetails.tlsSessionResumptionModeAsString())
                .setStatOption(protocolDetails.setStatOptionAsString())
                .as2Transports(as2Transports)
                .build();
    }
}
