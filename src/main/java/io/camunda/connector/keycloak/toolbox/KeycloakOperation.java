package io.camunda.connector.keycloak.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class KeycloakOperation {

  public final static String ERROR_KEYCLOAK_CONNECTION = "KEYCLOAK_CONNECTION";
  public final static String ERROR_KEYCLOAK_CONNECTION_LABEL = "Error arrived during the Keycloak connection";
  public final static String ERROR_CREATE_USER = "CREATE_USER";
  public final static String ERROR_CREATE_USER_LABEL = "Create user failed";
  public final static String ERROR_UNKNOWN_USER = "UNKNOWN_USER";
  public final static String ERROR_UNKNOWN_USER_LABEL = "Userid given is not found in Keycloak";
  public final static String ERROR_USER_ALREADY_EXIST = "USER_ALREADY_EXIST";
  public final static String ERROR_USER_ALREADY_EXIST_LABEL = "The username is unique in keycloak";
  public final static String ERROR_USER_SET_PASSWORD = "USER_SET_PASSWORD";
  public final static String ERROR_USER_SET_PASSWORD_LABEL = "Password can't be set";

  public final static String ERROR_UNKNOWN_ROLE = "UNKNOWN_ROLE";
  public final static String ERROR_UNKNOWN_ROLE_LABEL = "Role given is not found in Keycloak";
  public final static String ERROR_CANT_ACCESS_USER_ROLES = "CANT_ACCESS_USER_ROLE";
  public final static String ERROR_CANT_ACCESS_USER_ROLES_LABEL = "Can't access user roles";
  public final static String ERROR_DELETE_ROLE_USER = "DELETE_ROLE";
  public final static String ERROR_DELETE_ROLE_USER_LABEL = "Removing the role from the user fail";
  public final static String ERROR_CANT_UPDATE_USERNAME = "CANT_UPDATE_USERNAME";
  public final static String ERROR_CANT_UPDATE_USERNAME_LABEL = "Keycloak does not allow to update the username";
  public final static String ERROR_ADD_ROLE_USER = "ADD_ROLE";
  public final static String ERROR_ADD_ROLE_USER_LABEL = "Adding a role to the user fail";
  public final static String ERROR_UPDATE_USER = "UPDATE_USER";
  public final static String ERROR_UPDATE_USER_LABEL = "During update user in Keycloak";
  public final static String ERROR_DELETE_USER = "DELETE_USER";
  public final static String ERROR_DELETE_USER_LABEL = "During delete user in Keycloak";
  public final static String ERROR_SEARCH_USER = "SEARCH_USER";
  public final static String ERROR_SEARCH_USER_LABEL = "During search user(s) in Keycloak";
  private final Logger logger = LoggerFactory.getLogger(KeycloakOperation.class.getName());
  private Keycloak keycloak;
  private String keycloakSignature;
  // Initialize Keycloak client
  // https://github.com/camunda-cloud/identity/blob/main/management-api/src/main/java/io/camunda/identity/impl/keycloak/initializer/KeycloakUserInitializer.java
  // https://github.com/camunda-cloud/identity/blob/main/management-api/src/main/java/io/camunda/identity/impl/keycloak/initializer/KeycloakUserInitializer.java#L111-L124

  public KeycloakOperation() {
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

  /**
   * @param serverUrl         url to access keycloak, without the ream (ex http://localhost:18080/auth)
   * @param adminUserRealm    ream where the admin user is (ex master)
   * @param clientId          client id available in the realm, to access permission attached to the user (ex admin-cli)
   * @param adminUserName     Admin user Name in the realm (ex admin)
   * @param adminUserPassword admin user password in the real (ex H4sre'133)
   * @param context           context of connection (to log)
   */
  public void openByUser(String serverUrl,
                         String adminUserRealm,
                         String clientId,
                         String adminUserName,
                         String adminUserPassword,
                         String context) throws ConnectorException {
    keycloakSignature =
        "Url[" + serverUrl + "] Realm[" + adminUserRealm + "] clientId[" + clientId + "] adminUserName[" + adminUserName
            + "] adminUserPassword[" + getLogSecret(adminUserPassword) + "] }";

    try {
      logger.debug("Connect Keycloak by User : {}]", keycloakSignature);
      keycloak = KeycloakBuilder.builder()
          .grantType(OAuth2Constants.PASSWORD) // Grant type
          .serverUrl(serverUrl)
          .realm(adminUserRealm)
          .clientId(clientId)
          .username(adminUserName)
          .password(adminUserPassword)
          .build();
    } catch (Exception e) {
      logger.error("Error during KeycloakConnection {} :  {}", keycloakSignature, e);
      throw new ConnectorException(ERROR_KEYCLOAK_CONNECTION,
          "Error during keycloakConnection function[" + context + "] : " + e.getMessage());
    }
  }

  /**
   * Open by a client ID/ Client Secret
   * never works
   *
   * @param serverUrl    serverULR to connect
   * @param realm        realm where the clientID is
   * @param clientId     client ID
   * @param clientSecret client Secret
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
      throw new ConnectorException(ERROR_KEYCLOAK_CONNECTION,
          "Error during keycloakConnection function[" + context + "] : " + e.getMessage());
    }
  }

  /**
   * Open by a KeycloakConnection
   * USERID;serverUrl;realm;clientid;adminusername;adminpasswor
   * CLIENTID;serverUrl;realm;clientid;clientsecret
   *
   * @param connectionUrl connectionURL, see format in description
   * @param context       context for log
   * @throws ConnectorException: ERROR_KEYCLOAK_CONNECTION
   */
  public void openByConnectionURL(String connectionUrl, String context) throws ConnectorException {
    if (connectionUrl == null)
      throw new ConnectorException(ERROR_KEYCLOAK_CONNECTION,
          "Error during keycloakConnection function[" + context + "] : connectionUrl is null ");
    StringTokenizer st = new StringTokenizer(connectionUrl, ";");
    String connectionType = st.hasMoreTokens() ? st.nextToken() : null;
    if ("USER".equals(connectionType)) {
      openByUser(st.hasMoreTokens() ? st.nextToken() : null, // serverUrl
          st.hasMoreTokens() ? st.nextToken() : null, // String adminUserRealm,
          st.hasMoreTokens() ? st.nextToken() : null, // String clientId,
          st.hasMoreTokens() ? st.nextToken() : null, // String adminUserName,
          st.hasMoreTokens() ? st.nextToken() : null, // String adminUserPassword,
          context);
    } else if ("CLIENTID".equals(connectionType)) {
      openByClientId(st.hasMoreTokens() ? st.nextToken() : null, // serverUrl
          st.hasMoreTokens() ? st.nextToken() : null, // String clientUserRealm,
          st.hasMoreTokens() ? st.nextToken() : null, // String clientId,
          st.hasMoreTokens() ? st.nextToken() : null, // String clientSecret
          context);
    } else {
      throw new ConnectorException(ERROR_KEYCLOAK_CONNECTION,
          "Can't decode KeycloakUrl: must start by USER|CLIENTID : [" + connectionUrl + "]");
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

  /**
   * addUser
   *
   * @param realm        realm to create the user
   * @param userName     user name
   * @param firstName    first name
   * @param lastName     last name
   * @param email        email
   * @param userPassword password of user
   * @param enabledUser  user is enabled
   * @return
   * @throws ConnectorException ERROR_USER_ALREADY_EXIST,ERROR_CREATE_USER, ERROR_USER_SET_PASSWORD
   */
  public KeycloakResult createUser(String realm,
                                   String userName,
                                   String firstName,
                                   String lastName,
                                   String email,
                                   String userPassword,
                                   boolean enabledUser,
                                   boolean errorIfUserExists) throws ConnectorException {
    KeycloakResult keycloakResult = new KeycloakResult();
    keycloakResult.status = KeycloakResult.Status.CREATED;

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
      int responseStatus = response == null ? -1 : response.getStatus();

      if (responseStatus == 409) {
        response.close();
        if (errorIfUserExists) {
          logger.error("UserName[{}] already exist in a Realm", userName);
          throw new ConnectorException(ERROR_USER_ALREADY_EXIST, "User[" + userName + "] already exist");
        } else {
          // search the UserId
          UserRepresentation userRepresentation = searchUserByUserName(realm, userName);
          keycloakResult.status = KeycloakResult.Status.UPDATED;
          keycloakResult.userId = userRepresentation.getId();

          updateUser(realm, keycloakResult.userId,
              Map.of(UserProperties.FIRSTNAME, firstName, UserProperties.LASTNAME, lastName, UserProperties.ENABLED,
                  enabledUser, UserProperties.EMAIL, email));
        }
      } else {
        keycloakResult.status = KeycloakResult.Status.CREATED;
        keycloakResult.userId = CreatedResponseUtil.getCreatedId(response);
      }
      if (response != null)
        response.close();

      setPasswordInternal(realm, keycloakResult.userId, userPassword);

      logger.info("UserName[{}] Realm[{}] created with/updated {} userId:[{}] status[{}] in {} ms", userName, realm,
          keycloakResult.status.toString(), keycloakResult.userId, responseStatus,
          System.currentTimeMillis() - markerTime);
      return keycloakResult;
    } catch (WebApplicationException e) {
      logger.error("Error during creation UserName[{}] Realm[{}] : {}", userName, realm, e);
      throw new ConnectorException(ERROR_CREATE_USER, "UserName[" + userName + "] failed " + e.getMessage());
    }

  }

  /**
   * Set a password to an existing user
   *
   * @param realm        where is the suer
   * @param userId       userid
   * @param userPassword password to save
   * @throws ConnectorException: ERROR_USER_SET_PASSWORD
   */
  public void setPassword(String realm, String userId, String userPassword) throws ConnectorException {
    long markerTime = System.currentTimeMillis();
    setPasswordInternal(realm, userId, userPassword);
    logger.info("UserId[{}] Realm[{}] Password created in {} ms", userId, realm,
        System.currentTimeMillis() - markerTime);

  }

  /**
   * setpasswordInternal : do not log anything, and throw error.
   *
   * @param realm        where is the suer
   * @param userId       userid
   * @param userPassword password to save
   * @throws ConnectorException: ERROR_USER_SET_PASSWORD
   */
  public void setPasswordInternal(String realm, String userId, String userPassword) throws ConnectorException {

    try {
      RealmResource realmResource = keycloak.realm(realm);
      UsersResource usersResource = realmResource.users();
      UserResource userResource = usersResource.get(userId);

      // Set password credential
      CredentialRepresentation passwordCred = new CredentialRepresentation();
      passwordCred.setTemporary(false);
      passwordCred.setType(CredentialRepresentation.PASSWORD);
      passwordCred.setValue(userPassword);

      userResource.resetPassword(passwordCred);
    } catch (Exception e) {
      logger.error("Error during setPassword UserId[{}] Realm[{}] : {}", userId, realm, e);
      throw new ConnectorException(ERROR_USER_SET_PASSWORD, "UserId[" + userId + "] failed " + e.getMessage());

    }
  }

  /**
   * UpdateUser
   *
   * @param realm            real where is the userId
   * @param userId           userId
   * @param updateProperties update properties
   * @throws ConnectorException: ERROR_CANT_UPDATE_USERNAME, ERROR_UNKNOWN_USER, ERROR_UPDATE_USER
   */
  public void updateUser(String realm, String userId, Map<UserProperties, Object> updateProperties)
      throws ConnectorException {
    UserResource userResource;
    long markerTime = System.currentTimeMillis();
    try {
      userResource = keycloak.realm(realm).users().get(userId);
    } catch (Exception e) {
      logger.error("Unknown user [{}]", userId);
      throw new ConnectorException(ERROR_UNKNOWN_USER, "UserId[" + userId + "] unknown");
    }

    String trace = "";
    try {
      UserRepresentation userRepresentation = userResource.toRepresentation();
      for (Map.Entry<UserProperties, Object> entry : updateProperties.entrySet()) {
        trace += "; " + entry.getKey() + "[" + entry.getValue() + "]";
        switch (entry.getKey()) {
        case USERNAME -> {
          logger.error("Keycloak does not allow to change the userName - userId[{}] realm[{}]", userId, realm);
          throw new ConnectorException(ERROR_CANT_UPDATE_USERNAME,
              "Can't update UserName for userId[" + userId + "] realm[" + realm + "]");
        }
        case FIRSTNAME -> userRepresentation.setFirstName((String) entry.getValue());
        case LASTNAME -> userRepresentation.setLastName((String) entry.getValue());
        case EMAIL -> userRepresentation.setEmail((String) entry.getValue());
        case ENABLED -> userRepresentation.setEnabled((Boolean) entry.getValue());
        default -> trace += "Unknown Key;";
        }
      }
      // Update the user in Keycloak
      userResource.update(userRepresentation);
      logger.info("UpdateUser UserId[{}] Realm[{}] {} updated in {} ms", userId, realm, trace,
          System.currentTimeMillis() - markerTime);

    } catch (Exception e) {
      logger.error("Error during update userId[{}] in realm[{}] {} :{}", userId, realm, trace, e);
      throw new ConnectorException(ERROR_UPDATE_USER,
          "During update user[" + userId + "] in realm[" + realm + "] : " + e.getMessage());
    }
  }

  /**
   * @param realm  realm where th user is
   * @param userId user id
   * @throws ConnectorException: ERROR_DELETE_USER, ERROR_UNKNOWN_USER
   */
  public void deleteUser(String realm, String userId) throws ConnectorException {
    long markerTime = System.currentTimeMillis();
    try {
      RealmResource realmResource = keycloak.realm(realm);
      UsersResource usersResource = realmResource.users();
      Response response = usersResource.delete(userId);
      // Check if the user was created successfully
      int responseStatus = response == null ? -1 : response.getStatus();
      if (response != null)
        response.close(); // Close the response to avoid resource leaks
      if (responseStatus == 404) {
        logger.error("DeleteUser: UserId[{}] unknown in real[{}]", userId, realm);
        throw new ConnectorException(KeycloakOperation.ERROR_UNKNOWN_USER,
            "User[" + userId + "] unknown in real[" + realm + "]");
      }
      logger.info("DeleteUser UserId[{}] Realm[{}] in {} ms", userId, realm, System.currentTimeMillis() - markerTime);

    } catch (Exception e) {
      logger.error("Error during update user [{}]", userId, e);
      throw new ConnectorException(ERROR_DELETE_USER, "During delere user[" + userId + "]");
    }
  }

  /**
   * Update role: unassign all roles non desired, and assign desired role
   *
   * @param realm        realm where the user is
   * @param userId       user id
   * @param desiredRoles role to set
   * @throws ConnectorException: ERROR_UNKNOWN_USER, ERROR_CANT_ACCESS_USER_ROLES, ERROR_UNKNOWN_ROLE, ERROR_DELETE_ROLE_USER, ERROR_ADD_ROLE_USER
   */
  public void updateRoles(String realm, String userId, Set<String> desiredRoles) throws ConnectorException {

    RealmResource realmResource = keycloak.realm(realm);
    UsersResource usersResource = realmResource.users();
    long markerTime = System.currentTimeMillis();
    // get User
    UserResource userResource;
    try {
      userResource = usersResource.get(userId);
    } catch (Exception e) {
      logger.error("Unknown userId[{}] in realm[{}}] : {} ", userId, realm, e);
      throw new ConnectorException(ERROR_UNKNOWN_USER, "UserId[" + userId + "] unknown");
    }

    // getAll current roles attaches to the user
    List<RoleRepresentation> currentRoles;
    try {
      RoleMappingResource roleMappingResource = userResource.roles();
      currentRoles = roleMappingResource.realmLevel().listAll();
    } catch (Exception e) {
      logger.error("Can't access curent role for userId[{}] in realm[{}}] : {} ", userId, realm, e);
      throw new ConnectorException(ERROR_CANT_ACCESS_USER_ROLES, "UserId[" + userId + "] unknown");
    }
    Set<String> currentRoleNames = currentRoles.stream().map(RoleRepresentation::getName).collect(Collectors.toSet());

    // Convert desired role in RoleRepresentation
    List<RoleRepresentation> desiredRoleRepresentations = new ArrayList<>();
    for (String roleName : desiredRoles) {
      try {
        RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();
        desiredRoleRepresentations.add(roleRepresentation);
      } catch (Exception e) {
        logger.error("Unknow role[{}] in realm[{}] : {} ", roleName, realm, e);
        throw new ConnectorException(ERROR_UNKNOWN_ROLE,
            "Role[" + roleName + "] unknown in real[" + realm + "] " + e.getMessage());
      }
    }

    // ---------------- remove
    List<RoleRepresentation> rolesToRemove = currentRoles.stream()
        .filter(role -> !(desiredRoles.contains(role.getName()) || "Default user role".equals(role.getName())))
        .collect(Collectors.toList());

    if (!rolesToRemove.isEmpty()) {
      try {
        userResource.roles().realmLevel().remove(rolesToRemove);
      } catch (Exception e) {
        logger.error("Error during removing Roles[{}] in realm[{}] : {} ", rolesToRemove, realm, e);
        throw new ConnectorException(ERROR_DELETE_ROLE_USER,
            "Error during removing Roles[" + rolesToRemove + "] in realm[" + realm + "] " + e.getMessage());
      }
    }

    //------------------ add
    List<RoleRepresentation> rolesToAdd = desiredRoleRepresentations.stream()
        .filter(role -> !currentRoleNames.contains(role.getName()))
        .collect(Collectors.toList());

    if (!rolesToAdd.isEmpty()) {
      try {
        userResource.roles().realmLevel().add(rolesToAdd);
      } catch (Exception e) {
        logger.error("Error adding Roles[{}] in realm[{}] : {} ", rolesToAdd, realm, e);
        throw new ConnectorException(ERROR_ADD_ROLE_USER,
            "Error during adding Roles[" + rolesToAdd + "] in realm[" + realm + "] " + e.getMessage());

      }
    }
    logger.info("Update userId[{}] realm[{}] roles added[{}] deleted[{}] in {} ms", userId, realm,
        rolesToAdd.stream().map(RoleRepresentation::getName).collect(Collectors.joining(",")),
        rolesToRemove.stream().map(RoleRepresentation::getName).collect(Collectors.joining(",")),
        System.currentTimeMillis() - markerTime);

  }

  /* ******************************************************************** */
  /*                                                                      */
  /*  roles operation                                                     */
  /*                                                                      */
  /* ******************************************************************** */

  /**
   * @param realm  realm where to run the search
   * @param userId userid searched
   * @return the userRepresentation, else null
   * @throws ConnectorException ERROR_SEARCH_USER
   */
  public UserRepresentation searchUserByUserId(String realm, String userId) throws ConnectorException {
    try {
      RealmResource realmResource = keycloak.realm(realm);
      UsersResource usersResource = realmResource.users();
      UserResource userResource = usersResource.get(userId);
      return userResource == null ? null : userResource.toRepresentation();
    } catch (Exception e) {
      logger.error("SearchByUserid[{}] in Realm[{}] : {}", userId, realm, e);
      throw new ConnectorException(ERROR_SEARCH_USER, "UserId[" + userId + "] in Realm[" + realm + "] : " + e);
    }
  }

  /* ******************************************************************** */
  /*                                                                      */
  /*  Search                                                              */
  /*                                                                      */
  /* ******************************************************************** */

  /**
   * userName is unique, only one use is expected
   *
   * @param realm    realm to search
   * @param userName userName
   * @return a userRepresentation or null
   * @throws ConnectorException: ERROR_SEARCH_USER
   */
  public UserRepresentation searchUserByUserName(String realm, String userName) throws ConnectorException {
    try {
      List<UserRepresentation> usersList = searchUsersByCriteria(realm, userName, null, null, null, 0, 2);
      if (!usersList.isEmpty())
        return usersList.get(0);
      return null;

    } catch (Exception e) {
      logger.error("SearchByUserName[{}] in Realm[{}] : {}", userName, realm, e);
      throw new ConnectorException(ERROR_SEARCH_USER, "userName[" + userName + "] in Realm[" + realm + "] : " + e);
    }
  }

  /**
   * @param realm      realm to search
   * @param userName   username to search
   * @param firstName  firstname to search
   * @param lastName   lastname to search
   * @param email      email to search
   * @param pageNumber start a 0
   * @param pageSize   number of record per page
   * @return the list of user representation found
   * @throws ConnectorException: ERROR_SEARCH_USER
   */
  public List<UserRepresentation> searchUsersByCriteria(String realm,
                                                        String userName,
                                                        String firstName,
                                                        String lastName,
                                                        String email,
                                                        int pageNumber,
                                                        int pageSize) throws ConnectorException {
    try {
      RealmResource realmResource = keycloak.realm(realm);
      UsersResource usersResource = realmResource.users();

      return usersResource.search(userName, firstName, lastName, email, pageNumber, pageSize);
    } catch (Exception e) {
      logger.error(
          "SearchUsersByCriteria userName[{}] firstName[{}] lastName[{}] email[{}] pageNumber[{}] pageSize[{}] in Realm[{}] : {}",
          userName, firstName, lastName, email, pageNumber, pageSize, realm, e);
      throw new ConnectorException(ERROR_SEARCH_USER, "userName[" + userName //
          + "] firstName[" + firstName //
          + "] lastName[" + lastName //
          + "] email[" + email //
          + "] pageNumber[" + pageNumber //
          + "] pageSize[" + pageSize //
          + "] in Realm[" + realm + "] : " + e);
    }
  }

  public enum UserProperties {USERNAME, FIRSTNAME, LASTNAME, EMAIL, ENABLED}

  /* ******************************************************************** */
  /*                                                                      */
  /*  Private                                                             */
  /*                                                                      */
  /* ******************************************************************** */

  /* ******************************************************************** */
  /*                                                                      */
  /*  users operation                                                     */
  /*                                                                      */
  /* ******************************************************************** */
  public class KeycloakResult {
    public String userId;
    public Status status;

    public enum Status {CREATED, UPDATED}
  }

}
