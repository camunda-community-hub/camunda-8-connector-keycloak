package io.camunda.connector.keycloak.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.keycloak.KeycloakFunction;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeycloakOperation {

  private final Logger logger = LoggerFactory.getLogger(KeycloakOperation.class.getName());

  public final static String ERROR_CREATE_USER = "CREATE_USER";
  public final static String ERROR_CREATE_USER_LABEL = "Create user failed";

  public final static String ERROR_UNKNONW_USER = "UNKNOWN_USER";
  public final static String ERROR_UNKNONW_USER_LABEL = "Userid given is not found in Keycloak";

  public final static String ERROR_USER_ALREADY_EXIST = "USER_ALREADY_EXIST";
  public final static String ERROR_USER_ALREADY_EXIST_LABEL = "The username is unique in keycloak";

  public final static String ERROR_UNKNONW_ROLE = "UNKNOWN_ROLE";
  public final static String ERROR_UNKNONW_ROLE_LABEL = "Role given is not found in Keycloak";

  public final static String ERROR_UPDATE_USER = "UPDATE_USER";
  public final static String ERROR_UPDATE_USER_LABEL = "During update user in Keycloak";

  public final static String ERROR_DELETE_USER = "DELETE_USER";
  public final static String ERROR_DELETE_USER_LABEL = "During delete user in Keycloak";

  private Keycloak keycloak;
  private String keycloakSignature;
  // Initialize Keycloak client
  // https://github.com/camunda-cloud/identity/blob/main/management-api/src/main/java/io/camunda/identity/impl/keycloak/initializer/KeycloakUserInitializer.java
  // https://github.com/camunda-cloud/identity/blob/main/management-api/src/main/java/io/camunda/identity/impl/keycloak/initializer/KeycloakUserInitializer.java#L111-L124

  public KeycloakOperation() {
  }

  /**
   * @param serverUrl         url to access keycloak, without the ream (ex http://localhost:18080/auth)
   * @param realm             ream where the admin user is (ex master)
   * @param clientId          client id available in the realm, to access permission attached to the user (ex admin-cli)
   * @param adminUserName     Admin user name in the realm (ex admin)
   * @param adminUserPassword admin user password in the real (ex H4sre'133)
   * @param context           context of connection (to log)
   */
  public void openByUser(String serverUrl,
                         String realm,
                         String clientId,
                         String adminUserName,
                         String adminUserPassword,
                         String context) throws ConnectorException {
    keycloakSignature =
        "Url[" + serverUrl + "] Realm[" + realm + "] clientId[" + clientId + "] adminUserName[" + adminUserName
            + "] adminUserPassword[" + getLogSecret(adminUserPassword) + "] }";

    try {
      logger.info("Connect Keycloak by User");
      keycloak = KeycloakBuilder.builder()
          .grantType(OAuth2Constants.PASSWORD) // Grant type
          .serverUrl(serverUrl)
          .realm(realm)
          .clientId(clientId)
          .username(adminUserName)
          .password(adminUserPassword)
          .build();
    } catch (Exception e) {
      logger.error("Error during KeycloakConnection {} :  {}", keycloakSignature, e);
      throw new ConnectorException(KeycloakFunction.ERROR_KEYCLOAK_CONNECTION,
          "Error during keycloakConnection function[" + context + "] : " + e.getMessage());
    }
  }

  /**
   * Open by a client ID/ Client Secret
   * never works
   *
   * @param serverUrl
   * @param realm
   * @param clientId
   * @param clientSecret
   */
  public void openByClientId(String serverUrl, String realm, String clientId, String clientSecret, String context)
      throws ConnectorException {
    keycloakSignature =
        "Url[" + serverUrl + "] Realm[" + realm + "] clientId[" + clientId + "] clientSecret[" + getLogSecret(
            clientSecret) + "] }";

    try {
      logger.info("Connect Keycloak by client");

      keycloak = KeycloakBuilder.builder().grantType(OAuth2Constants.CLIENT_CREDENTIALS) // Grant type
          .serverUrl(serverUrl).realm(realm).clientId(clientId).clientSecret(clientSecret) //
          .build();
    } catch (Exception e) {
      logger.error("Error during KeycloakConnection {} :  {}", keycloakSignature, e);
      throw new ConnectorException(KeycloakFunction.ERROR_KEYCLOAK_CONNECTION,
          "Error during keycloakConnection function[" + context + "] : " + e.getMessage());
    }
  }

  /**
   * Close the keyconnection to keycloak
   */
  public void close() {
    if (keycloak != null && !keycloak.isClosed())
      keycloak.close();
  }

  public String getKeycloakSignature() {
    return keycloakSignature;
  }


  /* ******************************************************************** */
  /*                                                                      */
  /*  users operation                                                     */
  /*                                                                      */
  /* ******************************************************************** */

  /**
   * addUser
   *
   * @param realm
   * @param userName
   * @param firstName
   * @param lastName
   * @param email
   * @param userPassword
   * @param enabledUser
   * @return
   */
  public String addUser(String realm,
                        String userName,
                        String firstName,
                        String lastName,
                        String email,
                        String userPassword,
                        boolean enabledUser) {
    RealmResource realmResource = keycloak.realm(realm);
    UsersResource usersResource = realmResource.users();

    try {
      // Define user
      UserRepresentation user = new UserRepresentation();
      user.setEnabled(enabledUser);
      user.setUsername(userName);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmail(email);
      user.setAttributes(Collections.singletonMap("origin", Arrays.asList("KeycloakCamundaConnector")));

      long markerTime = System.currentTimeMillis();
      Response response = usersResource.create(user);
      if(response!=null && response.getStatus()==409)
        throw new ConnectorException(ERROR_USER_ALREADY_EXIST, "User["+userName+"] already exist");

      String userId = CreatedResponseUtil.getCreatedId(response);
      setPasswordInternal(realm, userId, userPassword);

      logger.info("UserName[{}] Realm[}] created with userId:[{}] status[{}] in {} ms", userName, realm, userId,
          response.getStatus(), System.currentTimeMillis() - markerTime);
      return userId;
    } catch (WebApplicationException e) {
      logger.error("Error during creation UserName[{}] Realm[{}] : {}", userName, realm, e);
      throw new ConnectorException(ERROR_CREATE_USER, "UserName[" + userName + "] failed " + e.getMessage());
    }

  }

  /**
   * Set a password to an existing user
   * @param realm where is the suer
   * @param userId userid
   * @param userPassword password to save
   */
  public void setPassword(String realm, String userId, String userPassword) {
    long markerTime = System.currentTimeMillis();
    setPasswordInternal(realm, userId, userPassword);
    logger.info("UserId[{}] Realm[}] Password created in {} ms", userId, realm,
        System.currentTimeMillis() - markerTime);

  }

  /**
   * setpasswordInternal : do not log anything, and throw error.
   * @param realm where is the suer
   * @param userId userid
   * @param userPassword password to save
   */
  public void setPasswordInternal(String realm, String userId, String userPassword) {
    RealmResource realmResource = keycloak.realm(realm);
    UsersResource usersResource = realmResource.users();
    UserResource userResource = usersResource.get(userId);

    // Set password credential
    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setTemporary(false);
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue(userPassword);

    userResource.resetPassword(passwordCred);

  }

  public enum UserProperties {USERNAME, FIRSTNAME, LASTNAME, EMAIL, ENABLED}

  public void updateUser(String realm, String userId, Map<UserProperties, Object> updateProperties) {
    try {
      UserResource user = keycloak.realm(realm).users().get(userId);
      UserRepresentation userRepresentation = user.toRepresentation();
      for (Map.Entry<UserProperties, Object> entry : updateProperties.entrySet()) {
        switch (entry.getKey()) {
        case USERNAME -> userRepresentation.setUsername((String) entry.getValue());
        case FIRSTNAME -> userRepresentation.setFirstName((String) entry.getValue());
        case LASTNAME -> userRepresentation.setLastName((String) entry.getValue());
        case EMAIL -> userRepresentation.setEmail((String) entry.getValue());
        case ENABLED -> userRepresentation.setEnabled((Boolean) entry.getValue());

        }

        // Update the user in Keycloak
        user.update(userRepresentation);

      }
    } catch (Exception e) {
      logger.error("Error during update user [{}]", userId, e);
      throw new ConnectorException(ERROR_UPDATE_USER, "During update user[" + userId + "]");
    }
  }

  public void deleteUser(String realm, String userId) {
    try {
      RealmResource realmResource = keycloak.realm(realm);
      UsersResource usersResource = realmResource.users();
      Response response = usersResource.delete(userId);
      // Check if the user was created successfully
      int responseStatus = response.getStatus();
      response.close(); // Close the response to avoid resource leaks
    } catch (Exception e) {
      logger.error("Error during update user [{}]", userId, e);
      throw new ConnectorException(ERROR_DELETE_USER, "During delere user[" + userId + "]");
    }
  }

  /* ******************************************************************** */
  /*                                                                      */
  /*  roles operation                                                     */
  /*                                                                      */
  /* ******************************************************************** */

  public void addRole(String realm, String userId, List<String> roles) {
    // Get realm role "tester" (requires view-realm role)
    RealmResource realmResource = keycloak.realm(realm);
    UsersResource usersResource = realmResource.users();
    UserResource userResource = usersResource.get(userId);
    if (userResource == null) {
      logger.error("Unknown user [{}]", userId);
      throw new ConnectorException(ERROR_UNKNONW_USER, "UserId[" + userId + "] unknown");

    }
    long markerTime = System.currentTimeMillis();
    for (String role : roles) {
      RoleRepresentation keycloakRole = realmResource.roles().get(role).toRepresentation();
      if (keycloakRole == null) {
        logger.error("Unknown role[{}]", role);
        throw new ConnectorException(ERROR_UNKNONW_ROLE, "Role[" + role + "] unknown");
      }
      //
      // Assign realm role tester to user
      markerTime = System.currentTimeMillis();
      userResource.roles().realmLevel().add(Arrays.asList(keycloakRole));
      logger.info("Role[{}] set to userId[{}]  in {} ms", role, userId, System.currentTimeMillis() - markerTime);

    }
  }

  public void updateRole(String realm, String userId, List<String> roles) {

  }

  /**
   * Protect the value by obfuscated part of it
   *
   * @param secret value to protect
   * @return part of the value
   */
  public static String getLogSecret(String secret) {
    if (secret == null)
      return "null";
    if (secret.length() > 3)
      return secret.substring(0, 3) + "****";
    return "****";
  }

}
