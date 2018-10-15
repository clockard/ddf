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

import java.util.Collections;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.transformer.InternalTaxonomyTransformerImpl;

public class DdfCstTaxonomyTransformer extends InternalTaxonomyTransformerImpl {

  public DdfCstTaxonomyTransformer() {
    super("cst:ddf");
    init();
  }

  public void init() {
    addNameTransform("subtitle", "ext.ddms.subtitle");
    addTaxonomyTransform(
        "resource-size",
        pair -> Collections.singleton(new CstPair(pair.getKey(), pair.getValue().toString())),
        "resource-size",
        pair ->
            Collections.singleton(
                new CstPair(pair.getKey(), Long.parseLong(pair.getValue().toString()))));
  }
}
