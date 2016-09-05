package com.github.semres;

import org.eclipse.rdf4j.repository.Repository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private List<Serializer> serializers;
    private Repository repository;
    private String baseIri = "http://example.org/";

    public Database(List<Class> serializerClasses, Repository repository) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Serializer> serializers = new ArrayList<>();
        for (Class serializerClass: serializerClasses) {
            Serializer loadedSerializer = (Serializer) serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            serializers.add(loadedSerializer);
        }
        this.serializers = serializers;
        this.repository = repository;
    }
}
