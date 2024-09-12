package io.camunda.connector.keycloak.user;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakFunction;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(AddUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(Keycloak keycloak,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    String userSignature = "User name:["+keycloakInput.getUserName()
        +"] firstName["+keycloakInput.getUserFirstName()
        +"] lastName["+keycloakInput.getUserLastName()
        +"] Email["+keycloakInput.getUserEmail()
        +"] Password["+KeycloakFunction.getLogSecret(keycloakInput.getUserPassword())+"]";
    try {
      // Initialize Keycloak client
      // https://github.com/camunda-cloud/identity/blob/main/management-api/src/main/java/io/camunda/identity/impl/keycloak/initializer/KeycloakUserInitializer.java
      // https://github.com/camunda-cloud/identity/blob/main/management-api/src/main/java/io/camunda/identity/impl/keycloak/initializer/KeycloakUserInitializer.java#L111-L124

      // Get realm
      RealmResource realmResource = keycloak.realm(keycloakInput.getRealm());
      UsersResource usersResource = realmResource.users();


      // Create a password credential
      CredentialRepresentation passwordCredential = new CredentialRepresentation();
      passwordCredential.setTemporary(false);
      passwordCredential.setType(CredentialRepresentation.PASSWORD);
      passwordCredential.setValue(keycloakInput.getAdminUserPassword());

      // Create new user representation
      UserRepresentation newUser = new UserRepresentation();
      newUser.setUsername(keycloakInput.getUserName());
      newUser.setFirstName(keycloakInput.getUserFirstName());
      newUser.setLastName(keycloakInput.getUserLastName());
      newUser.setEmail(keycloakInput.getUserEmail());
      newUser.setEnabled(true);
      newUser.setCredentials(Collections.singletonList(passwordCredential));


      // Create the user in Keycloak
      Response response = usersResource.create(newUser);
      Optional<String> userId = Optional.of(CreatedResponseUtil.getCreatedId(response));

      // Check if the user was created successfully
      int responseStatus = response.getStatus();
      response.close(); // Close the response to avoid resource leaks

      if (responseStatus == 201) {
        logger.info("User [{}] created with success", userSignature);

      } else {
        logger.info("Failed to create user [{}] in {} status:{}", userSignature, KeycloakFunction.getKeycloackSignature(keycloakInput), responseStatus);
        throw new ConnectorException(KeycloakFunction.ERROR_CREATE_USER,
            "Fail create " + userSignature + " status [" + responseStatus + "]");
      }
      KeycloakOutput keycloakOutput = new KeycloakOutput();
      keycloakOutput.status = "SUCCESS";
      keycloakOutput.dateOperation = response.getDate();
      return keycloakOutput;
    } catch (Exception e) {
      logger.error("Error during KeycloakAddUser on {} {} : {}",KeycloakFunction.getKeycloackSignature(keycloakInput),userSignature, e);
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
