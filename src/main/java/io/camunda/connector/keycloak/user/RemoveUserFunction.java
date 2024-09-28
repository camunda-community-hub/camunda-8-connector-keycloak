package io.camunda.connector.keycloak.user;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakFunction;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;
import io.camunda.connector.keycloak.toolbox.KeycloakOperation;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RemoveUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(RemoveUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    // Initialize Keycloak client
    keycloakOperation.deleteUser(keycloakInput.getRealm(), keycloakInput.getUserId());

    KeycloakOutput keycloakOutput = new KeycloakOutput();
    keycloakOutput.status = "SUCCESS";
    keycloakOutput.dateOperation = new Date();
    return keycloakOutput;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return List.of();
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return List.of();
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(KeycloakFunction.ERROR_REMOVE_USER, KeycloakFunction.ERROR_REMOVE_USER_LABEL);
  }

  @Override
  public String getSubFunctionName() {
    return "RemoveUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Remove a user from its ID";
  }

  @Override
  public String getSubFunctionType() {
    return "remove-user";
  }
}