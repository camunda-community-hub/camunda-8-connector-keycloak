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
import java.util.Set;
import java.util.stream.Collectors;

public class CreateUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(CreateUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    KeycloakOutput keycloakOutput = new KeycloakOutput();
    try {
      KeycloakOperation.KeycloakResult result = keycloakOperation.createUser(keycloakInput.getUserRealm(), //
          keycloakInput.getUserName(), //
          keycloakInput.getUserFirstName(), //
          keycloakInput.getUserLastName(), //
          keycloakInput.getUserEmail(), //
          keycloakInput.getUserPassword(), //
          Boolean.TRUE.equals(keycloakInput.getUserEnabled()), //
          Boolean.TRUE.equals(keycloakInput.getErrorIfUserExists()));

      // add role?
      if (keycloakInput.getUserRoles() != null) {
        Set<String> listRoles = Arrays.stream(keycloakInput.getUserRoles().split(",")).collect(Collectors.toSet());
        keycloakOperation.updateRoles(keycloakInput.getUserRealm(), result.userId, listRoles);

      }
      keycloakOutput.userId = result.userId;
      keycloakOutput.status = result.status.toString();
      keycloakOutput.dateOperation = new Date();
      return keycloakOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during KeycloakCreateUser on {} {} : {}", keycloakOperation.getKeycloakSignature(),
          keycloakInput.getUserSignature(), e.getMessage());
      throw new ConnectorException(KeycloakOperation.ERROR_CREATE_USER, "Error during add-user " + e.getMessage());
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

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_NAME, //
            KeycloakInput.INPUT_USER_NAME_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.REQUIRED, //
            KeycloakInput.INPUT_USER_NAME_EXPLANATION),//

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

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_PASSWORD, //
            KeycloakInput.INPUT_USER_PASSWORD_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_PASSWORD_EXPLANATION),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_ENABLED, //
            KeycloakInput.INPUT_USER_ENABLED_LABEL, //
            Boolean.class, //
            Boolean.FALSE, //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_ENABLED_EXPLANATION).setVisibleInTemplate(),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_ERROR_IF_USER_EXISTS, //
            KeycloakInput.INPUT_ERROR_IF_USER_EXISTS_LABEL, //
            Boolean.class, //
            Boolean.TRUE, //
            RunnerParameter.Level.REQUIRED, //
            KeycloakInput.INPUT_ERROR_IF_USER_EXISTS_EXPLANATION), //

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_ROLES, //
            KeycloakInput.INPUT_USER_ROLES_LABEL, //
            String.class, //
            "Operate,Tasklist,Optimize", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_ROLES_EXPLANATION)//

    );
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(KeycloakOutput.OUTPUT_USER_ID, //
            KeycloakOutput.OUTPUT_USER_ID_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakOutput.OUTPUT_USER_ID_EXPLANATION), //

        RunnerParameter.getInstance(KeycloakOutput.OUTPUT_STATUS, //
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
        KeycloakOperation.ERROR_USER_ALREADY_EXIST, KeycloakOperation.ERROR_USER_ALREADY_EXIST_LABEL, //
        KeycloakOperation.ERROR_CREATE_USER, KeycloakOperation.ERROR_CREATE_USER_LABEL, //
        KeycloakOperation.ERROR_USER_SET_PASSWORD, KeycloakOperation.ERROR_USER_SET_PASSWORD_LABEL, //
        KeycloakOperation.ERROR_UNKNOWN_USER, KeycloakOperation.ERROR_UNKNOWN_USER_LABEL, //
        KeycloakOperation.ERROR_CANT_ACCESS_USER_ROLES, KeycloakOperation.ERROR_CANT_ACCESS_USER_ROLES_LABEL, //
        KeycloakOperation.ERROR_UNKNOWN_ROLE, KeycloakOperation.ERROR_UNKNOWN_ROLE_LABEL,  //
        KeycloakOperation.ERROR_DELETE_ROLE_USER, KeycloakOperation.ERROR_DELETE_ROLE_USER_LABEL, //
        KeycloakOperation.ERROR_ADD_ROLE_USER, KeycloakOperation.ERROR_ADD_ROLE_USER_LABEL);

  }

  @Override
  public String getSubFunctionName() {
    return "createUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Create a user in Keycloak";
  }

  @Override
  public String getSubFunctionType() {
    return "create-user";
  }
}
