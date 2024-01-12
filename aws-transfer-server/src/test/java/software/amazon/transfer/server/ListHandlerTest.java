package software.amazon.transfer.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.ListServersRequest;
import software.amazon.awssdk.services.transfer.model.ListServersResponse;
import software.amazon.awssdk.services.transfer.model.ListedServer;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        String arn = getTestServerArn("testServerId");
        ListServersResponse listServersResponse = ListServersResponse.builder()
                .servers(
                        ListedServer.builder().arn(arn).serverId("testServerId").build())
                .build();
        doReturn(listServersResponse).when(sdkClient).listServers(any(ListServersRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).size().isEqualTo(1);
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackContext()).isNull();
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        ResourceModel result = response.getResourceModels().get(0);
        softly.assertThat(result.getArn()).isEqualTo(arn);
        softly.assertThat(result.getServerId()).isEqualTo("testServerId");

        verify(sdkClient, atLeastOnce()).listServers(any(ListServersRequest.class));
    }
}
