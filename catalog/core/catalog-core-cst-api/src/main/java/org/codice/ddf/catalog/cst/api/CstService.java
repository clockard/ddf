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
package org.codice.ddf.catalog.cst.api;

import java.util.Set;

public interface CstService {

  Set<String> getOrderedNamespaces();

  Set<CstDefinition> getCstDefinitions();

  Set<CstDefinition> getCstDefinitions(String namespace);

  CstDefinition getCstDefinition(String namespace, int version);

  Set<CstPair> transform(Set<CstPair> pairs, CstDefinition from, CstDefinition to);

  Set<CstPair> transform(CstPair pair, CstDefinition from, CstDefinition to);

  Set<CstPair> toCatalogTaxonomy(Set<CstPair> pairs, CstDefinition from);

  Set<CstPair> toCatalogTaxonomy(CstPair pair, CstDefinition from);

  Set<CstPair> fromCatalogTaxonomy(Set<CstPair> pairs, CstDefinition to);

  Set<CstPair> fromCatalogTaxonomy(CstPair pair, CstDefinition to);
}
