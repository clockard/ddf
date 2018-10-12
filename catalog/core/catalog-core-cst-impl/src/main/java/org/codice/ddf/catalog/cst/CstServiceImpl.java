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
package org.codice.ddf.catalog.cst;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.codice.ddf.catalog.cst.api.CstDefinition;
import org.codice.ddf.catalog.cst.api.CstException;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.CstService;
import org.codice.ddf.catalog.cst.api.InternalTaxonomyTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CstServiceImpl implements CstService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CstServiceImpl.class);

  private Map<String, Map<Integer, CstDefinition>> cstDefinitions = new ConcurrentHashMap<>();

  private Map<String, InternalTaxonomyTransformer> taxonomyTransformers = new ConcurrentHashMap<>();

  private Set<String> orderedNamespaces = new HashSet<>();

  @Override
  public Set<String> getOrderedNamespaces() {
    return Collections.unmodifiableSet(orderedNamespaces);
  }

  @Override
  public Set<CstDefinition> getCstDefinitions() {
    return cstDefinitions
        .values()
        .stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<CstDefinition> getCstDefinitions(String namespace) {
    return new HashSet<>(cstDefinitions.getOrDefault(namespace, new HashMap<>()).values());
  }

  @Override
  public CstDefinition getCstDefinition(String namespace, int version) {
    return cstDefinitions.get(namespace).get(version);
  }

  @Override
  public Set<CstPair> transform(Set<CstPair> pairs, CstDefinition from, CstDefinition to) {

    validateDefinitionConversion(from, to);

    Map<Integer, CstDefinition> definitions = cstDefinitions.get(from.getNamespace());

    int direction = to.getVersion() - from.getVersion() >= 0 ? 1 : -1;
    Set<CstPair> transformedPairs = pairs;
    for (int version = from.getVersion(); version != to.getVersion(); version += direction) {
      if (direction > 0) {
        transformedPairs = definitions.get(version + 1).fromPreviousVersion(transformedPairs);
      } else {
        transformedPairs = definitions.get(version).toPreviousVersion(transformedPairs);
      }
    }
    return transformedPairs;
  }

  @Override
  public Set<CstPair> transform(CstPair pair, CstDefinition from, CstDefinition to) {
    return transform(Collections.singleton(pair), from, to);
  }

  @Override
  public Set<CstPair> toCatalogTaxonomy(Set<CstPair> pairs, CstDefinition from) {
    Map<Integer, CstDefinition> definitions = cstDefinitions.get(from.getNamespace());
    if (definitions == null) {
      throw new CstException(
          "No CST definitions registered for the given namespace: " + from.getNamespace());
    }
    Optional<CstDefinition> latestVersion =
        definitions.values().stream().max(Comparator.comparingInt(CstDefinition::getVersion));
    if (!latestVersion.isPresent()) {
      throw new CstException(
          "Could not determine latest version for CST namespace: " + from.getNamespace());
    }
    CstDefinition latest = latestVersion.get();
    validateDefinitionConversion(from, latest);

    Set<CstPair> transformedPairs = transform(pairs, from, latest);
    InternalTaxonomyTransformer taxonomyTransformer = taxonomyTransformers.get(from.getNamespace());
    return taxonomyTransformer.toInternalTaxonomy(transformedPairs);
  }

  @Override
  public Set<CstPair> toCatalogTaxonomy(CstPair pair, CstDefinition from) {
    return toCatalogTaxonomy(Collections.singleton(pair), from);
  }

  @Override
  public Set<CstPair> fromCatalogTaxonomy(Set<CstPair> pairs, CstDefinition to) {
    Map<Integer, CstDefinition> definitions = cstDefinitions.get(to.getNamespace());
    if (definitions == null) {
      throw new CstException(
          "No CST definitions registered for the given namespace: " + to.getNamespace());
    }
    Optional<CstDefinition> latestVersion =
        definitions.values().stream().max(Comparator.comparingInt(CstDefinition::getVersion));
    if (!latestVersion.isPresent()) {
      throw new CstException(
          "Could not determine latest version for CST namespace: " + to.getNamespace());
    }
    CstDefinition latest = latestVersion.get();
    validateDefinitionConversion(to, latest);
    InternalTaxonomyTransformer taxonomyTransformer = taxonomyTransformers.get(to.getNamespace());
    Set<CstPair> transformedPairs = taxonomyTransformer.fromInternalTaxonomy(pairs);
    return transform(transformedPairs, latest, to);
  }

  @Override
  public Set<CstPair> fromCatalogTaxonomy(CstPair pair, CstDefinition to) {
    return fromCatalogTaxonomy(Collections.singleton(pair), to);
  }

  public void registerCstDefinition(CstDefinition definition) {
    if (definition == null) {
      return;
    }
    LOGGER.debug(
        "Registering CST definition for {}:{}", definition.getNamespace(), definition.getVersion());
    cstDefinitions.putIfAbsent(definition.getNamespace(), new ConcurrentHashMap<>());
    Map<Integer, CstDefinition> definitionMap = cstDefinitions.get(definition.getNamespace());
    definitionMap.put(definition.getVersion(), definition);
  }

  public void unregisterCstDefinition(CstDefinition definition) {
    if (definition == null) {
      return;
    }
    LOGGER.debug(
        "Unregistering CST definition for {}:{}",
        definition.getNamespace(),
        definition.getVersion());
    Map<Integer, CstDefinition> definitionMap =
        cstDefinitions.getOrDefault(definition.getNamespace(), new HashMap<>());
    definitionMap.remove(definition.getVersion());
  }

  public void registerTransformer(InternalTaxonomyTransformer transformer) {
    if (transformer == null) {
      return;
    }
    taxonomyTransformers.put(transformer.getNamespace(), transformer);
  }

  public void unregisterTransformer(InternalTaxonomyTransformer transformer) {
    if (transformer == null) {
      return;
    }
    taxonomyTransformers.remove(transformer.getNamespace());
  }

  public void setOrderedNamespaces(List<String> namespaces) {
    if (namespaces == null) {
      return;
    }
    orderedNamespaces.clear();
    orderedNamespaces.addAll(namespaces);
    orderedNamespaces.addAll(cstDefinitions.keySet());
  }

  private void validateDefinitionConversion(CstDefinition from, CstDefinition to) {
    if (!from.getNamespace().equals(to.getNamespace())) {
      throw new CstException(
          "CST transformations must be between CST definitions within the same namespace. From namespace: "
              + from.getNamespace()
              + " To namespace: "
              + to.getNamespace());
    }

    Map<Integer, CstDefinition> definitions = cstDefinitions.get(from.getNamespace());
    if (definitions == null) {
      throw new CstException(
          "No CST definitions registered for the given namespace: " + from.getNamespace());
    }
    int direction = to.getVersion() - from.getVersion() >= 0 ? 1 : -1;
    for (int version = from.getVersion(); version != to.getVersion(); version += direction) {
      if (!definitions.containsKey(version)) {
        throw new CstException(
            "No CST definitions registered for a required version in the transformation chain. Missing version: "
                + version
                + " Namespace "
                + from.getNamespace());
      }
    }
  }
}
