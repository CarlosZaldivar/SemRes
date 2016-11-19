package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
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

    public void removeSynset(Synset synset) {
        try (RepositoryConnection conn = repository.getConnection()) {
            ValueFactory factory = conn.getValueFactory();
            String queryString = String.format("CONSTRUCT { ?s ?p ?o } WHERE {?s <%s> %s . ?s ?p ?o }", SR.ID, factory.createLiteral(synset.getId()));
            GraphQueryResult results = conn.prepareGraphQuery(queryString).evaluate();
            while (results.hasNext()) {
                Statement statement = results.next();
                conn.remove(statement);
            }
        }
    }

    private Synset getSynset(IRI synsetIri, String type) {
        return getSerializerForSynset(type).rdfToSynset(synsetIri);
    }

    private Edge getEdge(IRI edgeIri, String type, Synset originSynset, Synset pointedSynset) {
        return getSerializerForEdge(type).rdfToEdge(edgeIri, pointedSynset, originSynset);
    }

    public List<Synset> getSynsets() {
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> }", RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET);
        return getSynsets(queryString);
    }

    public List<Synset> searchSynsets(String searchPhrase) {
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> . ?synset <%s> %s }",
                RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET, RDFS.LABEL, factory.createLiteral(searchPhrase));

        return getSynsets(queryString);
    }

    private List<Synset> getSynsets(String queryString) {
        List<Synset> synsets = new ArrayList<>();
        ValueFactory factory = repository.getValueFactory();
        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String type = bindingSet.getValue("type").stringValue();
                    IRI synsetIri = factory.createIRI(bindingSet.getValue("synset").stringValue());

                    synsets.add(getSynset(synsetIri, type));
                }
            }
        }
        return synsets;
    }

    public void loadEdges(Synset originSynset) {
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?edgeType ?edge ?pointedSynset ?pointedSynsetType" +
                " WHERE { ?originSynset <%s> %s . ?originSynset ?edge ?pointedSynset . ?edge <%s> ?edgeType . ?edgeType <%s> <%s> ." +
                         "?pointedSynset <%s> ?pointedSynsetType }",
                SR.ID, factory.createLiteral(originSynset.getId()), RDF.TYPE, RDFS.SUBCLASSOF, SR.EDGE, RDF.TYPE);

        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String edgeType = bindingSet.getValue("edgeType").stringValue();
                    IRI edgeIri = factory.createIRI(bindingSet.getValue("edge").stringValue());
                    IRI pointedSynsetIri = factory.createIRI(bindingSet.getValue("pointedSynset").stringValue());
                    String pointedSynsetType = bindingSet.getValue("pointedSynsetType").stringValue();

                    Synset pointedSynset = getSynset(pointedSynsetIri, pointedSynsetType);
                    originSynset.addEdge(getEdge(edgeIri, edgeType, originSynset, pointedSynset));
                }
            }
        }
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

    private EdgeSerializer getSerializerForEdge(String type) {
        EdgeSerializer serializer;
        try {
            serializer = edgeSerializers
                    .stream()
                    .filter(x -> x.getEdgeClassIri().stringValue().equals(type))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        }
        return serializer;
    }
}
