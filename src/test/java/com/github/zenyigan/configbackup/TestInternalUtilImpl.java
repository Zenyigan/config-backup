package com.github.zenyigan.configbackup;

import java.io.File;
import java.time.Instant;

public class TestInternalUtilImpl implements InternalUtil {
  private final InternalUtil delegate = new InternalUtilImpl();

  @Override
  public void copy(File source, File destination) {
    delegate.copy(source, destination);
  }

  @Override
  public void delete(File source) {
    delegate.delete(source);
  }

  @Override
  public File defaultDestinationRoot() {
    return delegate.defaultDestinationRoot();
  }

  @Override
  public String nowUtcName() {
    return delegate.nowUtcName();
  }

  @Override
  public String utcName(Instant instant) {
    return delegate.utcName(instant);
  }

  @Override
  public Instant parseUtcName(String timestamp) {
    return delegate.parseUtcName(timestamp);
  }
}
