package io.camunda.connector.keycloak;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.toolbox.KeycloakOperation;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import io.camunda.connector.keycloak.function.CreateUserFunction;
import io.camunda.connector.keycloak.function.DeleteUserFunction;
import io.camunda.connector.keycloak.function.SearchUserFunction;
import io.camunda.connector.keycloak.function.UpdateUserFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OutboundConnector(name = "KeycloakFunction", inputVariables = { KeycloakInput.INPUT_KEYCLOAK_FUNCTION,
    KeycloakInput.INPUT_CONNECTION_TYPE, //
    KeycloakInput.INPUT_KEYCLOAK_CONNECTION_URL, //
    KeycloakInput.INPUT_SERVER_URL, //
    KeycloakInput.INPUT_CONNECTION_REALM, // real to connect
    KeycloakInput.INPUT_ADMIN_USER_NAME, //
    KeycloakInput.INPUT_ADMIN_USER_PASSWORD, //
    KeycloakInput.INPUT_CLIENT_ID, //
    KeycloakInput.INPUT_CLIENT_SECRET, //

    KeycloakInput.INPUT_USER_REALM, // all function
    KeycloakInput.INPUT_USER_NAME, // addUser
    KeycloakInput.INPUT_USER_FIRSTNAME, // addUser
    KeycloakInput.INPUT_USER_LASTNAME, // addUser
    KeycloakInput.INPUT_USER_EMAIL, // addUser
    KeycloakInput.INPUT_USER_PASSWORD, // addUser
    KeycloakInput.INPUT_USER_ENABLED, // addUser
    KeycloakInput.INPUT_ERROR_IF_USER_EXISTS, // addUSer

    KeycloakInput.INPUT_SEARCH_BY_USER_ID, // SearchUser
    KeycloakInput.INPUT_SEARCH_BY_USER_NAME, // SearchUser
    KeycloakInput.INPUT_SEARCH_BY_USER_FIRSTNAME, // SearchUser
    KeycloakInput.INPUT_SEARCH_BY_USER_LASTNAME, // SearchUser
    KeycloakInput.INPUT_SEARCH_BY_USER_EMAIL, // SearchUser

    KeycloakInput.INPUT_USER_ROLES, // AddUsers
    KeycloakInput.INPUT_USER_ID, // Searchuser/DeleteUser
    KeycloakInput.INPUT_PAGE_NUMBER, KeycloakInput.INPUT_PAGE_SIZE }, type = "c-keycloak-function")

public class KeycloakFunction implements OutboundConnectorFunction, CherryConnector {
  public static final String ERROR_UNKNOWN_FUNCTION = "UNKNOWN_FUNCTION";
  public static final String ERROR_UNKNOWN_FUNCTION_LABEL = "The function is unknown. There is a limited number of operation";
  public static final List<Class<?>> allFunctions = Arrays.asList(CreateUserFunction.class, // add User
      DeleteUserFunction.class, // removeUser
      SearchUserFunction.class, // Search User
      UpdateUserFunction.class); // Update user
  private static final String WORKER_LOGO = "data:image/svg+xml,%3C?xml version='1.0' encoding='UTF-8' standalone='no'?%3E%3Csvg   width='17.999758'   height='16.226999'   viewBox='0 0 4.7623956 4.2933293'   version='1.1'   id='svg14'   xmlns='http://www.w3.org/2000/svg'   xmlns:svg='http://www.w3.org/2000/svg'%3E  %3Cdefs     id='defs14' /%3E  %3Cpath     d='M 0.61762502,1.0508277 1.2268303,-5.736842e-4 3.6800646,-0.001 4.2860725,1.0610592 4.2868186,3.1853909 3.6802777,4.2465975 1.2276829,4.2474501 0.61144344,3.1853909 Z'     style='display:inline;fill:%234d4d4d;fill-opacity:1;stroke-width:0.028199'     id='path1' /%3E  %3Cpath     d='M 0.61144344,3.1850712 H 1.7172001 L 1.1098066,2.1149119 1.6036935,1.0510408 0.61762502,1.0508277 0,2.1235448'     style='fill:%23ededed;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path2' /%3E  %3Cpath     d='M 2.0524974,3.1850712 H 2.860579 L 3.5752975,2.1434751 2.8730488,1.0610592 H 1.922258 L 1.3496093,2.1070251 Z'     style='fill:%23e0e0e0;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path3' /%3E  %3Cpath     d='M 0,2.1234382 0.61155002,3.1853909 H 1.7172001 l -0.600679,-1.058329 z'     style='fill:%23acacac;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path4' /%3E  %3Cpath     d='M 1.3614395,2.1236514 2.0524974,3.1854975 H 2.860579 L 3.5641067,2.1240777 Z'     style='fill:%239e9e9e;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path5' /%3E  %3Cpath     d='M 1.6351343,2.1243974 1.4264527,2.1850409 1.226404,2.1240777 2.0435448,0.7078566 2.2479632,1.0612724'     style='fill:%2300b8e3;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path6' /%3E  %3Cpath     d='M 2.2472172,3.1851777 2.043758,3.5402988 1.5009514,2.9555001 1.2259777,2.124504 v -4.263e-4 h 0.40905'     style='fill:%2333c6e9;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path7' /%3E  %3Cpath     d='m 1.226404,2.1240777 h -3.198e-4 v 3.197e-4 L 1.0216658,2.4784527 0.81650134,2.1259961 1.0245435,1.7651198 1.6350277,0.7078566 h 0.4086237'     style='fill:%23008aaa;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path8' /%3E  %3Cpath     d='M 3.4750067,3.1850712 H 4.7124949 L 4.7117488,1.0609527 H 3.4749001 Z'     style='fill:%23d4d4d4;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path9' /%3E  %3Cpath     d='m 3.4750067,2.1270619 v 1.058329 h 1.23525 v -1.058329 z'     style='fill:%23919191;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path10' /%3E  %3Cpath     d='M 2.0438645,3.5406185 H 1.6350277 L 1.0216658,2.4784527 1.2260842,2.124504 Z'     style='fill:%2300b8e3;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path11' /%3E  %3Cpath     d='M 3.6788922,2.1240777 2.8615382,3.5404054 C 2.7864001,3.4299896 2.6576527,3.1853909 2.6576527,3.1853909 L 3.2708014,2.1239711 Z'     style='fill:%23008aaa;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path12' /%3E  %3Cpath     d='M 3.2698422,3.5404054 2.8615382,3.5402988 3.6789988,2.1240777 3.8833106,1.7705553 4.0880488,2.126529 M 3.6788922,2.1240777 H 3.270908 L 2.6575461,1.0611658 2.860579,0.70796318 3.3570238,1.3391237 Z'     style='fill:%2300b8e3;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path13' /%3E  %3Cpath     d='m 3.8832041,1.770129 v 3.197e-4 L 3.6788922,2.1240777 2.8606856,0.70806976 3.2700554,0.70828292 Z'     style='fill:%2333c6e9;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.0352487'     id='path14' /%3E%3C/svg%3E";
  private final Logger logger = LoggerFactory.getLogger(KeycloakFunction.class.getName());



  @Override
  public KeycloakOutput execute(OutboundConnectorContext outboundConnectorContext) throws Exception {

    KeycloakInput keycloakInput = outboundConnectorContext.bindVariables(KeycloakInput.class);
    // search the sub-function referenced
    String function = keycloakInput.getKeycloakFunction();
    long beginTime = System.currentTimeMillis();

    // do the connection
    KeycloakOperation keycloakOperation = new KeycloakOperation();

    // connect (a Connector exception will be thrown in case of issue)
    if (KeycloakInput.INPUT_CONNECTION_TYPE_V_USER.equals(keycloakInput.getConnectionType())
        || keycloakInput.getConnectionType() == null || keycloakInput.getConnectionType().isEmpty()) {
      keycloakOperation.openByUser(keycloakInput.getServerUrl(), //
          keycloakInput.getConnectionRealm(), //
          keycloakInput.getClientId(), //
          keycloakInput.getAdminUserName(), //
          keycloakInput.getAdminUserPassword(), //
          function);

    } else if (KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID.equals(keycloakInput.getConnectionType())) {
      keycloakOperation.openByClientId(keycloakInput.getServerUrl(), //
          keycloakInput.getConnectionRealm(),//
          keycloakInput.getClientId(), //
          keycloakInput.getClientSecret(), //
          function);

    } else if (KeycloakInput.INPUT_CONNECTION_TYPE_V_KEYCLOAK_CONNECTION_URL.equals(
        keycloakInput.getConnectionType())) {
      keycloakOperation.openByConnectionURL(keycloakInput.getKeycloakConnectionUrl(), function);
    } else {
      throw new ConnectorException(KeycloakOperation.ERROR_KEYCLOAK_CONNECTION,
          "Unknown connectionType connection[" + keycloakInput.getConnectionType() + "] expect["
              + KeycloakInput.INPUT_CONNECTION_TYPE_V_USER + "|" + KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID + "|"
              + KeycloakInput.INPUT_CONNECTION_TYPE_V_KEYCLOAK_CONNECTION_URL + "]");
    }
    long endKeycloakConnectionTime = System.currentTimeMillis();

    if (endKeycloakConnectionTime - beginTime > 500)
      logger.info("KeycloakFunction receive function [{}]  to Keycloack server {} in {} ms", function,
          keycloakOperation.getKeycloakSignature(), endKeycloakConnectionTime - beginTime);

    for (KeycloakSubFunction inputSubFunction : getListSubFunctions()) {
      if (inputSubFunction.getSubFunctionType().equals(function)) {
        KeycloakOutput keycloakOutput = inputSubFunction.executeSubFunction(keycloakOperation, keycloakInput,
            outboundConnectorContext);
        logger.info("KeycloakFunction End function [{}] in {} ms", function, System.currentTimeMillis() - beginTime);
        keycloakOperation.close();
        return keycloakOutput;
      }
    }
    // Close Keycloak client
    keycloakOperation.close();

    throw new ConnectorException(ERROR_UNKNOWN_FUNCTION, "Keycloak Function Unknown [" + function + "]");
  }

  @Override
  public String getDescription() {
    return "Keycloak operation (add/remove user";
  }

  @Override
  public String getLogo() {
    return WORKER_LOGO;
  }

  @Override
  public String getCollectionName() {
    return "UserManagement";
  }

  @Override
  public Map<String, String> getListBpmnErrors() {
    Map<String, String> allErrors = new HashMap<>();
    allErrors.put(ERROR_UNKNOWN_FUNCTION, ERROR_UNKNOWN_FUNCTION_LABEL);

    for (KeycloakSubFunction subFunction : getListSubFunctions()) {
      allErrors.putAll(subFunction.getSubFunctionListBpmnErrors());
    }

    return allErrors;
  }

  @Override
  public Class<?> getInputParameterClass() {
    return KeycloakInput.class;
  }

  @Override
  public Class<?> getOutputParameterClass() {
    return KeycloakOutput.class;
  }

  @Override
  public List<String> getAppliesTo() {
    return List.of("bpmn:ServiceTask");
  }

  public List<KeycloakSubFunction> getListSubFunctions() {
    List<KeycloakSubFunction> listSubFunction = new ArrayList<>();

    for (Class<?> classFunction : allFunctions) {
      try {
        Constructor<?> constructor = classFunction.getConstructor();
        KeycloakSubFunction inputSubFunction = (KeycloakSubFunction) constructor.newInstance();
        listSubFunction.add(inputSubFunction);
      } catch (Exception e) {
        logger.error("Can't call a constructor on {} : {}", classFunction.getName(), e.toString());
      }

    }
    return listSubFunction;
  }

  /**
   * Return the common parameters to all sub functions
   *
   * @return
   */
  public static List<RunnerParameter> getInputCommonParameters() {
    return Arrays.asList(RunnerParameter.getInstance(KeycloakInput.INPUT_CONNECTION_TYPE, //
                KeycloakInput.INPUT_CONNECTION_TYPE_LABEL, //
                String.class, //
                KeycloakInput.INPUT_CONNECTION_TYPE_V_USER, //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_CONNECTION_TYPE_EXPLANATION) //
            .addChoice(KeycloakInput.INPUT_CONNECTION_TYPE_V_USER, "Admin User") //
            .addChoice(KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID, "ClientId/Secret") //
            .addChoice(KeycloakInput.INPUT_CONNECTION_TYPE_V_KEYCLOAK_CONNECTION_URL, "KeycloakUrl")
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_SERVER_URL, //
                KeycloakInput.INPUT_SERVER_URL_LABEL, //
                String.class, //
                "http://localhost:18080/auth", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_SERVER_URL_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE, Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_USER,
                KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID))
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_CONNECTION_REALM, //
                KeycloakInput.INPUT_CONNECTION_REALM_LABEL, //
                String.class, //
                "master", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_CONNECTION_REALM_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE, Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_USER,
                KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID))
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_CLIENT_ID, //
                KeycloakInput.INPUT_CLIENT_ID_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_CLIENT_ID_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE, Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_USER,
                KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID))
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_CLIENT_SECRET, //
                KeycloakInput.INPUT_CLIENT_SECRET_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_CLIENT_SECRET_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE,
                Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_CLIENT_ID))
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_ADMIN_USER_NAME, //
                KeycloakInput.INPUT_ADMIN_USER_NAME_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_ADMIN_USER_NAME_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE,
                Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_USER))
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_ADMIN_USER_PASSWORD, //
                KeycloakInput.INPUT_ADMIN_USER_PASSWORD_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_ADMIN_USER_PASSWORD_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE,
                Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_USER))
            .setGroup("Keycloak connection"),//

        RunnerParameter.getInstance(KeycloakInput.INPUT_KEYCLOAK_CONNECTION_URL, //
                KeycloakInput.INPUT_KEYCLOAK_CONNECTION_URL_LABEL, //
                String.class, //
                "", //
                RunnerParameter.Level.REQUIRED, //
                KeycloakInput.INPUT_KEYCLOAK_CONNECTION_URL_EXPLANATION)
            .addCondition(KeycloakInput.INPUT_CONNECTION_TYPE,
                Arrays.asList(KeycloakInput.INPUT_CONNECTION_TYPE_V_KEYCLOAK_CONNECTION_URL))
            .setGroup("Keycloak connection"));//

  }
}
