package io.camunda.connector.keycloak.user;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;
import io.camunda.connector.keycloak.toolbox.KeycloakOperation;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeleteUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(DeleteUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    // Initialize Keycloak client
    keycloakOperation.deleteUser(keycloakInput.getConnectionRealm(), keycloakInput.getUserId());

    KeycloakOutput keycloakOutput = new KeycloakOutput();
    keycloakOutput.status = "SUCCESS";
    keycloakOutput.dateOperation = new Date();
    return keycloakOutput;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(KeycloakInput.INPUT_USER_REALM, //
                KeycloakInput.INPUT_USER_REALM_LABEL, //
                String.class, //
                KeycloakInput.INPUT_USER_REALM_DEFAULT, //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_USER_REALM_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),  //

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_ID, //
                KeycloakInput.INPUT_USER_ID_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_USER_ID_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())));

  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(KeycloakOutput.OUTPUT_STATUS, //
            KeycloakOutput.OUTPUT_STATUS_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.REQUIRED, //
            KeycloakOutput.OUTPUT_STATUS_EXPLANATION), //

        RunnerParameter.getInstance(KeycloakOutput.OUTPUT_DATE_OPERATION, //
            KeycloakOutput.OUTPUT_DATE_OPERATION_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.REQUIRED, //
            KeycloakOutput.OUTPUT_DATE_OPERATION_EXPLANATION));
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(KeycloakOperation.ERROR_DELETE_USER, KeycloakOperation.ERROR_DELETE_USER_LABEL);
  }

  @Override
  public String getSubFunctionName() {
    return "DeleteUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Delete a user from its ID";
  }

  @Override
  public String getSubFunctionType() {
    return "delete-user";
  }
}