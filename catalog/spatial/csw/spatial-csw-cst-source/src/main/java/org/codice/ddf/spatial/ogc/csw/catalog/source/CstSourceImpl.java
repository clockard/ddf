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
package org.codice.ddf.spatial.ogc.csw.catalog.source;

import com.thoughtworks.xstream.converters.Converter;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.security.encryption.EncryptionService;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.opengis.ows.v_1_0_0.DomainType;
import net.opengis.ows.v_1_0_0.Operation;
import org.apache.commons.lang3.StringUtils;
import org.codice.ddf.catalog.cst.api.CstService;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.spatial.ogc.csw.catalog.common.CswSourceConfiguration;
import org.codice.ddf.spatial.ogc.csw.catalog.common.source.AbstractCswSource;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CstSourceImpl extends AbstractCswSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(CstSourceImpl.class);

  private CstService cstService;

  public CstSourceImpl(
      BundleContext context,
      CswSourceConfiguration cswSourceConfiguration,
      Converter provider,
      ClientFactoryFactory clientFactoryFactory,
      EncryptionService encryptionService,
      CstService cstService) {
    super(context, cswSourceConfiguration, provider, clientFactoryFactory, encryptionService);
    this.cstService = cstService;
  }

  public CstSourceImpl(
      EncryptionService encryptionService,
      ClientFactoryFactory clientFactoryFactory,
      CstService cstService) {
    super(encryptionService, clientFactoryFactory);
    this.cstService = cstService;
  }

  @Override
  public SourceResponse query(QueryRequest queryRequest) throws UnsupportedQueryException {
    String targetCst = getCommonCst();

    if (StringUtils.isEmpty(targetCst)) {
      throw new UnsupportedQueryException(
          "Cannot query "
              + this.getId()
              + ". No common CST definitions could be found. Check to make sure the remote system supports CST.");
    }

    this.cswSourceConfiguration.setQueryTypeName(targetCst);

    return super.query(queryRequest);
  }

  private String getCommonCst() {
    String targetCst = this.cswSourceConfiguration.getQueryTypeName();
    if (StringUtils.isEmpty(targetCst)) {
      LOGGER.debug("No CST version set for query. Looking up common CST.");
      Set<String> namespacePriority = cstService.getOrderedNamespaces();
      List<String> typeNames =
          capabilities
              .getOperationsMetadata()
              .getOperation()
              .stream()
              .filter(op -> op.getName().equals("GetRecords"))
              .map(Operation::getParameter)
              .flatMap(Collection::stream)
              .filter(dt -> dt.getName().equals("typeNames"))
              .map(DomainType::getValue)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());

      Optional<String> commonNs =
          namespacePriority
              .stream()
              .filter(ns -> typeNames.stream().anyMatch(tn -> tn.startsWith(ns)))
              .findFirst();
      if (commonNs.isPresent()) {
        String baseNs = commonNs.get();
        LOGGER.debug("Common CST namespace found: {}", baseNs);
        List<Integer> versions =
            typeNames
                .stream()
                .filter(s -> s.startsWith(baseNs))
                .map(s -> s.substring(s.lastIndexOf(':')))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        Collections.sort(versions);
        Optional<Integer> commonVersion =
            versions
                .stream()
                .filter(ver -> cstService.getCstDefinition(baseNs, ver) != null)
                .findFirst();
        if (commonVersion.isPresent()) {
          targetCst = commonNs + ":" + commonVersion.get();
          LOGGER.debug("Common CST version found: {}", baseNs);
        } else {
          LOGGER.warn(
              "Could not find a common cst version for base namespace {}, remote namespaces are {}",
              baseNs,
              versions);
        }
      } else {
        LOGGER.warn(
            "Could not find a common cst base namespace. Remote namespaces are {}", typeNames);
      }
    }
    return targetCst;
  }

  @Override
  protected Map<String, Consumer<Object>> getAdditionalConsumers() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Serializable> getQueryFilterTransformerProperties() {
    return Collections.singletonMap("incomingQuery", false);
  }
}
