package io.camunda.connector.keycloak;

import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.keycloak.toolbox.ParameterToolbox;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class KeycloakOutput implements CherryOutput {

  public static final String OUTPUT_STATUS = "status";
  public String status;

  public static final String OUTPUT_DATE_OPERATION = "dateOperation";
  public Date dateOperation = null;

  public static final String OUTPUT_USER_ID = "userId";
  public String userId;

  public static final String OUTPUT_LIST_USERS = "listUsers";
  public List<Map<String,Object>> listUsers;

  @Override
  public List<Map<String, Object>> getOutputParameters() {
    return ParameterToolbox.getOutputParameters();
  }

}
