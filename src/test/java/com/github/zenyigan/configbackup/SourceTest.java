package com.github.zenyigan.configbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import org.junit.jupiter.api.Test;

public class SourceTest {
  @Test
  void noSourceIsNotAccepted() {
    assertThatExceptionOfType(ConfigBackupException.class)
        .isThrownBy(() -> ConfigBackup.builder().build())
        .withMessageContaining("At least one source definition is required");
  }

  @Test
  void nullSourceIsNotAccepted() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ConfigBackup.source(null))
        .withMessageContaining("source must not be null");
  }

  @Test
  void sourceIsAccepted() {
    assertThat(ConfigBackup.source(new File(""))).isNotNull();
  }

  @Test
  void sourceByBuilderIsAccepted() {
    assertThat(ConfigBackup.builder().source(new File("")).build()).isNotNull();
    assertThat(
            ConfigBackup.builder().source(new ConfigSourceDefinition("test", new File(""))).build())
        .isNotNull();
    assertThat(ConfigBackup.builder().source("test", new File("")).build()).isNotNull();
  }

  @Test
  void multipleSourcesAreAccepted() {
    assertThat(ConfigBackup.builder().source(new File("1")).source(new File("2")).build())
        .isNotNull();
    assertThat(
            ConfigBackup.builder()
                .source(new ConfigSourceDefinition("test1", new File("")))
                .source(new ConfigSourceDefinition("test2", new File("")))
                .build())
        .isNotNull();
  }

  @Test
  void sourcesWithSameNameAreNotAccepted() {
    assertThatExceptionOfType(ConfigBackupException.class)
        .isThrownBy(
            () ->
                ConfigBackup.builder()
                    .source(new ConfigSourceDefinition("test-a", new File("")))
                    .source(new ConfigSourceDefinition("test", new File("")))
                    .source(new ConfigSourceDefinition("test-b", new File("")))
                    .source(new ConfigSourceDefinition("test", new File("")))
                    .build())
        .withMessageContaining("Duplicate definitionName: test");
  }
}
