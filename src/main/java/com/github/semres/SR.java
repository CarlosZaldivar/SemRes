package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SR {
    public static final IRI SYNSET;
    public static final IRI EDGE;
    public static final IRI ID;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        SYNSET = factory.createIRI("http://www.example.org/Synset");
        EDGE = factory.createIRI("http://www.example.org/Edge");
        ID = factory.createIRI("http://www.example.org/Id");
    }
}
