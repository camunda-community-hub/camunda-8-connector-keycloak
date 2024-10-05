
[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# camunda-8-connector-keycloak

A connector will be used to manage users and authorization on Keycloak.


# Principle


## Keycloak and Identity

A user must be registered if the "identity" is set up to access the platform (Operate, TaskList, Optimize).
It's possible to set up this application to use a "demo" user, but this configuration is not the purpose of this discussion.


You can branch an SSO via Keykloak or directly via Identity. However, you want only some users registered in the SSO to access Operate.
This is why users have to be registered to be accepted in Operate, or/and Tasklist, or/and Optimize

It's possible to connect Identity directly to an SSO. Then, users must register via Identity. Identity offers a Web Application for this, but not yet an API.

![Operate-identity.png](/doc/Operate-identity.png)

Identity can be connected to a Keycloak server. Then, Operate goes via Keycloak to check permissions, and users can be registered in Keycloack without needing to be in Identity.
Keycloak has an API, and this connector uses it.

![Operate-identity-Keycloak.png](/doc/Operate-identity-Keycloak.png)

In the following discussion, we consider the architecture with Identity and Keycloak.


## Authentication, Identification, Authorization

A tool like Operate checks the three levels. It communicates with Keycloak to check the user. Operate asks for a Login/Password if an LDAP is registered and sends it to Keycloak, which verifies it in the LDAP database to validate it.
Keyclaok asks this component for the user name if a Single Sign is connected.

Then, the Operate checks if the user can access it. This is the Authorization level. You can also give that information in Identity and Keycloak.
In the beginning, Identity created different roles in Keycloak. The user must be registered in Keycloak, and roles must be associated with the user.

## camunda-platform realm

At startup, Identity connects to Keycloak. it creates inside the Keycloak database artifacts
* a Realm name "camunda-platform"
  A Realm in Keycloak groups users and roles. It offers a suitable container.

* Roles Operate, Tasklist, Optimize
  Users associated with these roles can access the application.

## Connect to Keycloak

Identity creates a Realm inside Keycloak. Otherwise, when you want to connect, you must specify the realm where the user is.
This user must be permitted to add, delete, and update users in the camunda-platform realm.
This is why the connector uses two different realms:
* the realm used for the connection, for example, **master**
* the realm used to create users, in general, **camunda-realm**

Then there are three ways to connect:
* userName
* Client
* KeycloakUrlString

### userName
The connection contains:
* The server's URL
* the realm where the user is defined
* the user's name,
* the user's password
* a ClientID. The clientId contains the permissions. The clientId must be in the same realm as the user name.

## ClientID
ATTENTION: This connection does not pass our test. We keep the connection for testing purposes. If it's working with you, let us know!
You need
* The server's URL
* the realm where the ClientID is defined
* the clientID
* The clientSecret

## Connection URL
It's possible to use a Connection String to save the connection as a configMap with only one parameter and simplify the usage.

The connection string accepts two formats:
```
USERID;serverUrl;realm;clientid;adminusername;adminpasswor
CLIENTID;serverUrl;realm;clientid;clientsecret
```
The first parameter describes the type of connection. Then, according to the type, different parameters.




## Functions available in the connector

Functions available allow Camunda to manage users. Via the connector, all operations can be automated.

![KeycloakOperations.png](/doc/KeycloakOperations.png)

* Create a user: Even if Keycloak is linked to an SSO or an LDAP, it's mandatory to register users in Keycloak to pass the Authorization.
  The create user function registers a new user in a realm.

* Update a user: Should an email or password be changed, or should the permission be changed, removing the Operate role from a user?

* Search users: The search function starts with a username, first or last name, and email. It returns a pagination list of users.

* Delete user: For the userId, the user is deleted from Keycloak.

# Functions

The first parameter is the Keycloak function. According to the function you chose, different parameters show up.

All functions need access to the Keycloak. These parameters are shared with all functions.

| Name            | Description                                                          | Class             | Default | Level     |
|-----------------|----------------------------------------------------------------------|-------------------|---------|-----------|
| connectionType  | Type of Keycloak connection `USER` or `CLIENTID` or `CONNECTIONURL`  | java.lang.String  | `USER`  | REQUIRED  |

For connection type `USER`

| Name               | Description                                                    | Class             | Default  | Level     |
|--------------------|----------------------------------------------------------------|-------------------|----------|-----------|
| serverUrl          | URL to connect the Keycloak server (for `USER` and `CLIENTID`) | java.lang.String  |          | REQUIRED  |
| connectionRealm    | connection Realm                                               | java.lang.String  | `master` | REQUIRED  |
| clientId           | Client Id used for the connection, to retrieve permission      | java.lang.String  | `master` | REQUIRED  |
| adminUserName      | Connection based on User: the admin user                       | java.lang.String  | `admin`  | REQUIRED  |
| adminUserPassword  | Admin User Password                                            | java.lang.String  |          | REQUIRED  |

For connection type `CLIENTID`

| Name            | Description                                                    | Class             | Default  | Level     |
|-----------------|----------------------------------------------------------------|-------------------|----------|-----------|
| serverUrl       | URL to connect the Keycloak server (for `USER` and `CLIENTID`) | java.lang.String  |          | REQUIRED  |
| connectionRealm | connection Realm                                               | java.lang.String  | `master` | REQUIRED  |
| clientId        | Client Id used for the connection, to retrieve permission      | java.lang.String  |          | REQUIRED  |
| clientSecret    | Connection based on Client: the secret of clientId             | java.lang.String  |          | REQUIRED  |

For connection type `CONNECTIONURL`

| Name                   | Description             | Class             | Default | Level     |
|------------------------|-------------------------|-------------------|---------|-----------|
| keycloakConnectionUrl  | Keycloak Connection Url | java.lang.String  |         | REQUIRED  |

Check the "Connection URL" section.



Then, this parameter describes the function.

| Name              | Description                                                            | Class             | Default | Level     |
|-------------------|------------------------------------------------------------------------|-------------------|---------|-----------|
| keycloakFunction  | Function: `create-user`, `search-users`, `update-user`, `delete-user`  | java.lang.String  |         | REQUIRED  |



# Create user

## Principle

Create a user in Keycloak, in the real "camunda-platform." The user can access Operate, Tasklist, and Optimize. During the creation, roles can be assigned.


keycloakFunction = `create-user`

![KeycloakUser.png](/doc/KeycloakUser.png)

## Inputs
| Name               | Description                                                                                       | Class             | Default            | Level    |
|--------------------|---------------------------------------------------------------------------------------------------|-------------------|--------------------|----------|
| userRealm          | The user is created in a realm                                                                    | java.lang.String  | `camunda-platform` | REQUIRED |
| userName           | The user name must be unique in a realm's Keycloak                                                | java.lang.String  |                    | REQUIRED | 
| userFirstName      | "First name of the user                                                                           | Java.lang.String  |                    | OPTIONAL |
| userLastName       | Last name of the user                                                                             | Java.lang.String  |                    | OPTIONAL |
| userEmail          | Email of the user                                                                                 | Java.lang.String  |                    | OPTIONAL |
| userPassword       | Password of the user, if the password is managed by Keycloak                                      | Java.lang.String  |                    | OPTIONAL |
| userEnabled        | User enabled.                                                                                     | Java.lang.Boolean | false              | OPTIONAL |
| userRoles          | User roles assigned to the user, in `Operate`,`Tasklist`,`Optimize` (1)                           | Java.lang.String  |                    | OPTIONAL |
| errorIfUserExists  | If true, a BPMN error is thrown if the user already exists- else it's updated except the userName | Java.lang.Boolean | true               | OPTIONAL |

(1) UserRole: give a string separate by n like "Operate,Tasklist" or "Optimize"

## Output
| Name          | Description                          | Class             | Level    |
|---------------|--------------------------------------|-------------------|----------|
| userId        | Id of user created (or updated)      | java.lang.String  | REQUIRED |
| status        | Status of operation                  | java.lang.String  | REQUIRED |
| dateOperation | Date when the operation is performed | java.util.Date    | REQUIRED |

## BPMN Errors

| Name                   | Explanation                                                                        |
|------------------------|------------------------------------------------------------------------------------|
| KEYCLOAK_CONNECTION    | Error arrived during the Keycloak connection                                       |
| UNKNOWN_FUNCTION       | The function is unknown. There is a limited number of operations                   |
| USER_ALREADY_EXIST     | The username is unique in keycloak                                                 |
| CREATE_USER            | Create user failed                                                                 |
| USER_SET_PASSWORD      | password can't be set                                                              |
| UNKNOWN_USER           | Userid given is not found in Keycloak (Keycloak does not return a correct UserId)  |
| CANT_ACCESS_USER_ROLES | Can't access user roles                                                            |
| UNKNOWN_ROLE           | The Role given is not found in Keycloak                                            |
| DELETE_ROLE_USER       | Removing the role from the user fail                                               | 
| ADD_ROLE_USER          | Adding a role to the user fail                                                     |



# Search users

Search users on multiple criteria or from a user.
keycloakFunction = `search-users`

![KeycloakListOfUsers.png](/doc/KeycloakListOfUsers.png)

## Principle



## Inputs
| Name                  | Description                       | Class             | Default             | Level    |
|-----------------------|-----------------------------------|-------------------|---------------------|----------|
| userRealm             | Realm to search                   | java.lang.String  | `camunda-platform`  | OPTIONAL |     
| searchByUserId        | Search by User Id                 | java.lang.String  |                     | OPTIONAL |
| searchByUserName      | Search by User Name               | Java.lang.String  |                     | OPTIONAL |
| searchByUserFirstName | Search by First name of the user  | Java.lang.String  |                     | OPTIONAL |
| searchByUserLastName  | Search by Last name of the user   | Java.lang.String  |                     | OPTIONAL |
| searchByUUserEmail    | Search by Email of the user       | Java.lang.String  |                     | OPTIONAL |
| pageNumber            | Page number, start at 0           | Java.lang.Integer | 0                   | OPTIONAL |
| pageSize              | Number of record in the page      | Java.lang.Integer | 100                 | OPTIONAL |





## Output
| Name      | Description                       | Class            | Level    |
|-----------|-----------------------------------|------------------|----------|
| listUsers | List of users found by the search | java.lang.List   | REQUIRED |
| userId    | First userId of the list          | java.lang.String | REQUIRED |
| user      | First user of the list            | java.lang.Map    | REQUIRED |


## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|-------------------------------------------------------------------|
| KEYCLOAK_CONNECTION | Error arrived during the Keycloak connection                      |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations  |
| SEARCH_USER         | During search user(s) in Keycloak                                 |


# Update user

![KeycloakUserRoles.png](/doc/KeycloakUserRoles.png)

## Principle

From the userId, update the user's properties, such as first name, last name, and email. Also, update the list of assigned roles for the user.

keycloakFunction = `update-user`

## Inputs
| Name           | Description                                                             | Class             | Default            | Level    |
|----------------|-------------------------------------------------------------------------|-------------------|--------------------|----------|
| userRealm      | Realm to search                                                         | java.lang.String  | `camunda-platform` | OPTIONAL |     
| userId         | For the file to convert                                                 | java.lang.String  |                    | REQUIRED |
| userFirstName  | "First name of the user                                                 | Java.lang.String  |                    | OPTIONAL |
| userLastName   | Last name of the user                                                   | Java.lang.String  |                    | OPTIONAL |
| userEmail      | Email of the user                                                       | Java.lang.String  |                    | OPTIONAL |
| userPassword   | Password of the user, if the password is managed by Keycloak            | Java.lang.String  |                    | OPTIONAL |
| userEnabled    | User enabled.                                                           | Java.lang.Boolean | false              | OPTIONAL |
| userRoles      | User roles assigned to the user, in `Operate`,`Tasklist`,`Optimize` (1) | Java.lang.String  |                    | OPTIONAL |

(1) UserRole: give a string separate by n like "Operate,Tasklist" or "Optimize"

## Output
| Name          | Description                          | Class             | Level    |
|---------------|--------------------------------------|-------------------|----------|
| status        | Status of operation                  | java.lang.String  | REQUIRED |
| dateOperation | Date when the operation is performed | java.util.Date    | REQUIRED |

## BPMN Errors

| Name                 | Explanation                                                       |
|----------------------|-------------------------------------------------------------------|
| KEYCLOAK_CONNECTION  | Error arrived during the Keycloak connection                      |
| UNKNOWN_FUNCTION     | The function is unknown. There is a limited number of operations  |
| UPDATE_USER          | During the update user in Keycloak                                |
| CANT_UPDATE_USERNAME | Keycloak does not allow to update the username                    |
| UNKNOWN_USERID       | Userid given is not found in Keycloak                             |
| USER_SET_PASSWORD    | password can't be set                                             |


# Delete user

Delete a user from its userId

keycloakFunction = `update-user`
## Principle

Get a list of images and create a PDF from it.

## Inputs
| Name       | Description                          | Class             | Default             | Level    |
|------------|--------------------------------------|-------------------|---------------------|----------|
| userRealm  | Realm to search                      | java.lang.String  | `camunda-platform`  | OPTIONAL |     
| userId     | UserId to delete                     | java.lang.String  |                     | REQUIRED |


## Output
| Name          | Description                           | Class            | Level    |
|---------------|---------------------------------------|------------------|----------|
| status        | Status of operation                   | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed  | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                  | Explanation                                                       |
|-----------------------|-------------------------------------------------------------------|
| KEYCLOAK_CONNECTION   | Error arrived during the Keycloak connection                      |
| UNKNOWN_FUNCTION      | The function is unknown. There is a limited number of operations  |
| ERROR_DELETE_USER     | During deleting a user in Keycloa                                 |
| ERROR_UNKNOWN_USERID  | Userid given is not found in Keycloak                             |


# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependencies can be uploaded in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element template can be found in the [element-templates](/element-templates/keycloak-function.json) directory.
