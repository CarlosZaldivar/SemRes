package com.github.semres.babelnet;

import com.github.semres.SynsetSerializer;
import com.github.semres.Synset;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.repository.Repository;

public class BabelNetSynsetSerializer extends SynsetSerializer {
    public BabelNetSynsetSerializer(Repository repository, String baseIri) {
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
