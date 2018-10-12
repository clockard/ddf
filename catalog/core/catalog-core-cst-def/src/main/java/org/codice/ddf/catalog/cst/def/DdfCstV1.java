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
 */package org.codice.ddf.catalog.cst.def;

import org.codice.ddf.catalog.cst.api.CstDefinition;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.CstTerm;
import org.codice.ddf.catalog.cst.data.CstDefinitionImpl;
import org.codice.ddf.catalog.cst.data.CstTermImpl;

import java.util.HashSet;
import java.util.Set;

public class DdfCstV1 extends CstDefinitionImpl {

    static Set<CstTerm> terms = new HashSet<>();
    static {
        terms.add(CstTermImpl.newStringLengthTerm("id", "Metacard unique id", CstTerm.Type.STRING, 32));
        terms.add(CstTermImpl.newStringLengthTerm("id", "Metacard unique id", CstTerm.Type.STRING, 32));
        terms.add(CstTermImpl.newStringLengthTerm("id", "Metacard unique id", CstTerm.Type.STRING, 32));
        terms.add(CstTermImpl.newStringLengthTerm("id", "Metacard unique id", CstTerm.Type.STRING, 32));
    }

    public DdfCstV1() {
        super("cst:ddf",1, terms);
    }
}
