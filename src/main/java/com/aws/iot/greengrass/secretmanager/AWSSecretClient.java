package com.aws.iot.greengrass.secretmanager;

import com.aws.iot.evergreen.tes.LazyCredentialProvider;
import com.aws.iot.evergreen.util.Utils;
import com.aws.iot.greengrass.secretmanager.exception.SecretManagerException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InternalServiceErrorException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import javax.inject.Inject;

public class AWSSecretClient {

    private final SecretsManagerClient secretsManagerClient;

    /**
     * Constructor which utilizes TES for initializing AWS client.
     * @param credentialProvider TES credential provider
     */
    @Inject
    public AWSSecretClient(LazyCredentialProvider credentialProvider) {
        this.secretsManagerClient = SecretsManagerClient.builder().credentialsProvider(credentialProvider)
                .build();
    }

    // Constructor used for testing.
    AWSSecretClient(SecretsManagerClient secretsManager) {
        this.secretsManagerClient = secretsManager;
    }

    /**
     * Fetch secret from AWS cloud.
     * @param request AWS request for fetching secret from cloud
     * @return AWS secret response
     * @throws SecretManagerException If there is a problem fetching secret
     */
    public GetSecretValueResponse getSecret(GetSecretValueRequest request) throws SecretManagerException {
        // TODO: Add retry for fetches
        String errorMsg = String.format("Exception occurred while fetching secrets from AWSSecretsManager for key %s",
                request.secretId());
        try {
            validateInput(request);
            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            validateResponse(response);
            return response;
        } catch (InternalServiceErrorException
                | DecryptionFailureException
                | ResourceNotFoundException
                | InvalidParameterException
                | InvalidRequestException
                | IllegalArgumentException e) {
            // TODO: Separate out network errors for retry and try hard for secret download in that case.
            throw new SecretManagerException(errorMsg);
        }
    }

    private void validateResponse(GetSecretValueResponse response) throws IllegalArgumentException {
        String errorStr = "Invalid secret response, %s is missing";
        if (Utils.isEmpty(response.versionId())) {
            throw new IllegalArgumentException(String.format(errorStr, "version Id"));
        }
        if (Utils.isEmpty(response.arn())) {
            throw new IllegalArgumentException(String.format(errorStr, "arn"));
        }
        if (Utils.isEmpty(response.name())) {
            throw new IllegalArgumentException(String.format(errorStr, "name"));
        }
        if (response.createdDate() == null) {
            throw new IllegalArgumentException(String.format(errorStr, "created date"));
        }
        if (!response.hasVersionStages() || response.versionStages().isEmpty()) {
            throw new IllegalArgumentException(String.format(errorStr, "version stages"));
        }
        if (Utils.isEmpty(response.secretString()) && response.secretBinary() == null) {
            throw new IllegalArgumentException(String.format(errorStr, "both secret string and binary"));
        }
    }

    private void validateInput(GetSecretValueRequest request) throws IllegalArgumentException {
        if (Utils.isEmpty(request.secretId())) {
            throw new IllegalArgumentException("invalid secret request, secret id is required");
        }

        if (Utils.isEmpty(request.versionId()) && Utils.isEmpty(request.versionStage())) {
            throw new IllegalArgumentException("invalid secret request, either version Id or stage is required");
        }
    }
}
