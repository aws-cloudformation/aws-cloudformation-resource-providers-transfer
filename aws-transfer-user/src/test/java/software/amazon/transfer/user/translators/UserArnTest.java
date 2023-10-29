package software.amazon.transfer.user.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UserArnTest {
    @Test
    public void testFromString() {
        String arn = "arn:aws:transfer:us-east-1:123456789012:user/s-123456789012/avolanis";
        UserArn userArn = UserArn.fromString(arn);
        assertEquals(arn, userArn.getArn());
        assertEquals("transfer", userArn.getVendor());
        assertEquals("us-east-1", userArn.getRegion().getName());
        assertEquals("123456789012", userArn.getAccountId());
        assertEquals("user", userArn.getResourceType());
        assertEquals("s-123456789012", userArn.getServerId());
        assertEquals("avolanis", userArn.getUserName());
        assertEquals("s-123456789012/avolanis", userArn.getResourceId());
    }
}
