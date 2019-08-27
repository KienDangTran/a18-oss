package com.a18.common.exception;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.springframework.util.StringUtils;

public class LowerCamelCaseClassNameTypeResolver extends TypeIdResolverBase {
  @Override
  public String idFromValue(Object value) {
    String simpleName = value.getClass().getSimpleName();
    return StringUtils.uncapitalize(simpleName);
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return idFromValue(value);
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.CUSTOM;
  }
}
