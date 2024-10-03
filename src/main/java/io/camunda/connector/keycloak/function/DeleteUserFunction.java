package io.camunda.connector.keycloak.function;

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

public class DeleteUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(DeleteUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {
    try {
      // Initialize Keycloak client
      keycloakOperation.deleteUser(keycloakInput.getUserRealm(), keycloakInput.getUserId());

      KeycloakOutput keycloakOutput = new KeycloakOutput();
      keycloakOutput.status = "SUCCESS";
      keycloakOutput.dateOperation = new Date();
      return keycloakOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during deleteUser on {} {} userId[{}]: {}", keycloakOperation.getKeycloakSignature(),
          keycloakInput.getUserSignature(), keycloakInput.getUserId(), e.getMessage());
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
            KeycloakInput.INPUT_USER_ID_EXPLANATION));//

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
            KeycloakOutput.OUTPUT_DATE_OPERATION_EXPLANATION));
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(KeycloakOperation.ERROR_KEYCLOAK_CONNECTION, KeycloakOperation.ERROR_KEYCLOAK_CONNECTION_LABEL, //
        KeycloakFunction.ERROR_UNKNOWN_FUNCTION, KeycloakFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
        KeycloakOperation.ERROR_DELETE_USER, KeycloakOperation.ERROR_DELETE_USER_LABEL, //
        KeycloakOperation.ERROR_UNKNOWN_USER, KeycloakOperation.ERROR_UNKNOWN_USER_LABEL);
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