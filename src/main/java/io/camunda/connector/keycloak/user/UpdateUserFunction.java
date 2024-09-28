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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(UpdateUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    KeycloakOutput keycloakOutput = new KeycloakOutput();
    try {
      Map<KeycloakOperation.UserProperties, Object> updateProperties = new HashMap<>();
      if (containsInformation(keycloakInput.getUserName()))
        updateProperties.put(KeycloakOperation.UserProperties.USERNAME, keycloakInput.getUserName());
      if (containsInformation(keycloakInput.getUserFirstName()))
        updateProperties.put(KeycloakOperation.UserProperties.FIRSTNAME, keycloakInput.getUserFirstName());
      if (containsInformation(keycloakInput.getUserLastName()))
        updateProperties.put(KeycloakOperation.UserProperties.LASTNAME, keycloakInput.getUserLastName());
      if (containsInformation(keycloakInput.getUserEmail()))
        updateProperties.put(KeycloakOperation.UserProperties.EMAIL, keycloakInput.getUserEmail());
      if (containsInformation(keycloakInput.getEnabledUser()))
        updateProperties.put(KeycloakOperation.UserProperties.ENABLED, keycloakInput.getEnabledUser());

      if (! updateProperties.isEmpty())
        keycloakOperation.updateUser(keycloakInput.getRealm(), keycloakInput.getUserId(), updateProperties);

      if (containsInformation(keycloakInput.getUserPassword()))
        keycloakOperation.setPassword(keycloakInput.getRealm(), keycloakInput.getUserId(), keycloakInput.getUserPassword());

      // add role?
      if (keycloakInput.getUserRoles() != null) {
        List<String> listRoles = Arrays.stream(keycloakInput.getUserRoles().split(",")).collect(Collectors.toList());
        keycloakOperation.updateRole(keycloakInput.getRealm(), keycloakOutput.userId, listRoles);
      }

      keycloakOutput.status = "SUCCESS";
      keycloakOutput.dateOperation = new Date();
      return keycloakOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during UpdateUser on {} {} : {}", keycloakOperation.getKeycloakSignature(),
          keycloakInput.getUserSignature(), e);
      throw new ConnectorException(KeycloakFunction.ERROR_CREATE_USER, "Error during update-user " + e.getMessage());
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
    return "updateUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Update a user in Keycloak";
  }

  @Override
  public String getSubFunctionType() {
    return "update-user";
  }

  private boolean containsInformation(Object value) {
    if (value == null)
      return false;
    if (value instanceof String valueSt && valueSt.trim().isEmpty())
      return false;
    return true;
  }
}