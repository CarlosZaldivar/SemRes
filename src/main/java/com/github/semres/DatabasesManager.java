package com.github.semres;

import com.esotericsoftware.yamlbeans.YamlException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabasesManager {
    private final List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
    private final List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();

    private final Model metadataStatements;
    private final LocalRepositoryManager repositoryManager;

    DatabasesManager(Settings settings) {
        metadataStatements = new LinkedHashModel();
        for (Source source: settings.getSources()) {
            synsetSerializerClasses.add(source.getSynsetSerializerClass());
            edgeSerializerClasses.add(source.getEdgeSerializerClass());
            metadataStatements.addAll(source.getMetadataStatements());
        }
        repositoryManager = new LocalRepositoryManager(new File(settings.getDatabasesDirectory()));
        repositoryManager.initialize();
    }

    public DatabasesManager() throws FileNotFoundException, YamlException {
        this(new Settings());
    }

    public List<Class> getSynsetSerializerClasses() {
        return new ArrayList<>(synsetSerializerClasses);
    }

    public Set<String> getRepositoryIDs() {
        return repositoryManager.getRepositoryIDs();
    }

    public Board getBoard(String repositoryId) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Repository repository = repositoryManager.getRepository(repositoryId);
        Database database = new Database(synsetSerializerClasses, edgeSerializerClasses, repository);
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

    public Repository getRepository(String repositoryId) {
        return repositoryManager.getRepository(repositoryId);
    }

    public void save() {
        repositoryManager.shutDown();
    }

    private void initializeRepository(Repository repository) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(metadataStatements);
        }
    }
}
