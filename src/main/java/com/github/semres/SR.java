package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SR {

    // Classes
    public static final IRI SYNSET;
    public static final IRI EDGE;
    public static final IRI HOLONYM;
    public static final IRI HYPERNYM;
    public static final IRI HYPONYM;
    public static final IRI MERONYM;

    // Properties
    public static final IRI ID;
    public static final IRI RELATION_TYPE;
    public static final IRI REMOVED_RELATION;

    public static final IRI WEIGHT;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        SYNSET = factory.createIRI(SemRes.baseIri + "Synset");
        EDGE = factory.createIRI(SemRes.baseIri + "Edge");
        ID = factory.createIRI(SemRes.baseIri + "Id");
        REMOVED_RELATION = factory.createIRI(SemRes.baseIri + "RemovedRelation");
        RELATION_TYPE = factory.createIRI(SemRes.baseIri + "RelationType");
        HOLONYM = factory.createIRI(SemRes.baseIri + "Holonym");
        HYPERNYM = factory.createIRI(SemRes.baseIri + "Hypernym");
        HYPONYM = factory.createIRI(SemRes.baseIri + "Hyponym");
        MERONYM = factory.createIRI(SemRes.baseIri + "Meronym");

        WEIGHT = factory.createIRI(SemRes.baseIri + "Weight");
    }
}
