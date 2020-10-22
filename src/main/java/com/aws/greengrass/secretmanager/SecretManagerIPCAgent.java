/*
 * Copyright Amazon.com Inc. or its affiliates.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.secretmanager;

import lombok.AccessLevel;
import lombok.Setter;
import software.amazon.awssdk.aws.greengrass.GeneratedAbstractGetSecretValueOperationHandler;
import software.amazon.awssdk.aws.greengrass.model.GetSecretValueRequest;
import software.amazon.awssdk.aws.greengrass.model.GetSecretValueResponse;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandlerContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

import javax.inject.Inject;

/**
 * Class to handle business logic for all SecretManager requests over IPC.
 */
public class SecretManagerIPCAgent {

    @Inject
    @Setter(AccessLevel.PACKAGE)
    SecretManagerService secretManagerService;

    public GetSecretValueOperationHandler getSecretValueOperationHandler(OperationContinuationHandlerContext context) {
        return new GetSecretValueOperationHandler(context);
    }

    class GetSecretValueOperationHandler extends GeneratedAbstractGetSecretValueOperationHandler {
        private final String serviceName;

        protected GetSecretValueOperationHandler(OperationContinuationHandlerContext context) {
            super(context);
            serviceName = context.getAuthenticationData().getIdentityLabel();
        }

        @Override
        protected void onStreamClosed() {

        }

        @Override
        public GetSecretValueResponse handleRequest(GetSecretValueRequest request) {
            return secretManagerService.handleIPCRequest(request, serviceName);
        }

        @Override
        public void handleStreamEvent(EventStreamJsonMessage streamRequestEvent) {

        }

    }
}