package software.amazon.transfer.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.transfer.model.CreateWebAppRequest;
import software.amazon.awssdk.services.transfer.model.CreateWebAppResponse;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppRequest;
import software.amazon.awssdk.services.transfer.model.DescribeWebAppResponse;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ThrottlingException;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationRequest;
import software.amazon.awssdk.services.transfer.model.UpdateWebAppCustomizationResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private final CreateHandler handler = new CreateHandler();

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = simpleWebAppModel();
        model.setWebAppId(TEST_WEB_APP_ID);
        model.setArn(TEST_ARN);
        model.setAccessEndpoint(TEST_ACCESS_ENDPOINT);

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        setupCreateWebAppResponse();

        DescribeWebAppResponse describeResponse = describeWebAppResponseFromModel(model);
        doReturn(describeResponse).when(client).describeWebApp(any(DescribeWebAppRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(model);
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(client, atLeastOnce()).createWebApp(any(CreateWebAppRequest.class));
        verify(client, atLeastOnce()).describeWebApp(any(DescribeWebAppRequest.class));
    }

    @Test
    public void handleRequest_LoadedSuccess() {
        final ResourceModel model = fullyLoadedWebAppModel();
        model.setWebAppId(TEST_WEB_APP_ID);
        model.setArn(TEST_ARN);

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        setupCreateWebAppResponse();

        UpdateWebAppCustomizationResponse updateResponse = UpdateWebAppCustomizationResponse.builder()
                .webAppId(TEST_WEB_APP_ID)
                .build();
        doReturn(updateResponse).when(client).updateWebAppCustomization(any(UpdateWebAppCustomizationRequest.class));

        DescribeWebAppResponse describeResponse = describeWebAppResponseFromModel(model);

        doReturn(describeResponse).when(client).describeWebApp(any(DescribeWebAppRequest.class));

        doReturn(describeWebAppCustomizationResponseFromModel(model))
                .when(client)
                .describeWebAppCustomization(any(DescribeWebAppCustomizationRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        softly.assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        softly.assertThat(response.getResourceModel()).isEqualTo(model);
        softly.assertThat(response.getResourceModels()).isNull();
        softly.assertThat(response.getMessage()).isNull();
        softly.assertThat(response.getErrorCode()).isNull();

        verify(client, atLeastOnce()).createWebApp(any(CreateWebAppRequest.class));
        verify(client, atLeastOnce()).updateWebAppCustomization(any(UpdateWebAppCustomizationRequest.class));
        verify(client, atLeastOnce()).describeWebApp(any(DescribeWebAppRequest.class));
        verify(client, atLeastOnce()).describeWebAppCustomization(any(DescribeWebAppCustomizationRequest.class));
    }

    @Test
    public void errorPathsTest() {
        final ResourceModel model = simpleWebAppModel();

        final ResourceHandlerRequest<ResourceModel> request =
                requestBuilder().desiredResourceState(model).build();

        setupCreateWebAppResponse();

        Exception ex1 = ThrottlingException.builder().build();
        Exception ex2 = InvalidRequestException.builder().build();

        doThrow(ex1).doThrow(ex2).when(client).describeWebApp(any(DescribeWebAppRequest.class));

        ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);

        response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        softly.assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    private void setupCreateWebAppResponse() {
        CreateWebAppResponse createWebAppResponse =
                CreateWebAppResponse.builder().webAppId(TEST_WEB_APP_ID).build();
        doReturn(createWebAppResponse).when(client).createWebApp(any(CreateWebAppRequest.class));
    }
}
