package com.a18.common.config;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.validation.Validator;

@Configuration
public class ValidatorEventRegister implements InitializingBean {

  @Autowired @Lazy ValidatingRepositoryEventListener validatingRepositoryEventListener;

  @Autowired @Lazy private Map<String, Validator> validators;

  private static final List<String> event = List.of(
      "beforeCreate",
      "afterCreate",
      "beforeSave",
      "afterSave",
      "beforeLinkSave",
      "afterLinkSave",
      "beforeDelete",
      "afterDelete"
  );

  @Override
  public void afterPropertiesSet() {
    validators.forEach((key, value) ->
        event.stream()
             .filter(key::startsWith)
             .findFirst()
             .ifPresent(p -> validatingRepositoryEventListener.addValidator(p, value)));
  }
}
