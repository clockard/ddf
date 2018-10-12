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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.codice.ddf.catalog.cst.api.CstDefinition;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.CstService;
import org.codice.ddf.catalog.cst.api.CstTerm;
import org.geotools.filter.FilterFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.MultiValuedFilter;

@RunWith(MockitoJUnitRunner.class)
public class CstFilterVisitorTest {

  FilterFactory factory = new FilterFactoryImpl();

  @Mock CstService cstService;

  @Mock CstDefinition cstDefinition;

  @Before
  public void setup() {}

  @Test
  public void testPropertyIsEqual() {
    CstTerm mockTerm = mock(CstTerm.class);
    when(mockTerm.validateValue(any())).thenReturn("");
    when(cstDefinition.getTerm("cstTerm")).thenReturn(mockTerm);
    when(cstService.toCatalogTaxonomy(any(CstPair.class), any()))
        .thenReturn(Collections.singleton(new CstPair("internalTerm", "internalValue")));
    Object filter =
        new CstFilterVisitor(cstDefinition, cstService, true)
            .visit(
                factory.equal(
                    factory.property("cstTerm"),
                    factory.literal("myValue"),
                    false,
                    MultiValuedFilter.MatchAction.ALL),
                null);
  }

  @Test
  public void testPropertyIsEqualToMultipleTerms() {
    CstTerm mockTerm = mock(CstTerm.class);
    when(mockTerm.validateValue(any())).thenReturn("");
    when(cstDefinition.getTerm("cstTerm")).thenReturn(mockTerm);
    Set<CstPair> newTerms = new HashSet<>();
    newTerms.add(new CstPair("internalTerm1", "internalValue1"));
    newTerms.add(new CstPair("internalTerm2", "internalValue2"));
    when(cstService.toCatalogTaxonomy(any(CstPair.class), any())).thenReturn(newTerms);
    Object filter =
        new CstFilterVisitor(cstDefinition, cstService, true)
            .visit(
                factory.equal(
                    factory.property("cstTerm"),
                    factory.literal("myValue"),
                    false,
                    MultiValuedFilter.MatchAction.ALL),
                null);
  }

  @Test
  public void testPropertyIsEqualNoTerms() {
    when(cstDefinition.getTerm("cstTerm")).thenReturn(null);
    Object filter =
        new CstFilterVisitor(cstDefinition, cstService, true)
            .visit(
                factory.equal(
                    factory.property("cstTerm"),
                    factory.literal("myValue"),
                    false,
                    MultiValuedFilter.MatchAction.ALL),
                null);
  }
}
