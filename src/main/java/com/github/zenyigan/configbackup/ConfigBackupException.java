package com.github.zenyigan.configbackup;

public class ConfigBackupException extends RuntimeException {
  public ConfigBackupException(String message) {
    super(message);
  }

  public ConfigBackupException(String message, Throwable cause) {
    super(buildMessage(message, cause), cause);
  }

  private static String buildMessage(String message, Throwable cause) {
    if (cause == null) {
      return message;
    }
    String causeMsg = cause.getMessage();
    if (causeMsg != null && !causeMsg.isEmpty()) {
      return message + ": " + causeMsg;
    } else {
      return message + ": " + cause.getClass().getName();
    }
  }
}
