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
        SYNSET = factory.createIRI("http://www.example.org/Synset");
        EDGE = factory.createIRI("http://www.example.org/Edge");
        ID = factory.createIRI("http://www.example.org/Id");
        REMOVED_RELATION = factory.createIRI("http://www.example.org/RemovedRelation");
        RELATION_TYPE = factory.createIRI("http://www.example.org/RelationType");
        HOLONYM = factory.createIRI("http://www.example.org/Holonym");
        HYPERNYM = factory.createIRI("http://example.org/Hypernym");
        HYPONYM = factory.createIRI("http://example.org/Hyponym");
        MERONYM = factory.createIRI("http://example.org/Meronym");

        WEIGHT = factory.createIRI("http://example.org/Weight");
    }
}
