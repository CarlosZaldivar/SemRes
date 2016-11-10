package com.github.semres;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Database {
    private List<SynsetSerializer> synsetSerializers;
    private List<EdgeSerializer> edgeSerializers;
    private Repository repository;
    private String baseIri = "http://example.org/";

    public Database(List<Class> synsetSerializerClasses, List<Class> edgeSerializerClasses, Repository repository) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<SynsetSerializer> synsetSerializers = new ArrayList<>();
        for (Class serializerClass: synsetSerializerClasses) {
            SynsetSerializer loadedSynsetSerializer = (SynsetSerializer) serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            synsetSerializers.add(loadedSynsetSerializer);
        }
        this.synsetSerializers = synsetSerializers;

        List<EdgeSerializer> edgeSerializers = new ArrayList<>();
        for (Class serializerClass: edgeSerializerClasses) {
            EdgeSerializer loadedEdgeSerializer = (EdgeSerializer) serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            edgeSerializers.add(loadedEdgeSerializer);
        }
        this.edgeSerializers = edgeSerializers;

        this.repository = repository;
        this.repository.initialize();
    }

    public void addSynset(Synset synset) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForSynset(synset).synsetToRdf(synset));
        }
    }

    public void addEdge(Edge edge) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForEdge(edge).edgeToRdf(edge));
        }
    }

    private Synset getSynset(String id, String type) {
        return getSerializerForSynset(type).rdfToSynset(id);
    }

    public List<Synset> getSynsets() {
        List<Synset> synsets = new ArrayList<>();

        try (RepositoryConnection conn = repository.getConnection()) {
            // Find subjects of type that is a subclass of SR.SYNSET
            String queryString = String.format("SELECT ?s ?type WHERE { ?s <%s> ?type . ?type <%s> <%s> }", RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    String subject = bindingSet.getValue("s").stringValue();

                    // Get type.
                    String type = bindingSet.getValue("type").stringValue();

                    // Find id.
                    String id;
                    queryString = String.format("SELECT ?o WHERE { <%s> <%s> ?o }", subject, SR.ID);
                    tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                    try (TupleQueryResult ids = tupleQuery.evaluate()) {
                        id = QueryResults.asList(ids).get(0).getValue("o").stringValue();
                    }

                    synsets.add(getSynset(id, type));
                }
            }
        }
        return synsets;
    }

    private String getSynsetClass(String synsetId) {
        return null;
    }

    private SynsetSerializer getSerializerForSynset(Synset synset) {
        SynsetSerializer serializer;
        try {
            serializer = synsetSerializers
                    .stream()
                    .filter(x -> x.getSynsetClass().equals(synset.getClass().getCanonicalName()))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
        return serializer;
    }

    private SynsetSerializer getSerializerForSynset(String type) {
        SynsetSerializer serializer;
        try {
            serializer = synsetSerializers
                    .stream()
                    .filter(x -> x.getSynsetClassIri().stringValue().equals(type))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        }
        return serializer;
    }

    private EdgeSerializer getSerializerForEdge(Edge edge) {
        EdgeSerializer serializer;
        try {
            serializer = edgeSerializers
                    .stream()
                    .filter(x -> x.getEdgeClass().equals(edge.getClass().getCanonicalName()))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
        return serializer;
    }
}
