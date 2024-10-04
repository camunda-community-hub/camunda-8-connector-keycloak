
[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# camunda-8-connector-keycloak

Connector to manage users, authorization on Keycloak.


# Principle


## Keycloak and Identity

To access the platform (Operate, TaskList, Optimize), a user must be registered if the "identity" is set up. 
It's possible to set up these application to use a "demo" user, but this configuration is not the purpose of this discusssion.


Via Keykloak or directly via Identity, you can branch a SSO. But you don't want all users registered in the SSO access Operate.
This is why users has to be registered to be accepted in Operate, or/and Tasklist, or/and Optimize

It's possible to connect Identity directly to an SSO. Then, users must be registered via Identity. Identity offered an Web Application to do that, but not yet an API.

 ![Operate-identity.png](/doc/Operate-identity.png)

Identity can be connected to a Keycloak server. Then, Operate goes via Keycloak to check the permission, and user can be registered in Keycloack, not need to be in Identity.
Keycloak has an API, and this connector use it.

![Operate-identity-Keycloak.png](/doc/Operate-identity-Keycloak.png)

In the following of the discussion, we consider the architecture with Identity and Keycloak.


## Authentication, Identification, Authorization

A tool like Operate check the three level. It communicate with Keycloak to check the user. If an LDAP is registered, Operate ask for a Login/Password, and send it to Keycloak, which verufury it on the LDAP database, to validate it.
If a Single Sign On is connected, then Keyclaok ask this component for the user name.

Then Operate check if the user can access it. This is the Autorization level. It's possible to give that information in Identity, but in Keycloak too.
At begining, Identity created different roles in Keycloak. The user must be registered in Keycloak, and roles associated with the user. 

## camunda-platform realm

At startup, Identity connect to Keycloak. it creates inside the Keuycloak database multiple artifacts
* a Realm name "camunda-platform"
A Realm in Keycloak groups users, roles. It's offering a good container.

* Roles Operate, Tasklist, Optimize
Users associated with these roles can access the application.

## Connect to Keycloak

Identity create a Realm inside Keycloak. Otherweise, wwhen you want to connect, you must specify the realm where the user is. 
And this user must have enought permussion to add, delete, update users in the camunda-platform realm.
This is why the connector use two different realm:
* the realm used for the connection, for example **master**
* the realm used to creates users, in general, **camunda-realm**

Then, there is three ways to connect:
* userName
* Client
* KeycloakUrlString

### userName
The connection contains:
* the server's URL
* the realm where the user is defined
* the user's name,
* the user's password
* a ClientID. The clientId contains the permissions. The clientId must be in the same realm as the user name.

## ClientID
ATTENTION: this connection does not pass our test. We keep the connection for test purpose: if it's working with you, let us know!
You need
* the server's URL
* the realm where the ClientID is defined
* the clientID
* the clientSecreat

## Connection URL
To have only one parameter, and simplify the usage if you want to save the connection as a configMap, it's possible to use a Connection String.

The connection string accept two format:
```
USERID;serverUrl;realm;clientid;adminusername;adminpasswor
CLIENTID;serverUrl;realm;clientid;clientsecret
```
The first parameter describe the type of connection. Then, according to the type, different parameters.




## Functions available in the connector

Functions avalable allow the management of users for Camunda. Via the connector, all operations can be automate.

![KeycloakOperations.png](/doc/KeycloakOperations.png)

* Create a user: even if Keycloak is link to a SSO or a LDAP, it's mandatory to register users in Keycloak, to pass the Authorization.
The create user function register a new user in a realm.

* Update a user: Email change? Password change? or change the permission, removing Operate role on a user?

* Search users: The search function starts with userName, first or last name, email. It return a pagination list of users.

* Delete user: for the userId, the user is deleted in Keycloak.

# Functions

The first parameter is the Keycloak function. According to the function you chose, different parameters show up.

All functions need the access to the Keycloak. These parameters are shared with all functions

| Name            | Description                                                        | Class             | Default | Level     |
|-----------------|--------------------------------------------------------------------|-------------------|---------|-----------|
| connectionType  | Type of Keycloak connection `USER` or `CLIENTID` or `CONNECTIONURL` | java.lang.String  | `USER`  | REQUIRED  |

For connection type `USER`

| Name               | Description                                                    | Class             | Default  | Level     |
|--------------------|----------------------------------------------------------------|-------------------|----------|-----------|
| serverUrl          | URL to connect the Keycloak server (for `USER` and `CLIENTID`) | java.lang.String  |          | REQUIRED  |
| connectionRealm    | connection Realm                                               | java.lang.String  | `master` | REQUIRED  |
| clientId           | Client Id used for the connection, to retrieve permission      | java.lang.String  | `master` | REQUIRED  |
| adminUserName      | Connection based on User : the admin user                      | java.lang.String  | `admin`  | REQUIRED  |
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

Check the "Connection URL" section



Then this parameter describe the function

| Name              | Description           | Class             | Default | Level     |
|-------------------|-----------------------|-------------------|---------|-----------|
| keycloakFunction  | Function: `create-user`  | java.lang.String  |         | REQUIRED  |



# Create user

## Principle

From parameters, create a new user in the `camunda-platform` realm.
keycloakFunction = `create-user`

![KeycloakUser.png](/doc/KeycloakUser.png)

## Inputs
| Name               | Description                                                                                        | Class             | Default            | Level    |
|--------------------|----------------------------------------------------------------------------------------------------|-------------------|--------------------|----------|
| userRealm          | The user is created in a realm                                                                     | java.lang.String  | `camunda-platform` | REQUIRED |
| userName           | The user name must be unique in a realm's Keycloak                                                 | java.lang.String  |                    | REQUIRED | 
| userFirstName      | "First name of the user                                                                            | Java.lang.String  |                    | OPTIONAL |
| userLastName       | Last name of the user                                                                              | Java.lang.String  |                    | OPTIONAL |
| userEmail          | Email of the user                                                                                  | Java.lang.String  |                    | OPTIONAL |
| userPassword       | Password of the user, if password is manage by Keycloak                                            | Java.lang.String  |                    | OPTIONAL |
| userEnabled        | User enabled.                                                                                      | Java.lang.Boolean | false              | OPTIONAL |
| userRoles          | User roles assigned to the user, in `Operate`,`Tasklist`,`Optimize` (1)                            | Java.lang.String  |                    | OPTIONAL |
| errorIfUserExists  | If true, an BPMN error is thrown if the user already exists- else it's updated except the userName | Java.lang.Boolean | true               | OPTIONAL |

(1) UserRole: give a string separate by n like "Operate,Tasklist" or "Optimize"

## Output
| Name          | Description                          | Class             | Level    |
|---------------|--------------------------------------|-------------------|----------|
| userId        | Id of user created (or updated)      | java.lang.String  | REQUIRED |
| status        | Status of operation                  | java.lang.String  | REQUIRED |
| dateOperation | Date when the operation is performed | java.util.Date    | REQUIRED |

## BPMN Errors

| Name                   | Explanation                                                                         |
|------------------------|-------------------------------------------------------------------------------------|
| KEYCLOAK_CONNECTION    | Error arrived during the Keycloak connection                                        |
| UNKNOWN_FUNCTION       | The function is unknown. There is a limited number of operation                     |
| USER_ALREADY_EXIST     | The username is unique in keycloak                                                  |
| CREATE_USER            | Create user failed                                                                  |
| USER_SET_PASSWORD      | Password can't be set                                                               |
| UNKNOWN_USER           | Userid given is not found in Keycloak (Keycloak does not return an correct UserId)  |
| CANT_ACCESS_USER_ROLES | Can't access user roles                                                             |
| UNKNOWN_ROLE           | Role given is not found in Keycloak                                                 |
| DELETE_ROLE_USER       | Removing the role from the user fail                                                | 
| ADD_ROLE_USER          | Adding a role to the user fail                                                      |



# Search users


![KeycloakListOfUsers.png](/doc/KeycloakListOfUsers.png)

## Principle

A list of PDF documents is in input, and one document is produced.
The resulting document contains all pages from the first document, the second document, and so on.
The order from the input list is used to produce the result.

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

| Name                | Explanation                                                     |
|---------------------|-----------------------------------------------------------------|
| KEYCLOAK_CONNECTION | Error arrived during the Keycloak connection                    |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operation |
| SEARCH_USER         | During search user(s) in Keycloak                               |


# Update user

![KeycloakUserRoles.png](/doc/KeycloakUserRoles.png)

## Principle

A watermark is added to each page. The watermark can be placed at the bottom, center, or lower, rotated, and set in size and color.

## Inputs
| Name           | Description                                                             | Class             | Default            | Level    |
|----------------|-------------------------------------------------------------------------|-------------------|--------------------|----------|
| userRealm      | Realm to search                                                         | java.lang.String  | `camunda-platform` | OPTIONAL |     
| userId         | For the file to convert                                                 | java.lang.String  |                    | REQUIRED |
| userFirstName  | "First name of the user                                                 | Java.lang.String  |                    | OPTIONAL |
| userLastName   | Last name of the user                                                   | Java.lang.String  |                    | OPTIONAL |
| userEmail      | Email of the user                                                       | Java.lang.String  |                    | OPTIONAL |
| userPassword   | Password of the user, if password is manage by Keycloak                 | Java.lang.String  |                    | OPTIONAL |
| userEnabled    | User enabled.                                                           | Java.lang.Boolean | false              | OPTIONAL |
| userRoles      | User roles assigned to the user, in `Operate`,`Tasklist`,`Optimize` (1) | Java.lang.String  |                    | OPTIONAL |

(1) UserRole: give a string separate by n like "Operate,Tasklist" or "Optimize"

## Output
| Name          | Description                          | Class             | Level    |
|---------------|--------------------------------------|-------------------|----------|
| status        | Status of operation                  | java.lang.String  | REQUIRED |
| dateOperation | Date when the operation is performed | java.util.Date    | REQUIRED |

## BPMN Errors

| Name                 | Explanation                                                     |
|----------------------|-----------------------------------------------------------------|
| KEYCLOAK_CONNECTION  | Error arrived during the Keycloak connection                    |
| UNKNOWN_FUNCTION     | The function is unknown. There is a limited number of operation |
| UPDATE_USER          | During update user in Keycloak                                  |
| CANT_UPDATE_USERNAME | Keycloak does not allow to update the username                  |
| UNKNOWN_USERID       | Userid given is not found in Keycloak                           |
| USER_SET_PASSWORD    | Password can't be set                                           |


# Delete user


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

| Name                  | Explanation                                                     |
|-----------------------|-----------------------------------------------------------------|
| KEYCLOAK_CONNECTION   | Error arrived during the Keycloak connection                    |
| UNKNOWN_FUNCTION      | The function is unknown. There is a limited number of operation |
| ERROR_DELETE_USER     | During delete user in Keycloa                                   |
| ERROR_UNKNOWN_USERID  | Userid given is not found in Keycloak                           |


# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependencies can be upload in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element template can be found in the [element-templates](/element-template/keycloak-function.json) directory.

