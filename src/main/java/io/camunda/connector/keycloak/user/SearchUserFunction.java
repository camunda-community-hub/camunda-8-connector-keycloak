package io.camunda.connector.keycloak.user;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;
import io.camunda.connector.keycloak.toolbox.KeycloakOperation;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchUserFunction implements KeycloakSubFunction {

  private final Logger logger = LoggerFactory.getLogger(SearchUserFunction.class.getName());

  @Override
  public KeycloakOutput executeSubFunction(KeycloakOperation keycloakOperation,
                                           KeycloakInput keycloakInput,
                                           OutboundConnectorContext context) throws ConnectorException {

    KeycloakOutput keycloakOutput;

    if (keycloakInput.getUserId() != null && !keycloakInput.getUserId().isEmpty()) {
      keycloakOutput = searchByUserId(keycloakOperation, keycloakInput);
    } else {
      keycloakOutput = search(keycloakOperation, keycloakInput);
    }
    keycloakOutput.status = "SUCCESS";
    keycloakOutput.dateOperation = new Date();
    return keycloakOutput;
  }

  /**
   * Search by the UserId
   * Search by multiple criteria
   *
   * @param keycloakOperation operation
   * @param keycloakInput     parameter to search
   * @return the output
   */
  private KeycloakOutput searchByUserId(KeycloakOperation keycloakOperation, KeycloakInput keycloakInput)
      throws ConnectorException {
    String searchUserSignature = "userId[" + keycloakInput.getUserId() + "]";
    KeycloakOutput keycloakOutput = new KeycloakOutput();
    try {
      UserRepresentation user = keycloakOperation.searchUserByUserId(keycloakInput.getUserRealm(),
          keycloakInput.getUserId());
      if (user == null)
        keycloakOutput.listUsers = Collections.emptyList();
      else {
        keycloakOutput.listUsers = Stream.of(user).map(this::userToMap).toList();
        keycloakOutput.userId = user.getId();
        keycloakOutput.user = userToMap(user);
        logger.info("Search User {} Found {} users", searchUserSignature, keycloakOutput.listUsers);
      }
      return keycloakOutput;
    } catch (Exception e) {
      logger.error("Error during KeycloakSearchUser on {} : SearchUser {}{}", keycloakOperation.getKeycloakSignature(),
          keycloakInput.getUserSignature(), e);
      throw new ConnectorException(KeycloakOperation.ERROR_SEARCH_USER,
          "Fail search user " + keycloakInput.getUserSignature() + " : " + e.getMessage());

    }
  }

  /**
   * Search by multiple criteria
   *
   * @param keycloakOperation operation
   * @param keycloakInput     parameter to search
   * @return the output
   * @throws ConnectorException: ERROR_SEARCH_USER
   */
  private KeycloakOutput search(KeycloakOperation keycloakOperation, KeycloakInput keycloakInput)
      throws ConnectorException {
    String searchUserSignature = "userName[" + keycloakInput.getUserName() //
        + "] firstName[" + keycloakInput.getUserFirstName() //
        + "] lastName[" + keycloakInput.getUserLastName() //
        + "] email[" + keycloakInput.getUserEmail() //
        + "] pageNumber [" + keycloakInput.getPageNumber() //
        + "] pageSize[" + keycloakInput.getPageSize() //
        + "] realm[" + keycloakInput.getUserRealm() //
        + "]";
    KeycloakOutput keycloakOutput = new KeycloakOutput();
    try {
      List<UserRepresentation> usersList = keycloakOperation.searchUsersByCriteria(keycloakInput.getUserRealm(),
          getSearchCriteria(keycloakInput.getUserName()), getSearchCriteria(keycloakInput.getUserFirstName()),
          getSearchCriteria(keycloakInput.getUserLastName()), getSearchCriteria(keycloakInput.getUserEmail()),
          keycloakInput.getPageNumber(), keycloakInput.getPageSize());

      keycloakOutput.listUsers = usersList.stream().map(this::userToMap).toList();
      if (! usersList.isEmpty()) {
        keycloakOutput.userId = usersList.get(0).getId();
        keycloakOutput.user = userToMap(usersList.get(0));
      }
      logger.info("Search {} Found {} users", searchUserSignature, keycloakOutput.listUsers);
      return keycloakOutput;
    } catch (Exception e) {
      logger.error("Error during KeycloakSearchUser on {} : SearchUser {}{}", keycloakOperation.getKeycloakSignature(),
          keycloakInput.getUserSignature(), e);
      throw new ConnectorException(KeycloakOperation.ERROR_SEARCH_USER,
          "Fail search user " + keycloakInput.getUserSignature() + " : " + e.getMessage());

    }
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList(
    RunnerParameter.getInstance(KeycloakInput.INPUT_USER_REALM, //
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
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_NAME, //
                KeycloakInput.INPUT_USER_NAME_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_USER_NAME_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),

    RunnerParameter.getInstance(KeycloakInput.INPUT_USER_FIRSTNAME, //
            KeycloakInput.INPUT_USER_FIRSTNAME_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakInput.INPUT_USER_FIRSTNAME_EXPLANATION)//
        .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_LASTNAME, //
                KeycloakInput.INPUT_USER_LASTNAME_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.OPTIONAL, //
                KeycloakInput.INPUT_USER_LASTNAME_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),

        RunnerParameter.getInstance(KeycloakInput.INPUT_USER_EMAIL, //
                KeycloakInput.INPUT_USER_EMAIL_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.OPTIONAL, //
                KeycloakInput.INPUT_USER_EMAIL_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),

        RunnerParameter.getInstance(KeycloakInput.INPUT_PAGE_NUMBER, //
                KeycloakInput.INPUT_PAGE_NUMBER_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.OPTIONAL, //
                KeycloakInput.INPUT_PAGE_NUMBER_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())),


        RunnerParameter.getInstance(KeycloakInput.INPUT_PAGE_SIZE, //
                KeycloakInput.INPUT_PAGE_SIZE_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.OPTIONAL, //
                KeycloakInput.INPUT_PAGE_SIZE_EXPLANATION)//
            .addCondition(KeycloakInput.INPUT_KEYCLOAK_FUNCTION, List.of(getSubFunctionType())));
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(
        RunnerParameter.getInstance(KeycloakOutput.OUTPUT_LIST_USERS, //
            KeycloakOutput.OUTPUT_LIST_USERS_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakOutput.OUTPUT_LIST_USERS_EXPLANATION), //

        RunnerParameter.getInstance(KeycloakOutput.OUTPUT_USER_ID, //
            KeycloakOutput.OUTPUT_USER_ID_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.OPTIONAL, //
            KeycloakOutput.OUTPUT_USER_ID_EXPLANATION), //

    RunnerParameter.getInstance(KeycloakOutput.OUTPUT_USER, //
        KeycloakOutput.OUTPUT_USER_LABEL, //
        Date.class, //
        null, //
        RunnerParameter.Level.OPTIONAL, //
        KeycloakOutput.OUTPUT_USER_EXPLANATION));

  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(KeycloakOperation.ERROR_SEARCH_USER, KeycloakOperation.ERROR_SEARCH_USER_LABEL);
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
