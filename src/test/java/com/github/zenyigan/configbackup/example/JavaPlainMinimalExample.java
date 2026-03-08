package com.github.zenyigan.configbackup.example;

import com.github.zenyigan.configbackup.ConfigBackup;
import java.io.File;

public class JavaPlainMinimalExample {
  public static void main(String[] args) {
    ConfigBackup.source(new File(".../h2.db")).execute();

    // startup code of the app
  }
}
