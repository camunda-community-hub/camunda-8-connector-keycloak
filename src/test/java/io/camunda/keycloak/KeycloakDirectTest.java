package io.camunda.keycloak;

import io.camunda.connector.keycloak.user.SearchUserFunction;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;


// Source
// Camunda Identity : https://github.com/camunda-cloud/identity/blob/e6b7723496dbdac711e6ca2940c81d367ab26b0b/management-api/src/main/java/io/camunda/identity/impl/keycloak/config/KeycloakConfiguration.java
// Test: https://gist.github.com/thomasdarimont/c4e739c5a319cf78a4cff3b87173a84b

// How to run? See README.md on the project

public class KeycloakDirectTest {

  public static final String KEYCLOAK_URL = "http://localhost:18080/auth/admin";
  public static final String KEYCLOAK_REALM = "camunda-platform";
  public static final String KEYCLOAK_CLIENT_ID = "camunda-identity-resource-server";
  public static final String KEYCLOAK_CLIENT_SECRET = "HJNpwQx9AnmGrtcrCniOhwFxaI63ap1M";
  public static final String KEYCLOAK_ADMIN_USER_NAME = "admin";
  public static final String KEYCLOAK_ADMIN_USER_PASSWORD = "WkXmMnI3MO";

  private final static Logger logger = LoggerFactory.getLogger(KeycloakDirectTest.class.getName());

  public static void main(String[] args) {

    // String serverUrl = "http://localhost:18080/auth";
    // Caused by: javax.ws.rs.BadRequestException: HTTP 400 Bad Request
    //	at org.jboss.resteasy.client.jaxrs.internal.ClientInvocation.handleErrorStatus(ClientInvocation.java:264)
    //	at org.jboss.resteasy.client.jaxrs.internal.ClientInvocation.extractResult(ClientInvocation.java:240)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.extractors.BodyEntityExtractor.extractEntity(BodyEntityExtractor.java:64)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker.invokeSync(ClientInvoker.java:154)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker.invoke(ClientInvoker.java:115)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientProxy.invoke(ClientProxy.java:76)

    // String serverUrl = "http://localhost:18080";
    // Caused by: javax.ws.rs.NotFoundException: HTTP 404 Not Found
    // at org.jboss.resteasy.client.jaxrs.internal.ClientInvocation.handleErrorStatus(ClientInvocation.java:270)
    // at org.jboss.resteasy.client.jaxrs.internal.ClientInvocation.extractResult(ClientInvocation.java:240)
    // at org.jboss.resteasy.client.jaxrs.internal.proxy.extractors.BodyEntityExtractor.extractEntity(BodyEntityExtractor.java:64)
    // at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker.invokeSync(ClientInvoker.java:154)
    // at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker.invoke(ClientInvoker.java:115)
    // at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientProxy.invoke(ClientProxy.java:76)

    // String serverUrl = "http://localhost:18080/auth/admin";
// Caused by: javax.ws.rs.NotAuthorizedException: HTTP 401 Unauthorized
    //	at org.jboss.resteasy.client.jaxrs.internal.ClientInvocation.handleErrorStatus(ClientInvocation.java:266)
    //	at org.jboss.resteasy.client.jaxrs.internal.ClientInvocation.extractResult(ClientInvocation.java:240)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.extractors.BodyEntityExtractor.extractEntity(BodyEntityExtractor.java:64)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker.invokeSync(ClientInvoker.java:154)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker.invoke(ClientInvoker.java:115)
    //	at org.jboss.resteasy.client.jaxrs.internal.proxy.ClientProxy.invoke(ClientProxy.java:76)
    //	at jdk.proxy2/jdk.proxy2.$Proxy16.grantToken(Unknown Source)
    //	at org.keycloak.admin.client.token.TokenManager.grantToken(TokenManager.java:99)
    //	at org.keycloak.admin.client.token.TokenManager.getAccessToken(TokenManager.java:75)
    //	at org.keycloak.admin.client.token.TokenManager.getAccessTokenString(TokenManager.java:70)


    // idm-client needs to allow "Direct Access Grants: Resource Owner Password Credentials Grant"

    //		// Client "idm-client" needs service-account with at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
    //		Keycloak keycloak = KeycloakBuilder.builder() //
    //				.serverUrl(serverUrl) //
    //				.realm(realm) //
    //				.grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
    //				.clientId(clientId) //
    //				.clientSecret(clientSecret).build();

    // User "idm-admin" needs at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
    Keycloak keycloak = KeycloakBuilder.builder() //
        .serverUrl(KEYCLOAK_URL) //
        .realm(KEYCLOAK_REALM) //
        .grantType(OAuth2Constants.PASSWORD) //
        .clientId(KEYCLOAK_CLIENT_ID) //
        // .clientSecret(clientSecret) //
        .username(KEYCLOAK_ADMIN_USER_NAME) //
        .password(KEYCLOAK_ADMIN_USER_PASSWORD) //
        .build();
    RealmResource realmResource = keycloak.realm(KEYCLOAK_REALM);

    // Define user
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername("tester1");
    user.setFirstName("First");
    user.setLastName("Last");
    user.setEmail("tom+tester1@tdlabs.local");
    user.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));

    // Get realm
    UsersResource usersRessource = realmResource.users();

    // Create user (requires manage-users role)
    Response response = usersRessource.create(user);
    logger.info("Repsonse: {} {}", response.getStatus(), response.getStatusInfo());
    logger.info("location {}",response.getLocation());

    String userId = CreatedResponseUtil.getCreatedId(response);
    logger.info("User created with userId: {}}", userId);

    // Define password credential
    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setTemporary(false);
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue("test");

    UserResource userResource = usersRessource.get(userId);

    // Set password credential
    userResource.resetPassword(passwordCred);

    //        // Get realm role "tester" (requires view-realm role)
    RoleRepresentation testerRealmRole = realmResource.roles()//
        .get("tester").toRepresentation();
    //
    //        // Assign realm role tester to user
    userResource.roles().realmLevel() //
        .add(Arrays.asList(testerRealmRole));
    //
    //        // Get client
    ClientRepresentation app1Client = realmResource.clients() //
        .findByClientId(KEYCLOAK_CLIENT_ID).get(0);
    //
    //        // Get client level role (requires view-clients role)
    RoleRepresentation userClientRole = realmResource.clients().get(app1Client.getId()) //
        .roles().get("user").toRepresentation();
    //
    //        // Assign client level role to user
    userResource.roles() //
        .clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole));

    // Send password reset E-Mail
    // VERIFY_EMAIL, UPDATE_PROFILE, CONFIGURE_TOTP, UPDATE_PASSWORD, TERMS_AND_CONDITIONS
    //        usersRessource.get(userId).executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));

    // Delete User
    userResource.remove();

  }
}

