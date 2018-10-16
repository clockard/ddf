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
package org.codice.ddf.catalog.cst.transformer;

import ddf.catalog.operation.Query;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.transform.QueryFilterTransformer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.codice.ddf.catalog.cst.api.CstDefinition;
import org.codice.ddf.catalog.cst.api.CstService;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CstQueryFilterTransformer implements QueryFilterTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(CstQueryFilterTransformer.class);

  private CstDefinition definition;

  private CstService service;

  public CstQueryFilterTransformer(CstDefinition definition, CstService service) {
    this.definition = definition;
    this.service = service;
  }

  @Override
  public QueryRequest transform(QueryRequest queryRequest, Map<String, Serializable> properties) {
    Query originalQuery = queryRequest.getQuery();
    boolean toInternal = true;
    if (properties != null && properties.containsKey("incomingQuery")) {
      toInternal = (Boolean) properties.get("incomingQuery");
    }
    CstFilterVisitor visitor = new CstFilterVisitor(definition, service, toInternal);
    Filter updatedFilter = (Filter) originalQuery.accept(visitor, null);
    Query updatedQuery =
        new QueryImpl(
            updatedFilter,
            originalQuery.getStartIndex(),
            originalQuery.getPageSize(),
            originalQuery.getSortBy(),
            originalQuery.requestsTotalResultsCount(),
            originalQuery.getTimeoutMillis());

    HashMap<String, Serializable> updatedProperties = new HashMap<>(queryRequest.getProperties());
    updatedProperties.put("originalQuery", originalQuery.toString());
    updatedProperties.put("cstTransformWarnings", new ArrayList(visitor.getConversionWarnings()));
    updatedProperties.put("cstTransformErrors", new ArrayList(visitor.getConversionErrors()));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("CST filter transform errors: {}", visitor.getConversionErrors());
      LOGGER.debut("CST filter transform warning: {}", visitor.getConversionWarnings());
    }

    return new QueryRequestImpl(
        updatedQuery, queryRequest.isEnterprise(), queryRequest.getSourceIds(), updatedProperties);
  }
}
