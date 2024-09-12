package io.camunda.connector.keycloak.user;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakFunction;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(SearchUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(Keycloak keycloak,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    String searchUserSignature = "";
    try {

      // Get realm
      RealmResource realmResource = keycloak.realm(keycloakInput.getRealm());
      UsersResource usersResource = realmResource.users();

      KeycloakOutput keycloakOutput = new KeycloakOutput();

      if (keycloakInput.getUserId() != null && !keycloakInput.getUserId().isEmpty()) {
        searchUserSignature = "userId[" + keycloakInput.getUserId() + "]";
        List<UserRepresentation> users = usersResource.search(keycloakInput.getUserId(), true);
        keycloakOutput.listUsers = users.stream().map(this::userToMap).collect(Collectors.toList());

        //        UserResource userResource = usersResource.get(keycloakInput.getUserId());
        //        UserRepresentation user = userResource.toRepresentation();
        //        keycloakOutput.listUsers = List.of(userToMap(user));
      } else {
        searchUserSignature =
            "userName [" + keycloakInput.getUserName() + "] firstName [" + keycloakInput.getUserFirstName()
                + "] lastName [" + keycloakInput.getUserLastName() + "] email [" + keycloakInput.getUserEmail()
                + "] pageNumber [" + keycloakInput.getPageNumber() + "] pageSize [" + keycloakInput.getPageSize() + "]";
        List<UserRepresentation> users = usersResource.search(getSearchCriteria(keycloakInput.getUserName()),
            getSearchCriteria(keycloakInput.getUserFirstName()), getSearchCriteria(keycloakInput.getUserLastName()),
            getSearchCriteria(keycloakInput.getUserEmail()), keycloakInput.getPageNumber(),
            keycloakInput.getPageSize());

        keycloakOutput.listUsers = users.stream().map(this::userToMap).collect(Collectors.toList());
      }
      logger.info("Search User {} Found {} users", searchUserSignature, keycloakOutput.listUsers);

      // Close Keycloak client
      keycloak.close();
      keycloakOutput.status = "SUCCESS";
      return keycloakOutput;

    } catch (Exception e) {
      logger.error("Error during KeycloakSearchUser on {} : SearchUser {}{}",KeycloakFunction.getKeycloackSignature(keycloakInput), searchUserSignature, e);
      throw new ConnectorException(KeycloakFunction.ERROR_SEARCH_USER,
          "Fail search user " + searchUserSignature + " : " + e.getMessage());

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
    return Map.of(KeycloakFunction.ERROR_SEARCH_USER, KeycloakFunction.ERROR_SEARCH_USER_LABEL);
  }

  @Override
  public String getSubFunctionName() {
    return "searchUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Search users in Keycloak by it's ID, or name/firstName/lastName/email";
  }

  @Override
  public String getSubFunctionType() {
    return "search-users";
  }

  private String getSearchCriteria(String criteriaContent) {
    if (criteriaContent == null || criteriaContent.isEmpty())
      return null;
    return criteriaContent;
  }

  private Map<String, Object> userToMap(UserRepresentation userRepresentation) {
    Map<String, Object> recordUser = new HashMap<>();
    recordUser.put("userId", userRepresentation.getId());
    recordUser.put("userName", userRepresentation.getUsername());
    recordUser.put("firstName", userRepresentation.getFirstName());
    recordUser.put("lastName", userRepresentation.getLastName());
    recordUser.put("email", userRepresentation.getEmail());
    return recordUser;
  }
}
