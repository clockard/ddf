package org.codice.ddf.catalog.cst.api;

import ddf.catalog.data.AttributeType;

import java.io.Serializable;

public interface CstTerm<T extends Serializable> {

    String getName();

    String getDescription();

    AttributeType<T> getType();

    boolean validateValue(Serializable value);
}
