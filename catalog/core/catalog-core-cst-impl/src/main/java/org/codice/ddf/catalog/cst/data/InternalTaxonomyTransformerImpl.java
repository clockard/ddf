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
package org.codice.ddf.catalog.cst.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.InternalTaxonomyTransformer;

public class InternalTaxonomyTransformerImpl implements InternalTaxonomyTransformer {

  private final String namespace;

  private Map<String, Function<CstPair, Set<CstPair>>> toTaxonomy;

  private Map<String, Function<CstPair, Set<CstPair>>> fromTaxonomy;

  public InternalTaxonomyTransformerImpl(String namespace) {
    this(namespace, new HashMap<>(), new HashMap<>());
  }

  public InternalTaxonomyTransformerImpl(
      String namespace,
      Map<String, Function<CstPair, Set<CstPair>>> toTaxonomy,
      Map<String, Function<CstPair, Set<CstPair>>> fromTaxonomy) {
    this.namespace = namespace;
    this.toTaxonomy = toTaxonomy;
    this.fromTaxonomy = fromTaxonomy;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public Set<CstPair> toInternalTaxonomy(Set<CstPair> pairs) {
    return pairs
        .stream()
        .map(pair -> transform(pair, toTaxonomy.get(pair.getKey())))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<CstPair> fromInternalTaxonomy(Set<CstPair> pairs) {
    return pairs
        .stream()
        .map(pair -> transform(pair, fromTaxonomy.get(pair.getKey())))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  private Set<CstPair> transform(CstPair pair, Function<CstPair, Set<CstPair>> transform) {
    if (transform != null) {
      return transform.apply(pair);
    }
    return Collections.singleton(pair);
  }

  public void addTaxonomyTransform(
      String term, Function<CstPair, Set<CstPair>> to, Function<CstPair, Set<CstPair>> from) {
    toTaxonomy.put(term, to);
    fromTaxonomy.put(term, from);
  }
}
