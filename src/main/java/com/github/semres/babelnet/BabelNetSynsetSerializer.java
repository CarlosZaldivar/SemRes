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

    @Override
    public Synset rdfToSynset(String synsetId) {
        return null;
    }

    @Override
    public Synset rdfToSynset(IRI synsetIri) {
        return null;
    }

    @Override
    public String getSynsetClass() {
        return "com.github.semres.babelnet.BabelNetSynset";
    }

    @Override
    public IRI getSynsetClassIri() {
        return repository.getValueFactory().createIRI(baseIri + "classes/BabelNetSynset");
    }
}
