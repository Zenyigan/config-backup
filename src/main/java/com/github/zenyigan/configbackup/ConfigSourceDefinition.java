package com.github.zenyigan.configbackup;

import java.io.File;
import java.util.regex.Pattern;

public final class ConfigSourceDefinition {
  private final String definitionName;
  private final File source;

  private static final Pattern ASCII_ALPHANUMERIC_OR_DASH = Pattern.compile("^[a-z0-9-]+$");
  private static final String DESCRIBE_DEFINITION_NAME =
      "definitionName must be at least one char and valid chars are lower ASCII alphanumeric or dash.";

  public ConfigSourceDefinition(String definitionName, File source) {
    checkSourceDefinition(definitionName);
    if (source == null) {
      throw new IllegalArgumentException("source must not be null.");
    }
    this.definitionName = definitionName;
    this.source = source;
  }

  public String definitionName() {
    return definitionName;
  }

  public File source() {
    return source;
  }

  @Override
  public String toString() {
    return "ConfigSourceDefinition{"
        + "definitionName='"
        + definitionName
        + '\''
        + ", source="
        + source
        + '}';
  }

  private static void checkSourceDefinition(String definitionName) {
    if (definitionName == null) {
      throw new IllegalArgumentException(
          "definitionName must not be null. " + DESCRIBE_DEFINITION_NAME);
    }
    if (definitionName.isEmpty()) {
      throw new IllegalArgumentException(
          "definitionName must not be empty. " + DESCRIBE_DEFINITION_NAME);
    }
    if (!ASCII_ALPHANUMERIC_OR_DASH.matcher(definitionName).matches()) {
      throw new IllegalArgumentException(
          "definitionName has value '" + definitionName + "'. " + DESCRIBE_DEFINITION_NAME);
    }
  }
}
