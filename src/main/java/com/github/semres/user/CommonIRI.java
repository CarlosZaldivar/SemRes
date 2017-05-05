package com.github.semres.user;

import com.github.semres.SemRes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class CommonIRI {
    public static final IRI USER_SYNSET;
    public static final IRI USER_EDGE;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        USER_SYNSET = factory.createIRI(SemRes.baseIri + "User/UserSynset");
        USER_EDGE = factory.createIRI(SemRes.baseIri + "User/UserEdge");
    }
}
