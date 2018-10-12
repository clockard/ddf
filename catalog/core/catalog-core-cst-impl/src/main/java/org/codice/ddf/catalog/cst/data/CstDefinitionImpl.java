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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.codice.ddf.catalog.cst.api.CstDefinition;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.CstTerm;

public class CstDefinitionImpl implements CstDefinition {

  private final String namespace;

  private final int version;

  private Map<String, CstTerm> terms = new HashMap<>();

  private Map<String, Function<CstPair, Set<CstPair>>> toPreviousTransforms;

  private Map<String, Function<CstPair, Set<CstPair>>> fromPreviousTransforms;

  public CstDefinitionImpl(String namespace, int version) {
    this(namespace, version, new HashSet<>());
  }

  public CstDefinitionImpl(String namespace, int version, Collection<CstTerm> terms) {
    this(namespace, version, terms, new HashMap<>(), new HashMap<>());
  }

  public CstDefinitionImpl(
      String namespace,
      int version,
      Collection<CstTerm> terms,
      Map<String, Function<CstPair, Set<CstPair>>> toPreviousTransforms,
      Map<String, Function<CstPair, Set<CstPair>>> fromPreviousTransforms) {
    this.namespace = namespace;
    this.version = version;
    this.fromPreviousTransforms = fromPreviousTransforms;
    this.toPreviousTransforms = toPreviousTransforms;
    terms.stream().forEach(term -> this.terms.put(term.getName(), term));
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public CstTerm getTerm(String name) {
    return terms.get(name);
  }

  @Override
  public Set<CstTerm> getTerms() {
    return new HashSet<>(terms.values());
  }

  @Override
  public Set<CstPair> toPreviousVersion(Set<CstPair> pairs) {
    return pairs
        .stream()
        .map(pair -> transform(pair, toPreviousTransforms.get(pair.getKey())))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<CstPair> fromPreviousVersion(Set<CstPair> pairs) {
    return pairs
        .stream()
        .map(pair -> transform(pair, fromPreviousTransforms.get(pair.getKey())))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  private Set<CstPair> transform(CstPair pair, Function<CstPair, Set<CstPair>> transform) {
    if (transform != null) {
      return transform.apply(pair);
    }
    return Collections.singleton(pair);
  }

  public void addTerm(CstTerm term) {
    terms.put(term.getName(), term);
  }

  public void addTerm(
      CstTerm term,
      Function<CstPair, Set<CstPair>> toPrevious,
      Function<CstPair, Set<CstPair>> fromPrevious) {
    terms.put(term.getName(), term);
    toPreviousTransforms.put(term.getName(), toPrevious);
    fromPreviousTransforms.put(term.getName(), fromPrevious);
  }

  public void addTerm(
      CstTerm term,
      Function<CstPair, Set<CstPair>> toPrevious,
      String peviousTerm,
      Function<CstPair, Set<CstPair>> fromPrevious) {
    terms.put(term.getName(), term);
    toPreviousTransforms.put(term.getName(), toPrevious);
    fromPreviousTransforms.put(peviousTerm, fromPrevious);
  }
}
