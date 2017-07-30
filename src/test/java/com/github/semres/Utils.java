package com.github.semres;

import com.github.semres.babelnet.BabelNetEdgeSerializer;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.babelnet.BabelNetSynsetSerializer;
import com.github.semres.user.UserEdgeSerializer;
import com.github.semres.user.UserManager;
import com.github.semres.user.UserSynsetSerializer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static Database createTestDatabase() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String baseIri = "http://example.org/";
        Repository repo = createTestRepository(baseIri);
        List<Class<? extends SynsetSerializer>> synsetSerializers = new ArrayList<>();
        synsetSerializers.add(UserSynsetSerializer.class);
        synsetSerializers.add(BabelNetSynsetSerializer.class);
        List<Class<? extends EdgeSerializer>> edgeSerializers = new ArrayList<>();
        edgeSerializers.add(UserEdgeSerializer.class);
        edgeSerializers.add(BabelNetEdgeSerializer.class);

        return new Database(synsetSerializers, edgeSerializers, repo);
    }

    public static Repository createTestRepository(String baseIri) {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();

        try (RepositoryConnection conn = repo.getConnection()) {
            conn.add(new BabelNetManager().getMetadataStatements());
            conn.add(new UserManager().getMetadataStatements());
            Model model = new LinkedHashModel();

            ValueFactory factory = SimpleValueFactory.getInstance();
            for (RelationType relationType : new BabelNetManager().getRelationTypes()) {
                IRI relationTypeIri = factory.createIRI(baseIri + "relationTypes/" + relationType.getType());
                model.add(relationTypeIri, RDF.TYPE, SemRes.RELATION_TYPE_CLASS);
                model.add(relationTypeIri, RDFS.LABEL, factory.createLiteral(relationType.getType()));
                model.add(relationTypeIri, SemRes.SOURCE, factory.createLiteral(relationType.getSource()));
            }

            // Add base IRI
            model.add(factory.createIRI(baseIri), RDF.TYPE, SemRes.BASE_IRI);

            conn.add(model);
        }
        return repo;
    }
}
