package io.camunda.connector.keycloak;

import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.keycloak.toolbox.ParameterToolbox;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class KeycloakOutput implements CherryOutput {

  public static final String OUTPUT_STATUS = "status";
  public static final String OUTPUT_STATUS_LABEL="Status";
  public static final String OUTPUT_STATUS_EXPLANATION="Status of operation";
  public String status;

  public static final String OUTPUT_DATE_OPERATION = "dateOperation";
  public static final String OUTPUT_DATE_OPERATION_LABEL="Date of operation";
  public static final String OUTPUT_DATE_OPERATION_EXPLANATION="Date when the operation is done in Keycloak";
  public Date dateOperation = null;

  public static final String OUTPUT_USER_ID = "userId";
  public static final String OUTPUT_USER_ID_LABEL="User Id";
  public static final String OUTPUT_USER_ID_EXPLANATION="User Id created by keycloak";
  public String userId;

  public static final String OUTPUT_USER= "user";
  public static final String OUTPUT_USER_LABEL="User";
  public static final String OUTPUT_USER_EXPLANATION="Keycloak user";
  public Map<String,Object> user;


  public static final String OUTPUT_LIST_USERS = "listUsers";
  public static final String OUTPUT_LIST_USERS_LABEL="List Users";
  public static final String OUTPUT_LIST_USERS_EXPLANATION="List of users found by the search";
  public List<Map<String,Object>> listUsers;

  @Override
  public List<Map<String, Object>> getOutputParameters() {
    return ParameterToolbox.getOutputParameters();
  }

}
