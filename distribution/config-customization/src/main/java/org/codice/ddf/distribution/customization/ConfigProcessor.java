/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.distribution.customization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;

public class ConfigProcessor {

  private Path basePath;
  private Path mappingsPath;
  private Path inputPath;
  private Path customizationsPath;

  public ConfigProcessor(
      Path basePath, Path mappingsPath, Path inputPath, Path customizationsPath) {
    this.basePath = basePath;
    this.mappingsPath = mappingsPath;
    this.inputPath = inputPath;
    this.customizationsPath = customizationsPath;
  }

  public void run() throws Exception {

    Map<String, MappingEntry> mappings = loadConfigMappings(mappingsPath.toFile());

    Map<String, Object> inputConfig = loadInputConfig(inputPath.toFile());

    // TODO map values in input according to mappings

    // TODO validate input against mappings
    // required/optional values
    // validate input values

    runCustomization(mappings, inputConfig, basePath.toString());
  }

  private void addInputs(
      Map<String, Object> includedInputs, Map<String, Object> inputs, String child) {
    if (inputs.containsKey(child)) {
      if (includedInputs.containsKey(child)) {
        includedInputs.put(child, addValues(includedInputs.get(child), inputs.get(child)));
      } else {
        includedInputs.put(child, inputs.get(child));
      }
    }
  }

  private List addValues(Object originalValue, Object newValue) {
    List list = new ArrayList();
    if (originalValue instanceof List) {
      list = (List) originalValue;
    } else {
      list.add(originalValue);
    }

    if (newValue instanceof List) {
      list.addAll((List) newValue);
    } else {
      list.add(newValue);
    }
    return list;
  }

  private String getRelativePath(Path path, Path base) {
    try {
      return path.toFile().getCanonicalPath().replace(base.toString(), "").substring(1);
    } catch (IOException e) {
      throw new RuntimeException(
          "Error getting canonical path for:" + path.toFile().getAbsolutePath());
    }
  }

  private void runCustomization(
      Map<String, MappingEntry> mappings, Map<String, Object> inputConfig, String workDir)
      throws Exception {
    Path workPath = Paths.get(workDir);
    Map<String, Path> filesMap =
        Files.walk(customizationsPath)
            .filter(p -> p.toFile().isFile())
            .collect(
                Collectors.toMap(p -> getRelativePath(p, customizationsPath), Function.identity()));

    Configuration cfg = new Configuration();

    // Where do we load the templates from:
    cfg.setDirectoryForTemplateLoading(customizationsPath.toFile());
    // Some other recommended settings:
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.US);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    for (Map.Entry<String, MappingEntry> entry : mappings.entrySet()) {
      String key = entry.getKey();
      MappingEntry me = entry.getValue();
      System.out.println("Processing entry: " + key);
      Object input = inputConfig.get(key);
      List<Map<String, Object>> inputList = new ArrayList<>();
      if (input instanceof List) {
        inputList = (List<Map<String, Object>>) input;
      } else if (input != null) {
        inputList = Collections.singletonList((Map<String, Object>) input);
      }

      if (!me.getIncludedInputs().isEmpty()) {
        Map<String, Object> includedInputs = new HashMap<>();
        for (String included : me.getIncludedInputs()) {
          String[] parts = included.split("\\.");
          if (parts.length != 2) {
            System.out.println("Invalid includedInput: " + included);
            continue;
          }
          String parent = parts[0];
          String child = parts[1];
          Object includedVars = inputConfig.get(parent);
          if (includedVars instanceof List) {
            List<Map<String, Object>> listInput = (List<Map<String, Object>>) includedVars;
            for (Map<String, Object> map : listInput) {
              addInputs(includedInputs, map, child);
            }
          } else {
            addInputs(includedInputs, (Map<String, Object>) includedVars, child);
          }
        }

        if (!includedInputs.isEmpty()) {
          if (inputList.isEmpty()) {
            inputList.add(includedInputs);
          } else {
            inputList.forEach(map -> map.putAll(includedInputs));
          }
        }
      }
      if (inputList.isEmpty()) {
        System.out.println("No input for " + entry.getKey()+". Skipping.");
        me.getConfigs().forEach(config -> filesMap.remove(config));
        continue;
      }

      for (String config : me.getConfigs()) {
        Path outputDir = Paths.get(workPath.toString(), config).getParent();
        for (Map<String, Object> vars : inputList) {
          processConfig(cfg, config, outputDir, vars);
        }
        filesMap.remove(config);
      }
      for (Map<String, Object> vars : inputList) {
        runCommands(me.getCommands(), workPath, vars);
      }
    }

    System.out.println("Copying non-template files");
    for (Map.Entry<String, Path> entry : filesMap.entrySet()) {
      System.out.println("Copying: " + entry.getKey());
      Path outputDir = Paths.get(workPath.toString(), entry.getKey()).getParent();
      try (InputStream in = new FileInputStream(entry.getValue().toFile());
          OutputStream out =
              new FileOutputStream(
                  Paths.get(outputDir.toString(), entry.getValue().toFile().getName()).toFile())) {
        IOUtils.copy(in, out);
      } catch (IOException e) {

      }
    }
  }

  private void runCommands(List<String> commands, Path workPath, Map<String, Object> vars) {
    for (String command : commands) {
      try {
        String finalCommand = replaceVars(command, vars);
        System.out.println("Executing command: " + finalCommand);
        Process process = Runtime.getRuntime().exec(finalCommand, null, workPath.toFile());
        int returnCode = process.waitFor();
        if (returnCode != 0) {
          System.out.println("Warning: Command return code: " + returnCode);
        }
      } catch (IOException | InterruptedException e) {
        System.out.println("Failed to execute command: " + command);
      }
    }
  }

  private String replaceVars(String in, Map<String, Object> vars) {
    String result = in;
    for (Map.Entry<String, Object> entry : vars.entrySet()) {
      result =
          result.replaceAll(
              Pattern.quote("${" + entry.getKey() + "}"), entry.getValue().toString());
    }
    return result;
  }

  private void processConfig(
      Configuration cfg, String templatePath, Path outputDir, Map<String, Object> vars) {
    System.out.println("Processing: " + templatePath);
    Map<String, Object> finalMap = new HashMap<>(vars);
    for (Map.Entry<String, Object> entry : vars.entrySet()) {
      if (entry.getKey().endsWith(".list")) {
        continue;
      }
      if (entry.getValue() instanceof List) {
        finalMap.put(entry.getKey() + "-list", entry.getValue());
      } else {
        finalMap.put(entry.getKey() + "-list", Collections.singletonList(entry.getValue()));
      }
    }
    File templateFile = new File(templatePath);

    File outputFile =
        Paths.get(
                outputDir.toString(),
                templateFile.getName().replace("UUID", UUID.randomUUID().toString()))
            .toFile();
    Template template;

    try {
      template = cfg.getTemplate(templatePath);
    } catch (IOException e) {
      System.out.println("Error loading template");
      return;
    }

    try (Writer writer = new FileWriter(outputFile)) {
      template.process(finalMap, writer);
    } catch (IOException | TemplateException e) {
      System.out.println("Error processing template");
    }
  }

  private static Map<String, Object> loadInputConfig(File inputConfig) {
    Gson gson = new Gson();
    try (FileReader reader = new FileReader(inputConfig)) {
      Type type = new TypeToken<Map<String, Object>>() {}.getType();
      return gson.fromJson(reader, type);
    } catch (IOException e) {
      throw new RuntimeException(
          "Could not proccess json file: " + inputConfig.getAbsolutePath(), e);
    }
  }

  private static Map<String, MappingEntry> loadConfigMappings(File mappings) {
    Gson gson = new Gson();
    try (FileReader reader = new FileReader(mappings)) {
      Type type = new TypeToken<Map<String, MappingEntry>>() {}.getType();
      return gson.fromJson(reader, type);
    } catch (IOException e) {
      throw new RuntimeException("Could not proccess json file: " + mappings.getAbsolutePath(), e);
    }
  }
}
