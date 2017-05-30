package com.github.semres.user;

import com.github.semres.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.Collection;

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
        model.add(CommonIRI.USER_SYNSET, RDFS.SUBCLASSOF, SemRes.SYNSET);

        model.add(CommonIRI.USER_EDGE, RDF.TYPE, RDFS.CLASS);
        model.add(CommonIRI.USER_EDGE, RDFS.SUBCLASSOF, SemRes.EDGE);

        return model;
    }

    @Override
    public Collection<RelationType> getRelationTypes() {
        return new ArrayList<>();
    }
}
