package io.camunda.keycloak;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.keycloak.KeycloakFunction;
import io.camunda.connector.keycloak.toolbox.KeycloakSubFunction;
import io.camunda.connector.keycloak.toolbox.ParameterToolbox;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestParameters {
  static Logger logger = LoggerFactory.getLogger(TestParameters.class.getName());

  @Test
  public void uniqParameters() {
    List<Map<String, Object>> parameterList = ParameterToolbox.getInputParameters();
    // each parameters are unique
    // some parameters are register in multuiple sub function. They must be unique
    Set<String> nameSet = new HashSet<>();
    for (Map<String, Object> parameterMap : parameterList) {
      String parameterName = (String) parameterMap.get(CherryInput.PARAMETER_MAP_NAME);
      assert (parameterName != null);
      assert (!nameSet.contains(parameterName));
      nameSet.add(parameterName);
    }
  }

  @Test
  public void checkConditions() {
    List<Map<String, Object>> parameterList = ParameterToolbox.getInputParameters();
    KeycloakFunction keycloakFunction = new KeycloakFunction();

    Set<String> allSubtypes = keycloakFunction.getListSubFunctions()
        .stream()
        .map(KeycloakSubFunction::getSubFunctionType)
        .collect(Collectors.toSet());

    for (Map<String, Object> parameterMap : parameterList) {
      String parameterName = (String) parameterMap.get(CherryInput.PARAMETER_MAP_NAME);
      String parameterContion = (String) parameterMap.get(CherryInput.PARAMETER_MAP_CONDITION);
      String conditionEquals = (String) parameterMap.get(CherryInput.PARAMETER_MAP_CONDITION_EQUALS);
      List<String> conditionOneOf = (List<String>) parameterMap.get(CherryInput.PARAMETER_MAP_CONDITION_ONE_OF);

      // search where this name appears
      for (KeycloakSubFunction keycloakSubFunction : keycloakFunction.getListSubFunctions()) {
        List<RunnerParameter> listParameters = keycloakSubFunction.getInputsParameter();
        for (RunnerParameter parameter : listParameters) {
          if (!parameter.name.equals(parameterName))
            continue;
          logger.info("FunctionType[{}] Parameter[{}], conditionEquals[{}] conditionOneOf[{}]",
              keycloakSubFunction.getSubFunctionType(), parameterName, //
              conditionEquals,
              conditionOneOf == null ? "null" : conditionOneOf.stream().collect(Collectors.joining(",")));

          // two options:
          // condition may be something, or match a subtype. If it match a subtype, then it must match ALL subtype where the condition is visible

          if (conditionEquals != null) {
            if (allSubtypes.contains(conditionEquals))
              Assertions.assertEquals(conditionEquals, keycloakSubFunction.getSubFunctionType());
          }
          if (conditionOneOf != null && !conditionOneOf.isEmpty()) {
            boolean matchSubtype = false;
            for (String one : conditionOneOf) {
              if (allSubtypes.contains(one))
                matchSubtype = true;
            }
            if (matchSubtype)
              if (allSubtypes.contains(conditionEquals))
                assert (conditionOneOf.contains(keycloakSubFunction.getSubFunctionType()));
          }
        } // end all parameters
      }
    }
  }

  @Test
  public void checkCompletude() {
    List<Map<String, Object>> parameterList = ParameterToolbox.getInputParameters();
    Set<String> parameterNameSet = parameterList.stream() //
        .map(t -> (String) t.get(CherryInput.PARAMETER_MAP_NAME)) //
        .collect(Collectors.toSet());

    KeycloakFunction keycloakFunction = new KeycloakFunction();
    for (KeycloakSubFunction keycloakSubFunction : keycloakFunction.getListSubFunctions()) {
      List<RunnerParameter> listParameters = keycloakSubFunction.getInputsParameter();

      for (RunnerParameter runnerParameter : listParameters) {
        assert (parameterNameSet.contains(runnerParameter.getName()));
      }
    }
  }

}
