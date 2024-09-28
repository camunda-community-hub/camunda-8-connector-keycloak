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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(AddUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    KeycloakOutput keycloakOutput = new KeycloakOutput();
    try {
      keycloakOutput.userId = keycloakOperation.addUser(keycloakInput.getRealm(), keycloakInput.getUserName(),
          keycloakInput.getUserFirstName(), keycloakInput.getUserLastName(), keycloakInput.getUserEmail(),
          keycloakInput.getUserPassword(), Boolean.TRUE.equals(keycloakInput.getEnabledUser()));

      // add role?
      if (keycloakInput.getUserRoles() != null) {
        List<String> listRoles = Arrays.stream(keycloakInput.getUserRoles().split(",")).collect(Collectors.toList());
        keycloakOperation.addRole(keycloakInput.getRealm(), keycloakOutput.userId, listRoles);

      }
      keycloakOutput.status = "SUCCESS";
      keycloakOutput.dateOperation = new Date();
      return keycloakOutput;
    } catch( ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during KeycloakAddUser on {} {} : {}",keycloakOperation.getKeycloakSignature(),keycloakInput.getUserSignature(), e);
      throw new ConnectorException(KeycloakFunction.ERROR_CREATE_USER, "Error during add-user " + e.getMessage());
    }
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
    return Map.of(KeycloakFunction.ERROR_CREATE_USER, KeycloakFunction.ERROR_CREATE_USER_LABEL);
  }

  @Override
  public String getSubFunctionName() {
    return "addUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Add a user in Keycloak";
  }

  @Override
  public String getSubFunctionType() {
    return "add-user";
  }
}
