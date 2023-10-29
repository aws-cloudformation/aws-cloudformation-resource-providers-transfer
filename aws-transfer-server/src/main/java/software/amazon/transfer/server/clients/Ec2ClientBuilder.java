package software.amazon.transfer.server.clients;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.cloudformation.LambdaWrapper;

/** Create an Ec2Client instance. */
public class Ec2ClientBuilder {
    /**
     * Returns the Ec2Client instance.
     *
     * @return the Ec2Client instance.
     */
    public static Ec2Client getClient() {
        return Ec2Client.builder().httpClient(LambdaWrapper.HTTP_CLIENT).build();
    }
}
