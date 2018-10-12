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

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.codice.ddf.catalog.cst.api.CstTerm;

public class CstTermImpl implements CstTerm {

  private final String name;

  private final String description;

  private final Type type;

  private final Function<Serializable, String> validator;

  public CstTermImpl(String name, Type type) {
    this(name, null, type, null);
  }

  public CstTermImpl(
      String name, String description, Type type, Function<Serializable, String> validator) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.validator = validator;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String validateValue(Serializable value) {
    if (validator == null) {
      return "";
    }
    return validator.apply(value);
  }

  public static CstTerm newEnumeratedTerm(
      String name, String description, Type type, Collection<Serializable> terms) {
    return newEnumeratedTerm(name, description, type, terms, false);
  }

  public static CstTerm newEnumeratedTerm(
      String name,
      String description,
      Type type,
      Collection<Serializable> terms,
      boolean allowWildcard) {
    if (allowWildcard) {
      return new CstTermImpl(
          name,
          description,
          type,
          value ->
              contains(terms, (String) value)
                  ? ""
                  : String.format(
                      "Value: %s does not match the enumerated types for field: %s", value, name));
    }
    return new CstTermImpl(
        name,
        description,
        type,
        value ->
            terms.contains(value)
                ? ""
                : String.format(
                    "Value: %s does not match the enumerated types for field: %s", value, name));
  }

  public static CstTerm newRegexTerm(String name, String description, Type type, String regex) {
    return new CstTermImpl(
        name,
        description,
        type,
        value ->
            ((String) value).matches(regex)
                ? ""
                : String.format(
                    "Value: %s does not match regular expression %s for field: %s",
                    value, regex, name));
  }

  public static CstTerm newNumericRangeTerm(
      String name, String description, Type type, Number start, Number end) {
    return new CstTermImpl(
        name,
        description,
        type,
        value ->
            (start.doubleValue() >= ((Number) value).doubleValue()
                    && ((Number) value).doubleValue() <= end.doubleValue())
                ? ""
                : String.format(
                    "Value: %s is not in the range of %s to %s for field: %s",
                    value, start, end, name));
  }

  public static CstTerm newStringLengthTerm(
      String name, String description, Type type, int maxLength) {
    return new CstTermImpl(
        name,
        description,
        type,
        value ->
            ((String) value).length() <= maxLength
                ? ""
                : String.format(
                    "Value: %s exceeds the maximum length of %s for field: %s",
                    value, maxLength, name));
  }

  private static boolean contains(Collection<Serializable> terms, String value) {

    if (value.contains("*")) {
      Pattern p = Pattern.compile(value.replaceAll("\\*", ".*"));
      return terms.stream().anyMatch(v -> p.matcher(v.toString()).matches());
    }
    return terms.contains(value);
  }
}
