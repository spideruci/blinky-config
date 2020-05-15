package org.spideruci.analysis.config;

import static org.spideruci.analysis.config.utils.Logger.LOG;
import static org.spideruci.analysis.config.utils.SystemProperties.valueFor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.spideruci.analysis.config.utils.SystemProperties;
import org.spideruci.analysis.config.definer.ConfigFieldsDefiner;

public class Main {

  public final static String JAVA_PKG_SEP = "/";

  private static final String CONFIG_FILEPATH = "config.filepath";
  private static final String CONFIG_OVERRIDE = "config.override";
  private static final String CONFIG_CLASSNAME = "config.classname";
  private static final String CONFIG_COMPILEDOUTPUT = "config.compiledoutput";



  public static void main(String[] args) {
    LOG.info("Setting up Compile-time configurations for BLINKY CORE!");

    final String configFilePath = SystemProperties.has(CONFIG_OVERRIDE) ? valueFor(CONFIG_OVERRIDE) : valueFor(CONFIG_FILEPATH);

    ConfigFile conf = ConfigFile.create(configFilePath);
    Map<String, ?> config = conf.readConfig();

    final String configClassName = valueFor(CONFIG_CLASSNAME);
    final String className = configClassName.replaceAll("\\.", JAVA_PKG_SEP);

    Path configClassPath = getConfigClassPath(className);
    File configClassFile = configClassPath.toFile();

    byte[] modBytecode = 
        ConfigFieldsDefiner.define(className, configClassFile, config);

    writeToClassFile(modBytecode, configClassPath.toString());
  }

  private static boolean isClassNameValid(String className) {
    return className != null && className.length() != 0; 
  }

  private static Path getConfigClassPath(String className) {
    LOG.throwErrorIf(!isClassNameValid(className), "Invalid class name: %s", className);

    final String targetClasses = valueFor(CONFIG_COMPILEDOUTPUT);
    Path path = Paths.get(targetClasses);

    if(Files.exists(path)) {
      LOG.info(path.toString());
    } else {
      LOG.error("Unable to find the classes directory :(");
      throw new RuntimeException();
    }

    String[] classNameSplit = className.split(JAVA_PKG_SEP);
    for(int i = 0; i <= classNameSplit.length - 2; i += 1) {
      final String splitItem = classNameSplit[i];
      Path temp = path.resolve(splitItem);
      LOG.throwErrorIf(!Files.exists(temp), "Path is invalid: %s", String.valueOf(temp));
      path = temp;
    }

    Path configClassPath = path.resolve(classNameSplit[classNameSplit.length - 1] + ".class");
    final String confClsPathName = configClassPath.toString();
    if(Files.exists(configClassPath)) {
      LOG.info("Updating config class: %s", confClsPathName);
    } else {
      LOG.throwError("Path invalid: %s", confClsPathName);
    }

    return configClassPath;
  }

  private static void writeToClassFile(byte[] bytecode, String filePath) {
    try {
      PrintStream byteStream = new PrintStream(filePath);
      byteStream.write(bytecode);
      byteStream.close();
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
