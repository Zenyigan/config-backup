package com.github.zenyigan.configbackup;

import static com.github.zenyigan.configbackup.TestFileUtil.assertFileContentEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class BackupTest {
  @Test
  void backupSingleFileCanBeDone() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  String fn = "config.ser";
                  File f = new File(sourceDir, fn);
                  TestFileUtil.writeFileContent(f, 2_000_000);
                  Instant now = Instant.now();
                  String utcName = new InternalUtilImpl().utcName(now);

                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source(f)
                          .build();

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName;
                        }
                      });

                  b.execute();

                  TestFileUtil.assertDirectoryContainsOnly(backupDir, "default-0");
                  File defaultBackup = new File(backupDir, "default-0");
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackup, utcName);
                  File defaultBackupTs = new File(defaultBackup, utcName);
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackupTs, fn);
                  assertFileContentEquals(f, new File(defaultBackupTs, fn));
                }));
  }

  @Test
  void emptyBackupDirForNonExistingSource() {
    TestFileUtil.withTempDir(
        backupDir -> {
          File f = new File(backupDir, "nonexisting");
          Instant now = Instant.now();
          String utcName = new InternalUtilImpl().utcName(now);

          ConfigBackup b =
              ConfigBackup.builder().backupDestinationDirectory(backupDir).source(f).build();

          exchangeInternalUtil(
              b,
              new TestInternalUtilImpl() {
                @Override
                public String nowUtcName() {
                  return utcName;
                }
              });

          b.execute();

          TestFileUtil.assertDirectoryContainsOnly(backupDir, "default-0");
          File defaultBackup = new File(backupDir, "default-0");
          TestFileUtil.assertDirectoryContainsOnly(defaultBackup, utcName);
          File defaultBackupTs = new File(defaultBackup, utcName);
          TestFileUtil.assertDirectoryContainsOnly(defaultBackupTs);
        });
  }

  @Test
  void backupDirectoryCanBeDone() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  File subDirectory = new File(sourceDir, "sub1");
                  assertThat(subDirectory.mkdirs()).isTrue();
                  File content1 = new File(sourceDir, "config1.ser");
                  TestFileUtil.writeFileContent(content1, 1_000_000);
                  File content2 = new File(subDirectory, "config2.ser");
                  TestFileUtil.writeFileContent(content2, 1_000_000);

                  Instant now = Instant.now();
                  String utcName = new InternalUtilImpl().utcName(now);

                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source(sourceDir)
                          .build();

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName;
                        }
                      });

                  b.execute();

                  TestFileUtil.assertDirectoryContainsOnly(backupDir, "default-0");
                  File defaultBackup = new File(backupDir, "default-0");
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackup, utcName);
                  File defaultBackupTs = new File(defaultBackup, utcName);
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackupTs, sourceDir.getName());

                  File defaultBackupTsDir = new File(defaultBackupTs, sourceDir.getName());
                  TestFileUtil.assertDirectoriesEqual(defaultBackupTsDir, sourceDir);
                }));
  }

  @Test
  void multipleBackupsCanBeDone() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  String fn1 = "config1.ser";
                  File f1 = new File(sourceDir, fn1);
                  TestFileUtil.writeFileContent(f1, 2_000_000);
                  String fn2 = "config2.ser";
                  File f2 = new File(sourceDir, fn2);
                  TestFileUtil.writeFileContent(f2, 2_000_000);
                  Instant now = Instant.now();
                  String utcName = new InternalUtilImpl().utcName(now);

                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source("1", f1)
                          .source("2", f2)
                          .build();

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName;
                        }
                      });

                  b.execute();

                  TestFileUtil.assertDirectoryContainsOnly(backupDir, "1", "2");

                  File defaultBackup1 = new File(backupDir, "1");
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackup1, utcName);
                  File defaultBackupTs1 = new File(defaultBackup1, utcName);
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackupTs1, fn1);
                  assertFileContentEquals(f1, new File(defaultBackupTs1, fn1));

                  File defaultBackup2 = new File(backupDir, "2");
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackup2, utcName);
                  File defaultBackupTs2 = new File(defaultBackup2, utcName);
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackupTs2, fn2);
                  assertFileContentEquals(f2, new File(defaultBackupTs2, fn2));
                }));
  }

  @Test
  void cleanPhaseUsesMinimalBackupItemsToKeep() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  String fn = "config.ser";
                  File f = new File(sourceDir, fn);
                  TestFileUtil.writeFileContent(f, 2_000_000);
                  String utcName10 = new InternalUtilImpl().utcName(Instant.now().minusSeconds(10));
                  String utcName5 = new InternalUtilImpl().utcName(Instant.now().minusSeconds(5));
                  String utcName0 = new InternalUtilImpl().utcName(Instant.now());

                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source(f)
                          .minimalBackupItemsToKeep(2)
                          .minimalBackupDaysToKeep(0)
                          .build();

                  assertThat(b.minimalBackupItemsToKeep()).isEqualTo(2);
                  assertThat(b.minimalBackupDaysToKeep()).isEqualTo(0);

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName10;
                        }
                      });
                  b.execute();
                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName5;
                        }
                      });
                  b.execute();
                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName0;
                        }
                      });
                  b.execute();

                  TestFileUtil.assertDirectoryContainsOnly(backupDir, "default-0");
                  File defaultBackup = new File(backupDir, "default-0");
                  TestFileUtil.assertDirectoryContainsOnly(defaultBackup, utcName5, utcName0);
                }));
  }

  @Test
  void cleanPhaseUsesMinimalBackupDaysToKeep() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  String fn = "config.ser";
                  File f = new File(sourceDir, fn);
                  TestFileUtil.writeFileContent(f, 2_000_000);
                  String utcNameOld =
                      new InternalUtilImpl()
                          .utcName(Instant.now().minus(Duration.ofDays(1)).minusMillis(1));
                  String utcName10 = new InternalUtilImpl().utcName(Instant.now().minusSeconds(10));
                  String utcName5 = new InternalUtilImpl().utcName(Instant.now().minusSeconds(5));
                  String utcName0 = new InternalUtilImpl().utcName(Instant.now());

                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source(f)
                          .minimalBackupItemsToKeep(2)
                          .minimalBackupDaysToKeep(1)
                          .build();

                  assertThat(b.minimalBackupItemsToKeep()).isEqualTo(2);
                  assertThat(b.minimalBackupDaysToKeep()).isEqualTo(1);

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcNameOld;
                        }
                      });
                  b.execute();
                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName10;
                        }
                      });
                  b.execute();
                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName5;
                        }
                      });
                  b.execute();
                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName0;
                        }
                      });
                  b.execute();

                  TestFileUtil.assertDirectoryContainsOnly(backupDir, "default-0");
                  File defaultBackup = new File(backupDir, "default-0");
                  TestFileUtil.assertDirectoryContainsOnly(
                      defaultBackup, utcName10, utcName5, utcName0);
                }));
  }

  @Test
  void problemInCleaningIsAfterAllBackupPhase() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  String fn1 = "config1.ser";
                  File f1 = new File(sourceDir, fn1);
                  TestFileUtil.writeFileContent(f1, 2_000_000);
                  String fn2 = "config2.ser";
                  File f2 = new File(sourceDir, fn2);
                  TestFileUtil.writeFileContent(f2, 2_000_000);

                  String utcName10 = new InternalUtilImpl().utcName(Instant.now().minusSeconds(10));
                  String utcName5 = new InternalUtilImpl().utcName(Instant.now().minusSeconds(5));
                  String utcName0 = new InternalUtilImpl().utcName(Instant.now());

                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source("1", f1)
                          .source("2", f2)
                          .minimalBackupDaysToKeep(0)
                          .minimalBackupItemsToKeep(1)
                          .build();

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName10;
                        }

                        @Override
                        public void delete(File source) {
                          throw new RuntimeException("Cannot delete");
                        }
                      });
                  b.execute();

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName5;
                        }

                        @Override
                        public void delete(File source) {
                          throw new RuntimeException("Cannot delete");
                        }
                      });
                  assertThatThrownBy(b::execute).hasMessageContaining("Cannot delete");

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public String nowUtcName() {
                          return utcName0;
                        }

                        @Override
                        public void delete(File source) {
                          throw new RuntimeException("Cannot delete");
                        }
                      });
                  assertThatThrownBy(b::execute).hasMessageContaining("Cannot delete");

                  TestFileUtil.assertDirectoryContainsOnly(backupDir, "1", "2");

                  File defaultBackup1 = new File(backupDir, "1");
                  TestFileUtil.assertDirectoryContainsOnly(
                      defaultBackup1, utcName10, utcName5, utcName0);

                  File defaultBackup2 = new File(backupDir, "2");
                  TestFileUtil.assertDirectoryContainsOnly(
                      defaultBackup2, utcName10, utcName5, utcName0);
                }));
  }

  @Test
  void storageExceptionInBackupPhaseIsThrown() {
    TestFileUtil.withTempDir(
        backupDir ->
            TestFileUtil.withTempDir(
                sourceDir -> {
                  ConfigBackup b =
                      ConfigBackup.builder()
                          .backupDestinationDirectory(backupDir)
                          .source(sourceDir)
                          .build();

                  exchangeInternalUtil(
                      b,
                      new TestInternalUtilImpl() {
                        @Override
                        public void copy(File source, File destination) {
                          throw new RuntimeException("No space left on device");
                        }
                      });

                  assertThatThrownBy(b::execute).hasMessageContaining("No space left on device");
                }));
  }

  private void exchangeInternalUtil(ConfigBackup b, InternalUtil util) {
    TestReflectionUtil.setFinalField(b, "internalUtil", util);
  }
}
