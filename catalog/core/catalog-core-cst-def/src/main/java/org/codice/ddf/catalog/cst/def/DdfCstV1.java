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
package org.codice.ddf.catalog.cst.def;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.codice.ddf.catalog.cst.api.CstTerm;
import org.codice.ddf.catalog.cst.data.CstDefinitionImpl;
import org.codice.ddf.catalog.cst.data.CstTermImpl;

public class DdfCstV1 extends CstDefinitionImpl {

  private static final Set<Serializable> DATATYPES =
      ImmutableSet.of(
          "Collection",
          "Dataset",
          "Event",
          "Image",
          "Interactive Resource",
          "Moving Image",
          "Physical Object",
          "Service",
          "Software",
          "Sound",
          "Still Image",
          "Text");

  private static final Set<CstTerm> TERMS = new HashSet<>();

  static {
    TERMS.add(CstTermImpl.newStringLengthTerm("id", "Metacard unique id", CstTerm.Type.STRING, 32));
    TERMS.add(
        CstTermImpl.newEnumeratedTerm(
            "datatype",
            " DCMI Type term labels are expected here as opposed to term names.",
            CstTerm.Type.STRING,
            DATATYPES,
            true));
    TERMS.add(
        CstTermImpl.newStringLengthTerm(
            "title", "A name for the resource.", CstTerm.Type.STRING, 256));
    TERMS.add(
        CstTermImpl.newStringLengthTerm(
            "subtitle", "A subtitle for the resource.", CstTerm.Type.STRING, 256));
    TERMS.add(
        CstTermImpl.newNumericRangeTerm(
            "resource-size", "The resource size in bytes", CstTerm.Type.LONG, 0, Long.MAX_VALUE));
  }

  public DdfCstV1() {
    super("cst:ddf", 1, TERMS);
  }
}
