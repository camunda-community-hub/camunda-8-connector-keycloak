# Test

## TestParameters

This JUnit test verify parameters show up correctly then Cherry Runtime can generate the
Element-template correctly

## Keycloak Direct Test

Run the test under io.camunda.keycloack.KeycloakDirectTest

Edit the file and change the value of constant

```java
public static final String KEYCLOAK_URL = "http://localhost:18080/auth/admin";
public static final String KEYCLOAK_REALM = "camunda-platform";
public static final String KEYCLOAK_CLIENT_ID = "camunda-identity-resource-server";
public static final String KEYCLOAK_CLIENT_SECRET = "HJNpwQx9AnmGrtcrCniOhwFxaI63ap1M";
public static final String KEYCLOAK_ADMIN_USER_NAME = "admin";
public static final String KEYCLOAK_ADMIN_USER_PASSWORD = "WkXmMnI3MO";
```

* URL is the url to connect Keycloak

* REALM is the Realm created by Identity. This does not change
* CLIENT_ID is the client used by Identity. This does not change. The client is accessible in the
  List of client

![KeycloakListOfClients.png](/doc/KeycloakListOfClients.png)

* CLIENT_SECRET is accessible in the client detail.
  ![KeycloakClientSecret.png](/doc/KeycloakClientSecret.png)

* ADMIN_USER_NAME is the name used to access the admin tool
* ADMIN_USER_PASSWORD is the password used for the user. Camunda Helm chart generate a password. It
  is accessible via the command

```shell

kubectl get secret --namespace camunda "camunda-keycloak" -o jsonpath="{.data.admin-password}" | base64 --decode
``` 

Run the test. it will create a user, search for it, create a role, create a group, and clean
everything.
