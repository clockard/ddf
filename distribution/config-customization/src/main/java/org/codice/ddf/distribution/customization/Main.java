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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      System.out.println("Usage: ConfigProcessor /path/to/json/config.json");
      return;
    } else if (args.length > 1) {
      System.out.println("Invalid number of arguments");
      System.out.println("Usage: ConfigProcessor /path/to/json/config.json");
      return;
    }
    File configFile = new File(args[0]);
    if (!configFile.exists() || !configFile.canRead()) {
      System.out.println("Can't read config file: " + args[0]);
      return;
    }

    String workDir = System.getProperty("ddf.home");
    Path customizations = Paths.get(workDir, "customizations", "configs");
    Path configMappings = Paths.get(workDir, "customizations", "config-mappings.json");

    new ConfigProcessor(Paths.get(workDir), configMappings, configFile.toPath(), customizations)
        .run();
  }
}
