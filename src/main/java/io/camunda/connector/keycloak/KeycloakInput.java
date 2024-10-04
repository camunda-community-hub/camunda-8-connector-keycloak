package io.camunda.connector.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.keycloak.toolbox.KeycloakOperation;
import io.camunda.connector.keycloak.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class KeycloakInput implements CherryInput {

  public static final String INPUT_KEYCLOAK_FUNCTION = "keycloakFunction";
  public static final String INPUT_SERVER_URL = "serverUrl";
  public static final String INPUT_SERVER_URL_LABEL = "Server Url";
  public static final String INPUT_SERVER_URL_EXPLANATION = "URL to connect the Keycloak server";

  public static final String INPUT_CONNECTION_REALM = "connectionRealm";
  public static final String INPUT_CONNECTION_REALM_LABEL = "connection Realm";
  public static final String INPUT_CONNECTION_REALM_EXPLANATION =
      "Realm to connect Keycloak. This is NOT the realm " + "created by Identity ";

  public static final String INPUT_CLIENT_ID = "clientId";
  public static final String INPUT_CLIENT_ID_LABEL = "Client Id";
  public static final String INPUT_CLIENT_ID_EXPLANATION = "Client Id used for the connection, to retrieve permission";

  public static final String INPUT_CLIENT_SECRET = "clientSecret";
  public static final String INPUT_CLIENT_SECRET_LABEL = "Client Secret";
  public static final String INPUT_CLIENT_SECRET_EXPLANATION =
      "Connection based on Client: the secret of " + "this client";

  public static final String INPUT_ADMIN_USER_NAME = "adminUserName";
  public static final String INPUT_ADMIN_USER_NAME_LABEL = "Admin User Name";
  public static final String INPUT_ADMIN_USER_NAME_EXPLANATION = "Connection based on User : the admin user";

  public static final String INPUT_ADMIN_USER_PASSWORD = "adminUserPassword";
  public static final String INPUT_ADMIN_USER_PASSWORD_LABEL = "Admin User Password";
  public static final String INPUT_ADMIN_USER_PASSWORD_EXPLANATION = "Connection based on User: the password";

  public static final String INPUT_USER_REALM = "userRealm";
  public static final String INPUT_USER_REALM_LABEL = "User realm";
  public static final String INPUT_USER_REALM_EXPLANATION = "The user is managed in a realm";
  public static final String INPUT_USER_REALM_DEFAULT = "camunda-platform";
  public static final String INPUT_USER_NAME = "userName";
  public static final String INPUT_USER_NAME_LABEL = "User Name";
  public static final String INPUT_USER_NAME_EXPLANATION = "The user name must be unique in a realm's Keycloak";
  public static final String INPUT_USER_FIRSTNAME = "userFirstName";
  public static final String INPUT_USER_FIRSTNAME_LABEL = "First name";
  public static final String INPUT_USER_FIRSTNAME_EXPLANATION = "First name of the user";
  public static final String INPUT_USER_LASTNAME = "userLastName";
  public static final String INPUT_USER_LASTNAME_LABEL = "Last name";
  public static final String INPUT_USER_LASTNAME_EXPLANATION = "Last name of the user";
  public static final String INPUT_USER_EMAIL = "userEmail";
  public static final String INPUT_USER_EMAIL_LABEL = "Email";
  public static final String INPUT_USER_EMAIL_EXPLANATION = "Email of the user";

  public static final String INPUT_SEARCH_BY_USER_ID = "searchByUserId";
  public static final String INPUT_SEARCH_BY_USER_ID_LABEL = "Search by User Id";
  public static final String INPUT_SEARCH_BY_USER_ID_EXPLANATION = "Search by User Id";
  public static final String INPUT_SEARCH_BY_USER_NAME = "searchByUserName";
  public static final String INPUT_SEARCH_BY_USER_NAME_LABEL = "Search by User Name";
  public static final String INPUT_SEARCH_BY_USER_NAME_EXPLANATION = "Search by the user name";
  public static final String INPUT_SEARCH_BY_USER_FIRSTNAME = "searchByUserFirstName";
  public static final String INPUT_SEARCH_BY_USER_FIRSTNAME_LABEL = "Search by First name";
  public static final String INPUT_SEARCH_BY_USER_FIRSTNAME_EXPLANATION = "Search by First name of the user";
  public static final String INPUT_SEARCH_BY_USER_LASTNAME = "searchByUserLastName";
  public static final String INPUT_SEARCH_BY_USER_LASTNAME_LABEL = "Search by Last name";
  public static final String INPUT_SEARCH_BY_USER_LASTNAME_EXPLANATION = "Search by Last name of the user";
  public static final String INPUT_SEARCH_BY_USER_EMAIL = "searchByUUserEmail";
  public static final String INPUT_SEARCH_BY_USER_EMAIL_LABEL = "Search by Email";
  public static final String INPUT_SEARCH_BY_USER_EMAIL_EXPLANATION = "Search by Email of the user";

  public static final String INPUT_ERROR_IF_USER_EXISTS = "errorIfUserExists";
  public static final String INPUT_ERROR_IF_USER_EXISTS_LABEL = "Error if user already exist";
  public static final String INPUT_ERROR_IF_USER_EXISTS_EXPLANATION =
      "If true, an BPMN error is thrown if the user already exists- else it's " + "updated except the userName";
  public static final String INPUT_USER_PASSWORD = "userPassword";
  public static final String INPUT_USER_PASSWORD_LABEL = "Password";
  public static final String INPUT_USER_PASSWORD_EXPLANATION = "Password of the user, if password is manage by Keycloak";
  public static final String INPUT_USER_ENABLED = "userEnabled";
  public static final String INPUT_USER_ENABLED_LABEL = "User enabled";
  public static final String INPUT_USER_ENABLED_EXPLANATION = "User enabled. Default is false";
  public static final String INPUT_USER_ROLES = "userRoles";
  public static final String INPUT_USER_ROLES_LABEL = "User roles";
  public static final String INPUT_USER_ROLES_EXPLANATION = "User roles assigned to the user";

  public static final String INPUT_USER_ID = "userId";
  public static final String INPUT_USER_ID_LABEL = "User Id";
  public static final String INPUT_USER_ID_EXPLANATION = "User Id in Keycloak, to get the user in the Realm";
  public static final String INPUT_PAGE_NUMBER = "pageNumber";
  public static final String INPUT_PAGE_NUMBER_LABEL = "Page number";
  public static final String INPUT_PAGE_NUMBER_EXPLANATION = "Page to start to return the result (start at 0)";
  public static final String INPUT_PAGE_SIZE = "pageSize";
  public static final String INPUT_PAGE_SIZE_LABEL = "Page size";
  public static final String INPUT_PAGE_SIZE_EXPLANATION = "Number of records per page";
  public static final String INPUT_CONNECTION_TYPE = "connectionType";
  public static final String INPUT_CONNECTION_TYPE_V_USER = "USER";
  public static final String INPUT_CONNECTION_TYPE_V_CLIENT_ID = "CLIENTID";
  public static final String INPUT_CONNECTION_TYPE_V_KEYCLOAK_CONNECTION_URL = "CONNECTIONURL";
  public static final String INPUT_CONNECTION_TYPE_LABEL = "Type of Keycloack Connection ";
  public static final String INPUT_CONNECTION_TYPE_EXPLANATION = "Connection to Keycloak engine";

  public static final String INPUT_KEYCLOAK_CONNECTION_URL = "keycloakConnectionUrl";
  public static final String INPUT_KEYCLOAK_CONNECTION_URL_LABEL = "Keycloak Connection Url";
  public static final String INPUT_KEYCLOAK_CONNECTION_URL_EXPLANATION =
      "Check documentation: USERID;serverUrl;" + "realm;clientid;adminusername;adminpassword for example";

  private final Boolean userEnabled = null;
  private final String userRoles = "";
  private final String connectionRealm = "master";  // The realm to connect
  private final String clientId = "";  // Client ID for authentication
  private final String clientSecret = "";  // Client ID for authentication
  private final String adminUserName = "admin";  // Admin username
  private final String adminUserPassword = "admin";  // Admin password
  private final String keycloakFunction = "";
  private final String serverUrl = "http://localhost:8080/auth";  // Keycloak server URL
  private final String userRealm = INPUT_USER_REALM_DEFAULT;
  private final String userName = "";
  private final String userFirstName = "";
  private final String userLastName = "";
  private final String userEmail = "";

  private final String searchByUserId = "";
  private final String searchByUserName = "";
  private final String searchByUserFirstName = "";
  private final String searchByUserLastName = "";
  private final String searchByUserEmail = "";

  private Boolean errorIfUserExists = Boolean.TRUE;
  private String userPassword = "";
  private String userId = "";
  private int pageNumber = 0;
  private int pageSize = 100;

  private String connectionType = INPUT_CONNECTION_TYPE_V_USER;
  private String keycloakConnectionUrl = "";

  public String getKeycloakFunction() {
    return keycloakFunction;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getConnectionRealm() {
    return connectionRealm;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getAdminUserName() {
    return adminUserName;
  }

  public String getAdminUserPassword() {
    return adminUserPassword;
  }

  public String getUserRealm() {
    return userRealm;
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

  public Boolean getErrorIfUserExists() {
    return errorIfUserExists;
  }

  public String getSearchByUserId() {
    return searchByUserId;
  }

  public String getSearchByUserName() {
    return searchByUserName;
  }

  public String getSearchByUserFirstName() {
    return searchByUserFirstName;
  }

  public String getSearchByUserLastName() {
    return searchByUserLastName;
  }

  public String getSearchByUserEmail() {
    return searchByUserEmail;
  }

  public String getUserId() {
    return userId;
  }

  public Boolean getUserEnabled() {
    return userEnabled;
  }

  public String getUserRoles() {
    return userRoles;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public String getConnectionType() {
    return connectionType;
  }

  public String getKeycloakConnectionUrl() {
    return keycloakConnectionUrl;
  }

  public String getUserSignature() {
    return "UserId[" + getUserId() + "] UserName:[" + getUserName() + "] firstName[" + getUserFirstName()
        + "] lastName[" + getUserLastName() + "] Email[" + getUserEmail() + "] Enabled[" + getUserEnabled()
        + "] Password[" + KeycloakOperation.getLogSecret(getUserPassword()) + "]";
  }

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getInputParameters();
  }
}
