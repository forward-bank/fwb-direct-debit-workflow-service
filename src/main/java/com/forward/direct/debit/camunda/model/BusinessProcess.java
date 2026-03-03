package com.forward.direct.debit.camunda.model;

import java.util.HashMap;
import java.util.Map;

public class BusinessProcess {

    Map<String,Object> processVariablesMap = new HashMap<>();

     public Object getProcessVariable(String key) {
         return processVariablesMap.get(key);
     }
     public void setProcessVariable(String key, Object value) {
         processVariablesMap.put(key, value);
     }

}
