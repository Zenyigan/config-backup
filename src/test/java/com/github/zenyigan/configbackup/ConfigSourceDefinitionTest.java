package com.github.zenyigan.configbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import org.junit.jupiter.api.Test;

public class ConfigSourceDefinitionTest {
  @Test
  void noSourceIsNotAccepted() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new ConfigSourceDefinition("test", null))
        .withMessageContaining("source must not be null");
  }

  @Test
  void noNameIsNotAccepted() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new ConfigSourceDefinition(null, new File("")))
        .withMessageContaining("definitionName must not be null");
  }

  @Test
  void emptyNameIsNotAccepted() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new ConfigSourceDefinition("", new File("")))
        .withMessageContaining("definitionName must not be empty");
  }

  @Test
  void uppercaseNameIsNotAccepted() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new ConfigSourceDefinition("tesT", new File("")))
        .withMessageContaining(
            "definitionName must be at least one char and valid chars are lower ASCII alphanumeric or dash");
  }

  @Test
  void lowercaseDashNameIsAccepted() {
    assertThat(new ConfigSourceDefinition("test-123-test-", new File(""))).isNotNull();
  }

  @Test
  void toStringIsDefined() {
    File f = new File("/abc");
    assertThat(new ConfigSourceDefinition("test", f).toString())
        .isEqualTo("ConfigSourceDefinition{definitionName='test', source=" + f + "}");
  }
}
