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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingEntry {

  private List<String> includedInputs = new ArrayList<>();
  private List<String> configs = new ArrayList<>();
  private List<String> commands = new ArrayList<>();
  private Map<String, String> validation = new HashMap<>();
  private Map<String, String> keyMappings = new HashMap<>();

  public List<String> getConfigs() {
    return configs;
  }

  public void setConfigs(List<String> configs) {
    this.configs = configs;
  }

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }

  public Map<String, String> getValidation() {
    return validation;
  }

  public void setValidation(Map<String, String> validation) {
    this.validation = validation;
  }

  public Map<String, String> getKeyMappings() {
    return keyMappings;
  }

  public void setKeyMappings(Map<String, String> keyMappings) {
    this.keyMappings = keyMappings;
  }

  public List<String> getIncludedInputs() {
    return includedInputs;
  }

  public void setIncludedInputs(List<String> included) {
    this.includedInputs = included;
  }
}
