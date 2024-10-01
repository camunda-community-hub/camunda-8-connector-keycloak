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
  private String keycloakFunction = "";


  public static final String INPUT_SERVER_URL = "serverUrl";
  private String serverUrl = "http://localhost:8080/auth";  // Keycloak server URL

  public static final String INPUT_CONNECTION_REALM = "connectionRealm";
  String connectionRealm = "master";  // The realm to connect

  public static final String INPUT_CLIENT_ID = "clientId";
  String clientId = "";  // Client ID for authentication

  public static final String INPUT_CLIENT_SECRET = "clientSecret";
  String clientSecret = "";  // Client ID for authentication

  public static final String INPUT_ADMIN_USER_NAME = "adminUserName";
  String adminUserName = "admin";  // Admin username

  public static final String INPUT_ADMIN_USER_PASSWORD = "adminUserPassword";
  String adminUserPassword = "admin";  // Admin password

  public static final String INPUT_USER_REALM = "userRealm";
  public static final String INPUT_USER_REALM_LABEL = "User realm";
  public static final String INPUT_USER_REALM_EXPLANATION="The user is created in a realm";
  public static final String INPUT_USER_REALM_DEFAULT="camunda-platform";
  private String userRealm = INPUT_USER_REALM_DEFAULT;

  public static final String INPUT_USER_NAME = "userName";
  public static final String INPUT_USER_NAME_LABEL ="User Name";
  public static final String INPUT_USER_NAME_EXPLANATION="The user name must be unique in a realm's Keycloak";
  private String userName = "";

  public static final String INPUT_USER_FIRSTNAME = "userFirstName";
  public static final String INPUT_USER_FIRSTNAME_LABEL="First name";
  public static final String INPUT_USER_FIRSTNAME_EXPLANATION="First name of the user";
  private String userFirstName = "";

  public static final String INPUT_USER_LASTNAME = "userLastName";
  public static final String INPUT_USER_LASTNAME_LABEL="Last name";
  public static final String INPUT_USER_LASTNAME_EXPLANATION="Last name of the user";
  private String userLastName = "";

  public static final String INPUT_USER_EMAIL = "userEmail";
  public static final String INPUT_USER_EMAIL_LABEL="Email";
  public static final String INPUT_USER_EMAIL_EXPLANATION="Email of the user";
  private String userEmail = "";

  public static final String INPUT_ERROR_IF_USER_EXISTS = "errorIfUserExists";
  public static final String INPUT_ERROR_IF_USER_EXISTS_LABEL="Error if user already exist";
  public static final String INPUT_ERROR_IF_USER_EXISTS_EXPLANATION="If true, an BPMN error is thrown if the user already exists- else it's "
      + "updated except the userName";
  private Boolean errorIfUserExists = Boolean.TRUE;


  public static final String INPUT_USER_PASSWORD = "userPassword";
  public static final String INPUT_USER_PASSWORD_LABEL="Password";
  public static final String INPUT_USER_PASSWORD_EXPLANATION="Password of the user, if password is manage by Keycloak";
  private String userPassword = "";

  public static final String INPUT_USER_ENABLED = "userEnabled";
  public static final String INPUT_USER_ENABLED_LABEL="User enabled";
  public static final String INPUT_USER_ENABLED_EXPLANATION="User enabled. Default is false";

  public Boolean userEnabled= null;

  public static final String INPUT_USER_ROLES = "userRoles";
  public static final String INPUT_USER_ROLES_LABEL="User roles";
  public static final String INPUT_USER_ROLES_EXPLANATION="User roles assigned to the user";
  public String userRoles= "";

  public static final String INPUT_USER_ID = "userId";
  public static final String INPUT_USER_ID_LABEL="User Id";
  public static final String INPUT_USER_ID_EXPLANATION="User Id in Keycloak, to get the user in the Realm";
  private String userId = "";

  public static final String INPUT_PAGE_NUMBER = "pageNumber";
  public static final String INPUT_PAGE_NUMBER_LABEL="Page number";
  public static final String INPUT_PAGE_NUMBER_EXPLANATION="Page to start to return the result (start at 0)";
  private int pageNumber = 0;

  public static final String INPUT_PAGE_SIZE = "pageSize";
  public static final String INPUT_PAGE_SIZE_LABEL="Page size";
  public static final String INPUT_PAGE_SIZE_EXPLANATION="Number of records per page";
  private int pageSize = 100;

  public static final String INPUT_CONNECTION_TYPE = "connectionType";
  private String connectionType = INPUT_CONNECTION_TYPE_V_USER;

  public static final String INPUT_CONNECTION_TYPE_V_USER = "USER";
  public static final String INPUT_CONNECTION_TYPE_V_CLIENT_ID = "CLIENTID";
  public static final String INPUT_CONNECTION_TYPE_V_KEYCLOAK_CONNECTION_URL = "CONNECTIONURL";

  public static final String INPUT_KEYCLOAK_CONNECTION_URL = "keycloakConnectionUrl";
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

  public Boolean getErrorIfUserExists() { return errorIfUserExists;}

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
    return "UserId["+getUserId()
    +"] UserName:["+getUserName()
        +"] firstName["+getUserFirstName()
        +"] lastName["+getUserLastName()
        +"] Email["+getUserEmail()
        +"] Enabled["+getUserEnabled()
        +"] Password["+ KeycloakOperation.getLogSecret(getUserPassword())+"]";
  }
  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getInputParameters();
  }
}
