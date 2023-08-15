package software.amazon.transfer.server.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ServerArnTest {
    @Test
    public void testFromString() {
        String arn = "arn:aws:transfer:us-east-1:123456789012:server/s-123456789012";
        ServerArn serverArn = ServerArn.fromString(arn);
        assertEquals(arn, serverArn.getArn());
        assertEquals("transfer", serverArn.getVendor());
        assertEquals("us-east-1", serverArn.getRegion().getName());
        assertEquals("123456789012", serverArn.getAccountId());
        assertEquals("server", serverArn.getResourceType());
        assertEquals("s-123456789012", serverArn.getServerId());
        assertEquals("s-123456789012", serverArn.getResourceId());
    }
}
