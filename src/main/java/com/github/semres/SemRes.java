package com.github.semres;

import com.esotericsoftware.yamlbeans.YamlException;
import com.github.semres.gui.Main;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.application.Application;


public class SemRes {

    final static Logger logger = Logger.getLogger(SemRes.class);

    public List<Class> getSerializerClasses() {
        return new ArrayList<>(serializerClasses);
    }

    private List<Class> serializerClasses = new ArrayList<>();
    private Settings settings;
    private LocalRepositoryManager repositoryManager;
    private static SemRes semRes;

    public void addSerializerClass(Class serializer) {
        if (!SynsetSerializer.class.isAssignableFrom(serializer)) {
            throw new IllegalArgumentException();
        }
        serializerClasses.add(serializer);
    }

    public SemRes() throws FileNotFoundException, YamlException {
        this(new Settings());
    }

    public SemRes(Settings settings) {
        for (Source source: settings.getSources()) {
            serializerClasses.add(source.getSerializerClass());
        }
        repositoryManager = new LocalRepositoryManager(new File(settings.getDatabasesDirectory()));
        repositoryManager.initialize();
    }

    public static void main(String[] args) {
        try {
            semRes = new SemRes();
        } catch (IOException | NullPointerException e) {
            logger.error("Could not load settings from conf.yaml", e);
        }
        Application.launch(Main.class, args);
    }

    public static SemRes getInstance() {
        return semRes;
    }

    public Set<String> getRepositoryIDs() {
        return repositoryManager.getRepositoryIDs();
    }

    public Board getBoard(String repositoryId) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Repository repository = repositoryManager.getRepository(repositoryId);
        Database database = new Database(serializerClasses, repository);
        return new Board(database);
    }

    public void addRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException();
        }

        if (repositoryManager.getRepositoryIDs().contains(repositoryId)) {
            throw new IllegalArgumentException();
        }

        repositoryManager.addRepositoryConfig(new RepositoryConfig(repositoryId, new SailRepositoryConfig(new MemoryStoreConfig())));
    }

    public static String getBaseDirectory() {
        String path = null;
        try {
            path = URLDecoder.decode(Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unsupported");
        }

        // Leaves only the directory
        path = path.substring(0, path.lastIndexOf('/') + 1);
        return path;
    }

    public Repository getRepository(String repositoryId) {
        return repositoryManager.getRepository(repositoryId);
    }
}
