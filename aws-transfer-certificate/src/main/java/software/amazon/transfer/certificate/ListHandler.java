package software.amazon.transfer.certificate;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.model.InternalServiceErrorException;
import software.amazon.awssdk.services.transfer.model.InvalidRequestException;
import software.amazon.awssdk.services.transfer.model.ListCertificatesRequest;
import software.amazon.awssdk.services.transfer.model.ListCertificatesResponse;
import software.amazon.awssdk.services.transfer.model.TransferException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ListHandler extends BaseHandlerStd {
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<TransferClient> proxyClient,
            final Logger logger) {

        List<ResourceModel> models = new ArrayList<>();

        ListCertificatesRequest listCertificatesRequest = ListCertificatesRequest.builder()
                .maxResults(10)
                .nextToken(request.getNextToken())
                .build();

        try (TransferClient client = proxyClient.client()) {
            ListCertificatesResponse response =
                    proxy.injectCredentialsAndInvokeV2(listCertificatesRequest, client::listCertificates);

            response.certificates().forEach(listedCertificate -> {
                ResourceModel model = ResourceModel.builder()
                        .arn(listedCertificate.arn())
                        .certificateId(listedCertificate.certificateId())
                        .usage(listedCertificate.usageAsString())
                        .status(listedCertificate.statusAsString())
                        .activeDate(
                                listedCertificate.activeDate() != null
                                        ? listedCertificate.activeDate().toString()
                                        : null)
                        .inactiveDate(
                                listedCertificate.inactiveDate() != null
                                        ? listedCertificate.inactiveDate().toString()
                                        : null)
                        .type(listedCertificate.typeAsString())
                        .description(listedCertificate.description())
                        .build();
                models.add(model);
            });

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .nextToken(response.nextToken())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(e.getMessage() + " " + listCertificatesRequest.toString(), e);
        } catch (InternalServiceErrorException e) {
            throw new CfnServiceInternalErrorException("listCertificate", e);
        } catch (TransferException e) {
            throw new CfnGeneralServiceException(e.getMessage(), e);
        }
    }
}
