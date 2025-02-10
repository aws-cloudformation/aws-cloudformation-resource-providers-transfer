package software.amazon.transfer.webapp.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class WebAppArnTest {

    @Test
    public void testFromString() {
        String arn = "arn:aws:transfer:us-east-1:123456789012:webapp/webapp-123456789012";
        WebAppArn webAppArn = WebAppArn.fromString(arn);
        assertEquals(arn, webAppArn.getArn());
        assertEquals("transfer", webAppArn.getVendor());
        assertEquals("us-east-1", webAppArn.getRegion().getName());
        assertEquals("123456789012", webAppArn.getAccountId());
        assertEquals("webapp", webAppArn.getResourceType());
        assertEquals("webapp-123456789012", webAppArn.getWebAppId());
        assertEquals("webapp-123456789012", webAppArn.getResourceId());
    }
}
