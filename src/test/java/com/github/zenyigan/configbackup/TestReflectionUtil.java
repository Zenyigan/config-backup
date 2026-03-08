package com.github.zenyigan.configbackup;

import java.lang.reflect.Field;

public class TestReflectionUtil {
  public static void setFinalField(Object instance, String fieldName, Object fieldValue) {
    try {
      Field field = instance.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(instance, fieldValue);
    } catch (Exception e) {
      throw new RuntimeException("Cannot modify field " + fieldName, e);
    }
  }
}
