package software.amazon.transfer.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.DeleteServerRequest;
import software.amazon.awssdk.services.transfer.model.DeleteServerResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.transfer.server.translators.ServerArn;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {
        final DeleteHandler handler = new DeleteHandler();

        Region region = Region.getRegion(Regions.US_EAST_1);
        ServerArn serverArn = new ServerArn(region, "123456789012", "s-123456789012");
        final ResourceModel model =
                ResourceModel.builder().arn(serverArn.getArn()).build();

        final ResourceHandlerRequest<ResourceModel> request =
                getResourceHandlerRequestBuilder().desiredResourceState(model).build();

        DeleteServerResponse deleteServerResponse =
                DeleteServerResponse.builder().build();

        doReturn(deleteServerResponse).when(sdkClient).deleteServer(any(DeleteServerRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, proxyEc2Client, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).deleteServer(any(DeleteServerRequest.class));
    }
}
