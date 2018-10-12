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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Set;
import org.codice.ddf.catalog.cst.api.CstPair;
import org.codice.ddf.catalog.cst.api.CstTerm;
import org.codice.ddf.catalog.cst.api.InternalTaxonomyTransformer;
import org.codice.ddf.catalog.cst.data.CstDefinitionImpl;
import org.codice.ddf.catalog.cst.data.CstTermImpl;
import org.codice.ddf.catalog.cst.transformer.InternalTaxonomyTransformerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CstTest {

  private CstServiceImpl cstService;

  private CstDefinitionImpl def1;

  private CstDefinitionImpl def2;

  private InternalTaxonomyTransformer transformer;

  @Before
  public void setup() {
    def1 = new CstDefinitionImpl("cst:def", 1, Collections.emptyList());
    def2 = new CstDefinitionImpl("cst:def", 2, Collections.emptyList());
    transformer = new InternalTaxonomyTransformerImpl("cst:def");
    cstService = new CstServiceImpl();
    cstService.registerCstDefinition(def1);
    cstService.registerCstDefinition(def2);
    cstService.registerTransformer(transformer);
  }

  @Test
  public void testCstTransforms() {
    def1.addTerm(new CstTermImpl("name", CstTerm.Type.STRING));
    def1.addTerm(CstTermImpl.newStringLengthTerm("title", "description", CstTerm.Type.STRING, 10));
    def1.addTerm(
        CstTermImpl.newNumericRangeTerm("size", "description", CstTerm.Type.LONG, 0L, 100L));
    def1.addTerm(
        CstTermImpl.newEnumeratedTerm(
            "type", "description", CstTerm.Type.STRING, Collections.singletonList("myType")));
    def1.addTerm(
        CstTermImpl.newEnumeratedTerm(
            "type2",
            "description",
            CstTerm.Type.STRING,
            Collections.singletonList("otherType"),
            true));
    def1.addTerm(CstTermImpl.newRegexTerm("other", "description", CstTerm.Type.STRING, ".*aaa"));

    def2.addTerm(new CstTermImpl("name", CstTerm.Type.STRING));
    def2.addTerm(
        CstTermImpl.newStringLengthTerm("metadata.title", "description", CstTerm.Type.STRING, 10),
        (pair) -> Collections.singleton(new CstPair("title", pair.getValue())),
        "title",
        (pair) -> Collections.singleton(new CstPair("metadata.title", pair.getValue())));
    def2.addTerm(
        CstTermImpl.newNumericRangeTerm("size.bytes", "description", CstTerm.Type.LONG, 0L, 1000L),
        (pair) -> Collections.singleton(new CstPair("size", ((Long) pair.getValue()) / 1024)),
        "size",
        (pair) ->
            Collections.singleton(new CstPair("size.bytes", ((Long) pair.getValue()) * 1024)));
    def2.addTerm(
        CstTermImpl.newEnumeratedTerm(
            "type", "description", CstTerm.Type.STRING, Collections.singletonList("myType")));
    def2.addTerm(
        CstTermImpl.newEnumeratedTerm(
            "type2",
            "description",
            CstTerm.Type.STRING,
            Collections.singletonList("otherType"),
            true));
    def2.addTerm(CstTermImpl.newRegexTerm("newfield", "description", CstTerm.Type.STRING, ".*bbb"));
    // Sets.newHashSet
    Set<CstPair> pairs = cstService.transform(new CstPair("name", "myName"), def1, def2);
    assertThat(pairs.size(), is(1));
    CstPair pair = pairs.iterator().next();
    assertThat(pair.getKey(), is("name"));
    assertThat(pair.getValue(), is("myName"));

    pairs = cstService.transform(new CstPair("title", "myTitle"), def1, def2);
    assertThat(pairs.size(), is(1));
    pair = pairs.iterator().next();
    assertThat(pair.getKey(), is("metadata.title"));
    assertThat(pair.getValue(), is("myTitle"));

    pairs = cstService.transform(new CstPair("size", 20L), def1, def2);
    assertThat(pairs.size(), is(1));
    pair = pairs.iterator().next();
    assertThat(pair.getKey(), is("size.bytes"));
    assertThat(pair.getValue(), is(20 * 1024L));
  }
}
