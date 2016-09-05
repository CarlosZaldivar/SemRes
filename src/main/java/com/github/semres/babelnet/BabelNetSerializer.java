package com.github.semres.babelnet;

import com.github.semres.Serializer;
import com.github.semres.Synset;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.repository.Repository;

public class BabelNetSerializer extends Serializer {
    public BabelNetSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }

    @Override
    public Model synsetToRdf(Synset synset) {
        return null;
    }

    public @Override
    Synset rdfToSynset(String synsetId) {
        return null;
    }

}
