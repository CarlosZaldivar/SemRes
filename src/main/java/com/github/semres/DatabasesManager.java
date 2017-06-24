package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DatabasesManager {
    private final List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
    private final List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();
    private final Collection<RelationType> relationTypes;
    private final String baseIri = "http://example.org/";
    private final Model metadataStatements;
    private final LocalRepositoryManager repositoryManager;

    public DatabasesManager(Settings settings) {
        metadataStatements = new LinkedHashModel();
        relationTypes = new ArrayList<>();
        for (Source source: settings.getSources()) {
            synsetSerializerClasses.add(source.getSynsetSerializerClass());
            edgeSerializerClasses.add(source.getEdgeSerializerClass());
            metadataStatements.addAll(source.getMetadataStatements());
            relationTypes.addAll(source.getRelationTypes());
        }
        repositoryManager = new LocalRepositoryManager(new File(settings.getDatabasesDirectory()));
        repositoryManager.initialize();
    }

    public List<Class> getSynsetSerializerClasses() {
        return new ArrayList<>(synsetSerializerClasses);
    }

    public Set<String> getRepositoryIDs() {
        return repositoryManager.getRepositoryIDs();
    }

    public Board getBoard(String repositoryId) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Repository repository = repositoryManager.getRepository(repositoryId);
        Database database = new Database(baseIri, synsetSerializerClasses, edgeSerializerClasses, repository);
        return new Board(database);
    }

    public void addRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException();
        }

        if (repositoryManager.getRepositoryIDs().contains(repositoryId)) {
            throw new IllegalArgumentException();
        }

        repositoryManager.addRepositoryConfig(new RepositoryConfig(repositoryId, new SailRepositoryConfig(new MemoryStoreConfig(true))));
        // Add some initial statements to new repository.
        initializeRepository(repositoryManager.getRepository(repositoryId));
    }

    public void deleteRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException();
        }

        if (!repositoryManager.getRepositoryIDs().contains(repositoryId)) {
            throw new IllegalArgumentException();
        }

        repositoryManager.removeRepository(repositoryId);
    }

    public void save() {
        repositoryManager.shutDown();
    }

    private void initializeRepository(Repository repository) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(metadataStatements);

            // Add relation types
            Model model = new LinkedHashModel();
            for (RelationType relationType : relationTypes) {
                ValueFactory factory = SimpleValueFactory.getInstance();
                IRI relationTypeIri = factory.createIRI(baseIri + "relationTypes/" + relationType.getType());
                model.add(relationTypeIri, RDF.TYPE, SemRes.RELATION_TYPE_CLASS);
                model.add(relationTypeIri, RDFS.LABEL, factory.createLiteral(relationType.getType()));
                model.add(relationTypeIri, SemRes.SOURCE, factory.createLiteral(relationType.getSource()));
            }
            conn.add(model);
        }
    }
}
