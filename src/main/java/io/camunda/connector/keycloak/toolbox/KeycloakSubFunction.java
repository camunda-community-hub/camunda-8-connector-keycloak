package io.camunda.connector.keycloak.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakInput;
import io.camunda.connector.keycloak.KeycloakOutput;

import java.util.List;
import java.util.Map;
import org.keycloak.admin.client.Keycloak;

public interface KeycloakSubFunction {
  KeycloakOutput executeSubFunction(Keycloak keycloak, KeycloakInput pdfInput, OutboundConnectorContext context) throws ConnectorException;

  List<RunnerParameter> getInputsParameter();

  List<RunnerParameter> getOutputsParameter();

  Map<String, String> getSubFunctionListBpmnErrors();

  String getSubFunctionName();

  String getSubFunctionDescription();

  String getSubFunctionType();


}
