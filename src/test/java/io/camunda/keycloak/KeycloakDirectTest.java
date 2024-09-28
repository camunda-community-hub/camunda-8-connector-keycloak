package io.camunda.keycloak;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.keycloak.toolbox.KeycloakOperation;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Source
// Camunda Identity : https://github.com/camunda-cloud/identity/blob/e6b7723496dbdac711e6ca2940c81d367ab26b0b/management-api/src/main/java/io/camunda/identity/impl/keycloak/config/KeycloakConfiguration.java
// Test: https://gist.github.com/thomasdarimont/c4e739c5a319cf78a4cff3b87173a84b

// How to run? See README.md on the project

public class KeycloakDirectTest {

  /**
   * The connection must be done to the realù MASTER with clientID "admin-cli"
   * Then from that, it's possible to manage different realm
   */
  public static final String KEYCLOAK_URL = "http://localhost:18080/auth";
  public static final String KEYCLOAK_REALM_MASTER = "master";
  public static final String KEYCLOAK_REALM_CAMUNDA = "camunda-platform";
  public static final String KEYCLOAK_CLIENT_ID = "admin-cli";
  public static final String KEYCLOAK_CLIENT_SECRET = "admin-cli";

  public static final String KEYCLOAK_ADMIN_USER_NAME = "admin";
  public static final String KEYCLOAK_ADMIN_USER_PASSWORD = "admin";

  private final static Logger logger = LoggerFactory.getLogger(KeycloakDirectTest.class.getName());

  public static void main(String[] args) {

    // User "idm-admin" needs at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
    Keycloak keycloak = connectByUser();
    RealmResource realmResource = keycloak.realm(KEYCLOAK_REALM_MASTER);
    playUser(keycloak);
    playRoles(keycloak);
    keycloak.close();

    // now connect by clientID
    /*
    keycloak = connectByClient();
    playRoles(keycloak);
    playUser(keycloak);
     */

    // Use Keycloak Operation
    KeycloakOperation keycloakOperation = connectByKeycloakOperation();
    String userId = keycloakOperation.addUser(KEYCLOAK_REALM_CAMUNDA, // realm
        "Walter.Bates", // username
        "Walter", //firstName,
        "Bates", // lastName,
        "walter.bates@camunda.com", // String email,
        "bpm", // userPassword,
        true // boolean enabledUser
    );

    try{
      keycloakOperation.addUser(KEYCLOAK_REALM_CAMUNDA, // realm
          "Walter.Bates", // username
          "Walter", //firstName,
          "Bates", // lastName,
          "walter.bates@camunda.com", // String email,
          "bpm", // userPassword,
          true // boolean enabledUser
      );
      // we must have an exception and never come here
      assert(false);
    } catch (ConnectorException ce) {
      assert(ce.getErrorCode().equals(KeycloakOperation.ERROR_USER_ALREADY_EXIST));
    }

    keycloakOperation.deleteUser(KEYCLOAK_REALM_CAMUNDA, userId);

    logger.info("That's all folks");
  }

  public static KeycloakOperation connectByKeycloakOperation() {

    KeycloakOperation keycloakOperation = new KeycloakOperation();
    keycloakOperation.openByUser(KEYCLOAK_URL, //
        KEYCLOAK_REALM_MASTER, //
        KEYCLOAK_CLIENT_ID, //
        KEYCLOAK_ADMIN_USER_NAME, //
        KEYCLOAK_ADMIN_USER_PASSWORD, //
        "Test");
    return keycloakOperation;
  }

  public static Keycloak connectByUser() {
    logger.info("Connect Keycloak by USER");
    Keycloak keycloak = KeycloakBuilder.builder() //
        .serverUrl(KEYCLOAK_URL) //
        .realm(KEYCLOAK_REALM_MASTER) // realm
        .grantType(OAuth2Constants.PASSWORD) //
        .clientId(KEYCLOAK_CLIENT_ID) //
        .username(KEYCLOAK_ADMIN_USER_NAME) //
        .password(KEYCLOAK_ADMIN_USER_PASSWORD) //
        // .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
        .build();
    return keycloak;
  }

  public static Keycloak connectByClient() {
    logger.info("Connect Keycloak by CLIENT");

    Keycloak keycloak = KeycloakBuilder.builder() //
        .serverUrl(KEYCLOAK_URL) //
        .realm(KEYCLOAK_REALM_MASTER) // realm
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
        .clientId("test") //
        .clientSecret("3QxvHLlD8aFnOjghEU90Bo20bMNhp76t") //
        .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build()).build();

/*
    Keycloak keycloak = KeycloakBuilder.builder() //
        .serverUrl(KEYCLOAK_URL) //
        .realm("camunda-platform")
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
        .clientId("camunda-identity-resource-server") //
        .clientSecret("Y8Vbusr39pZKCqXh9Wo7aB5MmzEBSNGB") //
        .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build()).build();

 */
    return keycloak;
  }

  /**
   * @param keycloak
   */
  private static void playRoles(Keycloak keycloak) {
    RolesResource rolesResource = keycloak.realm(KEYCLOAK_REALM_CAMUNDA).roles();
    List<RoleRepresentation> roles = rolesResource.list();
    logger.info("Roles {}", roles);
  }

  /**
   * @param keycloak
   */
  private static void playUser(Keycloak keycloak) {
    RealmResource realmResource = keycloak.realm(KEYCLOAK_REALM_CAMUNDA);
    UsersResource usersRessource = realmResource.users();

    // Define user
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername("Walter.Bates");
    user.setFirstName("Walter");
    user.setLastName("Bates");
    user.setEmail("walter.bates@camunda.com");
    user.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));

    long markerTime = System.currentTimeMillis();
    Response response = usersRessource.create(user);
    logger.info("Response: status[{}} info[{}] location:[{}]", response.getStatus(), response.getStatusInfo(),
        response.getLocation());

    String userId = CreatedResponseUtil.getCreatedId(response);
    logger.info("User created with userId: {} in {} ms", userId, System.currentTimeMillis() - markerTime);

    // Define password credential
    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setTemporary(false);
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue("bpm");

    markerTime = System.currentTimeMillis();
    UserResource userResource = usersRessource.get(userId);
    logger.info("UserResource from userId[{}]get in  {} ms", userId, System.currentTimeMillis() - markerTime);

    // Set password credential
    markerTime = System.currentTimeMillis();
    userResource.resetPassword(passwordCred);
    logger.info("Password created in {} ms", System.currentTimeMillis() - markerTime);

    // Get realm role "tester" (requires view-realm role)
    markerTime = System.currentTimeMillis();
    RoleRepresentation operateRole = realmResource.roles().get("Operate").toRepresentation();
    logger.info("Role[operate] get in {} ms", System.currentTimeMillis() - markerTime);

    //
    // Assign realm role tester to user
    markerTime = System.currentTimeMillis();
    userResource.roles().realmLevel().add(Arrays.asList(operateRole));
    logger.info("Role[operate] set to user in {} ms", System.currentTimeMillis() - markerTime);

    // Send password reset E-Mail
    // VERIFY_EMAIL, UPDATE_PROFILE, CONFIGURE_TOTP, UPDATE_PASSWORD, TERMS_AND_CONDITIONS
    //        usersRessource.get(userId).executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));

    // Delete User
    markerTime = System.currentTimeMillis();
    userResource.remove();
    logger.info("User deleted in {} ms", System.currentTimeMillis() - markerTime);
  }

}

