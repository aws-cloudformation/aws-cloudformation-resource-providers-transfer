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

import software.amazon.awssdk.services.transfer.model.DescribeWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private final ReadHandler handler = new ReadHandler();

    private void assertSuccessfulResponse(
            ProgressEvent<ResourceModel, CallbackContext> response, ResourceHandlerRequest<ResourceModel> request) {
        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(client, atLeastOnce()).describeWebApp(any(DescribeWebAppRequest.class));
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        ResourceModel model = simpleWebAppModel();
        model.setWebAppId(TEST_WEB_APP_ID);
        model.setArn(TEST_ARN);
        model.setAccessEndpoint(TEST_ACCESS_ENDPOINT);

        ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        doReturn(describeWebAppResponseFromModel(model)).when(client).describeWebApp(any(DescribeWebAppRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertSuccessfulResponse(response, request);
        softly.assertThat(response.getResourceModel().getArn().equals(TEST_ARN));
    }

    @Test
    public void handleRequest_LoadedSuccess() {
        ResourceModel model = fullyLoadedWebAppModel();
        model.setWebAppId(TEST_WEB_APP_ID);
        model.setArn(TEST_ARN);

        ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        doReturn(describeWebAppResponseFromModel(model)).when(client).describeWebApp(any(DescribeWebAppRequest.class));

        doReturn(describeWebAppCustomizationResponseFromModel(model))
                .when(client)
                .describeWebAppCustomization(any(DescribeWebAppCustomizationRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertSuccessfulResponse(response, request);
        verify(client, atLeastOnce()).describeWebAppCustomization(any(DescribeWebAppCustomizationRequest.class));
        softly.assertThat(response.getResourceModel().getArn().equals(TEST_ARN));
    }
}
