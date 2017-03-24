package com.github.semres.user;

import com.github.semres.EdgeSerializer;
import com.github.semres.SR;
import com.github.semres.Source;
import com.github.semres.SynsetSerializer;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public class UserManager extends Source {
    @Override
    public Class<? extends SynsetSerializer> getSynsetSerializerClass() {
        return UserSynsetSerializer.class;
    }

    @Override
    public Class<? extends EdgeSerializer> getEdgeSerializerClass() {
        return UserEdgeSerializer.class;
    }

    @Override
    public Model getMetadataStatements() {
        Model model = new LinkedHashModel();

        model.add(CommonIRI.USER_SYNSET, RDF.TYPE, RDFS.CLASS);
        model.add(CommonIRI.USER_SYNSET, RDFS.SUBCLASSOF, SR.SYNSET);

        model.add(CommonIRI.USER_EDGE, RDF.TYPE, RDFS.CLASS);
        model.add(CommonIRI.USER_EDGE, RDFS.SUBCLASSOF, SR.EDGE);

        return model;
    }
}
