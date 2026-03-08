package com.github.zenyigan.configbackup;

import java.io.File;
import java.time.Instant;

public interface InternalUtil {
  void copy(File source, File destination);

  void delete(File source);

  File defaultDestinationRoot();

  String nowUtcName();

  String utcName(Instant instant);

  Instant parseUtcName(String timestamp);
}
