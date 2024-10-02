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
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(UpdateUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    KeycloakOutput keycloakOutput = new KeycloakOutput();
    String trace = " userId[" + keycloakInput.getUserId() + "]";
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
      if (containsInformation(keycloakInput.getUserEnabled()))
        updateProperties.put(KeycloakOperation.UserProperties.ENABLED, keycloakInput.getUserEnabled());
      if (!updateProperties.isEmpty()) {
        trace += "Update: " + updateProperties.toString() + ";";
        keycloakOperation.updateUser(keycloakInput.getUserRealm(), keycloakInput.getUserId(), updateProperties);
      }
      if (containsInformation(keycloakInput.getUserPassword())) {
        trace += "Update Password;";
        keycloakOperation.setPassword(keycloakInput.getConnectionRealm(), keycloakInput.getUserId(),
            keycloakInput.getUserPassword());
      }
      // add role?
      if (keycloakInput.getUserRoles() != null) {
        trace += "Update newRoles:[" + keycloakInput.getUserRoles() + "];";
        Set<String> rolesSet = Arrays.stream(keycloakInput.getUserRoles().split(",")).collect(Collectors.toSet());
        keycloakOperation.updateRoles(keycloakInput.getUserRealm(), keycloakInput.getUserId(), rolesSet);
      }
      logger.info("KeycloakUpdateUser: {}", trace);
      keycloakOutput.status = "SUCCESS";
      keycloakOutput.dateOperation = new Date();
      return keycloakOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during UpdateUser on {} {} {}: {}", keycloakOperation.getKeycloakSignature(),
          keycloakInput.getUserSignature(), trace, e);
      throw new ConnectorException(KeycloakOperation.ERROR_UPDATE_USER, "Error during update-user " + e.getMessage());
    }
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(KeycloakInput.INPUT_USER_REALM, //
            KeycloakInput.INPUT_USER_REALM_LABEL, //
            String.class, //
            KeycloakInput.INPUT_USER_REALM_DEFAULT, //
            RunnerParameter.Level.REQUIRED, //
            KeycloakInput.INPUT_USER_REALM_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_ID, //
            KeycloakInput.INPUT_USER_ID_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.REQUIRED, //
            KeycloakInput.INPUT_USER_ID_EXPLANATION),//

        // NO Username for the moment
        /*
        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_NAME, //
                KeycloakInput.INPUT_USER_NAME_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_USER_NAME_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),
*/

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_FIRSTNAME, //
            KeycloakInput.INPUT_USER_FIRSTNAME_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_FIRSTNAME_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_LASTNAME, //
            KeycloakInput.INPUT_USER_LASTNAME_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_LASTNAME_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_EMAIL, //
            KeycloakInput.INPUT_USER_EMAIL_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_EMAIL_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_ENABLED, //
            KeycloakInput.INPUT_USER_ENABLED_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_ENABLED_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_PASSWORD, //
            KeycloakInput.INPUT_USER_PASSWORD_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_PASSWORD_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_ROLES, //
            KeycloakInput.INPUT_USER_ROLES_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_ROLES_EXPLANATION)//
    );

  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(KeycloakOutput.OUTPUT_STATUS, //
            KeycloakOutput.OUTPUT_STATUS_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakOutput.OUTPUT_STATUS_EXPLANATION), //

        RunnerParameter.getInstance(KeycloakOutput.OUTPUT_DATE_OPERATION, //
            KeycloakOutput.OUTPUT_DATE_OPERATION_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakOutput.OUTPUT_DATE_OPERATION_EXPLANATION)); //
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(KeycloakOperation.ERROR_KEYCLOAK_CONNECTION, KeycloakOperation.ERROR_KEYCLOAK_CONNECTION_LABEL, //
        KeycloakFunction.ERROR_UNKNOWN_FUNCTION, KeycloakFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
        KeycloakOperation.ERROR_UPDATE_USER, KeycloakOperation.ERROR_UPDATE_USER_LABEL, //
        KeycloakOperation.ERROR_CANT_UPDATE_USERNAME, KeycloakOperation.ERROR_CANT_UPDATE_USERNAME_LABEL, //
        KeycloakOperation.ERROR_UNKNOWN_USER, KeycloakOperation.ERROR_UNKNOWN_USER_LABEL, //
        KeycloakOperation.ERROR_UPDATE_USER, KeycloakOperation.ERROR_UPDATE_USER_LABEL, //
        KeycloakOperation.ERROR_USER_SET_PASSWORD, KeycloakOperation.ERROR_USER_SET_PASSWORD_LABEL);
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