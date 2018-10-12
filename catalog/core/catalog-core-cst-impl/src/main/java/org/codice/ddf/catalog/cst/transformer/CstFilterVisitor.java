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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.codice.ddf.catalog.cst.api.CstDefinition;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.CstService;
import org.codice.ddf.catalog.cst.api.CstTerm;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.filter.visitor.NullExpressionVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.During;

public class CstFilterVisitor extends DuplicatingFilterVisitor {

  private final CstDefinition cstDefinition;

  private final CstService cstService;

  private final boolean toInternal;

  private final ExpressionValueVisitor visitor = new ExpressionValueVisitor();

  private List<String> conversionWarnings = new ArrayList<>();

  private List<String> conversionErrors = new ArrayList<>();

  public CstFilterVisitor(CstDefinition cstDefinition, CstService cstService, boolean toInternal) {
    this.cstDefinition = cstDefinition;
    this.cstService = cstService;
    this.toInternal = toInternal;
  }

  @Override
  public Object visit(PropertyIsBetween filter, Object extraData) {
    Expression expr = this.visit(filter.getExpression(), extraData);
    Expression lower = this.visit(filter.getLowerBoundary(), extraData);
    Expression upper = this.visit(filter.getUpperBoundary(), extraData);
    return this.getFactory(extraData).between(expr, lower, upper, filter.getMatchAction());
  }

  @Override
  public Object visit(PropertyIsEqualTo filter, Object extraData) {
    Expression expr1 = this.visit(filter.getExpression1(), extraData);
    Expression expr2 = this.visit(filter.getExpression2(), extraData);
    boolean matchCase = filter.isMatchingCase();

    Set<CstPair> transformedPairs = getTransformedPairsFromExpressions(expr1, expr2, extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData).equal(expr1, expr2, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.equal(
                factory.property(pair.getKey()),
                factory.literal(pair.getValue()),
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
    Expression expr1 = this.visit(filter.getExpression1(), extraData);
    Expression expr2 = this.visit(filter.getExpression2(), extraData);
    boolean matchCase = filter.isMatchingCase();
    Set<CstPair> transformedPairs = getTransformedPairsFromExpressions(expr1, expr2, extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData).notEqual(expr1, expr2, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.notEqual(
                factory.property(pair.getKey()),
                factory.literal(pair.getValue()),
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsGreaterThan filter, Object extraData) {
    Expression expr1 = this.visit(filter.getExpression1(), extraData);
    Expression expr2 = this.visit(filter.getExpression2(), extraData);
    boolean matchCase = filter.isMatchingCase();
    Set<CstPair> transformedPairs = getTransformedPairsFromExpressions(expr1, expr2, extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData).greater(expr1, expr2, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.greater(
                factory.property(pair.getKey()),
                factory.literal(pair.getValue()),
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
    Expression expr1 = this.visit(filter.getExpression1(), extraData);
    Expression expr2 = this.visit(filter.getExpression2(), extraData);
    boolean matchCase = filter.isMatchingCase();
    Set<CstPair> transformedPairs = getTransformedPairsFromExpressions(expr1, expr2, extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData)
          .greaterOrEqual(expr1, expr2, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.greaterOrEqual(
                factory.property(pair.getKey()),
                factory.literal(pair.getValue()),
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsLessThan filter, Object extraData) {
    Expression expr1 = this.visit(filter.getExpression1(), extraData);
    Expression expr2 = this.visit(filter.getExpression2(), extraData);
    boolean matchCase = filter.isMatchingCase();
    Set<CstPair> transformedPairs = getTransformedPairsFromExpressions(expr1, expr2, extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData).less(expr1, expr2, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.less(
                factory.property(pair.getKey()),
                factory.literal(pair.getValue()),
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
    Expression expr1 = this.visit(filter.getExpression1(), extraData);
    Expression expr2 = this.visit(filter.getExpression2(), extraData);
    boolean matchCase = filter.isMatchingCase();
    Set<CstPair> transformedPairs = getTransformedPairsFromExpressions(expr1, expr2, extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData)
          .lessOrEqual(expr1, expr2, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.lessOrEqual(
                factory.property(pair.getKey()),
                factory.literal(pair.getValue()),
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsLike filter, Object extraData) {
    Expression expr = this.visit(filter.getExpression(), extraData);
    String pattern = filter.getLiteral();
    String wildcard = filter.getWildCard();
    String singleChar = filter.getSingleChar();
    String escape = filter.getEscape();
    boolean matchCase = filter.isMatchingCase();

    Set<CstPair> transformedPairs =
        getTransformedPairsFromExpressions(expr, new LiteralExpressionImpl(pattern), extraData);
    if (transformedPairs == null) {
      return this.getFactory(extraData)
          .like(expr, pattern, wildcard, singleChar, escape, matchCase, filter.getMatchAction());
    }

    FilterFactory factory = this.getFactory(extraData);
    return createFilter(
        pair ->
            factory.like(
                factory.property(pair.getKey()),
                pair.getValue().toString(),
                wildcard,
                singleChar,
                escape,
                matchCase,
                filter.getMatchAction()),
        transformedPairs,
        factory);
  }

  @Override
  public Object visit(PropertyIsNull filter, Object extraData) {
    Expression expr = this.visit(filter.getExpression(), extraData);
    return this.getFactory(extraData).isNull(expr);
  }

  @Override
  public Object visit(PropertyIsNil filter, Object extraData) {
    Expression expr = this.visit(filter.getExpression(), extraData);
    return this.getFactory(extraData).isNil(expr, filter.getNilReason());
  }

  @Override
  public Object visit(After after, Object extraData) {
    return this.getFactory(extraData)
        .after(
            this.visit(after.getExpression1(), extraData),
            this.visit(after.getExpression2(), extraData),
            after.getMatchAction());
  }

  @Override
  public Object visit(Before before, Object extraData) {
    return this.getFactory(extraData)
        .before(
            this.visit(before.getExpression1(), extraData),
            this.visit(before.getExpression2(), extraData),
            before.getMatchAction());
  }

  @Override
  public Object visit(During during, Object extraData) {
    return this.getFactory(extraData)
        .during(
            this.visit(during.getExpression1(), extraData),
            this.visit(during.getExpression2(), extraData),
            during.getMatchAction());
  }

  @Override
  public Object visit(Contains filter, Object extraData) {
    Expression geometry1 = this.visit(filter.getExpression1(), extraData);
    Expression geometry2 = this.visit(filter.getExpression2(), extraData);
    return this.getFactory(extraData).contains(geometry1, geometry2, filter.getMatchAction());
  }

  @Override
  public Object visit(DWithin filter, Object extraData) {
    Expression geometry1 = this.visit(filter.getExpression1(), extraData);
    Expression geometry2 = this.visit(filter.getExpression2(), extraData);
    double distance = filter.getDistance();
    String units = filter.getDistanceUnits();
    return this.getFactory(extraData)
        .dwithin(geometry1, geometry2, distance, units, filter.getMatchAction());
  }

  @Override
  public Object visit(Intersects filter, Object extraData) {
    Expression geometry1 = this.visit(filter.getExpression1(), extraData);
    Expression geometry2 = this.visit(filter.getExpression2(), extraData);
    return this.getFactory(extraData).intersects(geometry1, geometry2, filter.getMatchAction());
  }

  @Override
  public Object visit(Within filter, Object extraData) {
    Expression geometry1 = this.visit(filter.getExpression1(), extraData);
    Expression geometry2 = this.visit(filter.getExpression2(), extraData);
    return this.getFactory(extraData).within(geometry1, geometry2, filter.getMatchAction());
  }

  private Set<CstPair> getTransformedPairsFromExpressions(
      Expression expr1, Expression expr2, Object extraData) {
    String propertyName = (String) expr1.accept(visitor, extraData);
    Serializable value = (Serializable) expr2.accept(visitor, extraData);

    CstTerm term = cstDefinition.getTerm(propertyName);
    if (term == null) {
      conversionWarnings.add(
          String.format(
              "The term %s is not part of the CST %s:%s and will be passed through as is",
              propertyName, cstDefinition.getNamespace(), cstDefinition.getVersion()));
      return null;
    }

    String validationError = term.validateValue(value);
    if (StringUtils.isNotEmpty(validationError)) {
      conversionErrors.add(validationError);
      throw new UnsupportedOperationException(validationError);
    }

    CstPair validatedPair = new CstPair(propertyName, value);
    Set<CstPair> transformedPairs = null;
    if (toInternal) {
      transformedPairs = cstService.toCatalogTaxonomy(validatedPair, cstDefinition);
    } else {
      transformedPairs = cstService.fromCatalogTaxonomy(validatedPair, cstDefinition);
    }

    if (transformedPairs.isEmpty()) {
      conversionWarnings.add(
          String.format("CST term %s was dropped from the filter", propertyName));
    }
    return transformedPairs;
  }

  private Filter createFilter(
      Function<CstPair, Filter> filterFunction, Set<CstPair> pairs, FilterFactory factory) {
    if (pairs.isEmpty()) {
      return null;
    }
    List<Filter> filters = pairs.stream().map(filterFunction::apply).collect(Collectors.toList());

    if (filters.size() == 1) {
      return filters.get(0);
    }
    return factory.or(filters);
  }

  static class ExpressionValueVisitor extends NullExpressionVisitor {
    @Override
    public Object visit(Literal expression, Object extraData) {
      Object value = expression.getValue();
      if (!(value instanceof Serializable)) {
        throw new UnsupportedOperationException("PropertyName and Literal is required for search.");
      }
      return value;
    }

    @Override
    public Object visit(PropertyName expr, Object extraData) {
      String value = expr.getPropertyName();
      if (StringUtils.isBlank(value)) {
        throw new UnsupportedOperationException("PropertyName and Literal is required for search.");
      }
      return value;
    }
  }

  public List<String> getConversionWarnings() {
    return conversionWarnings;
  }

  public List<String> getConversionErrors() {
    return conversionErrors;
  }
}
