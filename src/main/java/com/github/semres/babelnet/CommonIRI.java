package com.github.semres.babelnet;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class CommonIRI {
    public static final IRI BABELNET_SYNSET;
    public static final IRI BABELNET_EDGE;
    public static final IRI EDGES_DOWNLOADED;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        BABELNET_SYNSET = factory.createIRI("http://github.com/SemRes/BabelNet/BabelNetSynset");
        BABELNET_EDGE = factory.createIRI("http://github.com/SemRes/BabelNet/BabelNetEdge");
        EDGES_DOWNLOADED = factory.createIRI("http://github.com/SemRes/BabelNet/EdgesDownloaded");
    }
}