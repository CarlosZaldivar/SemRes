package com.github.semres;

import org.eclipse.rdf4j.repository.Repository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private List<SynsetSerializer> synsetSerializers;
    private Repository repository;
    private String baseIri = "http://example.org/";

    public Database(List<Class> serializerClasses, Repository repository) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<SynsetSerializer> synsetSerializers = new ArrayList<>();
        for (Class serializerClass: serializerClasses) {
            SynsetSerializer loadedSynsetSerializer = (SynsetSerializer) serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            synsetSerializers.add(loadedSynsetSerializer);
        }
        this.synsetSerializers = synsetSerializers;
        this.repository = repository;
    }
}
