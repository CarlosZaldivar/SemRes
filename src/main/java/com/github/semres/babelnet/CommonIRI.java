package com.github.semres.babelnet;

import com.github.semres.SemRes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class CommonIRI {
    public static final IRI BABELNET_SYNSET;
    public static final IRI BABELNET_EDGE;
    public static final IRI EDGES_DOWNLOADED;
    public static final IRI REMOVED_RELATION;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        BABELNET_SYNSET = factory.createIRI(SemRes.baseIri + "BabelNet/classes/BabelNetSynset");
        BABELNET_EDGE = factory.createIRI(SemRes.baseIri + "BabelNet/classes/BabelNetEdge");
        EDGES_DOWNLOADED = factory.createIRI(SemRes.baseIri + "BabelNet/properties/EdgesDownloaded");
        REMOVED_RELATION = factory.createIRI(SemRes.baseIri + "BabelNet/properties/RemovedRelation");
    }
}
