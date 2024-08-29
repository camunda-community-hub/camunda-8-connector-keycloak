package io.camunda.connector.keycloak.user;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakFunction;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;

import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
public class AddUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(AddUserFunction.class.getName());


  @Override
  public KeycloakOutput executeSubFunction(KeycloakInput keycloakInput, OutboundConnectorContext context) throws
      ConnectorException {


    // Initialize Keycloak client
    Keycloak keycloak = KeycloakBuilder.builder()
        .serverUrl(keycloakInput.getServerUrl())
        .realm(keycloakInput.getRealm())
        .clientId(keycloakInput.getClientId())
        .username(keycloakInput.getAdminUserName())
        .password(keycloakInput.getAdminUserPassword())
        .build();

    // Get realm
    RealmResource realmResource = keycloak.realm(keycloakInput.getRealm());
    UsersResource usersResource = realmResource.users();

    // Create new user representation
    UserRepresentation newUser = new UserRepresentation();
    newUser.setUsername(keycloakInput.getUserName());
    newUser.setFirstName(keycloakInput.getUserFirstName());
    newUser.setLastName(keycloakInput.getUserLastName());
    newUser.setEmail(keycloakInput.getUserEmail());
    newUser.setEnabled(true);

    // Create a password credential
    CredentialRepresentation passwordCredential = new CredentialRepresentation();
    passwordCredential.setTemporary(false);
    passwordCredential.setType(CredentialRepresentation.PASSWORD);
    passwordCredential.setValue(keycloakInput.getAdminUserPassword());

    // Set password credentials to the user
    newUser.setCredentials(Collections.singletonList(passwordCredential));

    // Create the user in Keycloak
    Response response = usersResource.create(newUser);

    // Check if the user was created successfully
    int responseStatus =response.getStatus();
    response.close(); // Close the response to avoid resource leaks

    // Close Keycloak client
    keycloak.close();

    if (responseStatus == 201) {
      logger.info("User [{}] created with success", keycloakInput.getUserName());

    } else {
      logger.info("Failed to create user [{}]  status:{}", keycloakInput.getUserName(),responseStatus);
      throw new ConnectorException(KeycloakFunction.ERROR_CREATE_USER, "Fail create user [" + keycloakInput.getUserName()+"] status ["+responseStatus + "]");
    }
    KeycloakOutput keycloakOutput = new KeycloakOutput();
    keycloakOutput.status="SUCCESS";
    keycloakOutput.dateOperation = response.getDate();
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
    return Map.of(KeycloakFunction.ERROR_CREATE_USER,KeycloakFunction.ERROR_CREATE_USER_LABEL);
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
