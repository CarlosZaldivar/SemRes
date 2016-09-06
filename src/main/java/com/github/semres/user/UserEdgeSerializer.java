package com.github.semres.user;

import com.github.semres.Edge;
import com.github.semres.EdgeSerializer;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;

public class UserEdgeSerializer extends EdgeSerializer {
    @Override
    public Model edgeToRdf(Edge edge) {
        return null;
    }

    @Override
    public Edge rdfToEdge(String edgeId) {
        return null;
    }

    public UserEdgeSerializer(Repository repository, String baseIri) {
        super(repository, baseIri);
    }
}
