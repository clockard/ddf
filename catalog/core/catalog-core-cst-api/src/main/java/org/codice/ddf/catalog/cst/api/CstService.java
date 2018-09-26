package org.codice.ddf.catalog.cst.api;

import ddf.catalog.data.Attribute;

import java.util.Set;

public interface CstService {

    Set<CstDefinition> getCstDefinitions();

    CstDefinition getCstDefinition(String name);

    Attribute transform(Attribute attribute, CstDefinition from, CstDefinition to);

    Attribute toCatalogTaxonomy(Attribute attribute, CstDefinition from);

    Attribute fromCatalogTaxonomy(Attribute attribute, CstDefinition to);
}
