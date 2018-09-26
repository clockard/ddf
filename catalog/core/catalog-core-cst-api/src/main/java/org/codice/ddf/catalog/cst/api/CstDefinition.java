package org.codice.ddf.catalog.cst.api;

import ddf.catalog.data.Attribute;

import java.util.Set;

public interface CstDefinition {
    String getName();

    String getNamespace();

    float getVersion();

    CstTerm getTerm(String name);

    Set<CstTerm> getTerms();

    Attribute toPreviousVersion(Attribute attribute);

    Attribute fromPreviousVersion(Attribute attribute);
}
