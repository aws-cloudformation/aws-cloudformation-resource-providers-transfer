package software.amazon.transfer.webapp;

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

import software.amazon.awssdk.services.transfer.model.ListWebAppsRequest;
import software.amazon.awssdk.services.transfer.model.ListWebAppsResponse;
import software.amazon.awssdk.services.transfer.model.ListedWebApp;
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

        final ResourceModel model = simpleWebAppModel();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        ListWebAppsResponse listWebAppsResponse = ListWebAppsResponse.builder()
                .webApps(ListedWebApp.builder()
                        .arn(TEST_ARN)
                        .webAppId(TEST_WEB_APP_ID)
                        .build())
                .build();
        doReturn(listWebAppsResponse).when(client).listWebApps(any(ListWebAppsRequest.class));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

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
        softly.assertThat(result.getArn()).isEqualTo(TEST_ARN);
        softly.assertThat(result.getWebAppId()).isEqualTo(TEST_WEB_APP_ID);

        verify(client, atLeastOnce()).listWebApps(any(ListWebAppsRequest.class));
    }
}
