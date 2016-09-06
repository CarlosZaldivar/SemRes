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

    public static final IRI POINTED_SYSNET;
    public static final IRI ORIGIN_SYNSET;
    public static final IRI WEIGHT;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        SYNSET = factory.createIRI("http://www.example.org/Synset");
        EDGE = factory.createIRI("http://www.example.org/Edge");
        ID = factory.createIRI("http://www.example.org/Id");
        RELATION_TYPE = factory.createIRI("http://www.example.org/RelationType");
        HOLONYM = factory.createIRI("http://www.example.org/Holonym");
        HYPERNYM = factory.createIRI("http://example.org/Hypernym");
        HYPONYM = factory.createIRI("http://example.org/Hyponym");
        MERONYM = factory.createIRI("http://example.org/Meronym");

        POINTED_SYSNET = factory.createIRI("http://example.org/PointedSynset");
        ORIGIN_SYNSET = factory.createIRI("http://example.org/OriginSynset");
        WEIGHT = factory.createIRI("http://example.org/Weight");
    }
}
