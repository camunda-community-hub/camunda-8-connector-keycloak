package io.camunda.connector.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import io.camunda.connector.keycloak.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class KeycloakInput implements CherryInput {

  public static final String INPUT_KEYCLOAK_FUNCTION = "keycloakFunction";
  private String keycloakFunction = "";


  public static final String INPUT_SERVER_URL = "serverUrl";
  private String serverUrl = "http://localhost:8080/auth";  // Keycloak server URL

  public static final String INPUT_REALM = "realm";
  String realm = "camunda-realm";  // The realm you want to manage

  public static final String INPUT_ADMIN_CLIENT_ID = "adminClientId";
  String clientId = "admin-cli";  // Client ID for authentication

  public static final String INPUT_ADMIN_USER_NAME = "adminUsername";
  String adminUserName = "admin";  // Admin username

  public static final String INPUT_ADMIN_PASSWORD = "adminPassword";
  String adminUserPassword = "admin";  // Admin password

  public static final String INPUT_USER_NAME = "userName";
  private String userName = "walter.bates";

  public static final String INPUT_USER_FIRSTNAME = "userFirstName";
  private String userFirstName = "Walter";

  public static final String INPUT_USER_LASTNAME = "userLastName";
  private String userLastName = "Bates";

  public static final String INPUT_USER_EMAIL = "userEmail";
  private String userEmail = "walter.bates@camunda.com";

  public static final String INPUT_USER_PASSWORD = "userPassword";
  private String userPassword = "123";

  public static final String INPUT_USER_ID = "userId";
  private String userId = "";

  public static final String INPUT_PAGE_NUMBER = "pageNumber";
  private int pageNumber = 0;
  public static final String INPUT_PAGE_SIZE = "pageSize";
  private int pageSize = 100;


  public String getKeycloakFunction() {
    return keycloakFunction;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getRealm() {
    return realm;
  }

  public String getClientId() {
    return clientId;
  }

  public String getAdminUserName() {
    return adminUserName;
  }

  public String getAdminUserPassword() {
    return adminUserPassword;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserFirstName() {
    return userFirstName;
  }

  public String getUserLastName() {
    return userLastName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public String getUserId() {
    return userId;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getInputParameters();
  }
}
